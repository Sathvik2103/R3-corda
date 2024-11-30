package com.onboarding.user;

import co.paralleluniverse.fibers.Suspendable;
import com.onboarding.user.contracts.*;
import com.onboarding.user.states.ApplicantAadhaarState;
import com.onboarding.user.states.ApplicantBankState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Arrays;
import java.util.UUID;

public class KYCVerificationFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {
        private final String ApplicantId;

        private final String applicantFullName;
        private final String applicantBankAccountNumber;
        private final String applicantBankName;
        private final String applicantBankIFSCCode;
        private final String applicantBankBranchName;

        private final Party otherParty;


        private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction");
        private final ProgressTracker.Step VERIFYING_TRANSACTION = new ProgressTracker.Step("Verifying contract constraints.");
        private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
        private final ProgressTracker.Step GATHERING_SIGS = new ProgressTracker.Step("Gathering the counterparty's signature.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return CollectSignaturesFlow.Companion.tracker();
            }
        };
        private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return FinalityFlow.Companion.tracker();
            }
        };

        // The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
        // checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call()
        // function.
        private final ProgressTracker progressTracker = new ProgressTracker(
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
        );

        public Initiator(String jsonString, Party otherParty) throws ParseException {
            this.otherParty = otherParty;

            JSONParser parser = new JSONParser();
            JSONObject jsonData =  (JSONObject)parser.parse(jsonString);

            this.applicantFullName = (String) jsonData.get("applicantFullName");
            this.applicantBankAccountNumber = (String) jsonData.get("applicantBankAccountNumber");
            this.applicantBankName = (String) jsonData.get("applicantBankName");
            this.applicantBankIFSCCode = (String) jsonData.get("applicantBankIFSCCode");
            this.applicantBankBranchName = (String) jsonData.get("applicantBankBranchName");
            this.ApplicantId = (String) jsonData.get("ApplicantId");
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // Initiator flow logic goes here.

            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            progressTracker.setCurrentStep(GENERATING_TRANSACTION);

            /* Aadhaar Verification Steps*/

            int applicantAadharNumber = 1235027890;
            int applicantAadharVerificationId = 1852556820;
            boolean applicantBankVerified = true;
            boolean applicantAadharIsVerified = true;

            // Convert the applicantId (String) to UniqueIdentifier
            UUID applicantUUID = UUID.fromString(ApplicantId); // Convert from String to UUID
            UniqueIdentifier applicantId = new UniqueIdentifier(null,applicantUUID);


            // Generate an unsigned transaction.
            ApplicantBankState applicantBankState = new ApplicantBankState(applicantId, applicantFullName, applicantBankAccountNumber, applicantBankName, applicantBankIFSCCode, applicantBankBranchName, applicantBankVerified, getOurIdentity(), otherParty);

            ApplicantAadhaarState applicantAadhaarState = new ApplicantAadhaarState(applicantId, applicantFullName, applicantAadharNumber, applicantAadharVerificationId, applicantAadharIsVerified, getOurIdentity(), otherParty);

            final Command<ApplicantBankVerifyContract.Verify> bankCommand = new Command<>(new ApplicantBankVerifyContract.Verify(), Arrays.asList(applicantBankState.getCreateParty().getOwningKey(), applicantBankState.getUpdateParty().getOwningKey()));

            final Command<ApplicantAadharVerifyContract.Verify> aadharCommand = new Command<>(new ApplicantAadharVerifyContract.Verify(), Arrays.asList(applicantAadhaarState.getCreateParty().getOwningKey(), applicantAadhaarState.getUpdateParty().getOwningKey()));

            final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(applicantBankState, ApplicantBankVerifyContract.ID)
                    .addOutputState(applicantAadhaarState, ApplicantAadharVerifyContract.ID)
                    .addCommand(bankCommand)
                    .addCommand(aadharCommand);

            // Stage 2.
            progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
            // Verify that the transaction is valid.
            txBuilder.verify(getServiceHub());

            // Stage 3.
            progressTracker.setCurrentStep(SIGNING_TRANSACTION);
            // Sign the transaction.
            final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

            // Stage 4.
            progressTracker.setCurrentStep(GATHERING_SIGS);
            // Send the state to the counterparty, and receive it back with their signature.
            FlowSession otherPartySession = initiateFlow(otherParty);
            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(partSignedTx, Arrays.asList(otherPartySession), CollectSignaturesFlow.Companion.tracker()));

            // Stage 5.
            progressTracker.setCurrentStep(FINALISING_TRANSACTION);
            // Notarise and record the transaction in both parties' vaults.
            return subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(otherPartySession)));

        }
    }

    @InitiatedBy(Initiator.class)
    public static class Acceptor extends FlowLogic<SignedTransaction> {

        private final FlowSession otherPartySession;

        public Acceptor(FlowSession otherPartySession) {
            this.otherPartySession = otherPartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            class SignTxFlow extends SignTransactionFlow {
                private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                    super(otherPartyFlow, progressTracker);
                }

                @Override
                protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {

                }
            }
            final SignTxFlow signTxFlow = new SignTxFlow(otherPartySession, SignTransactionFlow.Companion.tracker());
            final SecureHash txId = subFlow(signTxFlow).getId();

            return subFlow(new ReceiveFinalityFlow(otherPartySession, txId));
        }
    }
}
