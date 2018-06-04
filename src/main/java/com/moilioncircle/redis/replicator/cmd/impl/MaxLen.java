/*
 * Copyright 2016-2018 Leon Chen
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

package com.moilioncircle.redis.replicator.cmd.impl;

import java.io.Serializable;

/**
 * @author Leon Chen
 * @since 2.6.0
 */
public class MaxLen implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Boolean approximation;
	
	private long count;
	
	public MaxLen() {
	
	}
	
	public MaxLen(Boolean approximation, long count) {
		this.approximation = approximation;
		this.count = count;
	}
	
	public Boolean getApproximation() {
		return approximation;
	}
	
	public void setApproximation(Boolean approximation) {
		this.approximation = approximation;
	}
	
	public long getCount() {
		return count;
	}
	
	public void setCount(long count) {
		this.count = count;
	}
	
	@Override
	public String toString() {
		return "MaxLen{" +
				"approximation=" + approximation +
				", count=" + count +
				'}';
	}
}
