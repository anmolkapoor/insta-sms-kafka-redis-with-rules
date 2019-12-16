#### Techstack used:
1. `Java 8 maven  3.6.1`
2. `Kafka streams` for event processing
3. `Redis` for storage : idempotency check and saving customer context

    
#### Event flow :
1. Messages pushed to topic  `instasms-producer-topic-1` by `EventSource` .
2. Messages consumed from topic `instasms-producer-topic-1` by `IdempotentFilter` . 
    Duplicates are removed using Redis keys with TTL . 
    Unique messages are published to `instasms-unique-events-topic-1`
3. Messages consumed from topic `instasms-unique-events-topic-1` by `SaveAndDecorateEventProcessor`.
 Messages are decorated with previous events saved in redis. Redis data pruning happens here.
 Decorated messages `EventDtoWithPastCustomersEvents` are published to topic `instasms-decorated-events-topic-1`.
4. Messages consumed from topic `instasms-decorated-events-topic-1` and rules are run by `RulesProcessor`
stateless rules extends interface `StatelessRule` while statefull extend `SatefullRule`
Rules are injected using MapBinder from `RuleModule`. 
5. If successful events with Executed Rule and Reason is published to `instasms-events-for-sms-alerts-1`
 
#### Prerequisites:
* `kafka` working on `localhost:9092`
* `redis` working on `localhost:6379`

#### Events details
* `EventDto` : DTO recieved, 
* `EventDtoWithPastCustomersEvents`: Decorated Dto with past events, 
* `FinalSMSToBeSentEventsDto` : Final Event Generated with Triggred Rules + Reason
* `RedisSavedCustomerEvents` : List of `RedisMinimalEventEntry` stored in redis 


#### Steps
1. mvn clean compile package
`mvn clean package`
2. start event source :
`  Using ide run class: com.cred.producer.EventSource` or
` mvn exec:java -Dexec.mainClass=com.cred.producer.EventSource`
2. start IdempotentFilter
   Using ide run class : `com.cred.workersStreams.CustomStreamRunner -p com.cred.workersStreams.IdempotentFilterProcessor` or
   ` mvn exec:java -Dexec.mainClass=com.cred.workersStreams.CustomStreamRunner -Dexec.args="-p com.cred.workersStreams.IdempotentFilterProcessor"
`
3. Start Event Decorator
    Using ide run class : `com.cred.workersStreams.CustomStreamRunner -p com.cred.workersStreams.SaveAndDecorateEventProcessor`
  or ` mvn exec:java -Dexec.mainClass=com.cred.workersStreams.CustomStreamRunner -Dexec.args="-p com.cred.workersStreams.SaveAndDecorateEventProcessor"
`
4. Start Rule Processor
    Using ide run class : `com.cred.workersStreams.CustomStreamRunner -p com.cred.workersStreams.RulesProcessor`
    or ` mvn exec:java -Dexec.mainClass=com.cred.workersStreams.CustomStreamRunner -Dexec.args="-p com.cred.workersStreams.RulesProcessor"
`
5. Listen events to send SMS on to customers:
    `on command line run : `
    `kafka-console-consumer --bootstrap-server localhost:9092 --topic instasms-events-for-sms-alerts-1`
    
#### Configuration files:
1. Yaml file `app_config.yaml` -> Configs for rules
2. Constants.java -> Constants
3. EventDto.json -> Sample Json to be used as base.


#### Directory Structure

| Location | details |
|---|---|
|com/cred/commons   |  App commons such as Builders, Constant, Configuration  | 
|com/cred/models  |  App DTOs   |
|com/cred/producer | EventSource to generate events |
| com/cred/rules/statefull | Stateful Rules: `DuplicateTransaction`,`HighSpendOnSingleMerchant`  |
| com/cred/rules/stateless | Stateless Rules: `ForeignCurrencyTransaction` ,`HighValueTransaction` |
| com/cred/rules/workerStreams | `KafkaStream` topologies to consume events and transform them. |
| resources | Configuration for rules and application |

