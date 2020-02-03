package org.springmeetup.elasticsearch.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfiguration {

	@Value("#{'${elasticsearch.hosts}'.split(',')}") 
	private List<String> elasticHosts;

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.registerModule(new JavaTimeModule())
				;
	}

	@Bean
	public RestHighLevelClient client() {
		
		return new RestHighLevelClient(
				RestClient.builder(elasticHosts.stream()
						.map(this::createUrl)
						.map(u ->  new HttpHost(u.getHost(), u.getPort(), u.getProtocol()))
						.toArray(HttpHost[]::new)));
	}
	
	private URL createUrl(String url) {
	    try {
	        return new URL(url);
	    } catch (MalformedURLException error) {
	        throw new IllegalArgumentException(error.getMessage(), error);
	    }
	}
}