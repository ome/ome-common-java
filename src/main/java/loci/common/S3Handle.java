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

/**
 * Provides random access to S3 buckets using the IRandomAccess interface.
 * Instances of S3Handle are read-only.
 *
 * TODO: How does one handle buckets with periods
 *
 * @see IRandomAccess
 * @see StreamHandle
 * @see java.net.URLConnection
 *
 */
public class S3Handle extends StreamHandle {

  public final static String DEFAULT_SERVER = "https://s3.amazonaws.com";

  /** Format: "s3://accessKey:secrectKey@bucket.endpoint/key" */
  public final static String URI_PATTERN = "(s3://)?" +
          "((?<access>.*):(?<secret>.*)@)?" +
          "(?<bucket>.*?)"+
          "([.](?<server>.*?)((:)(?<port>\\d+))?)?"+
          "/(?<path>.*)";

  public final static Pattern URI_PARSER = Pattern.compile(URI_PATTERN);

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

  public S3Handle(String url) throws IOException {
    this(null, url);
  }

  public S3Handle(String server, String uri) throws IOException {
    this(server, uri, true);
  }

  public S3Handle(String uri, boolean initialize) throws IOException {
    this(null, uri, initialize);
  }

  public S3Handle(String server, String uri, boolean initialize) throws IOException {
    this.uri = uri;
    Matcher m = URI_PARSER.matcher(uri);
    if (!m.matches()) {
      throw new RuntimeException(String.format(
              "%s does not match pattern %s", uri, URI_PATTERN));
    }
    this.accessKey = m.group("access");
    this.secretKey = m.group("secret");
    this.bucket = m.group("bucket");
    this.server = server(m, server);
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

  private String server(Matcher m, String server) {
    String s = m.group("server");
    if (s == null) {
      s = server != null ? server : DEFAULT_SERVER;
    }
    if (!s.contains("://")) {
      s = "http://" + s;
    }
    return s;
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
              "failed to load s3: %s\n" +
              "\tserver:%s\n"+
              "\tport:%d\n"+
              "\tbucket:%s\n"+
              "\tpath:%s", uri, server, port, bucket, path), e);
    }
  }
}
