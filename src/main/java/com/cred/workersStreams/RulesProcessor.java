package com.cred.workersStreams;

import com.cred.models.*;
import com.cred.rules.StatefullRule;
import com.cred.rules.StatelessRule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

import static com.cred.commons.Constants.*;

@Slf4j
public class RulesProcessor implements CustomStreamProcessor {
    private final Set<StatelessRule> statelessRules;
    private final Set<StatefullRule> statefullRules;
    private final ObjectMapper objectMapper;
    private final JedisPool jedisPool;
    private final ProcessorUtil processorUtil;

    @Inject
    public RulesProcessor(Set<StatelessRule> statelessRules, Set<StatefullRule> statefullRules, ObjectMapper objectMapper, JedisPool jedisPool, ProcessorUtil processorUtil) {

        this.statelessRules = statelessRules;
        this.statefullRules = statefullRules;
        this.objectMapper = objectMapper;
        this.jedisPool = jedisPool;
        this.processorUtil = processorUtil;
    }

    @Override
    public String getAppId() {
        return "stream-RulesProcessor";
    }


    private Pair<Boolean, Optional<String>> checkAndReturnReasons(String key, EventDtoWithPastCustomersEvents eventDtoWithPastCustomersEvents) {
        EventDto eventDto = eventDtoWithPastCustomersEvents.getEventDto();
        RedisSavedCustomerEvents redisSavedCustomerEvents = eventDtoWithPastCustomersEvents.getRedisSavedCustomerEvents();

        log.info("key: " + key + " event: " + eventDto.getEventId());
        Map<String, String> ruleAndReasonMap = new HashMap<>();


        for (StatelessRule rule : statelessRules) {
            // apply stateless rule

            Pair<Boolean, Optional<String>> response = rule.applyRuleAndReturnReasonIfTrue(eventDto);
            if (response.getKey()) {
                ruleAndReasonMap.put(rule.getRuleName(), response.getValue().orElse(""));
            }
        }

        for (StatefullRule rule : statefullRules) {
            // apply stateless rule
            Pair<Boolean, Optional<String>> response = rule.applyRuleAndReturnReasonIfTrue(eventDto, redisSavedCustomerEvents.getEntries());
            if (response.getKey()) {
                ruleAndReasonMap.put(rule.getRuleName(), response.getValue().orElse(""));
            }

        }
        if (ruleAndReasonMap.isEmpty()) {

            return new Pair<>(false, Optional.empty());
        } else {
            FinalSMSToBeSentEventsDto finalSMSToBeSentEventsDto = new FinalSMSToBeSentEventsDto();
            finalSMSToBeSentEventsDto.setEventDto(eventDto);
            finalSMSToBeSentEventsDto.setRuleTriggeredAndReason(ruleAndReasonMap);
            String finalSMSToBeSentEventsDtoStr = null;
            try {
                finalSMSToBeSentEventsDtoStr = objectMapper.writeValueAsString(finalSMSToBeSentEventsDto);
            } catch (JsonProcessingException e) {
                return new Pair<>(true, Optional.empty());
            }
            if (ruleAndReasonMap.isEmpty()) {
                log.info("EventId : " + eventDto.getEventId() + " No Rules triggered");
            } else {
                log.info("EventId : " + eventDto.getEventId() + " rules : " + ruleAndReasonMap.toString());
            }
            return new Pair<>(true, Optional.of(finalSMSToBeSentEventsDtoStr));
        }


    }


    @Override
    public StreamsBuilder addToBuilder(StreamsBuilder builder) {
        builder.<String, String>stream(EVENTS_DECORATED_EVENTS_TOPIC)
                .map(new KeyValueMapper<String, String, KeyValue<String, Optional<EventDtoWithPastCustomersEvents>>>() {
                    @Override
                    public KeyValue<String, Optional<EventDtoWithPastCustomersEvents>> apply(String key, String value) {
                        return new KeyValue<>(key, processorUtil.convertToEventDtoWithPastCustomersEvents(value));
                    }
                }).filter((key, value) -> value.isPresent())
                .map(new KeyValueMapper<String, Optional<EventDtoWithPastCustomersEvents>, KeyValue<String, EventDtoWithPastCustomersEvents>>() {
                    @Override
                    public KeyValue<String, EventDtoWithPastCustomersEvents> apply(String key, Optional<EventDtoWithPastCustomersEvents> value) {
                        return new KeyValue<>(key, value.get());
                    }
                })
                .map(new KeyValueMapper<String, EventDtoWithPastCustomersEvents, KeyValue<String, Pair<Boolean, Optional<String>>>>() {
                    @Override
                    public KeyValue<String, Pair<Boolean, Optional<String>>> apply(String key, EventDtoWithPastCustomersEvents value) {
                        Pair<Boolean, Optional<String>> booleanOptionalPair = checkAndReturnReasons(key, value);
                        return new KeyValue<>(key, booleanOptionalPair);
                    }


                })
                .filter((key, value) -> value.getKey())
                .map((KeyValueMapper<String, Pair<Boolean, Optional<String>>, KeyValue<String, String>>) (key, value)
                        -> new KeyValue<>(key, value.getValue().get()))
                .to(EVENTS_FOR_SMS_ALERTS);

        return builder;
    }
}
