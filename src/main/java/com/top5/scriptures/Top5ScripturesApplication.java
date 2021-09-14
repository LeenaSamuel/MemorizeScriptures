package com.top5.scriptures;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = {"com.top5.scriptures"})
public class Top5ScripturesApplication {

	public static void main(String[] args) {
		SpringApplication.run(Top5ScripturesApplication.class, args);
	}

}
