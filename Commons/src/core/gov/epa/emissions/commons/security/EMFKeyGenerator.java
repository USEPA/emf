package gov.epa.emissions.commons.security;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class EMFKeyGenerator {

    private KeyPairGenerator keyGen;

    public EMFKeyGenerator() throws NoSuchAlgorithmException {

        keyGen = KeyPairGenerator.getInstance(SecurityConstants.ENCRYPTION_ALGORITHM);
        SecureRandom random = SecureRandom.getInstance(SecurityConstants.RANDOM_ALGORITHM);
        keyGen.initialize(SecurityConstants.KEY_SIZE, random);

    }

    public KeyPair generateKeys() {
        return keyGen.genKeyPair();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {

        EMFKeyGenerator keyGenerator = new EMFKeyGenerator();

        KeyPair keyPair = keyGenerator.generateKeys();
        PublicKey publicKey = keyPair.getPublic();
        System.out.println("Public Key:");
        System.out.println(publicKey);

        /*
         * Send a string "across the wire"
         */
        byte[] encodedPublicKey = publicKey.getEncoded();
        String encodedPKStr = new String(encodedPublicKey, SecurityConstants.CHAR_SET);
        
        /*
         * This decodes it "on the other side"
         */
        KeyFactory keyFactory = KeyFactory.getInstance(SecurityConstants.ENCRYPTION_ALGORITHM);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(encodedPKStr.getBytes(SecurityConstants.CHAR_SET));
        PublicKey decodedPublicKey = keyFactory.generatePublic(x509KeySpec);

        System.out.println("Decoded Public Key:");
        System.out.println(decodedPublicKey);

        PrivateKey privateKey = keyPair.getPrivate();
        System.out.println("Private Key:");
        System.out.println(privateKey);
    }
}

