/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.ofbiz.base.util.collections;

import org.apache.ofbiz.base.util.UtilObject;

import java.util.Map;

public class GenericMapEntry<K, V> implements Map.Entry<K, V> {
	protected final GenericMap<K, V> map;
	protected final K key;
	protected final boolean noteAccess;

	public GenericMapEntry(GenericMap<K, V> map, K key, boolean noteAccess) {
		this.map = map;
		this.key = key;
		this.noteAccess = noteAccess;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return map.get(key, noteAccess);
	}

	public V setValue(V value) {
		return map.put(key, value);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Map.Entry<?, ?>)) return false;
		if (this == o) return true;
		Map.Entry<?, ?> other = (Map.Entry<?, ?>) o;
		return UtilObject.equalsHelper(getKey(), other.getKey()) && UtilObject.equalsHelper(getValue(), other.getValue());
	}

	@Override
	public int hashCode() {
		return UtilObject.doHashCode(getKey()) ^ UtilObject.doHashCode(getValue());
	}
}
