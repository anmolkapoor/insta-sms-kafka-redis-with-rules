package com.cred.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.joda.time.DateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventTransformedPayload {
    @JsonProperty("merchant")
    private String merchant;
    @JsonProperty("txn_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private DateTime txnTime;
    @JsonProperty("txn_amount")
    private Integer txnAmount;
    @JsonProperty("available_limit")
    private Double availableLimit;
    @JsonProperty("current_outstanding")
    private Double currentOutstanding;
    @JsonProperty("txn_currency")
    private String txnCurrency;
}
