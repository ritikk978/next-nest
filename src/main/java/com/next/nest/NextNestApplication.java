package com.next.nest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@OpenAPIDefinition(
		info = @Info(
				title = "NextNest API",
				version = "1.0",
				description = "API documentation for NextNest property rental platform",
				contact = @Contact(
						name = "NextNest Support",
						email = "support@nextnest.com"
				),
				license = @License(
						name = "Private License",
						url = "https://nextnest.com/license"
				)
		)
)
public class NextNestApplication {
	public static void main(String[] args) {
		SpringApplication.run(NextNestApplication.class, args);
	}
}