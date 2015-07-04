package sample.data.jpa.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "english")
public class MySettings {
	private String audioDir;

	/**
	 * @return the audioDir
	 */
	public String getAudioDir() {
		return this.audioDir;
	}

	/**
	 * @param audioDir the audioDir to set
	 */
	public void setAudioDir(String audioDir) {
		this.audioDir = audioDir;
	}

}