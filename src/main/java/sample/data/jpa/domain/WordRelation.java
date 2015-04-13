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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class WordRelation implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(optional = false)
	private Word word1;

	@ManyToOne(optional = false)
	private Word word2;

	/**
	 * 1 ��ʾ���
	 */
	@Column
	private int relation;

	public WordRelation() {
	}

	public Word getWord1() {
		return this.word1;
	}

	public void setWord1(Word word1) {
		this.word1 = word1;
	}

	public Word getWord2() {
		return this.word2;
	}

	public void setWord2(Word word2) {
		this.word2 = word2;
	}

	public int getRelation() {
		return this.relation;
	}

	public void setRelation(int relation) {
		this.relation = relation;
	}

	// @Override
	// public String toString() {
	// return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	// }
}
