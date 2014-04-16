
package gov.epa.mims.analysisengine.gui;

/*
 * HasChangedListener.java
 * To notify the changes from parent to child component
 * and child to parent component
 * Should be implemented by parent and child component should implement ChildHasChangedListener
 * for this to work
 *
 * @author  Parthee Partheepan
 */
public interface HasChangedListener
{
   
   public void update();
   
}
