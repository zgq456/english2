package sample.data.jpa.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Sentence implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	private String content;

	@ManyToOne
	private Article article;

	@OneToMany(mappedBy = "sen")
	// fetch = FetchType.EAGER,
	private List<UserSenAsso> users;

	@Column(nullable = true)
	private String lastUpt;

	@Column(nullable = true)
	private String createDate;

	@Column(nullable = true)
	private String audioPath;

	@Column(nullable = true)
	private String tempFlag;

	public Sentence() {
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

	public String getTempFlag() {
		return this.tempFlag;
	}

	public void setTempFlag(String tempFlag) {
		this.tempFlag = tempFlag;
	}

	public List<UserSenAsso> getUsers() {
		return this.users;
	}

	public void setUsers(List<UserSenAsso> users) {
		this.users = users;
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

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		// if (content != null && content.length() >= 255) {
		// content = content.substring(0, 250) + "...";
		// }
		this.content = content;
	}

	public Article getArticle() {
		return this.article;
	}

	public void setArticle(Article article) {
		this.article = article;
	}

	// @Override
	// public String toString() {
	// return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	// }
}
