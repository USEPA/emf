package gov.epa.mims.analysisengine.gui;

import java.awt.Component;
/*
 * ChildHaschangedListener.java
 *
 * Created on July 6, 2005, 9:45 AM
 * @author  Parthee R Partheepan
 */
public interface ChildHasChangedListener extends HasChangedListener
{
   public void setParentComponent(Component parent);
   
   public void setHasChanged(boolean hasChanged);
   
}
