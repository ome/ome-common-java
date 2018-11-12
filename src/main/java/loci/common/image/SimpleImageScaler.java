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
 * Basic implementation of {@link IImageScaler}.
 * A n-by-n source region is transformed to 1 output pixel by
 * picking the upper-left-most of the n-by-n pixels.
 */
public class SimpleImageScaler implements IImageScaler {

  /**
   * @see loci.common.image.IImageScaler#downsample(byte[], int, int, double,
   *  int, boolean, boolean, int, boolean)
   */
  @Override
  public byte[] downsample(byte[] srcImage, int width, int height,
    double scaleFactor, int bytesPerPixel, boolean littleEndian,
    boolean floatingPoint, int channels, boolean interleaved)
  {
    if (scaleFactor < 1) {
      throw new IllegalArgumentException("Scale factor cannot be less than 1");
    }
    int newW = (int) (width / scaleFactor);
    int newH = (int) (height / scaleFactor);
    if (newW == 0 || newH == 0) {
      throw new IllegalArgumentException(
        "Scale factor too large; new width = " + newW +
        ", new height = " + newH);
    }
    if (newW == width && newH == height) {
      return srcImage;
    }

    int yd = (height / newH) * width - width;
    int yr = height % newH;
    int xd = width / newW;
    int xr = width % newW;

    byte[] outBuf = new byte[newW * newH * bytesPerPixel * channels];
    int count = interleaved ? 1 : channels;
    int pixelChannels = interleaved ? channels : 1;

    for (int c=0; c<count; c++) {
      int srcOffset = c * width * height;
      int destOffset = c * newW * newH;
      for (int yyy=newH, ye=0; yyy>0; yyy--) {
        for (int xxx=newW, xe=0; xxx>0; xxx--) {
          // for every pixel in the output image, pick the upper-left-most pixel
          // in the corresponding area of the source image, e.g. for a scale
          // factor of 2.0:
          //
          // ---------      -----
          // |a|b|c|d|      |a|c|
          // ---------      -----
          // |e|f|g|h|      |i|k|
          // --------- ==>  -----
          // |i|j|k|l|
          // ---------
          // |m|n|o|p|
          // ---------
          for (int rgb=0; rgb<pixelChannels; rgb++) {
            for (int b=0; b<bytesPerPixel; b++) {
              outBuf[bytesPerPixel * (destOffset * pixelChannels + rgb) + b] =
                srcImage[bytesPerPixel * (srcOffset * pixelChannels + rgb) + b];
            }
          }
          destOffset++;
          srcOffset += xd;
          xe += xr;
          if (xe >= newW) {
            xe -= newW;
            srcOffset++;
          }
        }
        srcOffset += yd;
        ye += yr;
        if (ye >= newH) {
          ye -= newH;
          srcOffset += width;
        }
      }
    }
    return outBuf;
  }

}
