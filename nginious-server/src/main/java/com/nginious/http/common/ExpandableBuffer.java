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
 * A buffer that is expanded in chunks of 8192 bytes as more data is added. Uses a two dimensional byte
 * array as backing store. Manages two internal pointers, one for writing and one for reading. Writing always starts 
 * from the current write pointer position. Reading always starts from the current read pointer 
 * position.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public class ExpandableBuffer implements Buffer {
	
	private static final int ELEMENT_SIZE = 8192;
	
	private int getIndex;
	
	private int putIndex;
	
	private int maxSize;
	
	private int curSize;
	
	private int numElements;
	
	private byte[][] content;
	
	private int contentPos;
	
	/**
	 * Constructs a new expandable buffer with the specified maximum size.
	 * 
	 * @param maxSize the maximum size
	 */
	public ExpandableBuffer(int maxSize) {
		super();
		this.maxSize = maxSize;
		this.numElements = calculateNumElements(maxSize);
		this.contentPos = 0;
		this.curSize = 0;
		this.content = new byte[this.numElements][];
		content[this.contentPos] = new byte[ELEMENT_SIZE];
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
		len -= keep;
		
		len = this.putIndex + len > this.maxSize ? this.maxSize - this.putIndex : len;
		put(this.putIndex, b, off, len);
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
		if(idx < 0 || idx  + b.length > this.maxSize) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		put(idx, b, 0, b.length);
		return b.length;
	}
	
	/**
	 * Writes len bytes from the specified byte array starting at the specified offset into
	 * this buffer starting at the element at the specified index.
	 * 
	 * @param idx the element index to start writing at
	 * @param b the byte array
	 * @param off the start position in the byte array
	 * @param len number of bytes to write
	 */
	private void put(int idx, byte[] b, int off, int len) {
		int curNumElements = calculateNumElements(this.curSize);
		int newNumElements = calculateNumElements(idx + len);
		
		if(newNumElements > curNumElements) {
			for(int i = curNumElements; i < newNumElements; i++) {
				content[i] = new byte[ELEMENT_SIZE];
			}
		}
		
		int savedLen = len;
		int savedIdx = idx;
		int bPos = 0;
		
		while(len > 0) {
			int element = (int)(idx / ELEMENT_SIZE);
			int ePos = idx % ELEMENT_SIZE;
			int size = len > ELEMENT_SIZE - ePos ? ELEMENT_SIZE - ePos : len;
			// System.out.println("Elem=" + element + ", epos=" + ePos + ", size=" + size);
			System.arraycopy(b, bPos + off, content[element], ePos, size);
			bPos += ELEMENT_SIZE - ePos;
			len -= ELEMENT_SIZE - ePos;
			idx += ELEMENT_SIZE - ePos;
		}

		if(savedIdx == this.curSize) {
			this.curSize += savedLen;
		}
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
		skip = this.putIndex + skip >= this.curSize ? this.curSize - this.putIndex : skip;
		this.putIndex += skip;
		return skip;
	}
	
	/**
	 * Returns one byte from the current read position.
	 * 
	 * @return the read byte or -1 if end of buffer is reached
	 */
	public int get() {
		if(this.getIndex == this.curSize) {
			return -1;
		}
		
		int element = this.getIndex / ELEMENT_SIZE;
		int pos = this.getIndex % ELEMENT_SIZE;
		this.getIndex++;
		return content[element][pos];
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
		if(this.getIndex == this.curSize) {
			return -1;
		}
		
		if(len > putIndex - getIndex) {
			len = putIndex - getIndex;
		}
		
		int startElement = this.getIndex / ELEMENT_SIZE;
		int startPos = this.getIndex % ELEMENT_SIZE;
		int endElement = (this.getIndex + len) / ELEMENT_SIZE;
		int endPos = (this.getIndex + len) % ELEMENT_SIZE;
		
		// System.out.println("Size: " + this.curSize + ", startElem=" + startElement + ", startPos=" + startPos + ", endElem=" + endElement + ", endPos=" + endPos + ", len=" + len);
		
		for(int i = startElement; i < endElement; i++) {
			System.arraycopy(content[i], startPos, b, off, ELEMENT_SIZE - startPos);
			off += ELEMENT_SIZE - startPos;
			startPos = 0;
		}
		
		if((this.getIndex + len) % ELEMENT_SIZE != 0) {
			System.arraycopy(content[endElement], 0, b, off, endPos);
		}
		
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
		if(idx < 0 || idx >= this.curSize) {
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
		return 0;
	}
	
	/**
	 * Sets the size of this buffer to the current write pointer position. A call to {@link #reset()}
	 * restores the original size of this buffer.
	 */
	public void compact() {
		return;
	}
	
	/**
	 * Returns the size of this buffer.
	 * 
	 * @return buffer size
	 */
	public int size() {
		return this.curSize;
	}
	
	/**
	 * Resets this buffer by setting the read position 0, write position to 0 and
	 * restoring the original size.
	 */
	public void reset() {
		this.getIndex = 0;
		this.putIndex = 0;
	}
	
	/**
	 * Wraps this buffer into a byte buffer.
	 * 
	 * @return the byte buffer
	 */
	public ByteBuffer toByteBuffer() {
		return ByteBuffer.wrap(toByteArray());
	}
	
	/**
	 * Wraps this buffer into a byte buffer starting at the specified skip position.
	 * 
	 * @param skip the start position
	 * @return the byte buffer
	 */
	public ByteBuffer toByteBuffer(int skip) {
		return ByteBuffer.wrap(toByteArray(), skip, this.curSize - skip);
	}
	
	/**
	 * Copies the bytes in this buffer into a byte array.
	 * 
	 * @return the byte array
	 */
	public byte[] toByteArray() {
		byte[] outArray = new byte[this.curSize];
		int numElements = calculateNumElements(this.curSize);
		
		if(numElements == 0) {
			return new byte[0];
		}
		
		for(int i = 0; i < numElements - 1; i++) {
			System.arraycopy(this.content[i], 0, outArray, i * ELEMENT_SIZE, ELEMENT_SIZE);
		}
		
		int lastSize = this.curSize % ELEMENT_SIZE == 0 ? ELEMENT_SIZE : this.curSize % ELEMENT_SIZE;
		System.arraycopy(this.content[numElements - 1], 0, outArray, (numElements - 1) * ELEMENT_SIZE, lastSize);
		return outArray;
	}
	
	/**
	 * Calculates number of elements in two dimensional backing store array based on the specified
	 * size. Each element contains 8192 bytes.
	 * 
	 * @param size the maximum size
	 * @return number of elements
	 */
	private int calculateNumElements(int size) {
		int numElements = (int)(size / ELEMENT_SIZE);
		
		if(size % ELEMENT_SIZE != 0) {
			numElements++;
		}
		
		return numElements;
	}
}
