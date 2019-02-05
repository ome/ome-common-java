/*
 * #%L
 * S3 client service
 * %%
 * Copyright (C) 2019 Open Microscopy Environment:
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

package loci.common.services;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import io.minio.ObjectStat;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * An S3 client
 */
public class S3ClientServiceImpl extends AbstractService implements S3ClientService {

  /**
   * Minio client
   */
  private MinioClient s3Client;

  /**
   * Default constructor.
   */
  public S3ClientServiceImpl() {
    checkClassDependency(io.minio.MinioClient.class);
  }

  // -- S3ClientService methods

  @Override
  public void initialize(String server, int port, String accessKey, String secretKey,
                         String appName, String appVersion)
      throws S3ClientServiceException {
    try {
      s3Client = new MinioClient(server, port, accessKey, secretKey);
      s3Client.setAppInfo(appName, appVersion);
    } catch (MinioException e) {
      throw new S3ClientServiceException(e);
    }
  }

  @Override
  public boolean bucketExists(String bucket) throws S3ClientServiceException, IOException {
    try {
      return s3Client.bucketExists(bucket);
    }
    catch (
        MinioException |
        InvalidKeyException |
        NoSuchAlgorithmException |
        XmlPullParserException e) {
      throw new S3ClientServiceException(e);
    }
  }

  @Override
  public S3ClientStat statObject(String bucket, String object) throws S3ClientServiceException, IOException {
    try {
      ObjectStat mcstat = s3Client.statObject(bucket, object);
      return new S3ClientStat(mcstat.length());
    }
    catch (
        MinioException |
        InvalidKeyException |
        NoSuchAlgorithmException |
        XmlPullParserException e) {
      throw new S3ClientServiceException(e);
    }
  }

  @Override
  public InputStream getObject(String bucket, String object, long offset) throws S3ClientServiceException, IOException {
    try {
      return s3Client.getObject(bucket, object, offset);
    }
    catch (
        InvalidKeyException |
        MinioException |
        NoSuchAlgorithmException |
        XmlPullParserException e) {
      throw new S3ClientServiceException(e);
    }
  }

  @Override
  public void getObject(String bucket, String object, String filename) throws S3ClientServiceException, IOException {
    try {
      s3Client.getObject(bucket, object, filename);
    }
    catch (
        InvalidKeyException |
        MinioException |
        NoSuchAlgorithmException |
        XmlPullParserException e) {
      throw new S3ClientServiceException(e);
    }
  }

}
