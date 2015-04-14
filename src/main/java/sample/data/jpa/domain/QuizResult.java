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
public class QuizResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private Quiz quiz;

	@ManyToOne
	private Word word;

	@ManyToOne
	private User user;

	@Column(nullable = true)
	private int isRight;

	@Column(nullable = true)
	private String lastUpt;

	public QuizResult() {
	}

	public Quiz getQuiz() {
		return this.quiz;
	}

	public void setQuiz(Quiz quiz) {
		this.quiz = quiz;
	}

	public int getIsRight() {
		return this.isRight;
	}

	public void setIsRight(int isRight) {
		this.isRight = isRight;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Word getWord() {
		return this.word;
	}

	public void setWord(Word word) {
		this.word = word;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getLastUpt() {
		return this.lastUpt;
	}

	public void setLastUpt(String lastUpt) {
		this.lastUpt = lastUpt;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	// @Override
	// public String toString() {
	// return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	// }
}
