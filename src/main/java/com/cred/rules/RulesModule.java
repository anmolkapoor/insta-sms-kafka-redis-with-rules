package com.cred.rules;

import com.cred.rules.statefull.DuplicateTransaction;
import com.cred.rules.statefull.HighSpendOnSingleMerchant;
import com.cred.rules.stateless.HighValueTransaction;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class RulesModule extends AbstractModule {
    @Override
    protected void configure() {
        super.configure();
        Multibinder<StatelessRule> statelessRuleMultibinder = Multibinder.newSetBinder(binder(), StatelessRule.class);
        statelessRuleMultibinder.addBinding().to(HighValueTransaction.class);
        Multibinder<StatefullRule> stateFullRuleMultibinder = Multibinder.newSetBinder(binder(), StatefullRule.class);
        stateFullRuleMultibinder.addBinding().to(DuplicateTransaction.class);
        stateFullRuleMultibinder.addBinding().to(HighSpendOnSingleMerchant.class);

    }
}
