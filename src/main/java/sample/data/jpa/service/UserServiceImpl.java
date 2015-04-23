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

package sample.data.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sample.data.jpa.domain.User;
import sample.data.jpa.repository.UserRepository;

@Component("userService")
@Transactional
public class UserServiceImpl {

	private final UserRepository userRepository;

	@Autowired
	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User getUser(String email, String password) {
		User user = this.userRepository.findByEmailAndPassword(email, password);
		return user;
	}

	public User getUserByEmail(String email) {
		User user = this.userRepository.findByEmail(email);
		return user;
	}

	public User getUserByOpenId(String openId) {
		User user = this.userRepository.findByOpenId(openId);
		return user;
	}
}
