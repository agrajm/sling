/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.ide.transport;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ProtectedNodes {
	
	JCR_CREATED(Repository.JCR_CREATED),JCR_CREATED_BY(Repository.JCR_CREATED_BY),JCR_PRIMARY_TYPE(Repository.JCR_PRIMARY_TYPE);

	private static final Map<String, ProtectedNodes> nameToValueMap =
			new HashMap<String, ProtectedNodes>();

	static {
		for (ProtectedNodes value : EnumSet.allOf(ProtectedNodes.class)) {			 
			nameToValueMap.put(value.getKey(), value);
		}
	}

	public static boolean exists(String key) {
		return nameToValueMap.containsKey(key);
	}
	
	
	
	private final String key;

	private ProtectedNodes(String key) {
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}
	
}
