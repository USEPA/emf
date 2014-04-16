package gov.epa.mims.analysisengine.rcommunicator;


/**
 * class_description
 *
 * @author Tommy E. Cathey
 * @version $Id: WrappedCmd.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 *
 **/
public class WrappedCmd
{
   /******************************************************
    *
    * fields
    *
    *****************************************************/
   /** time expired status flag */
   private boolean timeExpiredFlag = false;

   /** error occurred flag */
   private boolean errorFlag = false;

   /** results OK status flag */
   private boolean resultsOkFlag = false;

   /** execution complete status flag */
   private boolean executionCompleteFlag = false;

   /** the command to be executed by R */
   private String cmd;

   /** the command to be executed by R with an additional tag command for 
    * status
    */
   private String taggedCmd;

   /** unique id for this command */
   private int id;

   /** R error message */
   private String rErrorMsg = null;

   /** string used to identify R completion of execution of this command */
   private String scanCompletion = null;

   /** string used to identify R error */
   private String scanError = null;

   /** prefix of error message returned from R */
   private String scanErrorMsgPrefix = null;

   /** scan results from scanning R messages */
   private String scanResults = null;

   /******************************************************
    *
    * methods
    *
    *****************************************************/
   /**
    * Creates a new WrappedCmd object.
    *
    * @param cmd R command to execute
    * @param id unique id for this command
    * @pre cmd != null
    * @pre id >= 0
    */
   public WrappedCmd(String cmd, int id)
   {
      this.cmd = cmd;
      this.id = id;

//4-21-04 start
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
      cmd = b.toString();
//4-21-04 end

      if (id == -1)
      {
         this.taggedCmd = cmd;
         this.scanCompletion = "q()";
      }
      else
      {
         this.taggedCmd = cmd + "\nprint(\"EXECUTION_COMPLETE_" + id + "\")";
         this.scanCompletion = "[1] \"EXECUTION_COMPLETE_" + id + "\"";
      }
   }

   /**
    * retrieve R command to be executed
    *
    * @return R command to be executed
    * @pre cmd != null
    */
   public String getCmd()
   {
      return cmd;
   }

   /**
    * retrieve ID for this command
    *
    * @return ID
    * @pre id >= 0
    */
   public int getID()
   {
      return id;
   }

   /**
    * retrieve R command with a tag command to signal completion
    *
    * @return R command with a tag command to signal completion
    * @pre taggedCmd != null
    */
   public String getTaggedCmd()
   {
      return taggedCmd;
   }

   /**
    * set the execution complete flag
    *
    * @param b execution complete flag
    */
   public void setExecutionCompleteFlag(boolean b)
   {
      this.executionCompleteFlag = b;
   }

   /**
    * retrieve the execution complete flag
    *
    * @return execution complete flag
    */
   public boolean getExecutionCompleteFlag()
   {
      return executionCompleteFlag;
   }

   /**
    * set the time expired flag
    *
    * @param b time expired flag
    */
   public void setTimeExpiredFlag(boolean b)
   {
      this.timeExpiredFlag = b;
   }

   /**
    * retrieve the time expired flag
    *
    * @return time expired flag
    */
   public boolean getTimeExpiredFlag()
   {
      return timeExpiredFlag;
   }

   /**
    * set the error occurred flag
    *
    * @param b error occurred flag
    */
   public void setErrorFlag(boolean b)
   {
      this.errorFlag = b;
   }

   /**
    * retrieve the error occurred flag
    *
    * @return error occurred flag
    */
   public boolean getErrorFlag()
   {
      return errorFlag;
   }

   /**
    * set the results OK flag
    *
    * @param b results OK flag
    */
   public void setResultsOkFlag(boolean b)
   {
      this.resultsOkFlag = b;
   }

   /**
    * retrieve the results OK flag
    *
    * @return results OK flag
    */
   public boolean getResultsOkFlag()
   {
      return resultsOkFlag;
   }

   /**
    * set the scan results string
    *
    * @param s scan results string
    */
   public void setScanResults(String s)
   {
      this.scanResults = s;
   }

   /**
    * retrieve the scan results string
    *
    * @return scan results string
    */
   public String getScanResults()
   {
      return scanResults;
   }

   /**
    * set the scan error message prefix
    *
    * @param s scan error message prefix
    */
   public void setScanErrorMsgPrefix(String s)
   {
      this.scanErrorMsgPrefix = s;
   }

   /**
    * retrieve the scan error message prefix
    *
    * @return scan error message prefix
    */
   public String getScanErrorMsgPrefix()
   {
      return scanErrorMsgPrefix;
   }

   /**
    * set the scan error
    *
    * @param s scan error
    */
   public void setScanError(String s)
   {
      this.scanError = s;
   }

   /**
    * retrieve the scan error
    *
    * @return scan error
    */
   public String getScanError()
   {
      return scanError;
   }

   /**
    * retrieve the scan completion string
    *
    * @return scan completion string
    * @pre scanCompletion != null
    */
   public String getScanCompletion()
   {
      return scanCompletion;
   }

   /**
    * set the R error message string
    *
    * @param s R error message
    * @pre s != null
    */
   public void setRErrorMsg(String s)
   {
      this.rErrorMsg = s;
   }

   /**
    * retrieve the R error message string
    *
    * @return R error message
    */
   public String getRErrorMsg()
   {
      return rErrorMsg;
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
}
