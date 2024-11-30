package com.onboarding.user.states;

import com.onboarding.user.contracts.CompanyProfileContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(CompanyProfileContract.class)
public class CompanyProfileState implements LinearState {
    private final UniqueIdentifier linearId;

    private final String companyName;
    private final String companyCIN;
    private final String companyGSTIN;
    private final String companyPAN;
    private final String companyPhone;
    private final String companyEmail;
    private final String companyAddress;
    private final String companyMSME;

    private final Party createParty;
    private final Party updateParty;

    public CompanyProfileState(UniqueIdentifier linearId, String companyName, String companyCIN, String companyGSTIN, String companyPAN, String companyPhone, String companyEmail, String companyAddress, String companyMSME, Party createParty, Party updateParty) {
        this.linearId = linearId;
        this.companyName = companyName;
        this.companyCIN = companyCIN;
        this.companyGSTIN = companyGSTIN;
        this.companyPAN = companyPAN;
        this.companyPhone = companyPhone;
        this.companyEmail = companyEmail;
        this.companyAddress = companyAddress;
        this.companyMSME = companyMSME;
        this.createParty = createParty;
        this.updateParty = updateParty;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyCIN() {
        return companyCIN;
    }

    public String getCompanyGSTIN() {
        return companyGSTIN;
    }

    public String getCompanyPAN() {
        return companyPAN;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public String getCompanyMSME() {
        return companyMSME;
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
