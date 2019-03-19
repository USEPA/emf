package gov.epa.emissions.commons.security;

import gov.epa.emissions.commons.CommonsException;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Base64;

public class PasswordGenerator implements Serializable {

    /**
     * This method encrypts the plain text password and returns a string.
     */
    public String encrypt(String textPassword) throws CommonsException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(textPassword.getBytes("UTF-8"));

            byte raw[] = md.digest();
            String hash = Base64.getEncoder().encodeToString(raw);
            return hash;
        } catch (Exception e) {
            throw new CommonsException(e.getMessage());
        }

    }

}
