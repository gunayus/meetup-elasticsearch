package org.springmeetup.elasticsearch.c03_ranking;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ranking-search")
@RequiredArgsConstructor
public class RankingSearchController {

	public static final String INDEX_NAME = "meetup-music";

	private final RestHighLevelClient client;

	@GetMapping("/search")
	public List<Map> searchDocuments(@RequestParam("q") String queryString) throws IOException {

		SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.query(
				QueryBuilders.functionScoreQuery(
						QueryBuilders.multiMatchQuery(queryString)
								.field("name.prefix", 1)
								.field("name.prefix_wo_ascii", 1)
								.operator(Operator.AND),
						new FunctionScoreQueryBuilder.FilterFunctionBuilder[] {
								new FunctionScoreQueryBuilder.FilterFunctionBuilder(
									ScoreFunctionBuilders.scriptFunction(
											"Math.max(_score * ((!doc[\"ranking\"].empty ) ? Math.log(doc[\"ranking\"].value) : 1)  - _score , 0)"
									)
								)
						}
				)
				.boostMode(CombineFunction.SUM)
				.scoreMode(FunctionScoreQuery.ScoreMode.SUM)
		);


		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

		return Arrays.stream(searchResponse.getHits().getHits())
				.map(SearchHit::getSourceAsMap)
				.collect(Collectors.toList());
	}

}
