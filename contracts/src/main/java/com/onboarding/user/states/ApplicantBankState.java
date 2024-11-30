package com.onboarding.user.states;

import com.onboarding.user.contracts.ApplicantBankVerifyContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(ApplicantBankVerifyContract.class)
public class ApplicantBankState implements LinearState {
    private final UniqueIdentifier linearId;

    private final String applicantFullName;
    private final String applicantBankAccountNumber;
    private final String applicantBankName;
    private final String applicantBankIFSCCode;
    private final String applicantBankBranchName;
    private final Boolean applicantBankVerified;

    private final Party createParty;
    private final Party updateParty;

    public ApplicantBankState(UniqueIdentifier linearId, String applicantFullName, String applicantBankAccountNumber, String applicantBankName, String applicantBankIFSCCode, String applicantBankBranchName, Boolean applicantBankVerified, Party createParty, Party updateParty) {
        this.linearId = linearId;
        this.applicantFullName = applicantFullName;
        this.applicantBankAccountNumber = applicantBankAccountNumber;
        this.applicantBankName = applicantBankName;
        this.applicantBankIFSCCode = applicantBankIFSCCode;
        this.applicantBankBranchName = applicantBankBranchName;
        this.applicantBankVerified = applicantBankVerified;
        this.createParty = createParty;
        this.updateParty = updateParty;
    }

    public String getApplicantFullName() {
        return applicantFullName;
    }

    public String getApplicantBankAccountNumber() {
        return applicantBankAccountNumber;
    }

    public String getApplicantBankName() {
        return applicantBankName;
    }

    public String getApplicantBankIFSCCode() {
        return applicantBankIFSCCode;
    }

    public String getApplicantBankBranchName() {
        return applicantBankBranchName;
    }

    public Boolean getApplicantBankVerified() {
        return applicantBankVerified;
    }

    public Party getCreateParty() {
        return createParty;
    }

    public Party getUpdateParty() {
        return updateParty;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(createParty, updateParty);
    }

}
