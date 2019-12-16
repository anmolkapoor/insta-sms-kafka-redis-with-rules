package com.cred.workersStreams;

import com.cred.commons.Constants;
import com.cred.models.EventDto;
import com.cred.models.EventDtoWithPastCustomersEvents;
import com.cred.models.RedisMinimalEventEntry;
import com.cred.models.RedisSavedCustomerEvents;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import java.util.*;

import static com.cred.commons.Constants.EVENTS_DECORATED_EVENTS_TOPIC;
import static com.cred.commons.Constants.REDIS_SAVED_LIST_EVENT_KEY_PREFIX;

@Slf4j
public class SaveAndDecorateEventProcessor implements CustomStreamProcessor {
    private final ProcessorUtil processorUtil;
    private final JedisPool jedisPool;

    @Inject
    public SaveAndDecorateEventProcessor(ProcessorUtil processorUtil, JedisPool jedisPool) {
        this.processorUtil = processorUtil;
        this.jedisPool = jedisPool;
    }

    @Override
    public String getAppId() {
        return "stream-SaveAndDecorateEventProcessor";
    }

    @Override
    public StreamsBuilder addToBuilder(StreamsBuilder builder) {
        builder.<String, String>stream(Constants.EVENTS_UNIQUE_EVENTS_TOPIC)
                .map((KeyValueMapper<String, String, KeyValue<String, Optional<EventDto>>>) (key, value)
                        -> new KeyValue<>(key, processorUtil.convertToEventDto(value)))
                .filter((key, value) -> value.isPresent())
                .map(new KeyValueMapper<String, Optional<EventDto>, KeyValue<String, EventDto>>() {
                    @Override
                    public KeyValue<String, EventDto> apply(String key, Optional<EventDto> value) {
                        return new KeyValue<>(key, value.get());
                    }
                }).map(new KeyValueMapper<String, EventDto, KeyValue<String, EventDtoWithPastCustomersEvents>>() {
            @Override
            public KeyValue<String, EventDtoWithPastCustomersEvents> apply(String key, EventDto eventDto) {
                log.info("Received Key: " + key + " eventId: " + eventDto.getEventId());
                String redisKey = REDIS_SAVED_LIST_EVENT_KEY_PREFIX + eventDto.getUserId();
                // Generate a redis dto
                Optional<RedisMinimalEventEntry> redisMinimalEventEntry = processorUtil.convertEventDtoToRedisSavedEventInfoDto(eventDto);

                // get all others

                RedisSavedCustomerEvents redisSavedCustomerEvents = null;
                try (Jedis jedis = jedisPool.getResource()) {
                    String redisDataInString = jedis.get(redisKey);
                    redisSavedCustomerEvents = processorUtil.convertToRedisSavedCustomerEvents(redisDataInString).orElse(new RedisSavedCustomerEvents());

                }

                if (redisMinimalEventEntry.isPresent()) {
                    redisSavedCustomerEvents.getEntries().add(redisMinimalEventEntry.get());
                }
                // prune
                if (redisSavedCustomerEvents.getEntries().size() > Constants.REDIS_SAVED_LIST_MAX_SIZE) {
                    List<RedisMinimalEventEntry> entries = redisSavedCustomerEvents.getEntries();
                    entries = entries.subList(entries.size() - Constants.REDIS_SAVED_LIST_MAX_SIZE, entries.size());
                    redisSavedCustomerEvents.setEntries(entries);
                }

                // todo: can optimize here to write only the new one if not pruned. putting all thte value again in list


                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.set(redisKey, processorUtil.convertRedisSavedCustomerEventsToString(redisSavedCustomerEvents));
                    // set ttl
                    jedis.expire(redisKey, Constants.REDIS_SAVED_LIST_TTL_IN_SEC);
                }

                EventDtoWithPastCustomersEvents eventDtoWithPastCustomersEvents = new EventDtoWithPastCustomersEvents();
                eventDtoWithPastCustomersEvents.setEventDto(eventDto);
                eventDtoWithPastCustomersEvents.setRedisSavedCustomerEvents(redisSavedCustomerEvents);

                return new KeyValue<>(key, eventDtoWithPastCustomersEvents);
            }
        }).map(new KeyValueMapper<String, EventDtoWithPastCustomersEvents, KeyValue<String, String>>() {
            @Override
            public KeyValue<String, String> apply(String key, EventDtoWithPastCustomersEvents value) {
                return new KeyValue<>(key, processorUtil.convertEventDtoWithPastCustomersEventsToString(value));
            }
        })
                .to(EVENTS_DECORATED_EVENTS_TOPIC);


        return builder;
    }
}
