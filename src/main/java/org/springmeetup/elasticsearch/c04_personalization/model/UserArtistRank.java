package org.springmeetup.elasticsearch.c04_personalization.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserArtistRank {

	@JsonProperty("artist_id")
	private String artistId;

	@JsonProperty("artist_name")
	private String artistName;

	@JsonProperty("rank")
	private Long rank;

}
