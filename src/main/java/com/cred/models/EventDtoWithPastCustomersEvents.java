package com.cred.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown =  true)
public class EventDtoWithPastCustomersEvents {
    private EventDto eventDto;
    private RedisSavedCustomerEvents redisSavedCustomerEvents;
}
