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

package loci.common.image;

/**
 * Interface defining image scaling operations.
 */
public interface IImageScaler {

  /**
   * Downsamples the given image.
   *
   * @param srcImage a byte array representing the image to be downsampled
   * @param width the width in pixels of the source image
   * @param height the height in pixels of the source image
   * @param scaleFactor the value used to calculate the downsampled width and height; expected to be greater than 1
   * @param bytesPerPixel the number of bytes in one pixel (usually 1, 2, 4, or 8)
   * @param littleEndian true if bytes in a pixel are stored in little endian order
   * @param floatingPoint true if the pixels should be interpreted as float or double instead of uint32/uint64
   * @param channels the number of RGB channels included in srcImage
   * @param interleaved true if the RGB channels are stored in interleaved order (RGBRGBRGB... and not RRR...GGG...BBB)
   * @return the downsampled image
   */
  byte[] downsample(byte[] srcImage, int width, int height, double scaleFactor,
    int bytesPerPixel, boolean littleEndian, boolean floatingPoint,
    int channels, boolean interleaved);

}
