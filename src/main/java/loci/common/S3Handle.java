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
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.errors.*;
import org.xmlpull.v1.XmlPullParserException;

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

  private final static String DEFAULT_SERVER = "https://s3.amazonaws.com";

  private String server;

  private String url;

  private String bucket;

  private String path;

  private MinioClient s3Client;

  public S3Handle(String url) throws IOException {
    this(DEFAULT_SERVER, url);
  }


  public S3Handle(String server, String url) throws IOException {
    if (!url.startsWith("s3") && !url.startsWith("file:")) {
      url = "s3://" + url;
    }
    this.server = server;
    this.url = url;
    try {
      URI parser = new URI(url);
      bucket = parser.getAuthority();
      path = parser.getPath();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    resetStream();
  }

  @Override
  protected void resetStream() throws IOException {
    try {
      s3Client = new MinioClient(server);
      ObjectStat stat = s3Client.statObject(bucket, path);
      length = stat.length();
      stream = new DataInputStream(new BufferedInputStream(
              s3Client.getObject(bucket, path)));
      stream.skip(-1L);
      fp = 0;
      mark = 0;
    } catch (InvalidEndpointException |
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
      throw new IOException("failed to load s3: " + url, e);
    }
  }
}
