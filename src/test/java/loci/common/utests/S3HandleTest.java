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

import loci.common.S3Handle;
import loci.common.StreamHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Unit tests for the loci.common.S3Handle class.
 *
 * @see loci.common.URLHandle
 */
@Test(groups="readTests")
public class S3HandleTest {

  // -- Fields --

  private static boolean runS3RemoteTests;
  private Path TEMPDIR;

  private static final Logger LOGGER = LoggerFactory.getLogger(S3HandleTest.class);

  private static final String s3public = "s3+http://localhost:31836";
  private static final String s3private = "s3+http://accesskey:secretkey@localhost:31836";

  // -- Setup methods --

  @BeforeClass
  public void setup() throws IOException {
    TEMPDIR = Files.createTempDirectory("S3HandleTest-");
    TEMPDIR.toFile().deleteOnExit();

    runS3RemoteTests = TestUtilities.getPropValueInt("testng.runS3RemoteTests") > 0;

    if (!runS3RemoteTests) {
      LOGGER.warn("S3 tests are disabled!");
    }
  }

  private void skipIfS3Disabled() throws SkipException {
    if (!runS3RemoteTests) {
      throw new SkipException("S3 tests are disabled");
    }
  }

  // -- Test methods --

  @Test
  public void testCanHandleScheme() {
    assertTrue(S3Handle.canHandleScheme("s3://"));
    assertTrue(S3Handle.canHandleScheme("s3+transport://abc"));
    assertTrue(S3Handle.canHandleScheme("s3+transport"));
    assertFalse(S3Handle.canHandleScheme("s345://"));
    assertFalse(S3Handle.canHandleScheme("http+s3://"));
    assertFalse(S3Handle.canHandleScheme("https"));
  }

  @Test
  public void testParseLocalhost() throws IOException {
    S3Handle s3 = new S3Handle("s3://localhost:9000/bucket/key/file.tif", false, null);
    assertEquals("https://localhost", s3.getServer());
    assertEquals(9000, s3.getPort());
    assertEquals("bucket", s3.getBucket());
    assertEquals("key/file.tif", s3.getPath());
  }

  @Test
  public void testParseAuth() throws IOException {
    S3Handle s3 = new S3Handle(
      "s3://access:secret@s3.example.org/bucket/key/file.tif", false, null);
    assertEquals("https://s3.example.org", s3.getServer());
    assertEquals(0, s3.getPort());
    assertEquals("bucket", s3.getBucket());
    assertEquals("key/file.tif", s3.getPath());
  }

  @Test
  public void testParseAuthLocalhost() throws IOException {
    S3Handle s3 = new S3Handle("s3://access:secret@localhost:9000/bucket/key/file.tif", false, null);
    assertEquals("https://localhost", s3.getServer());
    assertEquals(9000, s3.getPort());
    assertEquals("bucket", s3.getBucket());
    assertEquals("key/file.tif", s3.getPath());
  }

  @Test
  public void testParseProtocol() throws IOException {
    S3Handle s3 = new S3Handle(
      "example://localhost/bucket/key/file.tif", false, null);
    assertEquals("example://localhost", s3.getServer());
    assertEquals(0, s3.getPort());
    assertEquals("bucket", s3.getBucket());
    assertEquals("key/file.tif", s3.getPath());
  }

  @Test
  public void testDefaultProtocol() throws IOException {
    S3Handle s3 = new S3Handle("s3+custom://localhost/bucket/key/file.tif", false, null);
    assertEquals("custom://localhost", s3.getServer());
    assertEquals(0, s3.getPort());
    assertEquals("bucket", s3.getBucket());
    assertEquals("key/file.tif", s3.getPath());
  }

  @Test
  public void testParseNoSlash() throws IOException {
    S3Handle s3 = new S3Handle("s3://localhost", false, null);
    assertEquals("https://localhost", s3.getServer());
    assertEquals(0, s3.getPort());
    assertEquals(null, s3.getBucket());
    assertEquals(null, s3.getPath());
  }

  @Test
  public void testParseSlashNoBucket() throws IOException {
    S3Handle s3 = new S3Handle("s3://localhost/", false, null);
    assertEquals("https://localhost", s3.getServer());
    assertEquals(0, s3.getPort());
    assertEquals(null, s3.getBucket());
    assertEquals(null, s3.getPath());
  }

