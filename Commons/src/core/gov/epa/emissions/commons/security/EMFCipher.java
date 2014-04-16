package gov.epa.emissions.commons.security;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JPasswordField;

public class EMFCipher {

    private Cipher cipher;

    public EMFCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.cipher = Cipher.getInstance(SecurityConstants.ENCRYPTION_ALGORITHM);
    }

    public byte[] encrypt(PublicKey publicKey, char[] chars) throws InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {

        byte[] bytes = new byte[chars.length];

        try {
            for (int i = 0; i < chars.length; i++) {
                bytes[i] = (byte) chars[i];
            }

            return this.encrypt(publicKey, bytes);
        } finally {

            /*
             * Be sure to clear out all clear text arrays after use
             */
            EMFCipher.clearChars(chars);
            EMFCipher.clearBytes(bytes);
        }
    }

    public byte[] encrypt(PublicKey publicKey, byte[] bytes) throws InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {

        try {
            this.cipher.init(ENCRYPT_MODE, publicKey);
            byte[] encryptedData = cipher.doFinal(bytes);

            return encryptedData;
        } finally {

            /*
             * Be sure to clear out all clear text arrays after use
             */
            EMFCipher.clearBytes(bytes);
        }
    }

    public byte[] decrypt(PrivateKey privateKey, byte[] encrpytedData) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {

        this.cipher.init(DECRYPT_MODE, privateKey);
        byte[] decryptedData = cipher.doFinal(encrpytedData);

        return decryptedData;
    }

    public static void clearBytes(byte[] bytes) {

        if (bytes != null) {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = 0;
            }
        }
    }

    public static void clearChars(char[] chars) {

        if (chars != null) {
            for (int i = 0; i < chars.length; i++) {
                chars[i] = 0;
            }
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, UnsupportedEncodingException {

        EMFKeyGenerator keyGenerator = new EMFKeyGenerator();
        KeyPair keyPair = keyGenerator.generateKeys();
        EMFCipher cipher = new EMFCipher();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        byte[] encodedPublicKey = publicKey.getEncoded();
        String encodedPKYStr = new String(encodedPublicKey, SecurityConstants.CHAR_SET);
        KeyFactory keyFactory = KeyFactory.getInstance(SecurityConstants.ENCRYPTION_ALGORITHM);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(encodedPKYStr.getBytes(SecurityConstants.CHAR_SET));
        PublicKey decodedPublicKey = keyFactory.generatePublic(x509KeySpec);
        
        /*
         * This is just an example of how to use a password field
         */
        JPasswordField passwordField = new JPasswordField();
        passwordField.setText("Hello World!");

        /*
         * Never read the password into a field, or convert it to a String
         */
        byte[] encryptedData = cipher.encrypt(decodedPublicKey, passwordField.getPassword());
        String encryptedString = new String(encryptedData);
        System.out.println("Encrypted String:");
        System.out.println(encryptedString);

        byte[] decryptedData = cipher.decrypt(privateKey, encryptedData);

        /*
         * If it's possible to use the decrypted password as a byte array and NOT a String. That would be preferable,
         * from a security standpoint, as Strings are immutable.
         */
        String decryptedString = new String(decryptedData);
        System.out.println("Decrypted String:");
        System.out.println(decryptedString);

        /*
         * Be sure to clear out all clear text arrays after use
         */
        EMFCipher.clearBytes(decryptedData);
    }
}

