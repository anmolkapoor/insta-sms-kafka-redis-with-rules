package com.cred.workersStreams;

import com.cred.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

@Slf4j
public class ProcessorUtil {
    private final ObjectMapper objectMapper;
    @Inject
    public ProcessorUtil(ObjectMapper objectMapper1) {
        this.objectMapper = objectMapper1;
    }
    public Optional<EventDto> convertToEventDto(String value){
        if(value == null){ return Optional.empty();}
        EventDto eventDto = null;
        try {
            eventDto = objectMapper.readValue(value, EventDto.class);
        } catch (IOException e) {

        }
        return Optional.ofNullable(eventDto);
    }

    public Optional<EventDtoWithPastCustomersEvents> convertToEventDtoWithPastCustomersEvents(String value){
        if(value == null){ return Optional.empty();}
        EventDtoWithPastCustomersEvents eventDto = null;
        try {
            eventDto = objectMapper.readValue(value, EventDtoWithPastCustomersEvents.class);
        } catch (IOException e) {

        }
        return Optional.ofNullable(eventDto);
    }

    public String convertEventDtoToString(EventDto value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
    public String convertEventDtoWithPastCustomersEventsToString(EventDtoWithPastCustomersEvents value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    public Optional<RedisMinimalEventEntry> convertEventDtoToRedisSavedEventInfoDto(EventDto eventDto) {
        RedisMinimalEventEntry redisMinimalEventEntry = new RedisMinimalEventEntry();
        Optional<EventTransformedPayload> transformedPayload = Optional.ofNullable(eventDto.getTransformedPayload());
        if(transformedPayload.isPresent()) {
            redisMinimalEventEntry.setEventId(eventDto.getEventId());
            redisMinimalEventEntry.setMerchant(eventDto.getTransformedPayload().getMerchant());
            redisMinimalEventEntry.setTxnAmount(eventDto.getTransformedPayload().getTxnAmount());
            redisMinimalEventEntry.setTxnTime(eventDto.getTransformedPayload().getTxnTime());
            return Optional.of(redisMinimalEventEntry);
        }else{
            return Optional.empty();
        }


    }

    public Optional<RedisSavedCustomerEvents> convertToRedisSavedCustomerEvents(String redisData) {
        if(redisData == null){ return Optional.empty();}
        RedisSavedCustomerEvents obj =null;
        try {

            obj = objectMapper.readValue(redisData, RedisSavedCustomerEvents.class);
        } catch (IOException e) {
            log.error("Unable to deserialize data :" + e.getLocalizedMessage(), e);
        }
        return Optional.ofNullable(obj);
    }

    public String convertRedisSavedCustomerEventsToString(RedisSavedCustomerEvents events){
        try {
            return objectMapper.writeValueAsString(events);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
