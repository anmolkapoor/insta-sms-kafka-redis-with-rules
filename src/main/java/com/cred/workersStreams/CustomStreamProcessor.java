package com.cred.workersStreams;

import org.apache.kafka.streams.StreamsBuilder;

public interface CustomStreamProcessor {
    public String getAppId();
    public StreamsBuilder addToBuilder(StreamsBuilder builder);

}
