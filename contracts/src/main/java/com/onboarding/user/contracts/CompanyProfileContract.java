package com.onboarding.user.contracts;

import com.onboarding.user.states.CompanyProfileState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class CompanyProfileContract implements Contract {

    public static final String ID = "com.onboarding.user.contracts.CompanyProfileContract";


    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {

        CommandWithParties<CompanyProfileContract.Create> command = requireSingleCommand(tx.getCommands(), CompanyProfileContract.Create.class);

        requireThat(require -> {
            // Ensure no input states are consumed
            require.using("No input State should be consumed", tx.getInputs().isEmpty());

            // Ensure there are exactly three output states
            require.using("Only Three Output State Should be there", tx.getOutputs().size() == 3);

            // Extract the CompanyProfileState from the transaction output
            final CompanyProfileState companyProfile = tx.outputsOfType(CompanyProfileState.class).get(0);

            // Ensure the createParty and updateParty are not the same
            require.using("Both createParty and updateParty should not be the same",
                    !companyProfile.getCreateParty().equals(companyProfile.getUpdateParty()));

            // Validate the company name
            require.using("Company Name cannot be empty.",
                    companyProfile.getCompanyName() != null && !companyProfile.getCompanyName().isEmpty());

            return null;
        });


        // Add more validations here
    }

    public static class Create implements CommandData {}
}
