package com.cred.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EventDto {
    @JsonProperty("event_id")
    private String eventId;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("raw_message")
    private String rawMessage;
    @JsonProperty("instrument_id")
    private String instrumentId;
    @JsonProperty("transformed_payload")
    private EventTransformedPayload transformedPayload;
}
