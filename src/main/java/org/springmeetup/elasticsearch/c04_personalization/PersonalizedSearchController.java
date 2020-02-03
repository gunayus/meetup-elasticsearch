package org.springmeetup.elasticsearch.c04_personalization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.ScriptScoreFunctionBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springmeetup.elasticsearch.c04_personalization.model.EventDocument;
import org.springmeetup.elasticsearch.c04_personalization.model.EventType;
import org.springmeetup.elasticsearch.c04_personalization.model.UserArtistRank;
import org.springmeetup.elasticsearch.c04_personalization.model.UserProfileDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/personalized-search")
@RequiredArgsConstructor
@Slf4j
public class PersonalizedSearchController {

	public static final String INDEX_NAME = "meetup-music";
	public static final String EVENT_INDEX_NAME = "meetup-event";

	private final RestHighLevelClient client;
	private final ObjectMapper objectMapper;

	@GetMapping("/event")
	public IndexResponse saveEvent(@RequestParam("userid") String userid,
	                         @RequestParam("artistid") String artistid,
	                         @RequestParam("event-type") EventType eventType) throws IOException {

		EventDocument eventDocument = EventDocument.builder()
				.userId(userid)
				.artistId(artistid)
				.eventType(eventType)
				.build();

		IndexRequest indexRequest = new IndexRequest(EVENT_INDEX_NAME);
		String jsonString;
		try {
			jsonString = objectMapper.writeValueAsString(eventDocument);
			System.out.println(jsonString + ",");
		} catch (JsonProcessingException jpe) {
			throw new RuntimeException(jpe);
		}

		indexRequest.source(jsonString, XContentType.JSON);

		IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
		return indexResponse;

	}

	@GetMapping("/search")
	public List<Map> searchDocuments(@RequestParam("u") String userid,
	                                 @RequestParam("q") String queryString) throws IOException {

		UserProfileDocument userProfileDocument = findUserProfile(userid);

		log.info("userprofile : {}", userProfileDocument);

		SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		// take the first artist id and rank
		UserArtistRank firstUserArtistRank = userProfileDocument.getArtist().get(0);

		ScriptScoreFunctionBuilder scriptScoreFunctionBuilder = ScoreFunctionBuilders.scriptFunction(
				"Math.max(_score * " +
								"((!doc[\"id\"].empty  && doc[\"id\"].value == \"" + firstUserArtistRank.getArtistId() + "\") ? " +
								" " + firstUserArtistRank.getRank() +
								" : 1) " +
							" - _score " +
						", 0)"
		);

		log.info(scriptScoreFunctionBuilder.getScript().toString());

		searchSourceBuilder.query(
				QueryBuilders.functionScoreQuery(
						QueryBuilders.multiMatchQuery(queryString)
								.field("name.prefix", 1)
								.field("name.prefix_wo_ascii", 1)
								.operator(Operator.AND),
						scriptScoreFunctionBuilder
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

	private UserProfileDocument findUserProfile(String userid) throws IOException {
		UserProfileDocument userProfileDocument = UserProfileDocument.builder()
				.userid(userid)
				.build();


		SearchRequest searchRequest = new SearchRequest();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.query(QueryBuilders.boolQuery()
				.filter(QueryBuilders.termQuery("user_id", userid))
		);

		searchSourceBuilder.aggregation(AggregationBuilders.terms("artists").field("artist_id"));

		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

		Terms terms = searchResponse.getAggregations().get("artists");

		List<UserArtistRank> userArtistRankList = terms.getBuckets().stream()
				.map(bucket -> UserArtistRank.builder()
						.artistId(((Terms.Bucket) bucket).getKeyAsString())
						.rank(((Terms.Bucket) bucket).getDocCount())
						.build()
				).collect(Collectors.toList());

		userProfileDocument.setArtist(userArtistRankList);
		return userProfileDocument;
	}

}
