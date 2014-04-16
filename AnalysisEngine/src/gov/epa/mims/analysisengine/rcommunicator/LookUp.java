package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.DateDataSetIfc;
import gov.epa.mims.analysisengine.tree.Text;

import java.awt.Color;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import java.util.HashMap;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;



/**
 * DOCUMENT ME!
 *
 * @version $Revision: 1.2 $
 * @author $author$
 */
public class LookUp implements java.io.Serializable,
gov.epa.mims.analysisengine.tree.MarginConstantsIfc,
gov.epa.mims.analysisengine.tree.CompassConstantsIfc,
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc,
gov.epa.mims.analysisengine.tree.BoxPlotConstantsIfc,
gov.epa.mims.analysisengine.tree.FontConstantsIfc,
gov.epa.mims.analysisengine.tree.LineTypeConstantsIfc,
gov.epa.mims.analysisengine.tree.PageConstantsIfc,
gov.epa.mims.analysisengine.tree.SymbolsConstantsIfc,
gov.epa.mims.analysisengine.tree.TextBoxConstantsIfc,
gov.epa.mims.analysisengine.tree.UnitsConstantsIfc
{
   /** DOCUMENT ME! */
   static final long serialVersionUID = 1;

   static HashMap hash = new HashMap();

   static private LookUp instance = new LookUp();

   // static initialization block
   {

      put(RIGHT_HAND_MARGIN,"maRight");
      put(LEFT_HAND_MARGIN,"maLeft");
      put(TOP_HAND_MARGIN,"maTop");
      put(BOTTOM_HAND_MARGIN,"maBot");
      put(PLOT_REGION,"plot");
      put(REFERENCE_LINE,"REFLINE");
      put(REGRESSION_LINE,"REGRESSION_LINE");

      put(NORTHWEST,"NW");
      put(NORTH,"N");
      put(NORTHEAST,"NE");
      put(WEST,"W");
      put(CENTER,"C");
      put(EAST,"E");
      put(SOUTHWEST,"SW");
      put(SOUTH,"S");
      put(SOUTHEAST,"SE");

      put(PLAIN_TEXT,"1");
      put(BOLD_TEXT,"2");
      put(ITALIC_TEXT,"3");
      put(BOLD_ITALIC_TEXT,"4");
   }

   private void LookUp(){}

   static private void put(String key, String val)
   {
      if( hash.containsKey(key) )
      {
         throw new IllegalArgumentException("LookUp: key="+key+" is already in the hash");
      }
      hash.put(key,val);
   }

   static public String get(String s)
   {
//System.out.println( "LookUp: s = " + s);
//Set keys = hash.keySet();
//System.out.println( "hash = " + hash);
//System.out.println( "keys = " + keys);
//Iterator iter = keys.iterator();
//while(iter.hasNext())
//{
//   Object key = iter.next();
//   System.out.println(hash.get(key));
//}
      return (String)hash.get(s);
   }
}

