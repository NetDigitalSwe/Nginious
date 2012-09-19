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

import java.util.Enumeration;
import java.util.Iterator;

/**
 * An enumeration that enumerates over elements in an iterator.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 * @param <E> the enumerated element
 */
public class IteratorEnumeration<E> implements Enumeration<E> {
	
	private Iterator<E> iterator;
	
	/**
	 * Constructs a new iterator enumeration with the specified iterator.
	 * 
	 * @param iterator the iterator to enumerate over
	 */
	public IteratorEnumeration(Iterator<E> iterator) {
		this.iterator = iterator;
	}
	
	/**
	 * Returns whether or not there are more elements to enumerate over.
	 * 
	 * @return <code>true</code> if there are more elements, <code>false</code> otherwise
	 */
	public boolean hasMoreElements() {
		return iterator.hasNext();
	}
	
	/**
	 * Returns next element.
	 * 
	 * @return next element
	 */
	public E nextElement() {
		return iterator.next();
	}
}
