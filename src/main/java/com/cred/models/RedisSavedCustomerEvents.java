package com.cred.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown =  true)
public class RedisSavedCustomerEvents {
    List<RedisMinimalEventEntry> entries = new ArrayList<>();
}
