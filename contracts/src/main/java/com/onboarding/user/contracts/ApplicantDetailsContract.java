package com.onboarding.user.contracts;

import com.onboarding.user.states.ApplicantDetailsState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class ApplicantDetailsContract implements Contract {

    public static final String ID = "com.onboarding.user.contracts.ApplicantDetailsContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) {

        CommandWithParties<ApplicantDetailsContract.Create> command = requireSingleCommand(tx.getCommands(), ApplicantDetailsContract.Create.class);

        requireThat(require -> {
            // Ensure no input states are consumed
            require.using("No input State should be consumed", tx.getInputs().isEmpty());

            // Ensure there are exactly three output states
            require.using("Only Three Output State Should be there", tx.getOutputs().size() == 3);

            // Extract the ApplicantDetailsState from the transaction output
            final ApplicantDetailsState applicantDetails = tx.outputsOfType(ApplicantDetailsState.class).get(0);

            // Ensure the createParty and updateParty are not the same
            require.using("Both createParty and updateParty should not be the same",
                    !applicantDetails.getCreateParty().equals(applicantDetails.getUpdateParty()));

            // Ensure the applicant's first name is not empty
            require.using("Applicant First Name cannot be empty.",
                    applicantDetails.getApplicantFirstName() != null && !applicantDetails.getApplicantFirstName().isEmpty());

            return null;
        });


        // Add more validation rules here

    }

    public static class Create implements CommandData {
    }
}
