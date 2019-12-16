package com.cred.workersStreams;

import com.cred.commons.CommonModule;
import com.cred.rules.RulesModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import redis.clients.jedis.JedisPool;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class CustomStreamRunner {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("p", true, "run stream processor");
        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption("p")) {
            String p = cmd.getOptionValue("p");
            runProcessor(p);
        } else {
            System.out.println("usage : -p <class name>");
        }


    }

    private static void runProcessor(String p) throws ClassNotFoundException {
        Injector injector = Guice.createInjector(new CommonModule(),new RulesModule());
        CustomStreamProcessor customStreamProcessor = (CustomStreamProcessor) injector.getInstance(Class.forName(p));
        Properties props = new Properties();


        props.put(StreamsConfig.APPLICATION_ID_CONFIG, customStreamProcessor.getAppId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        final StreamsBuilder builder = new StreamsBuilder();
        StreamsBuilder streamsBuilder = customStreamProcessor.addToBuilder(builder);
        final Topology topology = streamsBuilder.build();
        System.out.println(topology.describe());
        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);

    }
}
