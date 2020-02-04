package org.springmeetup.elasticsearch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class JacksonConfig {

	private final ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

}
