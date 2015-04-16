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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author Administrator
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		System.out.println("###SecurityConfig.configureGlobal");
		// auth.inMemoryAuthentication().withUser("user").password("password").roles("USER");
		auth.authenticationProvider(getAuthnticationProvider());
	}

	@Bean(name = "restAuthenticationProvider")
	public AuthenticationProvider getAuthnticationProvider() {
		System.out.println("AuthenticationProvider getAuthnticationProvider");
		return new CustomAuthenticationProvider();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.config.annotation.web.configuration.
	 * WebSecurityConfigurerAdapter
	 * #configure(org.springframework.security.config.annotation
	 * .web.builders.HttpSecurity)
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.csrf().disable();
		http.headers().cacheControl().disable();

		http.authorizeRequests().antMatchers("/assets/**").permitAll();
		http.authorizeRequests().antMatchers("/login").permitAll();
		http.authorizeRequests().antMatchers("/weixin").permitAll();
		http.authorizeRequests().antMatchers("/myregister").permitAll();
		// http.authorizeRequests().antMatchers("/**").permitAll();

		http.authorizeRequests().anyRequest().authenticated().and().formLogin()
				.defaultSuccessUrl("/html/ajax/ajax.html", false)
				.loginPage("/html/login.html").permitAll();

	}
	//
	// @Bean
	// public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
	// CasAuthenticationEntryPoint casAuthenticationEntryPoint = new
	// CasAuthenticationEntryPoint();
	// casAuthenticationEntryPoint.setLoginUrl("https://localhost:9443/cas/login");
	// casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
	// return casAuthenticationEntryPoint;
	// }

}
