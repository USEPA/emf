package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RemoteCommand {
    /**
     * Performes a command on a remote machine (or localhost)
     */
    private static Log LOG = LogFactory.getLog(RemoteCommand.class);

    private static final String lineSep = System.getProperty("line.separator");

    public synchronized static String logStdout(String title, InputStream inStream, boolean localHost) throws EmfException {
        /**
         * log the stdout from a remote command to the log
         */
        BufferedReader reader = null;
        String qId = null;

        // log the title of this series of message to the LOG
        LOG.warn(title);

        reader = new BufferedReader(new InputStreamReader(inStream));

        if (reader != null) {
            try {
                String message = reader.readLine();
                String lstNonNullMsg = message;

                if (message == null) {
                    message = reader.readLine();
                    lstNonNullMsg = message;
                }
                
                while (message != null) {
                    lstNonNullMsg = message;
                    LOG.warn(message);

                    if (qId == null)
                        qId = extractQId(message);
                    
                    message = reader.readLine();
                }
                
                LOG.warn("QID extracted: " + qId);
                
                if (qId == null && !localHost)
                    throw new EmfException("please check your queue options " + (lstNonNullMsg == null ? "" : "(" + lstNonNullMsg + ")"));
            } catch (Exception e) {
                e.printStackTrace();
                throw new EmfException("Error logging remote command's stdout/stderr: " + e.getMessage());
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return qId;
    }
    
    public static void logRemoteStdout(String title, InputStream inStream) throws EmfException {
        /**
         * log the stdout from a remote command to the log
         */
        BufferedReader reader = null;

        // log the title of this series of message to the LOG
        LOG.warn(title);

        reader = new BufferedReader(new InputStreamReader(inStream));

        if (reader != null) {
            try {
                String message = reader.readLine();

                if (message == null)
                    message = reader.readLine();
                
                while (message != null) {
                    LOG.warn(message);
                    message = reader.readLine();
                }
            } catch (Exception e) {
                throw new EmfException("Error logging remote command's stdout/stderr: " + e.getMessage());
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.error("", e);
                }
            }
        }
    }

    private static String extractQId(String message) {
        if (message == null || message.trim().isEmpty())
            return null;

        // TBD: need to generalize this to accept other queue ID formats - perhaps with
        //      new property
        Pattern p = Pattern.compile("^[0-9]*\\.(.)*", Pattern.MULTILINE);
        Matcher m = p.matcher(message.trim());

        if (m.find()) {
            return m.group();
        }

        return null;
    }

    public static void main(String[] args) {
        String str = "";
        
        str = "\n"
+ "        WARNING NOTICE\n"
+ "\n"
+ "\n"
+ "You are accessing a U.S. Government information system, which includes \n"
+ "(1) this computer, (2) this computer network, (3) all computers connected \n"
+ "to this network, and (4) all devices and storage media attached to this \n"
+ "network or to a computer on this network.  This information system is \n"
+ "provided for U.S. Government-authorized use only.  Unauthorized or improper \n"
+ "use of this system may result in disciplinary action, as well as civil and \n"
+ "criminal penalties.  By using this information system you understand and \n"
+ "consent to the following: \n"
+ "\n"
+ "o You have no reasonable expectation of privacy regarding any communications \n"
+ "or data transiting or stored on this information system.  At any time, \n"
+ "the government may for any lawful government purpose monitor, intercept, \n"
+ "search and seize any communication or data transiting or stored on this \n"
+ "information system. \n"
+ "\n"
+ "o Any communications or data transiting or stored on this information \n"
+ "system may be disclosed or used for any lawful government purpose. \n"
+ "\n"
+ "By continuing to access this information system, you acknowledge you \n"
+ "understand and you consent to the above terms. \n"
+ "\n"
+ "EPA Security Policy - Passwords will expire after 90 days. \n"
+ "\n"
+ "***************************************************************************** \n"
+ "\n"
+ "NEWS: \n"
+ "\n"
+ "23485.garnet01";
//        str = "23485.garnet01.co";
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(str.getBytes("UTF-8"));
            System.out.println(logStdout("", is, false));
        } catch (UnsupportedEncodingException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println(extractQId(str));
        }
    
    public static String logStderr(String title, InputStream inStream) throws Exception {
        // log the title of this series of message to the LOG
        LOG.error(title);

        /**
         * log the stderr from a remote command to the log
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
        String errorMsg = null;

        if (reader == null)
            return errorMsg;

        try {
            String message = reader.readLine();
            errorMsg = message;

            while (message != null) {
                LOG.error(message);
                message = reader.readLine();
            }
            
            return errorMsg;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Error logging remote command's stdout/stderr: " + e.getMessage());
        } finally {
            reader.close();
        }
    }

    public static InputStream execute(String username, String hostname, String remoteCmd) throws EmfException {
        /**
         * Executes command on a remote machine -- short form Inputs: username - username on remote machine
         * 
         * hostname - hostname of remote machine
         * 
         * remoteCmd = command to execute on remote machine
         * 
         * Outputs - InputStream - the output from the remote command (stdout)
         */
        // some command elements
        String unixShell = "csh";
        String unixOptions = "-c";
        String sshCmd = "ssh";
        String sshOptions = "-o PasswordAuthentication=no -f";

        try {
            return execute(unixShell, unixOptions, sshCmd, sshOptions, username, hostname, remoteCmd);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public static InputStream executeLocal(String localCmd) throws EmfException {
        /**
         * Executes command on local machine -- short form
         * 
         * Inputs: localCmd - command to execute on this machine
         */

        // some command elements
        String unixShell = "csh";
        String unixOptions = "-c";

        try {
            return executeLocal(unixShell, unixOptions, localCmd);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public static int processSleep(Process p) throws EmfException {
        /**
         * Tests the remote process and waits a predetermined amount of time for a return
         * 
         * time it waits is 1 minute
         */
        int errorLevel = 2;

        try {
            errorLevel = p.exitValue();
        } catch (IllegalThreadStateException e) {
            // process is not finished wait 10 sec
            try {
                Thread.sleep(10000);
            } catch (Exception eT) {
                // do nothing
            }

            try {
                errorLevel = p.exitValue();
            } catch (IllegalThreadStateException e2) {
                // process is not finished wait 50 sec
                try {
                    Thread.sleep(50000);
                } catch (Exception eT) {
                    // do nothing
                }
                try {
                    errorLevel = p.exitValue();
                } catch (IllegalThreadStateException e3) {
                    // process still hasn't finished return error
                    p.destroy();
                    throw new EmfException("Remote command ssh has not responded for 1 minute, killing subprocess");
                }
            }
        }
        return errorLevel;
    }

    public static InputStream execute(String unixShell, String unixOptions, String sshCmd, String sshOptions,
            String username, String hostname, String remoteCmd) throws EmfException {
        /**
         * Executes a command on a remote machine -- long form
         * 
         * Inputs: unixShell - unix shell to operate under
         * 
         * unixOptions - unix options for this shell
         * 
         * sshCmd - ssh or other remote access command
         * 
         * sshOptions - remote access options
         * 
         * username - username on remote machine
         * 
         * hostname - hostname of remote machine
         * 
         * remoteCmd = command to execute on remote machine
         * 
         * Outputs - InputStream - the output from the remote command (stdout)
         */

        int errorLevel = 0;
        String[] cmds = new String[3];
        final String executeCmd = sshCmd + " " + sshOptions + " " + username + "@" + hostname + " \"" + remoteCmd + "\" |& tee";
        cmds[0] = unixShell;
        cmds[1] = unixOptions;
        cmds[2] = executeCmd;

        try {
            final Process p = Runtime.getRuntime().exec(cmds);
            
            ProcessThread waitNKill = new ProcessThread(p, executeCmd, 300000);
            waitNKill.start(); //Starts the tick and ready to kill the process p after 5 minutes

            errorLevel = p.waitFor(); //If timeout (5 minutes), process has been terminated by now
            waitNKill.done(); //If not timeout, tell the thread to stop itself

            if (DebugLevels.DEBUG_26()) {
                
                LOG.warn("errorLevel = " + errorLevel + " for (" + hostname + ") : " + remoteCmd );
                try {
                    if (p.getErrorStream() != null)
                        logStderr("stderr from (" + hostname + "): " + remoteCmd, p.getErrorStream());
                //suppress exceptions for now
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (errorLevel > 0) {
                // if have error print remote commands error message to the logs
                // and throw an exception
                if (waitNKill.getErrorMsg() != null)
                    throw new EmfException(waitNKill.getErrorMsg());
                
//removed for now, since errorstream is already closed...                
//                String errorTitle = "stderr from (" + hostname + "): " + remoteCmd;
//                logStderr(errorTitle, p.getErrorStream());
                
                return p.getErrorStream();
            }

            return p.getInputStream();

        } catch (Exception e) {
            LOG.error("ERROR executing remote command: " + executeCmd, e);
            throw new EmfException("ERROR executing remote command: " + executeCmd);
        }
    }

    public static InputStream executeLocal(String unixShell, String unixOptions, String localCmd) throws EmfException {
        /**
         * Executes a command on a local machine -- long form
         * 
         * Inputs: unixShell - unix shell to operate under
         * 
         * unixOptions - unix options for this shell
         * 
         * localCmd - command to execute on this machine
         */

        String[] cmds = new String[3];
        cmds[0] = unixShell;
        cmds[1] = unixOptions;
        cmds[2] = localCmd;
        int errorLevel = -1;

        try {
            Process p = Runtime.getRuntime().exec(cmds);
            try {
                errorLevel = p.exitValue();
            } catch (IllegalThreadStateException e2) {
                // process is not finished wait -- don't wait
            }

            if (DebugLevels.DEBUG_0())
                LOG.warn("Started command on the local EMF machine: " + localCmd);

            if (errorLevel > 0) {
                // error in local command
                String errorMsg = logStderr("Error from localhost", p.getErrorStream());
                
                if (errorMsg == null)
                    errorMsg = "Error message not readable from localhost.";
                
                throw new EmfException("ERROR executing local command: " + localCmd + lineSep + errorMsg);
            }

            return p.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("ERROR in executing local command: " + localCmd);
            throw new EmfException("ERROR executing local command: " + e.getMessage());
        }
    }
    
    public static void mainOld(String[] args) {
        //NOTE: test Linux side (EMF server) remote access functions
        String unixShell = "csh";
        String unixOptions = "-c";
        String sshOptions = "sshpass -p '" + args[1] + "' ssh -o PasswordAuthentication=yes ";
        String[] cmds = new String[3];
        final String executeCmd = sshOptions + " " + args[0] + "@amber.nesc.epa.gov sleep " + args[2] + ";date";
        cmds[0] = unixShell;
        cmds[1] = unixOptions;
        cmds[2] = executeCmd;
        
        Process p = null;
        
        try {
            p =  Runtime.getRuntime().exec(cmds);
            
            ProcessThread waitNKill = new ProcessThread(p, executeCmd, (Integer.parseInt(args[2]) - 30)*1000);
            waitNKill.start(); //Starts the tick and ready to kill the process p after 5 minutes

            int errorLevel = p.waitFor(); //If timeout (5 minutes), process has been terminated by now
            waitNKill.done(); //If not timeout, tell the thread to stop itself
            
            System.out.println("error level: " + errorLevel);
            
            //System.out.println("local time: " + new Date());
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        if (reader == null)
            System.exit(1);

        try {
            String message = reader.readLine();
            System.out.println("message is null: " + (message == null));
            
            while (message != null) {
                System.out.println(message);
                message = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    
}
