/*
 * #%L
 * Common package for I/O and related utilities
 * %%
 * Copyright (C) 2015 - 2016 Open Microscopy Environment:
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

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import loci.common.DataTools;
import loci.common.image.SimpleImageScaler;

/**
 * Unit tests for {@link loci.common.image.SimpleImageScaler}.
 */
public class SimpleImageScalerTest {

  // 3 channel 4x4 16-bit image
  private static final byte[] SOURCE_IMAGE_INTERLEAVED = makeImage(4, 4, 3, 2, true);

  private static final byte[] SOURCE_IMAGE_NON_INTERLEAVED = makeImage(6, 3, 3, 2, false);

  private static byte[] makeImage(int width, int height, int channels,
    int bytes, boolean interleaved)
  {
    byte[] image = new byte[width * height * channels * bytes];
    int v = 0;
    int pos = 0;
    int count = interleaved ? 1 : channels;
    int pixelChannels = interleaved ? channels : 1;
    for (int c=0; c<count; c++) {
      for (int h=0; h<height; h++) {
        for (int w=0; w<width; w++) {
          for (int p=0; p<pixelChannels; p++) {
            DataTools.unpackBytes(v, image, pos, bytes, false);
            v++;
            pos += bytes;
          }
        }
      }
    }

    return image;
  }

  private SimpleImageScaler scaler;

  @BeforeMethod
  public void setUp() {
    scaler = new SimpleImageScaler();
  }

  @Test
  public void testDownsampleInterleaved() {
    byte[] downsample = scaler.downsample(SOURCE_IMAGE_INTERLEAVED, 4, 4, 2.0, 2, false, false, 3, true);
    // expected 2 x 2 image (x 3 channels x 2 bytes)
    assertEquals(downsample.length, 24);
    int[] expectedPixels = new int[] {0, 1, 2, 6, 7, 8, 24, 25, 26, 30, 31, 32};
    for (int i=0; i<expectedPixels.length; i++) {
      assertEquals(expectedPixels[i], DataTools.bytesToInt(downsample, i * 2, 2, false));
    }
  }

  @Test
  public void testDownsampleNonInterleaved() {
    byte[] downsample = scaler.downsample(SOURCE_IMAGE_NON_INTERLEAVED, 6, 3, 3.0, 2, false, false, 3, false);
    // expected 2 x 1 image (x 3 channels x 2 bytes)
    assertEquals(downsample.length, 12);
    int[] expectedPixels = new int[] {0, 3, 18, 21, 36, 39};
    for (int i=0; i<expectedPixels.length; i++) {
      assertEquals(expectedPixels[i], DataTools.bytesToInt(downsample, i * 2, 2, false));
    }
  }
}
