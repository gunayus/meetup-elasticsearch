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
public class EventDocument {

	@JsonProperty("user_id")
	private String userId;

	@JsonProperty("event_type")
	private EventType eventType;

	@JsonProperty("artist_id")
	private String artistId;

}
