package test;

import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

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

/**
 * @author Administrator
 */
public class TestEmma {

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		Scanner scanner = new Scanner(System.in);
		do {
			System.out.println("please input:");
			int x = scanner.nextInt();
			switch (x) {
			case 1:
				System.out.println(x);
				break;
			case 2:
				System.out.println(x);
				break;
			case 3:
				System.out.println(x);
				break;
			case 4:
				System.out.println(x);
				break;
			case 5:
				System.out.println(x);
				break;
			default:
				System.out.println("others:" + x);
				break;
			}
			System.out.println("here1");
			System.out.println("here2");
			System.out.println("here3");
			System.out.println("here4");
			System.out.println("here5");
			System.out.println(StringUtils.isEmpty("aaa"));

			Thread.sleep(10000);
		}
		while (true);

		// Thread.sleep(Integer.MAX_VALUE);
		//
		// System.out.println("end");
	}

}
