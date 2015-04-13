/*
 * Copyright 2012-2014 the original author or authors.
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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Administrator
 */
public class QuizWordBean {
	private long wordId;
	private String value;
	private String explain2;
	private int rank;
	private int blankCount;
	private String answer;
	private String sentence;
	private int mark;

	public QuizWordBean(long wordId, String value, String explain2, int rank,
			String sentence, int mark) {
		super();
		this.wordId = wordId;
		this.value = value;
		this.explain2 = explain2;
		this.rank = rank;
		this.sentence = sentence;
		this.mark = mark;
	}

	public int getMark() {
		return this.mark;
	}

	public void setMark(int mark) {
		this.mark = mark;
	}

	public String getAnswer() {
		return this.answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public int getBlankCount() {
		return this.blankCount;
	}

	public void setBlankCount(int blankCount) {
		this.blankCount = blankCount;
	}

	public String getSentence() {
		return this.sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public long getWordId() {
		return this.wordId;
	}

	public void setWordId(long wordId) {
		this.wordId = wordId;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getExplain2() {
		return this.explain2;
	}

	public void setExplain2(String explain2) {
		this.explain2 = explain2;
	}

	public int getRank() {
		return this.rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
