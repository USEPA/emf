package gov.epa.mims.analysisengine.gui;

import java.io.*;

/**
 * Hold the default interaction method.
 *
 * <p>
 * This is initialized to a StreamUserInteractor.
 *
 * @author Steve Fine
 * @version $Id: DefaultUserInteractor.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 *
 **/

public class DefaultUserInteractor
{

   /******************************************************
    *
    * fields
    *
    *****************************************************/
   
   /**
    * The default interface method -- used when we can't or don't want to
    * identify a source.
    **/
   static UserInteractor defaultUserInteractor = new StreamUserInteractor(
      System.in, System.out, System.err);
   
   
   
   /******************************************************
    *
    * static methods
    *
    *****************************************************/
   
   /**
    * Set the default interaction method  -- used when we can't or don't want to
    * identify a source.
    *
    * @author Steve Fine
    *
    * @param method UserInteractor that will be the new default
    ******************************************************************************/
   public static void set(UserInteractor method)
   {
      defaultUserInteractor = method;
   }
   
   
   /**
    * Get the default command source  -- used when we can't or don't want to
    * identify a source.
    *
    * @author Steve Fine
    *
    * @return default source
    ******************************************************************************/
   public static UserInteractor get()
   {
      return defaultUserInteractor;
   }
   
   
   public static void main(String args[])
   {
      DefaultUserInteractor.get().notify(null,"Title", "This is a note on standard output.",
         UserInteractor.NOTE);
           DefaultUserInteractor.set(new GUIUserInteractor());
           DefaultUserInteractor.get().notify(null,"Via GUI",
                                      "This is a note via the GUI", UserInteractor.NOTE);
           System.exit(0);
   }
   
}
   
