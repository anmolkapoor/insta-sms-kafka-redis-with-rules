package com.cred.rules;

import com.cred.models.EventDto;
import com.cred.models.RedisMinimalEventEntry;
import javafx.util.Pair;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface StatefullRule {
    public String getRuleName();
    public Pair<Boolean, Optional<String>> applyRuleAndReturnReasonIfTrue(EventDto eventDto, List<RedisMinimalEventEntry> redisMinimalEventInfoDtosList);
}
