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

public class SenBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private String content;

	private String usefulFlag = "No";

	public SenBean(Long id, String content, String usefulFlag) {
		super();
		this.id = id;
		this.content = content;
		this.usefulFlag = usefulFlag;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUsefulFlag() {
		return this.usefulFlag;
	}

	public void setUsefulFlag(String usefulFlag) {
		this.usefulFlag = usefulFlag;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
