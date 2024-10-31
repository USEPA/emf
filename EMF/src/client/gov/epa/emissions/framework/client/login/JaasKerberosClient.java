package gov.epa.emissions.framework.client.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.util.Arrays;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

public class JaasKerberosClient {

	public static void main(String[] args) {
//	    System.setProperty("java.security.krb5.realm", "EPA.GOV");
//	    System.setProperty("java.security.krb5.kdc", "aa.ad.epa.gov");
	    System.setProperty("java.security.auth.login.config", "D:\\emf_dev\\eclipse\\emf\\EMF\\jaas.conf");
		LoginContext lc = null;

		try {
		    MyCallbackHandler myCallbackHandler = new MyCallbackHandler();
			lc = new LoginContext("JaasSample", myCallbackHandler);
			lc.login();
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e);
		}
		Subject signedOnUserSubject = lc.getSubject();
        System.out.println(lc);
        System.out.println(signedOnUserSubject);
	}
	
}

/**
 * The application implements the CallbackHandler.
 *
 * <p> This application is text-based.  Therefore it displays information
 * to the user using the OutputStreams System.out and System.err,
 * and gathers input from the user using the InputStream System.in.
 */
class MyCallbackHandler implements CallbackHandler {

    /**
     * Invoke an array of Callbacks.
     *
     * <p>
     *
     * @param callbacks an array of <code>Callback</code> objects which contain
     *                  the information requested by an underlying security
     *                  service to be retrieved or displayed.
     *
     * @exception java.io.IOException if an input or output error occurs. <p>
     *
     * @exception UnsupportedCallbackException if the implementation of this
     *                  method does not support one or more of the Callbacks
     *                  specified in the <code>callbacks</code> parameter.
     */
    public void handle(Callback[] callbacks)
    throws IOException, UnsupportedCallbackException {

        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof TextOutputCallback) {

                // display the message according to the specified type
                TextOutputCallback toc = (TextOutputCallback)callbacks[i];
                switch (toc.getMessageType()) {
                case TextOutputCallback.INFORMATION:
                    System.out.println(toc.getMessage());
                    break;
                case TextOutputCallback.ERROR:
                    System.out.println("ERROR: " + toc.getMessage());
                    break;
                case TextOutputCallback.WARNING:
                    System.out.println("WARNING: " + toc.getMessage());
                    break;
                default:
                    throw new IOException("Unsupported message type: " +
                                        toc.getMessageType());
                }

            } else if (callbacks[i] instanceof NameCallback) {

                // prompt the user for a username
                NameCallback nc = (NameCallback)callbacks[i];

                String nameCallbackPrompt = nc.getPrompt();
                System.err.print(nameCallbackPrompt);
                System.err.flush();
                
                int kerberosPos = nameCallbackPrompt.indexOf("Kerberos username [");
                String username = nameCallbackPrompt.substring((kerberosPos == 0 ? ("Kerberos username [").length() : 0), 
                        nameCallbackPrompt.lastIndexOf("]"));
                
                nc.setName("DDELVECC");

            } else if (callbacks[i] instanceof PasswordCallback) {

                // prompt the user for sensitive information
                PasswordCallback pc = (PasswordCallback)callbacks[i];
                System.err.print(pc.getPrompt());
                System.err.flush();
//                                pc.setPassword(readPassword(System.in));
                pc.setPassword("*".toCharArray());

            } else {
                throw new UnsupportedCallbackException
                        (callbacks[i], "Unrecognized Callback");
            }
        }
    }

    // Reads user password from given input stream.
    private char[] readPassword(InputStream in) throws IOException {

        char[] lineBuffer;
        char[] buf;
        int i;

        buf = lineBuffer = new char[128];

        int room = buf.length;
        int offset = 0;
        int c;

loop:   while (true) {
            switch (c = in.read()) {
            case -1:
            case '\n':
                break loop;

            case '\r':
                int c2 = in.read();
                if ((c2 != '\n') && (c2 != -1)) {
                    if (!(in instanceof PushbackInputStream)) {
                        in = new PushbackInputStream(in);
                    }
                    ((PushbackInputStream)in).unread(c2);
                } else
                    break loop;

            default:
                if (--room < 0) {
                    buf = new char[offset + 128];
                    room = buf.length - offset - 1;
                    System.arraycopy(lineBuffer, 0, buf, 0, offset);
                    Arrays.fill(lineBuffer, ' ');
                    lineBuffer = buf;
                }
                buf[offset++] = (char) c;
                break;
            }
        }

        if (offset == 0) {
            return null;
        }

        char[] ret = new char[offset];
        System.arraycopy(buf, 0, ret, 0, offset);
        Arrays.fill(buf, ' ');

        return ret;
    }
}