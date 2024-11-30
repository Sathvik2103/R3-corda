package com.onboarding.user.contracts;

import com.onboarding.user.states.CompanyDirectorDetailsState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class CompanyDirectorDetailsContract implements Contract {

    public static final String ID = "com.onboarding.user.contracts.CompanyDirectorDetailsContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {

        CommandWithParties<CompanyDirectorDetailsContract.Create> command = requireSingleCommand(tx.getCommands(), CompanyDirectorDetailsContract.Create.class);

        requireThat(require -> {
            // Ensure no input states are consumed
            require.using("No input State should be consumed", tx.getInputs().isEmpty());

            // Ensure there are exactly three output states
            require.using("Only Three Output State Should be there", tx.getOutputs().size() == 3);

            // Extract the CompanyDirectorDetailsState from the transaction output
            final CompanyDirectorDetailsState companyDirectorDetails = tx.outputsOfType(CompanyDirectorDetailsState.class).get(0);

            // Ensure the createParty and updateParty are not the same
            require.using("Both createParty and updateParty should not be the same",
                    !companyDirectorDetails.getCreateParty().equals(companyDirectorDetails.getUpdateParty()));

            // Ensure the director's first name is not empty
            require.using("Director First Name cannot be empty.",
                    companyDirectorDetails.getDirectorFirstName() != null && !companyDirectorDetails.getDirectorFirstName().isEmpty());

            return null;
        });

        // Add more validation rules here

    }

    public static class Create implements CommandData {
    }
}
