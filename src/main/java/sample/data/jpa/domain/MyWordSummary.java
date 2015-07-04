/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.data.jpa.domain;

import java.io.Serializable;

public class MyWordSummary implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long userId;

	private long wordId;

	private int previousCount;

	public MyWordSummary(Long userId, long wordId, int previousCount) {
		super();
		this.userId = userId;
		this.wordId = wordId;
		this.previousCount = previousCount;
	}

	public Long getUserId() {
		return this.userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public long getWordId() {
		return this.wordId;
	}

	public void setWordId(long wordId) {
		this.wordId = wordId;
	}

	public int getPreviousCount() {
		return this.previousCount;
	}

	public void setPreviousCount(int previousCount) {
		this.previousCount = previousCount;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
