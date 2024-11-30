package com.onboarding.user;

import co.paralleluniverse.fibers.Suspendable;
import com.onboarding.user.contracts.ApplicantDetailsContract;
import com.onboarding.user.contracts.CompanyDirectorDetailsContract;
import com.onboarding.user.contracts.CompanyProfileContract;
import com.onboarding.user.states.ApplicantDetailsState;
import com.onboarding.user.states.CompanyDirectorDetailsState;
import com.onboarding.user.states.CompanyProfileState;
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

public class ApplicantInformationCollectionFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {
        //Company Profile
        private final String companyName;
        private final String companyCIN;
        private final String companyGSTIN;
        private final String companyPAN;
        private final String companyPhone;
        private final String companyEmail;
        private final String companyAddress;
        private final String companyMSME;

        //Applicant Details
        private final String applicantFirstName;
        private final String applicantLastName;
        private final String applicantEmail;
        private final String applicantPhone;
        private final String applicantDesignation;
        private final String applicantAadhaar;

        //Company Director Details
        private final String directorFirstName;
        private final String directorLastName;
        private final String directorEmail;
        private final String directorPhone;
        private final String directorDesignation;
        private final String directorAadhaar;
        private final String directorPAN;
        private final String directorTotalLoanCount;
        private final String directorTotalODCount;
        private final String directorCurrentLoanOutstanding;
        private final String directorCurrentLoanEMI;
        private final boolean isDirectorDueMissedLast6Months;
        private final boolean isDirectorDueMissedLast12Months;
        private final boolean isDirectorDueMissedLast18Months;

        //Party
        private final Party otherParty;


        private final ProgressTracker.Step CHECK_PREVIOUS_TRANSACTION = new ProgressTracker.Step("Check For Previous Transaction");
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
                CHECK_PREVIOUS_TRANSACTION,
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

            this.companyName = (String) jsonData.get("companyName");
            this.companyCIN = (String) jsonData.get("companyCIN");
            this.companyGSTIN = (String) jsonData.get("companyGSTIN");
            this.companyPAN = (String) jsonData.get("companyPAN");
            this.companyPhone = (String) jsonData.get("companyPhone");
            this.companyEmail = (String) jsonData.get("companyEmail");
            this.companyAddress = (String) jsonData.get("companyAddress");
            this.companyMSME = (String) jsonData.get("companyMSME");
            this.applicantFirstName = (String) jsonData.get("applicantFirstName");
            this.applicantLastName = (String) jsonData.get("applicantLastName");
            this.applicantEmail = (String) jsonData.get("applicantEmail");
            this.applicantPhone = (String) jsonData.get("applicantPhone");
            this.applicantDesignation = (String) jsonData.get("applicantDesignation");
            this.applicantAadhaar = (String) jsonData.get("applicantAadhaar");
            this.directorFirstName = (String) jsonData.get("directorFirstName");
            this.directorLastName = (String) jsonData.get("directorLastName");
            this.directorEmail = (String) jsonData.get("directorEmail");
            this.directorPhone = (String) jsonData.get("directorPhone");
            this.directorDesignation = (String) jsonData.get("directorDesignation");
            this.directorAadhaar = (String) jsonData.get("directorAadhaar");
            this.directorPAN = (String) jsonData.get("directorPAN");
            this.directorTotalLoanCount = (String) jsonData.get("directorTotalLoanCount");
            this.directorTotalODCount = (String) jsonData.get("directorTotalODCount");
            this.directorCurrentLoanOutstanding = (String) jsonData.get("directorCurrentLoanOutstanding");
            this.directorCurrentLoanEMI = (String) jsonData.get("directorCurrentLoanEMI");

            // Safe parsing for boolean values
            this.isDirectorDueMissedLast6Months = (Boolean) jsonData.get("isDirectorDueMissedLast6Months");
            this.isDirectorDueMissedLast12Months = (Boolean) jsonData.get("isDirectorDueMissedLast12Months");
            this.isDirectorDueMissedLast18Months = (Boolean) jsonData.get("isDirectorDueMissedLast18Months");
        }


        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }


        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            //Get The Notary
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            // Stage 1.
            progressTracker.setCurrentStep(CHECK_PREVIOUS_TRANSACTION);
            // Check With Previous States


            // Stage 2.
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            // Generate an unsigned transaction.
            Party me = getOurIdentity();
            UniqueIdentifier ApplicantId = new UniqueIdentifier();

            CompanyProfileState companyProfileState = new CompanyProfileState(ApplicantId, companyName, companyCIN, companyGSTIN, companyPAN, companyPhone, companyEmail, companyAddress, companyMSME, me, otherParty);

            ApplicantDetailsState applicantDetailsState = new ApplicantDetailsState(ApplicantId, applicantFirstName, applicantLastName, applicantEmail, applicantPhone, applicantDesignation, applicantAadhaar, me ,otherParty);

            CompanyDirectorDetailsState companyDirectorDetailsState = new CompanyDirectorDetailsState(ApplicantId,directorFirstName, directorLastName, directorEmail, directorPhone, directorDesignation, directorAadhaar, directorPAN, directorTotalLoanCount, directorTotalODCount, directorCurrentLoanOutstanding, directorCurrentLoanEMI, isDirectorDueMissedLast6Months, isDirectorDueMissedLast12Months, isDirectorDueMissedLast18Months, me ,otherParty);

            final Command<CompanyProfileContract.Create> txCommand01 = new Command<>(new CompanyProfileContract.Create(), Arrays.asList(companyProfileState.getCreateParty().getOwningKey(), companyProfileState.getUpdateParty().getOwningKey()));

            final Command<ApplicantDetailsContract.Create> txCommand02 = new Command<>(new ApplicantDetailsContract.Create(), Arrays.asList(applicantDetailsState.getCreateParty().getOwningKey(), applicantDetailsState.getUpdateParty().getOwningKey()));

            final Command<CompanyDirectorDetailsContract.Create> txCommand03 = new Command<>(new CompanyDirectorDetailsContract.Create(), Arrays.asList(applicantDetailsState.getCreateParty().getOwningKey(), applicantDetailsState.getUpdateParty().getOwningKey()));

            final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(companyProfileState, CompanyProfileContract.ID)
                    .addOutputState(applicantDetailsState, ApplicantDetailsContract.ID)
                    .addOutputState(companyDirectorDetailsState, CompanyDirectorDetailsContract.ID)
                    .addCommand(txCommand01)
                    .addCommand(txCommand02)
                    .addCommand(txCommand03);

            // Stage 3.
            progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
            // Verify that the transaction is valid.
            txBuilder.verify(getServiceHub());

            // Stage 4.
            progressTracker.setCurrentStep(SIGNING_TRANSACTION);
            // Sign the transaction.
            final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

            // Stage 5.
            progressTracker.setCurrentStep(GATHERING_SIGS);
            // Send the state to the counterparty, and receive it back with their signature.
            FlowSession otherPartySession = initiateFlow(otherParty);
            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(partSignedTx, Arrays.asList(otherPartySession), CollectSignaturesFlow.Companion.tracker()));

            // Stage 6.
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
