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
 * A fixed size buffer that uses a byte array as backing store. The buffer storage is allocated when the buffer 
 * is created. Manages two internal pointers, one for writing and one for reading. Writing always starts 
 * from the current write pointer position. Reading always starts from the current read pointer 
 * position. 
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class FixedBuffer implements Buffer {
	
	private int getIndex;
	
	private int putIndex;
	
	private int size;
	
	private byte[] content;
	
	/**
	 * Constructs a new fixed buffer of the specified size.
	 * 
	 * @param size the buffer size
	 */
	public FixedBuffer(int size) {
		super();
		this.size = size;
		this.content = new byte[size];
	}
	
	/**
	 * Writes the specified bytes to this buffer. A call to this method may result in
	 * less bytes being added than what is available in the specified byte array
	 * depending on this buffers size and the current position of the write pointer.
	 * 
	 * @param b the bytes to add
	 * @return number of bytes added to this buffer
	 */
	public int put(byte[] b) {
		return put(b, 0, b.length);
	}
	
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
	public int put(byte[] b, int off, int len) {
		return put(b, off, len, 0);
	}
	
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
	public int put(byte[] b, int off, int len, int keep) {
		len = this.putIndex + len > this.size ? this.size - this.putIndex : len;
		System.arraycopy(b, off, this.content, this.putIndex, len);
		this.putIndex += len;
		return len;
	}
	
	/**
	 * Writes the specified bytes to this buffer starting at specified position. A call to this
	 * method may result in less bytes being added than what is available in the specified
	 * byte array depending on this buffers size and the specified position.
	 * 
	 * @param idx the position to add bytes in this buffer
	 * @param b the bytes to add
	 * @return number of bytes added
	 */
	public int putAt(int idx, byte[] b) {
		if(idx < 0 || idx + b.length > this.size) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		int len = this.size - this.putIndex < b.length ? this.size - this.putIndex : b.length;
		System.arraycopy(b, 0, this.content, idx, b.length);
		return len;
	}
	
	/**
	 * Moves the buffers put pointer forward for the specified number of bytes. The next put
	 * operation on this buffer will start adding bytes at the new position. Calling this method
	 * may result in less bytes being skipped depending on the current position of the write pointer.
	 * 
	 * @param skip number of bytes to move pointer forward
	 * @return number of bytes skipped
	 */
	public int skip(int skip) {
		skip = this.putIndex + skip >= this.size ? this.size - this.putIndex : skip;
		this.putIndex += skip;
		return skip;
	}
	
	/**
	 * Returns one byte from the current read position.
	 * 
	 * @return the read byte or -1 if end of buffer is reached
	 */
	public int get() {
		if(this.getIndex == this.size) {
			return -1;
		}
		
		return this.content[this.getIndex++];
	}
	
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
	public int get(byte[] b, int off, int len) {
		if(this.getIndex == this.size) {
			return -1;
		}
		
		if(len > putIndex - getIndex) {
			len = putIndex - getIndex;
		}
		
		System.arraycopy(content, this.getIndex, b, off, len);
		this.getIndex += len;
		return len;
	}
	
	/**
	 * Returns this buffers current read pointer position.
	 * 
	 * @return the read pointer position
	 */
	public int getIndex() {
		return this.getIndex;
	}
	
	/**
	 * Sets this buffers read pointer position to the specified index. A call to this method
	 * may result in the read pointer not being set to the specified index since the read
	 * pointer can not be higher than the write pointer
	 * 
	 * @param idx desired new read pointer position
	 * @return actual new read pointer position
	 * @throws ArrayIndexOutOfBoundException if given index is outside this buffers range
	 */
	public int setIndex(int idx) {
		if(idx < 0 || idx >= this.size) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		if(idx > this.putIndex) {
			idx = this.putIndex;
		}
		
		this.getIndex = idx;
		return idx;
	}
	
	/**
	 * Returns remaining number of bytes that can be written to this buffer.
	 * 
	 * @return remaining number of bytes that can be written to this buffer
	 */
	public int remaining() {
		return this.size - this.putIndex;
	}
	
	/**
	 * Sets the size of this buffer to the current write pointer position. A call to {@link #reset()}
	 * restores the original size of this buffer.
	 */
	public void compact() {
		this.size = this.putIndex;
	}
	
	/**
	 * Returns the size of this buffer.
	 * 
	 * @return buffer size
	 */
	public int size() {
		return this.size;
	}
	
	/**
	 * Resets this buffer by setting the read position 0, write position to 0 and
	 * restoring the original size.
	 */
	public void reset() {
		this.getIndex = 0;
		this.putIndex = 0;
		this.size = content.length;
	}
	
	/**
	 * Wraps this buffer into a byte buffer.
	 * 
	 * @return the byte buffer
	 */
	public ByteBuffer toByteBuffer() {
		return toByteBuffer(0);
	}
	
	/**
	 * Wraps this buffer into a byte buffer starting at the specified skip position.
	 * 
	 * @param skip the start position
	 * @return the byte buffer
	 */
	public ByteBuffer toByteBuffer(int skip) {
		return ByteBuffer.wrap(this.content, skip, this.putIndex - skip);
	}
	
	/**
	 * Copies the bytes in this buffer into a byte array.
	 * 
	 * @return the byte array
	 */
	public byte[] toByteArray() {
		byte[] outArray = new byte[this.putIndex];
		System.arraycopy(this.content, 0, outArray, 0, this.putIndex);
		return outArray;
	}
}
