/*
 * #%L
 * Common package for I/O and related utilities
 * %%
 * Copyright (C) 2005 - 2016 Open Microscopy Environment:
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

package loci.common.utests;

import loci.common.services.DependencyException;
import loci.common.services.S3ClientService;
import loci.common.services.S3ClientServiceException;
import loci.common.services.S3ClientServiceImpl;
import loci.common.services.S3ClientStat;
import loci.common.services.ServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Unit tests for the loci.common.service.S3ClientServiceImpl class.
 * @deprecated The S3 implementations and services will be removed from
 * the ome-common library in version 7.0.0
 *
 * @see loci.common.URLHandle
 */
@Deprecated
@Test(groups="readTests")
public class S3ClientServiceTest {

  // -- Fields --

  private static boolean runS3RemoteTests;
  private Path TEMPDIR;
  private S3ClientService s3;

  private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientServiceTest.class);

  // -- Setup methods --

  @BeforeClass
  public void setup() throws IOException {
    TEMPDIR = Files.createTempDirectory("S3ClientServiceTest-");
    TEMPDIR.toFile().deleteOnExit();

    runS3RemoteTests = TestUtilities.getPropValueInt("testng.runS3RemoteTests") > 0;

    if (!runS3RemoteTests) {
      LOGGER.warn("S3 tests are disabled!");
    }
  }

  @BeforeMethod
  public void setupMethod() throws S3ClientServiceException, SkipException {
    if (!runS3RemoteTests) {
      throw new SkipException("S3 tests are disabled");
    }
    s3 = new S3ClientServiceImpl();
    s3.initialize("http://localhost", 31836, null, null, "S3ClientServiceTest", "0.0.0");
  }

  // -- Test methods --

  @Test
  public void testServiceLookup() throws DependencyException {
    ServiceFactory factory = new ServiceFactory();
    S3ClientService s3client = factory.getInstance(S3ClientService.class);
    assertTrue(S3ClientServiceImpl.class.isInstance(s3client));
  }

  @Test
  public void testBucketExists() throws S3ClientServiceException, IOException {
    assertTrue(s3.bucketExists("bioformats.test.public"));
  }

  @Test(expectedExceptions = { S3ClientServiceException.class })
  public void testBucketNotExists() throws S3ClientServiceException, IOException {
    s3.bucketExists("this.bucket.does.not.exist");
  }

  @Test
  public void testStatObject() throws S3ClientServiceException, IOException {
    S3ClientStat stat = s3.statObject("bioformats.test.public", "2MBfile.txt");
    assertEquals(2097152, stat.length());
  }

  @Test
  public void testGetObject() throws S3ClientServiceException, IOException {
    InputStream in = s3.getObject("bioformats.test.public", "2MBfile.txt", 1380896);
    String line = new BufferedReader(new InputStreamReader(in)).readLine();
    assertEquals(".                         43154", line);
    in.close();
  }

  @Test
  public void testGetObjectFile() throws S3ClientServiceException, IOException {
    final String filepath = TEMPDIR + "/2MBfile.txt";
    s3.getObject("bioformats.test.public", "2MBfile.txt", filepath);
    Path path = Paths.get(filepath);
    assertEquals(2097152, Files.size(path));
    String[] lines = Files.newBufferedReader(path).lines().toArray(String[]::new);
    assertEquals(".                             1", lines[0]);
    assertEquals(".                         65536", lines[lines.length - 1]);
  }

}