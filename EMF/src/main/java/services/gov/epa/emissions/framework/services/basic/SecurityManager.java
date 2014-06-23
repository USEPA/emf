package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.security.EMFCipher;
import gov.epa.emissions.commons.security.EMFKeyGenerator;
import gov.epa.emissions.framework.services.EmfException;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SecurityManager {
    
    private static Log LOG = LogFactory.getLog(SecurityManager.class);
    
    private static SecurityManager instance;
    
    private PublicKey publicKey;

    private PrivateKey privateKey;
    
    private EMFCipher cipher;
    
    private HashMap<String, byte[]> passMap = new HashMap<String, byte[]>();
    
    public static SecurityManager getInstance() {
        if (instance == null)
            instance = new SecurityManager();
        
        return instance;
    }
    
    private SecurityManager() {
        //
    }
    
    public String getDecryptedPassword(byte[] encodedPassword) throws EmfException {
        try {
            generateKeys();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Invalid encryption algorithm", e);
            throw new EmfException("Invalid encryption algorithm.");
        }
        
        try {
            getCipher();
            byte[] decryptedData = cipher.decrypt(privateKey, encodedPassword);
            String decryptedString = new String(decryptedData);
            EMFCipher.clearBytes(decryptedData);
            
            return decryptedString;
        } catch (InvalidKeyException e) {
            LOG.error("Invalid public/private key", e);
            throw new EmfException("Invalid public/private key");
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Invalid encryption algorithm", e);
            throw new EmfException("Invalid encryption algorithm");
        } catch (NoSuchPaddingException e) {
            LOG.error("Invalid encryption padding", e);
            throw new EmfException("Invalid encryption padding.");
        } catch (IllegalBlockSizeException e) {
            LOG.error("Invalid encryption block size", e);
            throw new EmfException("Invalid encryption block size");
        } catch (BadPaddingException e) {
            LOG.error("Invalid encryption padding", e);
            throw new EmfException("Invalid encryption padding");
        }
    }

    public synchronized byte[] getEncodedPublickey() throws EmfException {
        try {
            generateKeys();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Invalid encryption algorithm", e);
            throw new EmfException("Invalid encryption algorithm.");
        }
        
        return publicKey.getEncoded();
    }
    
    private void generateKeys() throws NoSuchAlgorithmException {
        if (publicKey != null && privateKey != null)
            return;

        EMFKeyGenerator keyGenerator = new EMFKeyGenerator();
        
        while (publicKey == null || privateKey == null) {
            KeyPair keyPair = keyGenerator.generateKeys();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
        }
    }

    public void updateEncryptedPassword(String host, String username, byte[] encodedPassword) throws EmfException {
//        String pass = getDecryptedPassword(encodedPassword);
//        InputStream inStream = RemoteCommand.execute("sh", "-c", "ssh", "-O PasswordAuthentication", username, host, "");
        try {
            getCipher();
            passMap.put(username+"@"+host, encodedPassword);
            byte[] decryptedData = cipher.decrypt(privateKey, encodedPassword);
            throw new EmfException("Your password is: " + new String(decryptedData));
        } catch (InvalidKeyException e) {
            throw new EmfException("under construction...");
        } catch (NoSuchPaddingException e) {
            throw new EmfException("under construction...");
        } catch (NoSuchAlgorithmException e) {
            throw new EmfException("under construction...");
        } catch (IllegalBlockSizeException e) {
            throw new EmfException("under construction...");
        } catch (BadPaddingException e) {
            throw new EmfException("under construction...");
        }
    }

    public boolean passwordRegistered(String user, String host) {
        return passMap.containsKey(user+"@"+host);
    }
    
    private void getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        try {
            if (cipher == null)
                cipher = new EMFCipher();
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (NoSuchPaddingException e) {
            throw e;
        }
    }
    
}
