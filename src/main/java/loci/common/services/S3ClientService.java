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

import java.io.IOException;
import java.io.InputStream;

/**
 * An S3 client
 */
public interface S3ClientService extends Service {

  /**
   * Initialise the S3 client
   * @param server servername
   * @param port port
   * @param accessKey access key
   * @param secretKey secret key
   * @param appName user agent application name
   * @param appVersion user agent application version
   */
  void initialise(String server, int port, String accessKey, String secretKey,
                  String appName, String appVersion)
      throws S3ClientServiceException;

  /**
   * Check whether a bucket exists
   * @param bucket Bucket name
   * @return true if bucket exists
   */
  boolean bucketExists(String bucket) throws S3ClientServiceException, IOException;

  /**
   * Stat the object
   * @param bucket Bucket name
   * @param object Object path
   * @return S3ClientStat object
   */
  S3ClientStat statObject(String bucket, String object) throws S3ClientServiceException, IOException;

  /**
   * Read an object
   * @param bucket Bucket name
   * @param object Object path
   * @param offset Start reading at this offset
   * @return InputStream to the object
   */
  InputStream getObject(String bucket, String object, long offset) throws S3ClientServiceException, IOException;

  /**
   * Download an object
   * @param bucket Bucket name
   * @param object Object path
   * @param filename Destination file
   */
  void getObject(String bucket, String object, String filename) throws S3ClientServiceException, IOException;

}
