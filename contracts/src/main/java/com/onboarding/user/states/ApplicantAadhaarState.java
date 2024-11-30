package com.onboarding.user.states;

import com.onboarding.user.contracts.ApplicantAadharVerifyContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(ApplicantAadharVerifyContract.class)
public class ApplicantAadhaarState implements LinearState {

    private final UniqueIdentifier linearId;
    private final String applicantAadharName;
    private final Integer applicantAadharNumber;
    private final Integer applicantAadharVerificationId;
    private final Boolean applicantAadharIsVerified;

    private final Party createParty;
    private final Party updateParty;

    public ApplicantAadhaarState(UniqueIdentifier linearId, String applicantAadharName, Integer applicantAadharNumber, Integer applicantAadharVerificationId, Boolean applicantAadharIsVerified, Party createParty, Party updateParty) {
        this.linearId = linearId;
        this.applicantAadharName = applicantAadharName;
        this.applicantAadharNumber = applicantAadharNumber;
        this.applicantAadharVerificationId = applicantAadharVerificationId;
        this.applicantAadharIsVerified = applicantAadharIsVerified;
        this.createParty = createParty;
        this.updateParty = updateParty;
    }

    public String getApplicantAadharName() {
        return applicantAadharName;
    }

    public Integer getApplicantAadharNumber() {
        return applicantAadharNumber;
    }

    public Integer getApplicantAadharVerificationId() {
        return applicantAadharVerificationId;
    }

    public Boolean getApplicantAadharIsVerified() {
        return applicantAadharIsVerified;
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
        return Arrays.asList(createParty,updateParty);
    }

}
