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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({ "groups", "articles", "users" })
public class Word implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = true)
	private String value;

	@Column(nullable = true)
	private String lowValue;

	@Column(nullable = true)
	private String explain2;

	@Column(nullable = true)
	private String pron;

	@Column(nullable = true)
	private String lastUpt;

	@Column(nullable = true)
	private String createDate;

	@Column(nullable = true)
	private Integer level;

	@ManyToMany(mappedBy = "words")
	// , fetch = FetchType.EAGER
	private List<Group> groups;

	@OneToMany(mappedBy = "word")
	// , fetch = FetchType.EAGER
	private List<QuizResult> qrList;

	@OneToMany(mappedBy = "word")
	// fetch = FetchType.EAGER,
	private List<ArticleWordAsso> articles;

	@OneToMany(mappedBy = "word")
	// fetch = FetchType.EAGER,
	private List<UserWordAsso> users;

	// temp_hit, uwo.rank temp_rank, uwo.user_id
	@Column
	private Long tempHit;
	@Column
	private String tempRank;
	@Column
	private Long tempUserId;

	/**
	 * 1 ��ʾԭ��
	 */
	@Column(nullable = true)
	private int mark;

	public Word() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLastUpt() {
		return this.lastUpt;
	}

	public void setLastUpt(String lastUpt) {
		this.lastUpt = lastUpt;
	}

	public String getCreateDate() {
		return this.createDate;
	}

	public Integer getLevel() {
		return this.level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<ArticleWordAsso> getArticles() {
		return this.articles;
	}

	public void setArticles(List<ArticleWordAsso> articles) {
		this.articles = articles;
	}

	public List<UserWordAsso> getUsers() {
		return this.users;
	}

	public void setUsers(List<UserWordAsso> users) {
		this.users = users;
	}

	public Long getTempHit() {
		return this.tempHit;
	}

	public void setTempHit(Long tempHit) {
		this.tempHit = tempHit;
	}

	public String getTempRank() {
		return this.tempRank;
	}

	public void setTempRank(String tempRank) {
		this.tempRank = tempRank;
	}

	public Long getTempUserId() {
		return this.tempUserId;
	}

	public void setTempUserId(Long tempUserId) {
		this.tempUserId = tempUserId;
	}

	public String getExplain2() {
		return this.explain2;
	}

	public void setExplain(String explain2) {
		this.explain2 = explain2;
	}

	public int getMark() {
		return this.mark;
	}

	public void setMark(int mark) {
		this.mark = mark;
	}

	public String getLowValue() {
		return this.lowValue;
	}

	public void setLowValue(String lowValue) {
		this.lowValue = lowValue;
	}

	public void setExplain2(String explain2) {
		if (explain2 != null && explain2.length() >= 250) {
			explain2 = explain2.substring(0, 250);
		}
		this.explain2 = explain2;
	}

	public List<Group> getGroups() {
		return this.groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public String getPron() {
		return this.pron;
	}

	public void setPron(String pron) {
		this.pron = pron;
	}

	// @Override
	// public String toString() {
	// return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	// }
}
