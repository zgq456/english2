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

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
public class WordBean implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = true)
	private String content;

	@Column(nullable = true)
	private String explain2;

	@Column(nullable = true)
	private String pron;

	@Column(nullable = true)
	private Long hit;

	@Column(nullable = true)
	private Integer rank;

	@Column(nullable = true)
	private Long user_id;

	public WordBean() {

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

	public Long getHit() {
		return this.hit;
	}

	public void setHit(Long hit) {
		this.hit = hit;
	}

	public Integer getRank() {
		return this.rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public Long getUser_id() {
		return this.user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
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
