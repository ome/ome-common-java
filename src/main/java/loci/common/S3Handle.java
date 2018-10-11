/*
 * #%L
 * Common package for I/O and related utilities
 * %%
 * Copyright (C) 2018 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package loci.common;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import io.minio.ObjectStat;
import io.minio.errors.*;
import org.xmlpull.v1.XmlPullParserException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides random access to S3 buckets using the IRandomAccess interface.
 * Instances of S3Handle are read-only.
 *
 * @see IRandomAccess
 * @see StreamHandle
 * @see java.net.URLConnection
 *
 */
public class S3Handle extends StreamHandle {

  /** Default protocol for fetching s3:// */
  public final static String DEFAULT_S3_PROTOCOL = "https";

  private static final Logger LOGGER = LoggerFactory.getLogger(S3Handle.class);

  protected final static Pattern SCHEME_PARSER = Pattern.compile("s3(\\+\\p{Alnum}+)?://.*");

  /** S3 configuration */
  private final Settings settings;

  /** Parsed URI used to configure this handle */
  private final URI uri;

  /** access key, if provided */
  private final String accessKey;

  /** secret key, if provided */
  private final String secretKey;

  /** name of the bucket */
  private final String bucket;

  /** endpoint to which requests will be sent */
  private final String server;

  /** port at the given server */
  private final int port;

  /** remaining path, or key, for this accessed resource */
  private final String path;

  private MinioClient s3Client;

  /**
   * Return true if this is a URL with an s3 scheme
   */
  public static boolean canHandleScheme(String url) {
    return SCHEME_PARSER.matcher(url).matches();
  }

  /**
   * Open an S3 file
   *
   * @param url the full URL to the S3 resource
   * @throws IOException if there is an error during opening
   */
  public S3Handle(String url) throws IOException {
    this(url, true, null);
  }

  /**
   * Open an S3 file
   *
   * @param url the full URL to the S3 resource
   * @param initialize If true open the stream, otherwise just parse connection
   *        string
   * @param s custom settings object
   * @throws IOException if there is an error during opening
   */
  public S3Handle(String uristr, boolean initialize, Settings s) throws
      IOException {
    if (s == null) {
      this.settings = new StreamHandle.Settings();
    }
    else {
      this.settings = s;
    }

    try {
      this.uri = new URI(uristr);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Invalid URI " + uristr, e);
    }

    // access[:secret]
    String auth = this.uri.getUserInfo();
    String accessKey = null;
    String secretKey = null;
    if (auth != null) {
      String[] authparts = auth.split(":", 2);
      accessKey = authparts[0];
      if (authparts.length > 1) {
        secretKey = authparts[1];
      }
    }
    this.accessKey = accessKey;
    this.secretKey = secretKey;

    String protocol;
    String scheme = this.uri.getScheme();
    if (scheme.equals("s3")) {
      protocol = DEFAULT_S3_PROTOCOL;
    }
    else if (scheme.startsWith("s3+")) {
      protocol = scheme.substring(3);
    }
    else {
      protocol = scheme;
    }
    this.server = protocol + "://" + this.uri.getHost();

    if (this.uri.getPort() == -1) {
      this.port = 0;
    }
    else {
      this.port = this.uri.getPort();
    }

    // First path component is the bucket
    // TODO: Parsing this seems way more complicated than it should be
    String fullpath = this.uri.getPath();
    if (fullpath == null || fullpath.length() == 0) {
      fullpath = "/";
    }
    // Leading / means first element is always ""
    String[] pathparts = fullpath.split("/", 3);
    if (pathparts[1].length() > 0) {
      this.bucket = pathparts[1];
    }
    else {
      this.bucket = null;
    }
    if (pathparts.length > 2 && pathparts[2].length() > 0) {
      this.path = pathparts[2];
    }
    else {
      this.path = null;
    }

    if (initialize) {
      resetStream();
    }
  }

  public String getServer() {
    return server;
  }

  public int getPort() {
    return port;
  }

