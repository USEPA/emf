package gov.epa.mims.analysisengine.gui;

import java.io.Serializable;


/**
 * World coordinates class
 *
 * @author Alison Eyth
 * @version $Id: WorldCoordinates.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 *
 **/
public class WorldCoordinates implements Cloneable
{
   double xmin;
   double ymin;
   double xmax;
   double ymax;

   /**
    * constructor need for class.newInstance
    */
   public WorldCoordinates()
   {
     //text = null;
     //initialize();
   }
}

