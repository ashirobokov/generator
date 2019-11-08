package ru.ashirobokov.microservices.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("ru.ashirobokov.microservices")
@SpringBootApplication
public class GeneratorApplication {

	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(GeneratorApplication.class, args);
	}

}
