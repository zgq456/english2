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

import com.google.code.mp3fenge.Mp3Fenge;

/**
 * @author Administrator
 */
public class TestMp3 {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Mp3Fenge helper = new Mp3Fenge(new File("testdata/02.mp3"));
		// helper.generateNewMp3ByTime(new File("testdata/e1.mp3"), 5000, 15000);
		int trackLength = helper.getMp3Info().getTrackLength();
		System.out.println("trackLength:" + trackLength);
	}

}
