package com.cred.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.joda.time.DateTime;

@Data
public class RedisMinimalEventEntry {
    @JsonProperty("event_id")
    private String eventId;
    @JsonProperty("merchant")
    private String merchant;
    @JsonProperty("txn_time")
    private DateTime txnTime;
    @JsonProperty("txn_amount")
    private Integer txnAmount;

}
