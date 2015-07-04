/*
 * Copyright 2012-2014 the original author or authors.
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

package sample.data.jpa.service.util;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;

/**
 * @author zgq
 */
public class MyUtil {
	public static String genAudioFile(String audioDirStr, String content) {
		System.out.println("genAudioFile content:" + content);
		File audioDir = new File(audioDirStr);
		File destFile = null;
		boolean hasException = false;
		int count = 0;

		if (!audioDir.exists()) {
			audioDir.mkdirs();
		}
		do {
			try {
				content = URLEncoder.encode(content, "UTF-8");
				content = StringUtils.trim(content);
				String urlStr = "http://tts-api.com/tts.mp3?q=" + content;
				URL url = new URL(urlStr);
				System.out.println(url.toString());
				String dateStr = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
						.format(new Date()) + "_" + RandomUtils.nextInt(9999);
				destFile = new File(audioDirStr + "/" + dateStr + ".mp3");
				FileUtils.copyURLToFile(url, destFile);
			}
			catch (Exception e) {
				hasException = true;
				// e.printStackTrace();
			}
		}
		while (hasException && count < 4);
		return hasException ? "" : destFile.getName();
	}
}
