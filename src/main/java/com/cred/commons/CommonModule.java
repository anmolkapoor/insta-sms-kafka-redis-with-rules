package com.cred.commons;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;

public class CommonModule extends AbstractModule {
    @Override
    protected void configure() {
        super.configure();

    }

    @Provides
    @Singleton
    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());

        return objectMapper;
    }

    @Provides
    @Singleton
    public JedisPool getJedisPool() {
        JedisPoolBuilder jedisPoolBuilder = new JedisPoolBuilder();
        return jedisPoolBuilder.getJedisPool();
    }

    @Provides
    @Singleton
    public AppConfiguration getAppConfiguration() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        AppConfiguration appConfiguration = null;
        try {
            File file = new File(getClass().getClassLoader().getResource("app_config.yaml").getFile());
            appConfiguration = mapper.readValue(file, AppConfiguration.class);
        } catch (IOException e) {
            appConfiguration = new AppConfiguration();
        }
        return appConfiguration;
    }


}
