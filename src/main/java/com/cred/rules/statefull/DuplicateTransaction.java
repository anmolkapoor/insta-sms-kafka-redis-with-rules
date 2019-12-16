package com.cred.rules.statefull;

import com.cred.commons.AppConfiguration;
import com.cred.models.EventDto;
import com.cred.models.RedisMinimalEventEntry;
import com.cred.rules.StatefullRule;

import javafx.util.Pair;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static com.cred.commons.Constants.RULE_DUPLICATE_TXN_REASON_DUPLICATE_TXN_OF;

public class DuplicateTransaction implements StatefullRule {
    private final AppConfiguration appConfiguration;

    @Inject
    public DuplicateTransaction(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    @Override
    public String getRuleName() {
        return DuplicateTransaction.class.getSimpleName();
    }

    @Override
    public Pair<Boolean, Optional<String>> applyRuleAndReturnReasonIfTrue(EventDto eventDto, List<RedisMinimalEventEntry> redisMinimalEventInfoDtosList) {
        for (RedisMinimalEventEntry otherEvent : redisMinimalEventInfoDtosList) {
            Optional<String> merchant = Optional.ofNullable(eventDto.getTransformedPayload())
                    .flatMap(x -> Optional.ofNullable(x.getMerchant()));

            Optional<DateTime> txnTime = Optional.ofNullable(eventDto.getTransformedPayload())
                    .flatMap(x -> Optional.ofNullable(x.getTxnTime()));


            if (otherEvent.getEventId() == null  || otherEvent.getEventId().equals(eventDto.getEventId())) {
                continue;
            }

            if (merchant.isPresent() && txnTime.isPresent() &&
                    merchant.get().equals(otherEvent.getMerchant())
                    && otherEvent.getTxnTime() != null) {
                Duration duration = new Duration(otherEvent.getTxnTime(), txnTime.get());
                if (duration.getStandardMinutes() < appConfiguration.getDuplicateTransactionTimeDurationInMinutes()) {
                    // we have found this as equal
                    return new Pair<>(true, Optional.of(RULE_DUPLICATE_TXN_REASON_DUPLICATE_TXN_OF + otherEvent.getEventId()));
                }

            }

        }
        return new Pair<>(false, Optional.empty());

    }
}
