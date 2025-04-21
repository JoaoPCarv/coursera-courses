package org.magnum.mobilecloud.video;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@EnableJpaRepositories(basePackages = "org.magnum.mobilecloud.video.repository")
@SpringBootApplication
@EnableResourceServer
public class Application {


	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
