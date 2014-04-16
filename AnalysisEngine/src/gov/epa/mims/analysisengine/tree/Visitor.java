package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

import java.lang.reflect.Method;

/***********************************************************************************************************************
 * DOCUMENT_ME
 * 
 * @version $Revision: 1.3 $
 * @author $author$
 **********************************************************************************************************************/
public class Visitor implements Serializable, VisitorIfc {
	/*******************************************************************************************************************
	 * Creates a new Visitor object.
	 ******************************************************************************************************************/
	public Visitor() {
		// Empty
	}

	/*******************************************************************************************************************
	 * reflectively invoke the correct visit routine
	 * 
	 * @param Object
	 *            the Visitable Object passed from the Visitable's accept() method
	 ******************************************************************************************************************/
	public Object visit(Object o) {
		/** require [Valid_Object] o != null; * */
		Object rtrnObj = null;
		String methodName = "visit";

		try {
			// Get the method visit(Foo foo)
			Method m = getClass().getMethod(methodName, new Class[] { o.getClass() });

			// System.out.println("Invoking method: " + m);
			// Try to invoke visit(Foo foo)
			rtrnObj = m.invoke(this, new Object[] { o });
		} catch (NoSuchMethodException e) {
			// No method, so do the default implementation
			e.printStackTrace();
		} catch (java.lang.IllegalAccessException e) {
			e.printStackTrace();
		} catch (java.lang.reflect.InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			e.printStackTrace();
		}

		return rtrnObj;
	}
}