package sample.data.jpa.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Quiz implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private User author;

	@Column(nullable = true)
	private String name;

	@Column(nullable = true)
	private String remark;

	@Column(nullable = true)
	private int type;

	@Column(nullable = true)
	private String password;

	@Column(nullable = true)
	private String lastUpt;

	@Column(nullable = true)
	private String expireDate;

	@Column(nullable = true)
	private int deleteFlag;

	@Column(nullable = true)
	private int hot;

	public Quiz() {
	}

	public int getHot() {
		return this.hot;
	}

	public void setHot(int hot) {
		this.hot = hot;
	}

	public int getDeleteFlag() {
		return this.deleteFlag;
	}

	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getAuthor() {
		return this.author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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

	public String getExpireDate() {
		return this.expireDate;
	}

	public void setExpireDate(String expireDate) {
		this.expireDate = expireDate;
	}

	public int getType() {
		return this.type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	// @Override
	// public String toString() {
	// return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	// }
}
