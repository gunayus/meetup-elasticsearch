package org.springmeetup.elasticsearch.c01_tfidf;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tfidf")
@RequiredArgsConstructor
public class TfIdfDemoController {

	public static final String INDEX_NAME = "meetup-tfidf";

	private final RestHighLevelClient client;

	@GetMapping("/search")
	public List<Map> searchDocuments(@RequestParam("q") String queryString) throws IOException  {

		SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.query(QueryBuilders.termQuery("description", queryString));

		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

		List<Map> result = new ArrayList<>();
		for (SearchHit searchHit : searchResponse.getHits()) {
			result.add(searchHit.getSourceAsMap());
		}

		return result;
	}
}
