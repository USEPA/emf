/*
 * reusable-components
 * Copyright (C) 2003  Michael Nascimento Santos
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * @version $Id: Enum.java,v 1.1 2005/12/21 16:26:55 parthee Exp $
 */

package gov.epa.emissions.commons;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a type-safe Enum, similar to C enums. Its main characteristics
 * are:
 * <ul>
 * <li>All instances contain a name that should be unique per subclass</li>
 * <li>All instances contain a sequential number whose value is depends on the
 * declaration order</li>
 * <li>A instance can be retrieved given its class and its name</li>
 * <li>All the values of an Enum subclass may be retrieved given the subclass
 * </li>
 * <li>All instances may be compared using only the <code>==</code> operator
 * </li>
 * <li>All instances are serializable; special care is taken so that
 * desserialized instances don't contain wrong values for their
 * <code>ordinal</code> property and don't break <code>==</code>-based
 * comparisson</li>
 * <li>Instances cannot be cloned as it would break the way <code>==</code>
 * works</li>
 * <li>Enum instances are comparable based on their <code>ordinal</code>
 * value</li>
 * <li><code>equals</code> and <code>hashCode</code> are overwritten in a
 * way that allows quick comparissons when needed</li>
 * </ul>
 * <p>
 * A subclass may be implemented as below:
 * </p>
 * 
 * <pre>
 * public final class ButtonState extends Enum {
 *     public static final ButtonState OFF = new ButtonState(&quot;Off&quot;);
 * 
 *     public static final ButtonState ON = new ButtonState(&quot;On&quot;);
 * 
 *     private ButtonState(String name) {
 *         super(name);
 *     }
 * }
 * </pre>
 * 
 * <p>
 * Subclasses should be final, as most methods provided in this superclass won't
 * work for subclasses of subclasses.
 * </p>
 * <p>
 * If you find yourself casting the return of
 * {@link #get(java.lang.Class, java.lang.String)} or calling
 * {@link #getInstances(java.lang.Class)} repeatedly, the following idiom is
 * recommended:
 * 
 * <pre>
 * public final class ButtonState extends Enum {
 *     public static final ButtonState OFF = new ButtonState(&quot;Off&quot;);
 * 
 *     public static final ButtonState ON = new ButtonState(&quot;On&quot;);
 * 
 *     private ButtonState(String name) {
 *         super(name);
 *     }
 * 
 *     public static java.util.List getInstances() {
 *         return getInstances(ButtonState.class);
 *     }
 * 
 *     public static ButtonState get(String name) {
 *         return (ButtonState) get(ButtonState.class, name);
 *     }
 * }
 * </pre>
 * 
 * <p>
 * There is special support for cases when Enum instances must be, for example,
 * instances of anonymous inner subclasses of a "real" Enum subclass. To get
 * more information on that, check {@link #getBaseClass()}.
 * </p>
 * 
 * @author mister__m
 */
public abstract class Enum implements Comparable, Serializable {
    /**
     * The "magic" field used by Java serialization mechanism.
     */
    private static final long serialVersionUID = -3531218321798293077L;

    /**
     * Keeps the next sequential value for a specific class.
     */
    private static final Map ordinalPerClass = new HashMap();

    /**
     * Holds references to all the instances of a subclass.
     */
    private static final Map instancesPerClass = new HashMap();

    /**
     * Helps to find instances based on their class and name.
     */
    private static final Map instancesPerNameAndClass = new HashMap();

    /**
     * The sequential value assigned to the instance during its initialization.
     * It is unique per subclass and is properly handled when subclasses are
     * restored and their instances' declaration order changed in the class
     * body.
     */
    private final transient int ordinal;

    /**
     * The unique name per subclass assigned to the current instance.
     */
    private final String name;

    /**
     * Creates a new Enum instance.
     * 
     * @param name
     *            unique name per subclass.
     * @exception NullPointerException
     *                if the <code>name</code> is <code>null</code>
     * @exception IllegalArgumentException
     *                if the <code>name</code> is not unique
     */
    protected Enum(final String name) {
        if (name == null) {
            throw new NullPointerException();
        }

        this.name = name.intern();
        checkAndAddInstance();

        ordinal = getNextOrdinal(getBaseClass());
    }

    /**
     * Verify if this instance complies to the unique naming rule and then adds
     * it to the required <code>Map</code>s.
     * 
     * @exception IllegalArgumentException
     *                if there is already an instance of with the same name
     */
    private void checkAndAddInstance() {
        checkBaseClass();
        final Map instancesPerName = getInstancesPerName(getBaseClass());
        if (instancesPerName.get(name) != null) {
            throw new IllegalArgumentException(name);
        }
        instancesPerName.put(name, this);
        getInstances(getBaseClass()).add(this);
    }

    /**
     * Returns an existent or initializes a new <code>Map</code> used to
     * associate instances with their unique name.
     * 
     * @param clazz
     *            the Enum subclass we want the <code>Map</code> to hold
     *            instances
     * @return Map that associates instances with their unique name
     */
    private static Map getInstancesPerName(final Class clazz) {
        Map instancesPerName = null;
        synchronized (instancesPerNameAndClass) {
            instancesPerName = (Map) instancesPerNameAndClass.get(clazz);

            if (instancesPerName == null) {
                instancesPerName = new IdentityHashMap();
                instancesPerNameAndClass.put(clazz, instancesPerName);
            }
        }
        return instancesPerName;
    }

    /**
     * Returns all the available instances of a Enum subclass.
     * 
     * @param clazz
     *            subclass of Enum whose instances are to be returned
     * @return Collection of available instances
     * @exception NullPointerException
     *                if <code>clazz</code> is <code>null</code>
     * @exception IllegalArgumentException
     *                if <code>clazz</code> is not a subclass of
     *                <code>Enum</code>
     */
    public static java.util.List getInstances(final Class clazz) {
        checkClass(clazz);
        java.util.List instances = null;
        synchronized (instancesPerClass) {
            instances = (java.util.List) instancesPerClass.get(clazz);
            if (instances == null) {
                instances = new ArrayList();
                instancesPerClass.put(clazz, instances);
            }
        }
        return instances;
    }

    /**
     * Return the next sequential number for <code>clazz</code>
     * 
     * @param clazz
     *            a subclass of Enum
     * @return int the next ordinal for <code>clazz</code>
     */
    private static int getNextOrdinal(final Class clazz) {
        Integer ordinal = null;
        synchronized (ordinalPerClass) {
            ordinal = (Integer) ordinalPerClass.get(clazz);
            if (ordinal == null) {
                ordinal = new Integer(0);
            } else {
                ordinal = new Integer(ordinal.intValue() + 1);
            }
            ordinalPerClass.put(clazz, ordinal);
        }
        return ordinal.intValue();
    }

    /**
     * Gets the instance of <code>clazz</code> whose name is <code>name</code>
     * 
     * @param clazz
     *            a subclass of Enum
     * @param name
     *            the name of the instance searched
     * @return the searched instance, if it exists; <code>null</code>
     *         otherwise
     * @exception NullPointerException
     *                if <code>clazz</code> or <code>name</code> are
     *                <code>null</code>
     * @exception IllegalArgumentException
     *                if <code>clazz</code> is not a subclass of
     *                <code>Enum</code>
     */
    public static Enum get(final Class clazz, final String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        checkClass(clazz);
        return (Enum) getInstancesPerName(clazz).get(name.intern());
    }

    /**
     * Checks if <code>clazz</code> is a valid Enum subclass.
     * 
     * @exception NullPointerException
     *                if <code>clazz</code> is <code>null</code>
     * @exception IllegalArgumentException
     *                if <code>clazz</code> is not a subclass of
     *                <code>Enum</code>
     */
    private static void checkClass(final Class clazz) {
        if (clazz == null) {
            throw new NullPointerException();
        }
        if (!Enum.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("clazz: " + clazz.getName().toString());
        }
    }

    /**
     * Checks if the Class instance returned by <code>getBaseClass()</code> is
     * valid.
     * 
     * @exception NullPointerException
     *                if the base class is <code>null</code>
     * @exception IllegalArgumentException
     *                if the base class is not a subclass of <code>Enum</code>
     *                or a super class of this instance's class
     */
    private void checkBaseClass() {
        final Class clazz = getBaseClass();
        checkClass(clazz);
        if (!clazz.isAssignableFrom(getClass())) {
            throw new IllegalArgumentException("clazz: " + clazz.getName().toString());
        }
    }

    /**
     * Checks if this instance is of the same class as <code>o</code>.
     * 
     * @return <code>true</code> if the base class of this instance is a super
     *         class of <code>o</code>'s class and if they have the same base
     *         class
     */
    private boolean compareClasses(final Object o) {
        final Class clazz = getBaseClass();
        return clazz.isAssignableFrom(o.getClass()) && clazz.isAssignableFrom(((Enum) o).getBaseClass());
    }

    /**
     * 
     * Gets the name for this instance.
     * 
     * @return the unique name assigned during instantiation
     */
    public final String getName() {
        return name;
    }

    /**
     * Gets the ordinal for this instance.
     * 
     * @return the unique ordinal automatically assigned during instantiation
     */
    public final int getOrdinal() {
        return ordinal;
    }

    /**
     * Returns the base class of this instance. It is useful when an object is
     * an instance of anonymous inner class that descends from an Enum class.
     * This allows constructions like the following to work:
     * 
     * <pre>
     * public class LogicalOperator extends Enum {
     *     public static final LogicalOperator AND = new LogicalOperator(&quot;and&quot;) {
     *         public boolean evaluate(boolean firstCondition, boolean secondCondition) {
     *             return firstCondition &amp;&amp; secondCondition;
     *         }
     *     };
     * 
     *     public static final LogicalOperator OR = new LogicalOperator(&quot;or&quot;) {
     *         public boolean evaluate(boolean firstCondition, boolean secondCondition) {
     *             return firstCondition || secondCondition;
     *         }
     *     };
     * 
     *     // More code here ...
     * 
     *     private LogicalOperator(String name) {
     *         super(name);
     *     }
     * 
     *     public abstract boolean evaluate(boolean firstCondition, boolean secondCondition);
     * 
     *     public Class getBaseClass() {
     *         return LogicalOperator.class;
     *     }
     * }
     * </pre>
     * 
     * @return the logical Enum base class for this instance; default value is a
     *         call to <code>getClass()</code>
     */
    public Class getBaseClass() {
        return getClass();
    }

    /**
     * Overrides equals to make it final. A simple <code>==</code> comparisson
     * is done for testing equality.
     * 
     * @param other
     *            the other instance to be compared with
     * @return <code>boolean</code> indicating equality
     */
    public final boolean equals(final Object other) {
        return this == other;
    }

    /**
     * Overrides hashCode so it is consistent with equals. It simply invokes
     * <code>System.identityHashCode(this)</code>.
     * 
     * @return the hashCode value for this instance.
     */
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * Converts this Enum toString by simply returning its name
     * 
     * @return name
     */
    public String toString() {
        // return this.getClass().getName() + "." + this.getName();
        return name;
    }

    /**
     * Compares this instance with another. The comparisson criteria is the
     * ordinal, in ascending order
     * 
     * @param o
     *            another instance of the same class to be compared to
     * @return -1, if <code>o</code> has a ordinal greather than this
     *         object's; 0, if they are the same; 1, if this instance has a
     *         greater value for ordinal than <code>o</code>
     * @exception ClassCastException
     *                if <code>o</code> is not of the same class as
     *                <code>this</code>
     */
    public int compareTo(final Object o) {
        if (!compareClasses(o)) {
            throw new ClassCastException(o.getClass().getName());
        }
        final int otherOrdinal = ((Enum) o).ordinal;
        return (ordinal == otherOrdinal) ? 0 : ((ordinal > otherOrdinal) ? 1 : -1);
    }

    /**
     * Throws CloneNotSupportedException. This guarantees that enums are never
     * cloned, which is necessary to preserve their "singleton" status.
     * 
     * @return (never returns)
     */
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * "Magic" method called by the desserialization process, overriden in order
     * to mantain the <code>==</code> operator working. It also allows ordinal
     * to have the correct value after declaration changes.
     * 
     * @return instance already existent that represents this one
     * @exception InvalidObjectException
     *                if the desserizalized instance does not exist anymore
     */
    protected final Object readResolve() throws ObjectStreamException {
        final Enum toReturn = (Enum) getInstancesPerName(getBaseClass()).get(name.intern());
        if (toReturn == null) {
            throw new InvalidObjectException(name);
        }
        return toReturn;
    }

    /**
     * Get a List of all names in the enumeration.
     * 
     * @return A List of all names of the enumerated items.
     */
    public static List getAllNames(final Class clazz) {
        List names = new ArrayList();
        for (Iterator it = getInstances(clazz).iterator(); it.hasNext();) {
            names.add(((Enum) it.next()).getName());
        }
        return names;
    }
}
