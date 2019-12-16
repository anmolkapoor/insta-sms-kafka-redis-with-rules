package com.cred.rules.stateless;

import com.cred.commons.AppConfiguration;
import com.cred.models.EventDto;
import com.cred.rules.StatelessRule;
import javafx.util.Pair;

import javax.inject.Inject;
import java.util.Optional;

import static com.cred.commons.Constants.RULE_FOREIGN_CURRENCY_TXN_REASON_VALUE_GREATER_THAN_LIMIT;
public class ForeignCurrencyTransaction implements StatelessRule {
    private final AppConfiguration appConfiguration;

    @Inject
    public ForeignCurrencyTransaction(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    @Override
    public String getRuleName() {
        return ForeignCurrencyTransaction.class.getSimpleName();
    }

    @Override
    public Pair<Boolean, Optional<String>> applyRuleAndReturnReasonIfTrue(EventDto eventDto) {
        Integer amount = Optional.ofNullable(eventDto.getTransformedPayload())
                .flatMap(x -> Optional.ofNullable(x.getTxnAmount())).orElse(-1);
        String currency = Optional.ofNullable(eventDto.getTransformedPayload())
                .flatMap(x -> Optional.ofNullable(x.getTxnCurrency())).orElse("INR");
        if(!currency.equalsIgnoreCase(appConfiguration.getCustomersOriginalCurrency())
            && amount>appConfiguration.getForeignCurrencyHighValueAmount()){
            return new Pair<>(true, Optional.of(RULE_FOREIGN_CURRENCY_TXN_REASON_VALUE_GREATER_THAN_LIMIT));
        }else{
            return new Pair<>(false, Optional.empty());
        }
    }
}
