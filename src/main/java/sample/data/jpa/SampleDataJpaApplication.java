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

package sample.data.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import sample.data.jpa.conf.AudioServlet;
import sample.data.jpa.conf.MySettings;

@SpringBootApplication
@EnableConfigurationProperties({ MySettings.class })
public class SampleDataJpaApplication extends SpringBootServletInitializer {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SampleDataJpaApplication.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SampleDataJpaApplication.class, args);
	}

	// @Bean
	// public Servlet testaudiofiles() {
	// return new AudioServlet();
	// }

	@Bean
	public ServletRegistrationBean servletRegistrationBean() {
		ServletRegistrationBean srb = new ServletRegistrationBean(new AudioServlet(),
				"/audiofiles/*");
		srb.setLoadOnStartup(0);
		return srb;
	}

	// @Override
	// protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
	// {
	// return application.sources(applicationClass);
	// }
	//
	// private static Class<SampleDataJpaApplication> applicationClass =
	// SampleDataJpaApplication.class;

}