  public String getBucket() {
    return bucket;
  }

  public String getPath() {
    return path;
  }

  /**
   * Download an S3 object to a file system cache if it doesn't already exist
   *
   * @param url the full URL to the S3 resource
   * @param s custom settings object
   * @return File path to the cached object
   * @throws IOException if there is an error during reading or writing
   */
  public static String cacheObject(String url, Settings s) throws
      IOException,
      HandleException {
    S3Handle s3 = new S3Handle(url, false, s);
    String cacheroot = s.getRemoteCacheRootDir();
    if (cacheroot == null) {
      throw new HandleException("Remote cache root dir is not set");
    }
    // TODO: Need to ensure this path is safe. Is there a Java method to check?
    String cacheobj = s3.getCacheKey();
    // Hopefully creates a cross-platform path
    Path cachepath = Paths.get(cacheroot, cacheobj);

    if (Files.exists(cachepath)) {
      LOGGER.debug("Found existing cache for {} at {}", s3, cachepath);
    }
    else {
      LOGGER.debug("Caching {} to {}", s3, cachepath);
      s3.downloadObject(cachepath);
      LOGGER.debug("Downloaded {}", cachepath);
    }
    return cachepath.toString();
  }

  public String getCacheKey(){
    String cachekey =
      getServer().replace("://", "/") + "/" +
      getPort() + "/" +
      getBucket() + "/" +
      getPath();
    return cachekey;
  }

  protected void downloadObject(Path destination) throws HandleException {
    LOGGER.trace("destination:{}", destination);
    if (path == null) {
      throw new HandleException("Download path=null not allowed");
    }
    try {
      s3Client = new MinioClient(server, port, accessKey, secretKey);
      ObjectStat stat = s3Client.statObject(bucket, path);
      Files.createDirectories(destination.getParent());
      s3Client.getObject(bucket, path, destination.toString());
    } catch (
      IOException |
      InvalidKeyException |
      MinioException |
      NoSuchAlgorithmException |
      XmlPullParserException e) {
        throw new HandleException("Download failed " + toString(), e);
      }
  }

  /**
   * Is this an accessible bucket?
   *
   * @return True if a bucket
   * @throws IOException if there is an error during reading or writing
   */
  public boolean isBucket() throws IOException {
    if (bucket == null || path !=null ) {
      return false;
    }
    try {
      s3Client = new MinioClient(server, port, accessKey, secretKey);
      boolean isBucket = s3Client.bucketExists(bucket);
      LOGGER.debug("isBucket? {} {}", this, isBucket);
      return isBucket;
    } catch (
      InvalidKeyException |
      MinioException |
      NoSuchAlgorithmException |
      XmlPullParserException e) {
      throw new IOException(String.format(
              "bucketExists failed: %s", this), e);
    }
  }

  @Override
  protected void resetStream() throws IOException {
    LOGGER.trace("Resetting");
    if (bucket == null) {
      throw new IOException("bucket is null");
    }
    if (path == null) {
      throw new IOException("path is null");
    }
    try {
      s3Client = new MinioClient(server, port, accessKey, secretKey);
      ObjectStat stat = s3Client.statObject(bucket, path);
      length = stat.length();
      stream = new DataInputStream(new BufferedInputStream(
              s3Client.getObject(bucket, path)));
      stream.skip(-1L);
      fp = 0;
      mark = 0;
    } catch (ConnectException |
            InvalidEndpointException |
            InvalidPortException |
            InvalidBucketNameException |
            NoSuchAlgorithmException |
            InsufficientDataException |
            InvalidKeyException |
            NoResponseException |
            XmlPullParserException |
            ErrorResponseException |
            InternalException |
            InvalidArgumentException e) {
      throw new IOException(String.format(
              "failed to load s3: %s\n\t%s", uri, this), e);
    }
  }

  public String toString() {
    return String.format("server:%s port:%d bucket:%s path:%s",
                          server, port, bucket, path);
  }
}
