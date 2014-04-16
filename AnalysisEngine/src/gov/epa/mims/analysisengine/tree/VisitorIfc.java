package gov.epa.mims.analysisengine.tree;

/**
 * Reflective visitor interface functions
 *
 * @author    Tommy E. Cathey
 * @created   July 30, 2004
 * @version   $Id: VisitorIfc.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 */
public interface VisitorIfc
{
   /**
    * visit a Object
    *
    * @param o  Object to be visited
    * @return   an Object
    * @pre      o != null
    */
   Object visit(Object o);
}
