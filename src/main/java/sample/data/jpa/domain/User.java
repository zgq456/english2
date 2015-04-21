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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({ "articles", "words", "qrList" })
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = true)
	private String name;

	@Column
	private String email;

	@Column
	private String password;

	@Column(nullable = true)
	private String wechat;

	@Column(nullable = true)
	private String degree;

	@Column(nullable = true)
	private Integer age;

	@Column(nullable = true)
	private String aboutMe;

	@Column(nullable = true)
	private String remark;

	@Column(nullable = true)
	private String lastUpt;

	@Column(nullable = true)
	private String createDate;

	@Column(nullable = true)
	private String openId;

	@Column(nullable = true)
	private String nickname;

	@Column(nullable = true)
	private int sex = -1;

	@Column(nullable = true)
	private String province;

	@Column(nullable = true)
	private String city;

	@Column(nullable = true)
	private String country;

	@Column(nullable = true)
	private String headimgurl;

	@OneToMany(mappedBy = "user")
	// fetch = FetchType.EAGER,
	private Set<Article> articles;

	@OneToMany(mappedBy = "user")
	// , fetch = FetchType.EAGER
	private List<QuizResult> qrList;

	@OneToMany(mappedBy = "user")
	// , cascade = { CascadeType.ALL }
	// fetch = FetchType.EAGER,
	private List<UserWordAsso> words;

	@OneToMany(mappedBy = "user")
	// , cascade = { CascadeType.ALL }
	// fetch = FetchType.EAGER,
	private List<UserSenAsso> sens;

	@OneToMany(mappedBy = "user")
	// , cascade = { CascadeType.ALL }
	// fetch = FetchType.EAGER,
	private List<UserArticleForkAsso> forkArticles;

	@ManyToMany
	@JoinTable(name = "uesr_role", joinColumns = { @JoinColumn(name = "user_id") }, inverseJoinColumns = { @JoinColumn(name = "role_id") })
	private Set<Role> roles = new HashSet<Role>();

	public User() {
	}

	public List<UserArticleForkAsso> getForkArticles() {
		return this.forkArticles;
	}

	public void setForkArticles(List<UserArticleForkAsso> forkArticles) {
		this.forkArticles = forkArticles;
	}

	public String getOpenId() {
		return this.openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getNickname() {
		return this.nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getSex() {
		return this.sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public String getProvince() {
		return this.province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return this.city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return this.country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getHeadimgurl() {
		return this.headimgurl;
	}

	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<QuizResult> getQrList() {
		return this.qrList;
	}

	public void setQrList(List<QuizResult> qrList) {
		this.qrList = qrList;
	}

	public List<UserSenAsso> getSens() {
		return this.sens;
	}

	public void setSens(List<UserSenAsso> sens) {
		this.sens = sens;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getWechat() {
		return this.wechat;
	}

	public void setWechat(String wechat) {
		this.wechat = wechat;
	}

	public String getDegree() {
		return this.degree;
	}

	public void setDegree(String degree) {
		this.degree = degree;
	}

	public Integer getAge() {
		return this.age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getAboutMe() {
		return this.aboutMe;
	}

	public void setAboutMe(String aboutMe) {
		this.aboutMe = aboutMe;
	}

	public Set<Article> getArticles() {
		return this.articles;
	}

	public void setArticles(Set<Article> articles) {
		this.articles = articles;
	}

	public List<UserWordAsso> getWords() {
		return this.words;
	}

	public void setWords(List<UserWordAsso> words) {
		this.words = words;
	}

	// @Override
	// public String toString() {
	// return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	// }

	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		for (Role role : this.getRoles()) {
			for (Privilege priv : role.getPrivileges()) {
				authorities.add(new SimpleGrantedAuthority(priv.getOepration() + "_"
						+ priv.getTarget()));
			}
		}
		return authorities;
	}

	public Set<String> getPrivs() {
		Set<String> privs = new HashSet<String>();
		for (Role role : this.getRoles()) {
			for (Privilege priv : role.getPrivileges()) {
				privs.add(priv.getOepration() + "_" + priv.getTarget());
			}
		}
		return privs;
	}

	public Set<Role> getRoles() {
		return this.roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Set<String> getRoleNames() {
		Set<String> names = new HashSet<String>();
		for (Role role : this.getRoles()) {
			names.add(role.getName());
		}
		return names;
	}

}
