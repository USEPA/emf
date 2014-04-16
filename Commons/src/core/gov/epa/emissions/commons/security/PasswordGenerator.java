package gov.epa.emissions.commons.security;

import gov.epa.emissions.commons.CommonsException;

import java.io.Serializable;
import java.security.MessageDigest;

import sun.misc.BASE64Encoder;

public class PasswordGenerator implements Serializable {

    /**
     * This method encrypts the plain text password and returns a string.
     */
    public String encrypt(String textPassword) throws CommonsException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(textPassword.getBytes("UTF-8"));

            byte raw[] = md.digest();
            String hash = (new BASE64Encoder()).encode(raw);
            return hash;
        } catch (Exception e) {
            throw new CommonsException(e.getMessage());
        }

    }

}
