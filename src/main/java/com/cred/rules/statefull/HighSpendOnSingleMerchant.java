package com.cred.rules.statefull;

import com.cred.commons.AppConfiguration;
import com.cred.models.EventDto;
import com.cred.models.RedisMinimalEventEntry;
import com.cred.rules.StatefullRule;

import javafx.util.Pair;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.cred.commons.Constants.RULE_HIGH_SPEND_ON_MERCH_MERCH_ID;

public class HighSpendOnSingleMerchant implements StatefullRule {
    private final AppConfiguration appConfiguration;

    @Inject
    public HighSpendOnSingleMerchant(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    @Override
    public String getRuleName() {
        return HighSpendOnSingleMerchant.class.getSimpleName();
    }

    @Override
    public Pair<Boolean, Optional<String>> applyRuleAndReturnReasonIfTrue(EventDto eventDto, List<RedisMinimalEventEntry> redisMinimalEventInfoDtosList) {
        Map<String, Integer> spendsOnMerch = new HashMap<>();
        for (RedisMinimalEventEntry entry : redisMinimalEventInfoDtosList) {
            Duration duration = new Duration(entry.getTxnTime(), DateTime.now());
            if (duration.getStandardMinutes() < appConfiguration.getHighSpendOnSingleMerchantTimeDurationInHours()) {
                spendsOnMerch.put(entry.getMerchant(), spendsOnMerch.getOrDefault(entry.getMerchant(), 0) + entry.getTxnAmount());
            }
        }
        for (Map.Entry<String, Integer> entry : spendsOnMerch.entrySet()) {
            if (entry.getValue() > appConfiguration.getHighSpendOnSingleMerchantTotalAmount()) {
                return new Pair<>(true, Optional.of(RULE_HIGH_SPEND_ON_MERCH_MERCH_ID + entry.getKey()));
            }
        }

        return new Pair<>(false, Optional.empty());

    }
}
