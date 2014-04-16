package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * base class for generating R commands
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class Cmd
{
   /** flag for disabling the command generation */
   private boolean issueCommand = true;

   /** banner flag; useful when reading generated R commands */
   private boolean bannerRequested = false;

   /** name of command being generated */
   private String name = null;

   /** the R variable the command should return results to */
   private String returnVariable = null;

   /** hash map of R command arguments and their values */
   private HashMap variables = new HashMap();

   /** List of R commands to execute prior to the main command */
   private ArrayList rCommandsPre = new ArrayList();

   /** List of R commands to execute after the main command */
   private ArrayList rCommandsPost = new ArrayList();

   /** List of this R command's arguments */
   private ArrayList keys = new ArrayList();

   public Cmd()
   {
   }

   /**
    * set the issueCommand flag
    *
    * @param issueCommand true-enables false-disables command generation
    ********************************************************/
   public void setIssueCommand(boolean issueCommand)
   {
      this.issueCommand = issueCommand;
   }

   /**
    * set the returnVariable 
    * (the R variable the command should return results to)
    *
    * @param returnVariable R return variable name
    ********************************************************/
   public void setReturnVariable(String returnVariable)
   {
      this.returnVariable = returnVariable;
   }

   /**
    * set the banner flag
    *
    * @param state true-banner on; false-banner off
    ********************************************************/
   public void setBannerOn(boolean state)
   {
      this.bannerRequested = state;
   }

   /**
    * set the name of the R command to generate
    *
    * @param name name of the R command to generate
    ********************************************************/
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * retrieve all the generated R commands
    *
    * @return all the generated R commands
    ********************************************************/
   public List getCommands()
   {
      //list to store all commands before returning them
      ArrayList rtrn = new ArrayList();

      if (issueCommand)
      {
         //print a banner if requested
         if (bannerRequested)
         {
            String tmp = (name == null)
                         ? ""
                         : name;
            rtrn.add("print(\"============" + tmp + " Commands============\")");
         }


         //all the "pre-commands" go first
         rtrn.addAll(rCommandsPre);

         //next goes the generated command
         String generatedCmd = genCommand();

         if (generatedCmd != null)
         {
            rtrn.add(generatedCmd);
         }


         //now the "post commands" are added
         rtrn.addAll(rCommandsPost);

         //print a ending banner if banner is requested
         if (bannerRequested)
         {
            rtrn.add("print(\"========End of " + name
                     + " Commands=========\")");
         }
      }

      return rtrn;
   }

   /**
    * takes the current keys or arguments and builds the R
    * command. The newly built R command is placed at the end
    * of the {@link #rCommandsPreAdd} list.
    ********************************************************/
   public void generateAsPreCommand()
   {
      String generatedCmd = genCommand();

      if (generatedCmd != null)
      {
         rCommandsPreAdd(generatedCmd);
      }
   }

   /**
    * clear the keys list. This might be useful after a call
    * to {@link #generateAsPreCommand} and the user wants to
    * start a new command from scratch.
    * 
    ********************************************************/
   public void clearAllVariables()
   {
      keys.clear();
   }

   /**
    * generate and return the R command
    *
    * @return generated R command
    ********************************************************/
   private String genCommand()
   {
      String rtrn = null;

      if (keys.size() > 0)
      {
         if (name == null)
         {
            throw new AnalysisException(getClass() + " name==null");
         }

         //if user has specified an R returnVariable,
         //then insert it at the beginning
         String rCmd = (returnVariable == null)
                       ? ""
                       : returnVariable + "<-";
         rCmd += name;

         //tmp String[] hold the R arguments or keys
         String[] tmp = new String[keys.size()];

         for (int i = 0; i < keys.size(); ++i)
         {
            Object key = keys.get(i);
            Object val = variables.get(key);

            if (val == null)
            {
               //the argument does not have a value; so no = sign
               tmp[i] = (String) key;
            }
            else
            {
               //the argument has a value; so add = sign
               tmp[i] = (String) key + "=" + (String) val;
            }
         }


         //this builds the command
         rtrn = Util.buildArrayCommand(rCmd, tmp);
      }

      return rtrn;
   }

   /**
    * add an R function argument and its value
    *
    * @param var R argument
    * @param val value of the R argument
    ********************************************************/
   protected void variableAdd(String var, String val)
   {
      if (!(keys.contains(var)) || (val == null))
      {
         keys.add(var);
      }

      variables.put(var, val);
   }

   /**
    * add an R function argument which does not have a value
    *
    * @param var R argument
    ********************************************************/
   protected void variableAdd(String var)
   {
      if (!(keys.contains(var)))
      {
         keys.add(var);
      }

      variables.put(var, null);
   }

   /**
    * remove an R function argument which was previously added
    *
    * @param var R argument to remove
    *
    * @return variables.remove(var)
    ********************************************************/
   protected String variableRemove(String var)
   {
      String rtrn = null;

      if (keys.contains(var))
      {
         rtrn = (String) variables.remove(var);
         keys.remove(keys.indexOf(var));
      }

      return rtrn;
   }

   /**
    * add an R command to the "pre-command" list
    *
    * @param cmd R command to add to the "pre-command" list
    ********************************************************/
   protected void rCommandsPreAdd(String cmd)
   {
      rCommandsPre.add(cmd);
   }

   /**
    * add an R command to the "post-command" list
    *
    * @param cmd R command to add to the "post-command" list
    ********************************************************/
   protected void rCommandsPostAdd(String cmd)
   {
      rCommandsPost.add(cmd);
   }

   /**
    * add a List of R commands to the "pre-command" list
    *
    * @param cmd List of R commands to add
    ********************************************************/
   protected void rCommandsPreAdd(List cmd)
   {
      rCommandsPre.addAll(cmd);
   }

   /**
    * add a List of R commands to the "post-command" list
    *
    * @param cmd List of R commands to add
    ********************************************************/
   protected void rCommandsPostAdd(List cmd)
   {
      rCommandsPost.addAll(cmd);
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
