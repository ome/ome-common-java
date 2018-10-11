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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

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

  // -- Setup methods --

  @BeforeMethod
  public void setup() {
    // no-op
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
}
