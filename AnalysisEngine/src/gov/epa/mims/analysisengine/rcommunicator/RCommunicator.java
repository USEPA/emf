package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.AnalysisException;
import gov.epa.mims.analysisengine.exec.Exec;
import gov.epa.mims.analysisengine.rcommunicator.WrappedCmd;

import java.io.IOException;

import java.lang.InterruptedException;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.io.FileNotFoundException;


/**
 * Singleton class which extends Exec and is responsible for
 * communicating with R
 *
 * @author Tommy E. Cathey
 * @version $Id: RCommunicator.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 **/
public class RCommunicator extends Exec implements java.io.Serializable,
                                                   Cloneable
{
   /** DOCUMENT_ME */
   static final long serialVersionUID = 1;

   /** DOCUMENT_ME */
   static private RCommunicator instance = new RCommunicator();

   /** DOCUMENT_ME */
   private static final String R_ERROR_PREFIX = "Error";

   /** DOCUMENT_ME */
   private String pathToR;

   /** DOCUMENT_ME */
   private int timeout = 0;

   /** DOCUMENT_ME */
   private int cmdCount = 0;

   /** DOCUMENT_ME */
   private boolean ioException = false;

   /** DOCUMENT_ME */
   private boolean startRfailure = false;

   /** DOCUMENT_ME */
   private boolean connectionOK = false;

   /** DOCUMENT_ME */
   private boolean interrupted = false;

   /** DOCUMENT_ME */
   private String errorEchoMsg = "ERROR";

   /** DOCUMENT_ME */
   private Exception exception = null;

   /** DOCUMENT_ME */
   private boolean terminateRequested = false;

   /** DOCUMENT_ME */
   private IssueCmdThread issueCmdThread;

   private PrintWriter commandLog = null;

   private String sourceENV = null;
   /**
    * private constructor for Singleton Pattern
    *
    * @author Tommy E. Cathey
    *
    *******************************************************************/
   private RCommunicator()
   {
    String userHome = System.getProperty("user.home",".");
    String FILE_SEPARATOR = System.getProperty("file.separator","/");
    String logFile = userHome+FILE_SEPARATOR+"AnalysisEngineCommands.log";
    try
    {
      commandLog = new PrintWriter(new FileOutputStream(logFile));
    }
    catch(FileNotFoundException e)
    {
      e.printStackTrace();
    }

   }

   /**
    * sets path to R executable
    *
    * @author Tommy E. Cathey
    *
    * @param pathToR the path to R executable
    *
    *******************************************************************/
   public void setPathToR(String pathToR)
   {
      this.pathToR = pathToR;
   }

   /**
    * DOCUMENT_ME
    ********************************************************/
   private void quitR()
   {
      WrappedCmd[] wrappedCmds = new WrappedCmd[1];
      wrappedCmds[0] = new WrappedCmd("q()", -1);
      wrappedCmds[0].setScanErrorMsgPrefix(R_ERROR_PREFIX);
      wrappedCmds[0].setScanError("[1] \"" + errorEchoMsg + "\"");
      wrappedCmds[0].setScanResults(null);

      suspendStdio();
      issueCmdThreadLauncher(wrappedCmds, timeout);
      killChildProcess();
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private boolean isConnectionOK()
   {
      WrappedCmd[] wrappedCmds = new WrappedCmd[1];
      wrappedCmds[0] = new WrappedCmd("print(8209387*9)", cmdCount++);
      wrappedCmds[0].setScanErrorMsgPrefix(R_ERROR_PREFIX);
      wrappedCmds[0].setScanError("[1] \"" + errorEchoMsg + "\"");
      wrappedCmds[0].setScanResults("[1] 73884483");

      issueCmdThreadLauncher(wrappedCmds, 1000);

      return wrappedCmds[0].getResultsOkFlag();
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private boolean initR()
   {

      boolean statusOK = false;
      WrappedCmd[] wrappedCmds = new WrappedCmd[1];
      wrappedCmds[0] = new WrappedCmd("options(error=expression(print(\""
                                      + errorEchoMsg + "\")))", cmdCount++);
      wrappedCmds[0].setScanErrorMsgPrefix(R_ERROR_PREFIX);
      wrappedCmds[0].setScanError("[1] \"" + errorEchoMsg + "\"");
      wrappedCmds[0].setScanResults(null);

      issueCmdThreadLauncher(wrappedCmds, timeout);

      if (wrappedCmds[0].getExecutionCompleteFlag()
             && !wrappedCmds[0].getErrorFlag())
      {
         statusOK = true;
      }

      //
      //have R read flat file RCommunicator.env in order to set
      //the R environment
      //
      File fileEnv = new File("RCommunicator.env");
      fileEnv.deleteOnExit();

      // if environment file does not exist, generate from jar file
      if( !fileEnv.exists() )
      {
         //  open RCommunicator environment file from jar and copy to tmp file
         try
         {
            String jarEnv = "/gov/epa/mims/analysisengine/rcommunicator/RCommunicator.env";
            InputStream is = getClass().getResourceAsStream(jarEnv);
            if (is == null)
            {
               throw new Exception("Could not find RCommunicator.env");
            }
            BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is));

            fileEnv.createNewFile();
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileEnv));

            String record;
            String crlf = System.getProperty("line.separator");
            while ((record=reader.readLine())!=null)
            {
               writer.write(record + crlf);
            }
            reader.close();
            writer.close();
         }
         catch (Exception ex)
         {
            ex.printStackTrace();
            return false;
         }
      }

      String str = fileEnv.getAbsolutePath();
   //   String str = Util.getBasenamePath(RCommunicator.class);
      str = Util.escapeQuote(str);
      String str2 = str.replace('\\', '/');

      ArrayList sourceCmd = new ArrayList();
      sourceCmd.add("source(" + str2 + ")");
      sourceCmd.add("ls()");
      issueCommands(sourceCmd, 2000);

      sourceENV = "source(" + str2 + ")";

      return statusOK;
   }

   /**
    * start R and wait for it to come up or time out
    *
    * @author Tommy E. Cathey
    *
    * @return true if R connection is OK; otherwise false
    *
    *******************************************************************/
   private boolean startR()
   {
      int maxTime = AnalysisEngineConstants.MAX_TIME_TO_START_CHILD;
      int sleepSlice = AnalysisEngineConstants.SLEEP_SLICE;
      int elapsedTime = 0;
      String[] searchStrings = new String[4];
      searchStrings[0] = " to quit R";
      searchStrings[1] = R_ERROR_PREFIX;
      searchStrings[2] = "[1] \"" + errorEchoMsg + "\"";
      searchStrings[3] = null;

      try
      {
         scanSetSearchStrings(searchStrings);

         StartChildProcess(pathToR + " --no-save"); // throws IOException

         while (!scanFound(0) && (elapsedTime <= maxTime))
         {
            Thread.sleep(sleepSlice);
            elapsedTime += sleepSlice;
         }
      }
      catch (IOException e)
      {
         ioException = true;
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
      }

      return initR();
   }

   /**
    * method to terminate R
    *
    * @author Tommy E. Cathey
    *
    *******************************************************************/
   public void terminate()
   {
      if (issueCmdThread.isAlive())
      {
         if (timeout == 0)
         {
            issueCmdThread.terminate();
         }
         else
         {
            try
            {
               issueCmdThread.join();
            }
            catch (InterruptedException e)
            {
            }
         }
      }

      quitR();
   }

   /**
    * issue a sequence of R commands; start R if not already running
    *
    * @author Tommy E. Cathey
    *
    * @param commands list of R commands
    * @param timeout max milliseconds allowed per command; 0 = wait indefinitely
    * @exception gov.epa.mims.analysisengine.AnalysisException on error or timeout
    *
    *******************************************************************/

   synchronized public void issueCommands(List commands, int timeout)
                                   throws AnalysisException
   {
      //
      //----------------------------------------------------------------------
      // check if R is running
      //----------------------------------------------------------------------
      //
      if (!isConnectionOK())
      {
         //System.out.println("starting R");
         startR();
         //System.out.println("R started");
      }

      //
      //----------------------------------------------------------------------
      // send commands to R
      //----------------------------------------------------------------------
      //
      WrappedCmd[] wrappedCmds = new WrappedCmd[commands.size() + 3];
      wrappedCmds[0] = new WrappedCmd("rm(list=ls())", cmdCount++);
      wrappedCmds[0].setScanErrorMsgPrefix(R_ERROR_PREFIX);
      wrappedCmds[0].setScanError("[1] \"" + errorEchoMsg + "\"");
      wrappedCmds[0].setScanResults(null);

      if(sourceENV != null)
      {
        wrappedCmds[1] = new WrappedCmd(sourceENV, cmdCount++);
      }
      else
      {
        wrappedCmds[1] = new WrappedCmd("ls()", cmdCount++);
      }
      wrappedCmds[1].setScanErrorMsgPrefix(R_ERROR_PREFIX);
      wrappedCmds[1].setScanError("[1] \"" + errorEchoMsg + "\"");
      wrappedCmds[1].setScanResults(null);

      wrappedCmds[2] = new WrappedCmd("ls()", cmdCount++);
      wrappedCmds[2].setScanErrorMsgPrefix(R_ERROR_PREFIX);
      wrappedCmds[2].setScanError("[1] \"" + errorEchoMsg + "\"");
      wrappedCmds[2].setScanResults(null);

      for (int i = 3; i < commands.size()+3; i++)
      {
         wrappedCmds[i] = new WrappedCmd((String) commands.get(i-3), cmdCount++);
         wrappedCmds[i].setScanErrorMsgPrefix(R_ERROR_PREFIX);
         wrappedCmds[i].setScanError("[1] \"" + errorEchoMsg + "\"");
         wrappedCmds[i].setScanResults(null);
      }

      issueCmdThreadLauncher(wrappedCmds, timeout);

      //
      //----------------------------------------------------------------------
      // did any R errors occur?
      //----------------------------------------------------------------------
      //
      String exceptionMsg = "issueCommands: \n";
      boolean errorOccurred = false;

      for (int i = 0; i < wrappedCmds.length; i++)
      {
         if (wrappedCmds[i].getTimeExpiredFlag())
         {
            errorOccurred = true;
            exceptionMsg += ("Command #" + wrappedCmds[i].getID() + " Timed Out\n");
            exceptionMsg += wrappedCmds[i].getCmd();
         }

         if (wrappedCmds[i].getErrorFlag())
         {
            errorOccurred = true;
            exceptionMsg += ("Command #" + wrappedCmds[i].getID() + "\n");
            exceptionMsg += wrappedCmds[i].getCmd();
            exceptionMsg += "\nProduced the following error\n";
            exceptionMsg += (wrappedCmds[i].getRErrorMsg() + "\n");
         }
      }

      if (errorOccurred)
      {
         throw new AnalysisException(exceptionMsg);
      }
   }

   /**
    * method to close all plot devices
    *
    * @author Tommy E. Cathey
    *
    * @param
    * @exception
    *
    *******************************************************************/
   public void closePlotWindows(int timeout) throws AnalysisException
   {
      ArrayList commands = new ArrayList();
      commands.add("graphics.off()");

      issueCommands(commands, timeout);
   }

   /**
    *
    *
    * @author Tommy E. Cathey
    *
    * @return class instance
    *
    *******************************************************************/
   private void issueCmdThreadLauncher(WrappedCmd[] wrappedCmds, int timeout)
   {
      for (int i = 0; i < wrappedCmds.length; i++)
      {
         issueCmdThread = new IssueCmdThread(wrappedCmds[i]);
         issueCmdThread.start();

         try
         {
            if (timeout > 0)
            {
               issueCmdThread.join(timeout);
            }
            else
            {
               issueCmdThread.join();
            }

            if (interrupted)
            {
               return; // interrupted so stop all work
            }
            else // thread timed out
            {
               if (!wrappedCmds[i].getExecutionCompleteFlag()
                      && !wrappedCmds[i].getErrorFlag())
               {
                  wrappedCmds[i].setTimeExpiredFlag(true);
               }
            }
         }
         catch (InterruptedException e)
         {
            if (issueCmdThread.isAlive())
            {
               issueCmdThread.interrupt();
            }

            e.printStackTrace();

            return; // interrupted so stop all work
         }
      }

      return;
   }

   /**
    * method to get the Singleton instance of this class
    *
    * @author Tommy E. Cathey
    *
    * @return class instance
    *
    *******************************************************************/
   static public RCommunicator getInstance()
   {
      return instance;
   }

   /**
    * describe object in a String
    *
    * @return String describing object
    ******************************************************/
   public String toString()
   {
      return Util.toString(this);
   }

   /**
    * thread class to communicate to R
    *
    * @author Tommy E. Cathey
    * @version $Id: RCommunicator.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
    *
    **/
   class IssueCmdThread extends Thread
   {
      /** DOCUMENT_ME */
      private WrappedCmd wrappedCmd;

      /** DOCUMENT_ME */
      private int sleepSlice = AnalysisEngineConstants.SLEEP_SLICE;

      /** DOCUMENT_ME */
      private boolean terminateRequested;

      /**
       * thread constructor
       *
       * @author Tommy E. Cathey
       *
       * @param commands list of R commands
       * @param timeout max milliseconds allowed per command; 0 = wait indefinitely
       * @exception gov.epa.mims.analysisengine.AnalysisException on error or timeout
       *******************************************************************/
      public IssueCmdThread(WrappedCmd wrappedCmd)
      {
         this.wrappedCmd = wrappedCmd;
         terminateRequested = false;
         ioException = false;
      }

      /**
       * DOCUMENT_ME
       ********************************************************/
      public void terminate()
      {
         terminateRequested = true;
      }
  private String protectBackslashes(String cmd)
  {
      //protect backslashes embedded in the cmd string
      StringBuffer b = new StringBuffer();
      String[] tmp = cmd.split("\\n");
      for(int i = 0;i<tmp.length;i++)
      {
         b.append(tmp[i]);
         if( i < tmp.length -1 )
         {
           b.append("\\n");
         }
      }
      return b.toString();
  }
      /**
       * threads run method
       *
       * @author Tommy E. Cathey
       *
       *******************************************************************/
      public void run()
      {
         String[] searchStrings = new String[4];
         searchStrings[0] = wrappedCmd.getScanCompletion();
         searchStrings[1] = wrappedCmd.getScanErrorMsgPrefix();
         searchStrings[2] = wrappedCmd.getScanError();
         searchStrings[3] = wrappedCmd.getScanResults();

         scanSetSearchStrings(searchStrings);

         try
         {
            commandLog.println(protectBackslashes(wrappedCmd.getCmd()));
            commandLog.flush();
            writeToChildsStdin(wrappedCmd.getTaggedCmd()); // throws IOException

            while (!isInterrupted() && !terminateRequested)
            {
               sleep(sleepSlice); // throws InterruptedException

               if (scanFound(2))
               {
                  wrappedCmd.setErrorFlag(true);

                  if (scanFound(1))
                  {
                     wrappedCmd.setRErrorMsg(scanGetMessage(1));
                  }

                  return;
               }

               if (scanFound(3))
               {
                  wrappedCmd.setResultsOkFlag(true);
               }

               if (scanFound(0))
               {
                  wrappedCmd.setExecutionCompleteFlag(true);

                  return;
               }
            }
         }
         catch (IOException e)
         {
            ioException = true;

            return;
         }
         catch (InterruptedException e)
         {
            interrupted = true;
         }
         catch (gov.epa.mims.analysisengine.AnalysisException e)
         {
            //this catch is here to catch the "Child not started"
            //message and stop it from being printed on the screen
            return;
         }
      }
   }
}
