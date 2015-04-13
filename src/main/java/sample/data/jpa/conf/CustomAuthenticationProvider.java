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

package sample.data.jpa.conf;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;

import sample.data.jpa.domain.User;
import sample.data.jpa.service.UserServiceImpl;

/**
 * @author Administrator
 */
public class CustomAuthenticationProvider implements AuthenticationProvider, Serializable {
	private static final Logger logger = LoggerFactory
			.getLogger(CustomAuthenticationProvider.class);

	@Autowired
	private UserServiceImpl userService;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.security.authentication.AuthenticationProvider#authenticate
	 * (org.springframework.security.core.Authentication)
	 */
	@Override
	@Transactional
	public Authentication authenticate(Authentication authenticate)
			throws AuthenticationException {
		String email = authenticate.getName();
		String password = authenticate.getCredentials().toString();

		User user = this.userService.getUser(email, password);
		if (user != null) {
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
					email, password, user.getAuthorities());
			auth.setDetails(user.getRoleNames());
			return auth;
		}
		else {
			throw new AuthenticationCredentialsNotFoundException(
					"no credential information found");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.security.authentication.AuthenticationProvider#supports(java
	 * .lang.Class)
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
