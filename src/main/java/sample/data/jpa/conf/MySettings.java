package sample.data.jpa.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "english")
public class MySettings {
	private String audioSnippetDir;
	private String audioDir;

	public String getAudioSnippetDir() {
		return this.audioSnippetDir;
	}

	public void setAudioSnippetDir(String audioSnippetDir) {
		this.audioSnippetDir = audioSnippetDir;
	}

	public String getAudioDir() {
		return this.audioDir;
	}

	public void setAudioDir(String audioDir) {
		this.audioDir = audioDir;
	}

}