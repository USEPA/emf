package gov.epa.emissions.framework.client.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.UserService;

public class KerebosSSOWebService {

    private UserService userService;
    
    public KerebosSSOWebService(UserService userService) {
        this.userService = userService;
    }

    public String login() throws EmfException {
        byte[] ticket = null;
        String user = null;

//        System.out.println("System.getProperty(\"user.name\") = " + System.getProperty("user.name") + "\n");
        
        //SERVER-SIDE Kerberos SSO 
        
//        System.out.println("SERVER-SIDE Kerberos SSO Lookup, make Http call to SSO Url\n");
        try {
            System.setProperty("sun.security.jgss.native", "true");
            //WAS VM Parameters, now using System.setProperty
            //-Dsun.security.krb5.debug=true  -Djavax.net.ssl.trustStoreType=WINDOWS-ROOT  -Dcom.sun.security.enableAIAcaIssuers=true
            System.setProperty("sun.security.krb5.debug", "true");
            System.setProperty("javax.net.ssl.trustStoreType", "WINDOWS-ROOT");
            System.setProperty("com.sun.security.enableAIAcaIssuers", "true");

            URL url = new URL(getSSOUrl()); //"https://bramble01.hesc.epa.gov/krbsso/"
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
            connection.setDoOutput(true); 
            connection.setInstanceFollowRedirects(false); 
            connection.setRequestMethod("GET"); 
            connection.setRequestProperty("Content-Type", "text/plain"); 
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            
            //connect to Url
            connection.connect();

//            System.out.println("Http Response:");
            BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream()), StandardCharsets.UTF_8));
            String line = reader.readLine();
            user = line.substring(line.indexOf("X-Emf-User: ") + "X-Emf-User: ".length(), line.indexOf("<!DOCTYPE html PUBLIC"));
//            System.out.println("User = " + user);

/*          while (line != null ? line.length() > 0 : false) {
                System.out.println(line);
                line = reader.readLine();
            }
*/
//            System.out.println("");

//            System.out.println("Http Response Headers:");
//            Map<String, List<String>> map = connection.getHeaderFields();
//            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
//                System.out.println("Key : " + entry.getKey() 
//                                   + " ,Value : " + entry.getValue());
//            }
            
//            String encodedTicket = connection.getHeaderField("WWW-Authenticate").substring("Negotiate ".length());
//            ticket = Base64.getDecoder().decode(encodedTicket);       
        
//            System.out.println("Http Response Code: " + connection.getResponseCode());
//            System.out.println("Http Response Message: " + connection.getResponseMessage());
        } catch (IOException e) {   // | KeyManagementException | NoSuchAlgorithmException | KeyStoreException | CertificateException
            throw new EmfException(e.getMessage(), e);
        }
        
        return user;
    }

    private String getSSOUrl() throws EmfException {
        String ssoUrl;
        try {
            ssoUrl = userService.getPropertyValue(EmfProperty.SSO_URL);
        } catch (EmfException e) {
            throw e;
        }
        return ssoUrl;
    }
}
