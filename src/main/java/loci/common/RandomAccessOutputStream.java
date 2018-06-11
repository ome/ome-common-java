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

package loci.common;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.google.common.math.LongMath;

/**
 * RandomAccessOutputStream provides methods for writing to files and
 * byte arrays.
 */
public class RandomAccessOutputStream extends OutputStream implements DataOutput, Closeable
{
  // -- Fields --

  private IRandomAccess outputFile;

  private int currentBit = 0;
  private int currentByte = 0;
  private boolean dirtyByte = false;

  // -- Constructor --

  /**
   * Constructs a random access stream around the given file.
   * @param file Filename to open the stream for.
   * @throws IOException If there is a problem opening the file.
   */
  public RandomAccessOutputStream(String file) throws IOException {
    outputFile = Location.getHandle(file, true);
  }

  /**
   * Constructs a random access stream around the given handle.
   * @param handle Handle to open the stream for.
   */
  public RandomAccessOutputStream(IRandomAccess handle) {
    outputFile = handle;
  }

  /**
   * Constructs a random access stream around the given byte array.
   *
   * @param array the byte array to be written to
   * @throws IOException if the array cannot be wrapped
   *                     in a {@link ByteArrayHandle}
   */
  public RandomAccessOutputStream(byte[] array) throws IOException {
    this(new ByteArrayHandle(array));
  }

  // -- RandomAccessOutputStream API methods --

  /**
   * Seeks to the given offset within the stream.
   *
   * @param pos the new offset within the stream
   * @throws IOException is the seek is not successful
   */
  public void seek(long pos) throws IOException {
    outputFile.seek(pos);
  }

  /**
   * @return the current offset within the stream.
   * @throws IOException if the offset cannot be retrieved
   */
  public long getFilePointer() throws IOException {
    return outputFile.getFilePointer();
  }

  /**
   * @return the length of the file
   * @throws IOException if the length cannot be retrieved
   */
  public long length() throws IOException {
    return outputFile.length();
  }

  /**
   * Advances the current offset by the given number of bytes.
   *
   * @param skip the number of bytes to skip
   * @throws IOException if the offset cannot be changed
   */
  public void skipBytes(int skip) throws IOException {
    final long currentPosition = outputFile.getFilePointer();
    final long newPosition;
    try {
      /* in Java 8 can instead use addExact */
      newPosition = LongMath.checkedAdd(currentPosition, skip);
    } catch (ArithmeticException ae) {
      /* translate to expected checked exception */
      throw new IOException("seek is out of range", ae);
    }
    outputFile.seek(newPosition);
  }

  /**
   * Sets the endianness of the stream.
   *
   * @param little true if the byte order of the stream is little-endian
   */
  public void order(boolean little) {
    outputFile.setOrder(
      little ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
  }

  /**
   * Gets the endianness of the stream.
   *
   * @return true if the byte order of the stream is little-endian
   */
  public boolean isLittleEndian() {
    return outputFile.getOrder() == ByteOrder.LITTLE_ENDIAN;
  }

  /**
   * Writes the given string followed by a newline character.
   *
   * @param s the line of text to be written.  A newline does not
   *          need to be appended, as this method automatically writes
   *          a newline character.
   * @throws IOException if writing is not possible
   */
  public void writeLine(String s) throws IOException {
    writeBytes(s);
    writeBytes("\n");
  }

  /**
   * Writes the given value using the given number of bits.
   *
   * @param value int value to be written
   * @param numBits exact number of bits to be written
   * @throws IOException if writing fails for any reason
   */
  public void writeBits(int value, int numBits) throws IOException {
    if (numBits <= 0) {
      return;
    }
    for (int i=numBits-1; i>=0; i--) {
      int b = ((value >> i) & 1) << (7 - currentBit);

      currentByte |= b;
      dirtyByte = true;
      currentBit++;

      if (currentBit > 7) {
        currentBit = 0;
        flush();
        currentByte = 0;
      }
    }
  }

  /**
   * Writes the bits represented by a bit string to the buffer.
   *
   * @param bitString a string of '0' and/or '1' characters representing
   *                  bits to be written. Each character in the string results
   *                  in a call to {@link #writeBits(int, int)}
   *                  that writes 1 bit.
   * @throws IllegalArgumentException If any characters other than
   *   '0' and '1' appear in the string.
   * @throws IOException if writing fails for any other reason
   */
  public void writeBits(String bitString) throws IOException {
    if (bitString == null) {
      throw new IllegalArgumentException("Bit string cannot be null");
    }
    for (char c : bitString.toCharArray()) {
      if (c == '1') {
        writeBits(1, 1);
      }
      else if (c == '0') {
        writeBits(0, 1);
      }
      else {
        throw new IllegalArgumentException(
          "Found illegal character '" + c + "'; write terminated");
      }
    }
  }

  // -- DataOutput API methods --

  /* @see java.io.DataOutput#write(byte[]) */
  @Override
  public void write(byte[] b) throws IOException {
    flush();
    outputFile.write(b);
  }

  /* @see java.io.DataOutput#write(byte[], int, int) */
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    flush();
    outputFile.write(b, off, len);
  }

