package gov.epa.emissions.commons.security;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Base64;

import gov.epa.emissions.commons.CommonsException;

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

    public static void main(String[] args) throws CommonsException {
        String pwd = "Di090968do!@";
        PasswordGenerator passwordGenerator = new PasswordGenerator();
        pwd = "Di090968do!@";
        System.out.println(pwd + " " + passwordGenerator.encrypt(pwd));
        pwd = "Di090968do!";
        System.out.println(pwd + " " + passwordGenerator.encrypt(pwd));
        pwd = "Ci090968ro!@";
        System.out.println(pwd + " " + passwordGenerator.encrypt(pwd));
        pwd = "Ci010795ro!@";
        System.out.println(pwd + " " + passwordGenerator.encrypt(pwd));
        pwd = "Ci010795ro!";
        System.out.println(pwd + " " + passwordGenerator.encrypt(pwd));
        pwd = "Ci090968ro!";
        System.out.println(pwd + " " + passwordGenerator.encrypt(pwd));
        pwd = "Su090968mi!@";
        System.out.println(pwd + " " + passwordGenerator.encrypt(pwd));
        pwd = "Su090968mi!";
        System.out.println(pwd + " " + passwordGenerator.encrypt(pwd));
        pwd = "Ma090968ya!@";
        System.out.println(pwd + " " + passwordGenerator.encrypt(pwd));
        pwd = "Ma090968ya!";
        System.out.println(pwd + " " + passwordGenerator.encrypt(pwd));
        pwd = "Yo090968gi!@";
        System.out.println(pwd + " " + passwordGenerator.encrypt(pwd));
        pwd = "Yo090968gi!";
        System.out.println(pwd + " " + passwordGenerator.encrypt(pwd));
    }
}
