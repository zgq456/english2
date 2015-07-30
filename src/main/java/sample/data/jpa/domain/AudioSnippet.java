package sample.data.jpa.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({ "audio", "answers" })
public class AudioSnippet implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	private long start;
	private long end;
	private String referAnswer;

	@Column(nullable = true)
	private String url;

	@ManyToOne
	private Audio audio;

	@OneToMany(mappedBy = "audioSnippet")
	// fetch = FetchType.EAGER,
	private List<AudioAnswer> answers;

	public AudioSnippet() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getStart() {
		return this.start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return this.end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public String getReferAnswer() {
		return this.referAnswer;
	}

	public void setReferAnswer(String referAnswer) {
		this.referAnswer = referAnswer;
	}

	public Audio getAudio() {
		return this.audio;
	}

	public void setAudio(Audio audio) {
		this.audio = audio;
	}

	public List<AudioAnswer> getAnswers() {
		return this.answers;
	}

	public void setAnswers(List<AudioAnswer> answers) {
		this.answers = answers;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	// @Override
	// public String toString() {
	// return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	// }
}
