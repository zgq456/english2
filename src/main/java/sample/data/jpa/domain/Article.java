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
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({ "sentences", "words", "users" })
public class Article implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = true)
	private String name;

	@Column(nullable = true)
	private String url;

	@Column(nullable = true)
	private String remark;

	@Column(nullable = true)
	private String lastUpt;

	@Column(nullable = true)
	private String createDate;

	@Column(nullable = true)
	private String openFlag;

	@Column(nullable = true)
	private String type;;

	@OneToMany(mappedBy = "article", cascade = { CascadeType.ALL })
	// fetch = FetchType.EAGER,
	private Set<Sentence> sentences;

	@OneToMany(mappedBy = "article", cascade = { CascadeType.ALL })
	// , cascade = { CascadeType.ALL }
	// fetch = FetchType.EAGER,
	private List<ArticleWordAsso> words;

	@OneToMany(mappedBy = "article", cascade = { CascadeType.ALL })
	// , cascade = { CascadeType.ALL }
	// fetch = FetchType.EAGER,
	private List<UserArticleForkAsso> users;

	@ManyToOne
	private User user;

	public Article() {
	}

	public List<UserArticleForkAsso> getUsers() {
		return this.users;
	}

	public void setUsers(List<UserArticleForkAsso> users) {
		this.users = users;
	}

	public List<ArticleWordAsso> getWords() {
		return this.words;
	}

	public void setWords(List<ArticleWordAsso> words) {
		this.words = words;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
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

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Set<Sentence> getSentences() {
		return this.sentences;
	}

	public void setSentences(Set<Sentence> sentences) {
		this.sentences = sentences;
	}

	public String getOpenFlag() {
		return this.openFlag;
	}

	public void setOpenFlag(String openFlag) {
		this.openFlag = openFlag;
	}

	// @Override
	// public String toString() {
	// return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	// }
}
