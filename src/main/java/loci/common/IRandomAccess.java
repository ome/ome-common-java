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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Interface for random access into structures (e.g., files or arrays).
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public interface IRandomAccess extends DataInput, DataOutput {
  /**
   * Closes this random access stream and releases
   * any system resources associated with the stream.
   *
   * @throws IOException if the underlying stream(s) could not be closed
   */
  void close() throws IOException;

  /**
   * Returns the current offset in this stream.
   *
   * @return the current byte offset within the file; expected to be
   *         non-negative and less than the value of #length()
   * @throws IOException if the offset cannot be retrieved
   */
  long getFilePointer() throws IOException;

  /**
   * Returns whether this refers to a valid object
   *
   * @return true if this refers to a valid object
   * @throws IOException if unable to determine whether the object is valid
   */
  boolean exists() throws IOException;

  /**
   * Returns the length of this stream.
   *
   * @return the length in bytes of the stream
   * @throws IOException if the length cannot be retrieved
   */
  long length() throws IOException;

  /**
   * Returns the current order (endianness) of the stream.
   * @return See above.
   */
  ByteOrder getOrder();

  /**
   * Sets the byte order (endianness) of the stream.
   * @param order Order to set.
   */
  void setOrder(ByteOrder order);

  /**
   * Reads up to b.length bytes of data
   * from this stream into an array of bytes.
   *
   * @param b the array to fill from this stream
   * @return the total number of bytes read into the buffer.
   * @throws IOException if reading is not possible
   */
  int read(byte[] b) throws IOException;

  /**
   * Reads up to len bytes of data from this stream into an array of bytes.
   *
   * @param b the array to fill from this stream
   * @param off the offset in <code>b</code> from which to start filling;
   *        expected to be non-negative and no greater than
   *        <code>b.length - len</code>
   * @param len the number of bytes to read; expected to be positive and
   *        no greater than <code>b.length - offset</code>
   * @return the total number of bytes read into the buffer.
   * @throws IOException if reading is not possible
   */
  int read(byte[] b, int off, int len) throws IOException;

  /**
   * Reads up to buffer.capacity() bytes of data
   * from this stream into a ByteBuffer.
   *
   * @param buffer the ByteBuffer to fill from this stream
   * @return the total number of bytes read into the buffer.
   * @throws IOException if reading is not possible
   */
  int read(ByteBuffer buffer) throws IOException;

  /**
   * Reads up to len bytes of data from this stream into a ByteBuffer.
   *
   * @param buffer the ByteBuffer to fill from this stream
   * @param offset the offset in <code>b</code> from which to start filling;
   *        expected to be non-negative and no greater than
   *        <code>buffer.capacity() - len</code>
   * @param len the number of bytes to read; expected to be positive and
   *        no greater than <code>buffer.capacity() - offset</code>
   * @return the total number of bytes read into the buffer.
   * @throws IOException if reading is not possible
   */
  int read(ByteBuffer buffer, int offset, int len) throws IOException;

  /**
   * Sets the stream pointer offset, measured from the beginning
   * of this stream, at which the next read or write occurs.
   *
   * @param pos new byte offset (pointer) in the current stream.
   *        Unless otherwise noted, may be larger or smaller than the
   *        current pointer, but must be non-negative and less than the
   *        value of #length()
   * @throws IOException if <code>pos</code> is invalid or the seek fails
   * @see #getFilePointer()
   */
  void seek(long pos) throws IOException;

  /**
   * Writes up to buffer.capacity() bytes of data from the given
   * ByteBuffer to this stream.
   *
   * @param buf the ByteBuffer containing bytes to write to this stream
   * @throws IOException if writing is not possible
   */
  void write(ByteBuffer buf) throws IOException;

  /**
   * Writes up to len bytes of data from the given ByteBuffer to this
   * stream.
   *
   * @param buf the ByteBuffer containing bytes to write to this stream
   * @param off the offset in <code>b</code> from which to start writing;
   *        expected to be non-negative and no greater than
   *        <code>buf.capacity() - len</code>
   * @param len the number of bytes to write; expected to be positive and
   *        no greater than <code>buf.capacity() - offset</code>
   * @throws IOException if writing is not possible
   */
  void write(ByteBuffer buf, int off, int len) throws IOException;
}
