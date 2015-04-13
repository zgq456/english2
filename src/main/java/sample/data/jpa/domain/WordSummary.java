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

public class WordSummary implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private Word word;

	private Long hit;
	private int rank;

	public WordSummary(Word word, int rank, long hit) {
		super();
		this.word = word;
		this.rank = rank;
		this.hit = hit;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getHit() {
		return this.hit;
	}

	public void setHit(Long hit) {
		this.hit = hit;
	}

	public Word getWord() {
		return this.word;
	}

	public void setWord(Word word) {
		this.word = word;
	}

	public int getRank() {
		return this.rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

}
