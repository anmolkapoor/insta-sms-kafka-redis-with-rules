package com.cred.producer;

import com.cred.commons.CommonModule;
import com.cred.models.EventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.StreamsConfig;
import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static com.cred.commons.Constants.EVENTS_PRODUCER_TOPIC;

@Slf4j
public class EventSource {


    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new CommonModule());
        ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer
                <>(props);


        Runtime.getRuntime().addShutdownHook(new Thread("event-source-shutdown-hook") {
            @Override
            public void run() {
                producer.close();

            }
        });
        Random random = new Random();
        long counter = 0;
        try {

            while (true) {
                File file = new File(EventSource.class.getClassLoader().getResource("EventDto.json").getFile());
                EventDto eventDto = objectMapper.readValue(file, EventDto.class);
                // Randomizing sometime to add duplicate evnts
                int noise = random.nextInt(2);
                int amount = random.nextInt(1000);
                int merch = random.nextInt(1000);


                int customer = random.nextInt(100000);
                List<String> currencyArr  = Lists.newArrayList("INR","INR","INR","INR","INR","INR","INR","INR","SGD","SGD");

                eventDto.setEventId(eventDto.getEventId() + "-" + (counter+noise));
                eventDto.getTransformedPayload().setTxnAmount(amount);
                eventDto.setUserId("UID-"+customer);
                eventDto.getTransformedPayload().setTxnTime(DateTime.now());
                eventDto.getTransformedPayload().setMerchant("M-"+merch);
                eventDto.getTransformedPayload().setTxnCurrency(currencyArr.get(random.nextInt(currencyArr.size())));
                counter++;



                String eventDtoStr = objectMapper.writeValueAsString(eventDto);

                log.info(EVENTS_PRODUCER_TOPIC + " Publishing : " + eventDtoStr);
                producer.send(new ProducerRecord<>(EVENTS_PRODUCER_TOPIC, eventDtoStr));
                Thread.sleep(1000);
            }
        } catch (final Exception e) {
            log.error(e.getLocalizedMessage(), e);
            System.exit(1);
        }
        System.exit(0);
    }


}

