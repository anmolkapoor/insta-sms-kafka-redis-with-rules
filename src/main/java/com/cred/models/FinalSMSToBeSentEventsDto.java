package com.cred.models;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class FinalSMSToBeSentEventsDto {
    private EventDto eventDto;
    private Map<String,String> ruleTriggeredAndReason = new HashMap<>();
}
