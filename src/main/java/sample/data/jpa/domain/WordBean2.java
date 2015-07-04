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

import org.apache.commons.lang3.builder.ToStringBuilder;

public class WordBean2 implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;

	private String content;

	private String explain2;

	private String pron;
	private String audioPath;

	private String lastUpt;

	public WordBean2() {

	}

	public WordBean2(Long id, String content, String explain2, String pron,
			String audioPath, String lastUpt) {
		super();
		this.id = id;
		this.content = content;
		this.explain2 = explain2;
		this.pron = pron;
		this.audioPath = audioPath;
		this.lastUpt = lastUpt;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getExplain2() {
		return this.explain2;
	}

	public void setExplain2(String explain2) {
		this.explain2 = explain2;
	}

	public String getPron() {
		return this.pron;
	}

	public void setPron(String pron) {
		this.pron = pron;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getLastUpt() {
		return this.lastUpt;
	}

	public void setLastUpt(String lastUpt) {
		this.lastUpt = lastUpt;
	}

	/**
	 * @return the audioPath
	 */
	public String getAudioPath() {
		return this.audioPath;
	}

	/**
	 * @param audioPath the audioPath to set
	 */
	public void setAudioPath(String audioPath) {
		this.audioPath = audioPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
