package org.springmeetup.elasticsearch.c04_personalization.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springmeetup.elasticsearch.c04_personalization.model.UserArtistRank;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileDocument {

	@JsonProperty("user_id")
	private String userid;

	private List<UserArtistRank> artist;

}
