/**
 * Copyright 2012 NetDigital Sweden AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.nginious.http.common;

import java.nio.ByteBuffer;

/**
 * A buffer for writing and reading at the same time. Manages two internal pointers, one
 * for writing and one for reading. Writing always starts from the current write pointer
 * position. Reading always starts from the current read pointer position.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public interface Buffer {
	
	/**
	 * Writes the specified bytes to this buffer. A call to this method may result in
	 * less bytes being added than what is available in the specified byte array
	 * depending on this buffers size and the current position of the write pointer.
	 * 
	 * @param b the bytes to add
	 * @return number of bytes added to this buffer
	 */
	public int put(byte[] b);
	
	/**
	 * Writes len bytes from the specified bytes starting at specified offset. A call to
	 * this method may result in less bytes being added than specified in len depending
	 * on this buffers size and the current position of the write pointer.
	 * 
	 * @param b the byte array to add bytes from
	 * @param off start position in byte array
	 * @param len number of bytes to add
	 * @return number of bytes added to this buffer
	 */
	public int put(byte[] b, int off, int len);
	
	/**
	 * Writes len bytes from the specified bytes starting at specified offset. A call to
	 * this method may result in less bytes being added than specified in len depending
	 * on this buffers size and the current position of the write pointer.
	 * 
	 * @param b the byte array to add bytes from
	 * @param off start position in byte array
	 * @param len number of bytes to add
	 * @param keep
	 * @return number of bytes added to this buffer
	 */
	public int put(byte[] b, int off, int len, int keep);
	
	/**
	 * Writes the specified bytes to this buffer starting at specified position. A call to this
	 * method may result in less bytes being added than what is available in the specified
	 * byte array depending on this buffers size and the specified position.
	 * 
	 * @param idx the position to add bytes in this buffer
	 * @param b the bytes to add
	 * @return number of bytes added
	 */
	public int putAt(int idx, byte[] b);
	
	/**
	 * Moves the buffers put pointer forward for the specified number of bytes. The next put
	 * operation on this buffer will start adding bytes at the new position. Calling this method
	 * may result in less bytes being skipped depending on the current position of the write pointer.
	 * 
	 * @param skip number of bytes to move pointer forward
	 * @return number of bytes skipped
	 */
	public int skip(int skip);
	
	/**
	 * Returns one byte from the current read position.
	 * 
	 * @return the read byte or -1 if end of buffer is reached
	 */
	public int get();
	
	/**
	 * Reads len bytes starting at the specified offset in the specified byte array. Calling
	 * this method may result in less bytes being read than specified in len depending on this
	 * buffers sie and the current position of the read pointer.
	 * 
	 * @param b byte array to write buffer bytes into
	 * @param off start position in byte array
	 * @param len desired number of bytes to write into byte array
	 * @return actual number of bytes written to byte array 
	 */
	public int get(byte[] b, int off, int len);
	
	/**
	 * Returns this buffers current read pointer position.
	 * 
	 * @return the read pointer position
	 */
	public int getIndex();
	
	/**
	 * Sets this buffers read pointer position to the specified index. A call to this method
	 * may result in the read pointer not being set to the specified index since the read
	 * pointer can not be higher than the write pointer
	 * 
	 * @param idx desired new read pointer position
	 * @return actual new read pointer position
	 * @throws ArrayIndexOutOfBoundException if given index is outside this buffers range
	 */
	public int setIndex(int idx);
	
	/**
	 * Returns remaining number of bytes that can be written to this buffer.
	 * 
	 * @return remaining number of bytes that can be written to this buffer
	 */
	public int remaining();
	
	/**
	 * Sets the size of this buffer to the current write pointer position. A call to {@link #reset()}
	 * restores the original size of this buffer.
	 */
	public void compact();
	
	/**
	 * Returns the size of this buffer.
	 * 
	 * @return buffer size
	 */
	public int size();
	
	/**
	 * Resets this buffer by setting the read position 0, write position to 0 and
	 * restoring the original size.
	 */
	public void reset();
	
	/**
	 * Wraps this buffer into a byte buffer.
	 * 
	 * @return the byte buffer
	 */
	public ByteBuffer toByteBuffer();
	
	/**
	 * Wraps this buffer into a byte buffer starting at the specified skip position.
	 * 
	 * @param skip the start position
	 * @return the byte buffer
	 */
	public ByteBuffer toByteBuffer(int skip);
	
	/**
	 * Copies the bytes in this buffer into a byte array.
	 * 
	 * @return the byte array
	 */
	public byte[] toByteArray();
}
