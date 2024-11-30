package com.onboarding.user.states;

import com.onboarding.user.contracts.ApplicantDetailsContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(ApplicantDetailsContract.class)
public class ApplicantDetailsState implements LinearState {

    private final UniqueIdentifier linearId;

    private final String applicantFirstName;
    private final String applicantLastName;
    private final String applicantEmail;
    private final String applicantPhone;
    private final String applicantDesignation;
    private final String applicantAadhaar;

    private final Party createParty;
    private final Party updateParty;

    public ApplicantDetailsState(UniqueIdentifier linearId, String applicantFirstName, String applicantLastName, String applicantEmail, String applicantPhone, String applicantDesignation, String applicantAadhaar, Party createParty, Party updateParty) {
        this.linearId = linearId;
        this.applicantFirstName = applicantFirstName;
        this.applicantLastName = applicantLastName;
        this.applicantEmail = applicantEmail;
        this.applicantPhone = applicantPhone;
        this.applicantDesignation = applicantDesignation;
        this.applicantAadhaar = applicantAadhaar;
        this.createParty = createParty;
        this.updateParty = updateParty;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }
    public String getApplicantFirstName() {
        return applicantFirstName;
    }

    public String getApplicantLastName() {
        return applicantLastName;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public String getApplicantPhone() {
        return applicantPhone;
    }

    public String getApplicantDesignation() {
        return applicantDesignation;
    }

    public String getApplicantAadhaar() {
        return applicantAadhaar;
    }

    public Party getCreateParty() {
        return createParty;
    }

    public Party getUpdateParty() {
        return updateParty;
    }


    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(createParty, updateParty);
    }
}
