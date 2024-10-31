package gov.epa.emissions.framework.services.rest.dtos;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.UserFeature;
import gov.epa.emissions.commons.security.User;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//@JsonIgnoreProperties({ "excludedDatasetTypes", "excludedUserFeatures" })
public class UserDto extends User {

//     @Override
//    public DatasetType[] getExcludedDatasetTypes() {
//        // NOTE Auto-generated method stub
//        return super.getExcludedDatasetTypes();
//    }
//
//    @Override
//    public UserFeature[] getExcludedUserFeatures() {
//        // NOTE Auto-generated method stub
//        return super.getExcludedUserFeatures();
//    }

    @Override
    public boolean isAccountDisabled() {
        // NOTE Auto-generated method stub
        return super.isAccountDisabled();
    }

    @Override
    public String getAffiliation() {
        // NOTE Auto-generated method stub
        return super.getAffiliation();
    }

    @Override
    public String getEmail() {
        // NOTE Auto-generated method stub
        return super.getEmail();
    }

    @Override
    public String getName() {
        // NOTE Auto-generated method stub
        return super.getName();
    }

    @Override
    public boolean isAdmin() {
        // NOTE Auto-generated method stub
        return super.isAdmin();
    }

    @Override
    public boolean isLoggedIn() {
        // NOTE Auto-generated method stub
        return super.isLoggedIn();
    }

    @Override
    public boolean getWantEmails() {
        // NOTE Auto-generated method stub
        return super.getWantEmails();
    }

    @Override
    public Date getLastLoginDate() {
        // NOTE Auto-generated method stub
        return super.getLastLoginDate();
    }

    @Override
    public Date getPasswordResetDate() {
        // NOTE Auto-generated method stub
        return super.getPasswordResetDate();
    }

    @Override
    public Date getLastResetDate() {
        // NOTE Auto-generated method stub
        return super.getLastResetDate();
    }

    @Override
    public String getUsername() {
        // NOTE Auto-generated method stub
        return super.getUsername();
    }

    @Override
    public String getPhone() {
        // NOTE Auto-generated method stub
        return super.getPhone();
    }

    @Override
    public int getId() {
        // NOTE Auto-generated method stub
        return super.getId();
    }

    @Override
    public Date getLockDate() {
        // NOTE Auto-generated method stub
        return super.getLockDate();
    }

    @Override
    public boolean isLocked(String owner) {
        // NOTE Auto-generated method stub
        return super.isLocked(owner);
    }

    @Override
    public boolean isLocked(User owner) {
        // NOTE Auto-generated method stub
        return super.isLocked(owner);
    }

    @Override
    public boolean isLocked() {
        // NOTE Auto-generated method stub
        return super.isLocked();
    }

    @Override
    public boolean getLocked() {
        // NOTE Auto-generated method stub
        return super.getLocked();
    }

    @Override
    public String getLockOwner() {
        // NOTE Auto-generated method stub
        return super.getLockOwner();
    }

    @Override
    public int getFailedLoginAttempts() {
        // NOTE Auto-generated method stub
        return super.getFailedLoginAttempts();
    }

}
