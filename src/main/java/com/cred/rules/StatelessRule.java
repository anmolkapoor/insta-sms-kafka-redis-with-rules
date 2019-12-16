package com.cred.rules;

import com.cred.models.EventDto;
import javafx.util.Pair;

import java.util.Optional;

public interface  StatelessRule {

    public String getRuleName();
    public Pair<Boolean, Optional<String>> applyRuleAndReturnReasonIfTrue(EventDto eventDto);
}