  /**
   * Writes bytes to the stream from the given buffer.
   * @param b Source buffer to read data from.
   * @throws IOException If there is an error writing to the stream.
   */
  public void write(ByteBuffer b) throws IOException {
    flush();
    outputFile.write(b);
  }

  /**
   * @param b Source buffer to read data from.
   * @param off Offset within the buffer to start reading from.
   * @param len Number of bytes to read.
   * @throws IOException If there is an error writing to the stream.
   */
  public void write(ByteBuffer b, int off, int len) throws IOException {
    flush();
    outputFile.write(b, off, len);
  }

  /* @see java.io.DataOutput#write(int) */
  @Override
  public void write(int b) throws IOException {
    flush();
    outputFile.write(b);
  }

  /* @see java.io.DataOutput#writeBoolean(boolean) */
  @Override
  public void writeBoolean(boolean v) throws IOException {
    flush();
    outputFile.writeBoolean(v);
  }

  /* @see java.io.DataOutput#writeByte(int) */
  @Override
  public void writeByte(int v) throws IOException {
    flush();
    outputFile.writeByte(v);
  }

  /* @see java.io.DataOutput#writeBytes(String) */
  @Override
  public void writeBytes(String s) throws IOException {
    flush();
    outputFile.writeBytes(s);
  }

  /* @see java.io.DataOutput#writeChar(int) */
  @Override
  public void writeChar(int v) throws IOException {
    flush();
    outputFile.writeChar(v);
  }

  /* @see java.io.DataOutput#writeChars(String) */
  @Override
  public void writeChars(String s) throws IOException {
    flush();
    outputFile.writeChars(s);
  }

  /* @see java.io.DataOutput#writeDouble(double) */
  @Override
  public void writeDouble(double v) throws IOException {
    flush();
    outputFile.writeDouble(v);
  }

  /* @see java.io.DataOutput#writeFloat(float) */
  @Override
  public void writeFloat(float v) throws IOException {
    flush();
    outputFile.writeFloat(v);
  }

  /* @see java.io.DataOutput#writeInt(int) */
  @Override
  public void writeInt(int v) throws IOException {
    flush();
    outputFile.writeInt(v);
  }

  /* @see java.io.DataOutput#writeLong(long) */
  @Override
  public void writeLong(long v) throws IOException {
    flush();
    outputFile.writeLong(v);
  }

  /* @see java.io.DataOutput#writeShort(int) */
  @Override
  public void writeShort(int v) throws IOException {
    flush();
    outputFile.writeShort(v);
  }

  /* @see java.io.DataOutput#writeUTF(String) */
  @Override
  public void writeUTF(String str) throws IOException {
    flush();
    outputFile.writeUTF(str);
  }

  // -- OutputStream API methods --

  /* @see java.io.OutputStream#close() */
  @Override
  public void close() throws IOException {
    flush();
    outputFile.close();
  }

  /* @see java.io.OutputStream#flush() */
  @Override
  public void flush() throws IOException {
    if (dirtyByte) {
      outputFile.writeByte(currentByte);
      dirtyByte = false;
    }
  }

}
