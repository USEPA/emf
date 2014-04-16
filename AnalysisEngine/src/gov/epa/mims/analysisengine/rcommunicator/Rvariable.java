package gov.epa.mims.analysisengine.rcommunicator;

import java.util.HashMap;


/**
 * DOCUMENT ME!
 *
 * @version $Revision: 1.2 $
 * @author $author$
 */
public class Rvariable implements java.io.Serializable
{
   /** DOCUMENT ME! */
   static final long serialVersionUID = 1;

   /** DOCUMENT ME! */
   private static HashMap key2variableMap = new HashMap();

   /** DOCUMENT ME! */
   private static long counter = 1;

   /**
    * DOCUMENT_ME
    */
   public static void reset()
   {
      key2variableMap.clear();
      counter = 1;
   }

   /**
    * DOCUMENT_ME
    *
    * @param key DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    */
   public static String getName(Object key)
   {
      String rVar = (String) key2variableMap.get(key);

      if (rVar == null)
      {
         rVar = "d" + counter++;
         key2variableMap.put(key, rVar);
      }

      return rVar;
   }

   /**
    * DOCUMENT_ME
    *
    * @param keys DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String[] getName(Object[] keys)
   {
      String[] rVars = new String[keys.length];

      for (int i = 0; i < keys.length; ++i)
      {
         rVars[i] = getName(keys[i]);
      }

      return rVars;
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
