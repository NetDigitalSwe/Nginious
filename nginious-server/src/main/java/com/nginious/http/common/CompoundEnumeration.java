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
import java.util.HashSet;
import java.util.Iterator;

/**
 * A compound enumeration enumerates over one or more other enumerations thereby creating
 * a merged view of several enumerations. Enumerations are enumerated over in the order
 * added to this compound enumeration.
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 * @param <E> the enumerated element
 */
public class CompoundEnumeration<E> implements Enumeration<E> {
	
	private HashSet<E> elements;
	
	private Iterator<E> iterator;
	
	/**
	 * Constructs a new compound enumeration.
	 */
	public CompoundEnumeration() {
		super();
		this.elements = new HashSet<E>();
	}
	
	/**
	 * Adds the specified enumeration to this compound enumeration.
	 * 
	 * @param elems the enumeration to add
	 */
	public void addAll(Enumeration<E> elems) {
		while(elems.hasMoreElements()) {
			elements.add(elems.nextElement());
		}
	}
	
	/**
	 * Returns whether or not this compound enumeration has any more elements to return.
	 * This enumeration doesn't have any more elements to return once all added enumerations
	 * have been enumerated over.
	 * 
	 * @return <code>true</code> if there are more elements to return, <code>false</code> otherwise
	 */
	public boolean hasMoreElements() {
		if(this.iterator == null) {
			this.iterator = elements.iterator();
		}
		
		return iterator.hasNext();
	}
	
	/**
	 * Returns next element from this compound enumeration.
	 * 
	 * @return next element
	 */
	public E nextElement() {
		if(this.iterator == null) {
			this.iterator = elements.iterator();
		}
		
		return iterator.next();
	}
}