  @Test
  public void testParseBucketNoSlash() throws IOException {
    S3Handle s3 = new S3Handle("s3://localhost/bucket", false, null);
    assertEquals("https://localhost", s3.getServer());
    assertEquals(0, s3.getPort());
    assertEquals("bucket", s3.getBucket());
    assertEquals(null, s3.getPath());
  }

  @Test
  public void testParseBucketSlash() throws IOException {
    S3Handle s3 = new S3Handle("s3://localhost/bucket/", false, null);
    assertEquals("https://localhost", s3.getServer());
    assertEquals(0, s3.getPort());
    assertEquals("bucket", s3.getBucket());
    assertEquals(null, s3.getPath());
  }

  @Test
  public void testIsBucket() throws IOException {
    skipIfS3Disabled();
    S3Handle s3 = new S3Handle(s3public + "/bioformats.test.public");
    assertTrue(s3.isBucket());
  }

  @Test
  public void testReadPublic() throws IOException {
    skipIfS3Disabled();
    S3Handle s3 = new S3Handle(s3public + "/bioformats.test.public/single-channel.ome.tiff");
    assertFalse(s3.isBucket());
    assertTrue(s3.exists());
    assertEquals(76097, s3.length());
  }

  @Test
  public void testReadPrivate() throws IOException {
    skipIfS3Disabled();
    S3Handle s3 = new S3Handle(s3private + "/bioformats.test.private/single-channel.ome.tiff");
    assertFalse(s3.isBucket());
    assertTrue(s3.exists());
    assertEquals(76097, s3.length());
  }

  @Test
  public void testReadAndSeek() throws IOException {
    skipIfS3Disabled();
    S3Handle s3 = new S3Handle(s3public + "/bioformats.test.public/2MBfile.txt");
    assertFalse(s3.isBucket());
    assertTrue(s3.exists());
    assertEquals(2097152, s3.length());

    byte[] buffer = new byte[32];
    int r;

    r = s3.read(buffer, 0, 32);
    assertEquals(32, r);
    assertEquals(".                             1\n", new String(buffer));

    r = s3.read(buffer, 0, 32);
    assertEquals(32, r);
    assertEquals(".                             2\n", new String(buffer));

    s3.seek(80);
    r = s3.read(buffer, 0, 32);
    assertEquals(32, r);
    assertEquals("              3\n.               ", new String(buffer));

    // Large seek (S3Handle.S3_MAX_FORWARD_SEEK)
    s3.seek(2097056);
    r = s3.read(buffer, 0, 32);
    assertEquals(32, r);
    assertEquals(".                         65534\n", new String(buffer));

    // Reverse seek
    s3.seek(144);
    r = s3.read(buffer, 0, 32);
    assertEquals(32, r);
    assertEquals("              5\n.               ", new String(buffer));
  }

  @Test
  public void testResetStream() throws IOException {
    class S3HandleWrapper extends S3Handle {
      public S3HandleWrapper(String url) throws IOException {
        super(url);
      }

      @Override
      public void resetStream() throws IOException {
        super.resetStream();
      }

      @Override
      public void resetStream(long offset) throws IOException {
        super.resetStream(offset);
      }
    }

    skipIfS3Disabled();
    S3HandleWrapper s3 = new S3HandleWrapper(s3private + "/bioformats.test.private/2MBfile.txt");
    assertFalse(s3.isBucket());
    assertTrue(s3.exists());
    assertEquals(2097152, s3.length());

    byte[] buffer = new byte[32];
    int r;
    s3.resetStream(750144);
    r = s3.read(buffer, 0, 32);
    assertEquals(32, r);
    assertEquals(".                         23443\n", new String(buffer));

    s3.resetStream();
    r = s3.read(buffer, 0, 32);
    assertEquals(32, r);
    assertEquals(".                             1\n", new String(buffer));
  }

  @Test
  public void testCache() throws IOException {
    class MockSettings extends StreamHandle.Settings {
      @Override
      public String getRemoteCacheRootDir() {
        return TEMPDIR.toString();
      }
    }

    skipIfS3Disabled();
    final String expectedPath = TEMPDIR + "/http/localhost/31836/bioformats.test.public/2MBfile.txt";

    String downloaded = S3Handle.cacheObject(
        s3public + "/bioformats.test.public/2MBfile.txt", new MockSettings());
    assertEquals(expectedPath, downloaded);
    assertEquals(2097152, Files.size(Paths.get(downloaded)));
  }
}