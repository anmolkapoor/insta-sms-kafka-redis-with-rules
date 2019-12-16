package com.cred.commons;

public class Constants {

    public static final String REDIS_EVENT_IDEMPOTENT_KEY_PREFIX = "instasms-idcheck-";
    public static final int REDIS_EVENT_IDEMPOTENT_KEY_TTL_IN_SEC = 100;
    public static final int REDIS_SAVED_LIST_TTL_IN_SEC = 100;
    public static final int REDIS_SAVED_LIST_MAX_SIZE = 20;
    public static final String REDIS_SAVED_LIST_EVENT_KEY_PREFIX = "instasms-saved-list-";



    public static final String EVENTS_PRODUCER_TOPIC = "instasms-producer-topic-1";
    public static final String EVENTS_UNIQUE_EVENTS_TOPIC = "instasms-unique-events-topic-1";
    public static final String EVENTS_DECORATED_EVENTS_TOPIC = "instasms-decorated-events-topic-1";
    public static final String EVENTS_FOR_SMS_ALERTS = "instasms-events-for-sms-alerts-1";
    public static final String RULE_HIGH_VALUE_TRANSACTION_REASON_VALUE_GREATER_THAN_LIMIT = "HIGH_VALUE_TRANSACTION_REASON_VALUE_GREATER_THAN_LIMIT";
    public static final String RULE_FOREIGN_CURRENCY_TXN_REASON_VALUE_GREATER_THAN_LIMIT = "FOREIGN_CURRENCY_TXN_REASON_VALUE_GREATER_THAN_LIMIT";
    public static final String RULE_DUPLICATE_TXN_REASON_DUPLICATE_TXN_OF = "DUPLICATE_TXN_OF-";
    public static final String RULE_HIGH_SPEND_ON_MERCH_MERCH_ID = "RULE_HIGH_SPEND_ON_MERCH_ID-";

}
