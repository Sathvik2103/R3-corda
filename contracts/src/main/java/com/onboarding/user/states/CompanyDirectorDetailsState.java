package com.onboarding.user.states;

import com.onboarding.user.contracts.CompanyDirectorDetailsContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(CompanyDirectorDetailsContract.class)
public class CompanyDirectorDetailsState implements LinearState {
    private final UniqueIdentifier linearId;

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
    private final String  directorCurrentLoanEMI;
    private final boolean isDirectorDueMissedLast6Months;
    private final boolean isDirectorDueMissedLast12Months;
    private final boolean isDirectorDueMissedLast18Months;


    private final Party createParty;
    private final Party updateParty;

    public CompanyDirectorDetailsState(UniqueIdentifier linearId, String directorFirstName, String directorLastName, String directorEmail, String directorPhone, String directorDesignation, String directorAadhaar, String directorPAN, String directorTotalLoanCount, String directorTotalODCount, String directorCurrentLoanOutstanding, String directorCurrentLoanEMI, boolean isDirectorDueMissedLast6Months, boolean isDirectorDueMissedLast12Months, boolean isDirectorDueMissedLast18Months, Party createParty, Party updateParty) {
        this.linearId = linearId;
        this.directorFirstName = directorFirstName;
        this.directorLastName = directorLastName;
        this.directorEmail = directorEmail;
        this.directorPhone = directorPhone;
        this.directorDesignation = directorDesignation;
        this.directorAadhaar = directorAadhaar;
        this.directorPAN = directorPAN;
        this.directorTotalLoanCount = directorTotalLoanCount;
        this.directorTotalODCount = directorTotalODCount;
        this.directorCurrentLoanOutstanding = directorCurrentLoanOutstanding;
        this.directorCurrentLoanEMI = directorCurrentLoanEMI;
        this.isDirectorDueMissedLast6Months = isDirectorDueMissedLast6Months;
        this.isDirectorDueMissedLast12Months = isDirectorDueMissedLast12Months;
        this.isDirectorDueMissedLast18Months = isDirectorDueMissedLast18Months;
        this.createParty = createParty;
        this.updateParty = updateParty;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public String getDirectorFirstName() {
        return directorFirstName;
    }

    public String getDirectorLastName() {
        return directorLastName;
    }

    public String getDirectorEmail() {
        return directorEmail;
    }

    public String getDirectorPhone() {
        return directorPhone;
    }

    public String getDirectorDesignation() {
        return directorDesignation;
    }

    public String getDirectorAadhaar() {
        return directorAadhaar;
    }

    public String getDirectorPAN() {
        return directorPAN;
    }

    public String getDirectorTotalLoanCount() {
        return directorTotalLoanCount;
    }

    public String getDirectorTotalODCount() {
        return directorTotalODCount;
    }

    public String getDirectorCurrentLoanOutstanding() {
        return directorCurrentLoanOutstanding;
    }

    public String getDirectorCurrentLoanEMI() {
        return directorCurrentLoanEMI;
    }

    public boolean isDirectorDueMissedLast6Months() {
        return isDirectorDueMissedLast6Months;
    }

    public boolean isDirectorDueMissedLast12Months() {
        return isDirectorDueMissedLast12Months;
    }

    public boolean isDirectorDueMissedLast18Months() {
        return isDirectorDueMissedLast18Months;
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
