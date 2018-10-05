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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.minio.MinioClient;
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

  /** Default protocol for fetching s3://
   # TODO: Default to https for improved security
   */
  public final static String DEFAULT_S3_PROTOCOL = "https";

  private static final Logger LOGGER = LoggerFactory.getLogger(S3Handle.class);

  /** Format: "s3://accessKey:secretKey@server-endpoint/bucket/path" */
  public final static String URI_PATTERN =
          "(?<protocol>.+?)://" +
          "((?<access>.*):(?<secret>.*)@)?" +
          "(?<server>.+?)((:)(?<port>\\d+))?"+
          "/(?<bucket>.*?)"+
          "/(?<path>.*)";

  public final static Pattern URI_PARSER = Pattern.compile(URI_PATTERN);

  /** S3 configuration */
  private final Settings settings;

  /** full string used to configure this handle */
  private final String uri;

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
  public S3Handle(String uri, boolean initialize, Settings s) throws IOException {
    if (s == null) {
      this.settings = new StreamHandle.Settings();
    }
    else {
      this.settings = s;
    }
    this.uri = uri;
    Matcher m = URI_PARSER.matcher(uri);
    if (!m.matches()) {
      throw new RuntimeException(String.format(
              "%s does not match pattern %s", uri, URI_PATTERN));
    }
    this.accessKey = m.group("access");
    this.secretKey = m.group("secret");
    this.bucket = m.group("bucket");
    this.server = server(m);
    this.path = m.group("path");
    this.port = port(m);
    if (initialize) {
      resetStream();
    }
  }

  private int port(Matcher m) {
    String p = m.group("port");
    if (p == null) {
      return 0;
    } else {
      return Integer.valueOf(p);
    }
  }

  private String server(Matcher m) {
    String protocol = m.group("protocol");
    if (protocol.equals("s3")) {
      protocol = this.settings.get("BF_S3_PROTOCOL");
      if (protocol == null) {
        protocol = DEFAULT_S3_PROTOCOL;
      }
    }
    return protocol + "://" + m.group("server");
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

  @Override
  protected void resetStream() throws IOException {
    LOGGER.trace("Resetting");
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
