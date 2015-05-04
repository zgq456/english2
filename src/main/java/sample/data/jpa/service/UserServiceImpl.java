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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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

	/**
	 * @param emailBind
	 * @param passwordBind
	 * @return
	 */
	public String bindAccount(String emailBind, String passwordBind, String openId) {
		String result = "";
		User userWeb = getUser(emailBind, passwordBind);
		if (userWeb == null) {
			result = "用户名或密码错误";
		}
		else if (StringUtils.isEmpty(openId)) {
			result = "openId为空，请在微信端访问";
		}
		else {
			User userWeixin = getUserByOpenId(openId);
			if (userWeixin == null) {
				result = "微信用户无效";
			}
			else {
				userWeb.setOpenId(openId);
				userWeb.setNickname(userWeixin.getNickname());
				userWeb.setSex(userWeixin.getSex());
				userWeb.setProvince(userWeixin.getProvince());
				userWeb.setCity(userWeixin.getCity());
				userWeb.setCountry(userWeixin.getCountry());
				userWeb.setHeadimgurl(userWeixin.getHeadimgurl());
				this.userRepository.save(userWeb);

				userWeixin.setOpenId(userWeixin.getOpenId() + "_deprecated");
				this.userRepository.save(userWeixin);
				// Authentication auth = SecurityContextHolder.getContext()
				// .getAuthentication();
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
						userWeb.getOpenId(), userWeb, userWeb.getAuthorities());
				auth.setDetails(MyConstants.TERMINAL_WEIXIN);
				SecurityContextHolder.getContext().setAuthentication(auth);
				result = "绑定成功";
			}
		}

		return result;
	}

	/**
	 * @param user
	 * @param emailVal
	 */
	public void updateUserReminder(User user, String bindReminderVal) {
		user.setBindRemind(bindReminderVal);
		this.userRepository.save(user);
	}
}
