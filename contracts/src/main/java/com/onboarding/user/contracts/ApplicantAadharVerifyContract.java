package com.onboarding.user.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class ApplicantAadharVerifyContract implements Contract {

    public static final String ID = "com.onboarding.user.contracts.ApplicantAadharVerifyContract";

    @Override
    public void verify(LedgerTransaction tx) {

        CommandWithParties<Verify> command = requireSingleCommand(tx.getCommands(), ApplicantAadharVerifyContract.Verify.class);

        requireThat(require -> {
            // Ensure no input states are consumed
            require.using("Only Two input State should be consumed", tx.getInputs().isEmpty());

            // Ensure there are exactly one output state
            require.using("Only Two Output State Should be there", tx.getOutputs().size() == 2);

            // Add more validation rules here

            return null;
        });

    }

    public static class Verify implements CommandData {}
}
