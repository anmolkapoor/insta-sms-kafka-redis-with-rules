package com.cred.rules.stateless;

import com.cred.commons.AppConfiguration;
import com.cred.models.EventDto;
import com.cred.rules.StatelessRule;
import javafx.util.Pair;

import javax.inject.Inject;
import java.util.Optional;

import static com.cred.commons.Constants.RULE_HIGH_VALUE_TRANSACTION_REASON_VALUE_GREATER_THAN_LIMIT;

public class HighValueTransaction implements StatelessRule {
    private final AppConfiguration appConfiguration;

    @Inject
    public HighValueTransaction(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    @Override
    public String getRuleName() {
        return HighValueTransaction.class.getSimpleName();
    }

    @Override
    public Pair<Boolean, Optional<String>> applyRuleAndReturnReasonIfTrue(EventDto eventDto) {
        Integer txnAmount = Optional.ofNullable(eventDto)
                .flatMap(event -> Optional.ofNullable(event.getTransformedPayload()))
                .flatMap(trasformedEvent -> Optional.ofNullable(trasformedEvent.getTxnAmount()))
                .orElse(-1);
        Pair<Boolean, Optional<String>> falseResponse = new Pair<>(false, Optional.empty());
        if (txnAmount < appConfiguration.getHighValueTransactionLimit()) {
            return falseResponse;
        } else {
            return new Pair<>(true, Optional.of(RULE_HIGH_VALUE_TRANSACTION_REASON_VALUE_GREATER_THAN_LIMIT));
        }


    }
}
