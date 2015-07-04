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

package research;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author zgq
 */
public class TestText2Speech {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// String q =
		// " The big question , from an engineering standpoint not a physics standpoint , is getting the sails to deploy .";
		// String q =
		// " It is made of Mylar , a material that is only one fourth as thick as human hair .";
		// String q =
		// " 科学家认为 ， 这种推进方式能够降低各个大学甚至是个人探索太空的费用 。 The sail is 32-square meters long .";
		// String q =
		// " Photons have no mass but they still impart momentum when they strike an object ... it 's a tiny , tiny amount of momentum .";
		// String q = "more conducive to learning .";
		String q = "hello,  h, e, l, l, o,  hello";
		q = URLEncoder.encode(q, "UTF-8");
		q = StringUtils.trim(q);
		String urlStr = "http://tts-api.com/tts.mp3?q=" + q;
		URL url = new URL(urlStr);
		System.out.println(url.toString());
		File onLineFile = FileUtils.toFile(url);
		String dateStr = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
		File destFile = new File("audio/" + dateStr + ".mp3");
		// FileUtils.copyFile(onLineFile, destFile);
		FileUtils.copyURLToFile(url, destFile);
	}

}
