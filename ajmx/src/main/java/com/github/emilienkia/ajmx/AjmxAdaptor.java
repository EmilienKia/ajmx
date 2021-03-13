package com.github.emilienkia.ajmx;

import com.github.emilienkia.ajmx.exceptions.NotAnAMBean;

import javax.management.JMException;
import javax.management.ObjectName;

/**
 * Interface of Annoted JMX server proxy.
 */
public interface AjmxAdaptor {

    /**
     * Test if an object is correctly annoted to be used as annoted MBean.
     * @param clazz Class to introspect.
     * @return true if the object class is correctly annoted.
     */
    boolean isAMBean(Class<?> clazz);

    /**
     * Test if the object is an already registered annoted MBean.
     * @param obj Object to look for.
     * @return True if already registered, false otherwise.
     */
    boolean hasAMBean(Object obj);

    /**
     * Retrieve the object registered under the specified name.
     * @param name Object name to look for. Shall be a fully specified name.
     * @return The corresponding object if found, null otherwise.
     */
    Object get(ObjectName name) throws JMException;

    /**
     * Register an object as annotated MBean.
     * @param obj Object to register.
     * @return JMX Object name onto which the AMBean is registered.
     * @throws NotAnAMBean Thrown if the object parameter is not an AMBean.
     */
    ObjectName registerAMBean(Object obj) throws JMException;

    /**
     * Register an object as annotated MBean to the specified name.
     * @param obj Object to register.
     * @param name Name of the object.
     * @return JMX Object name onto which the AMBean is registered.
     * @throws NotAnAMBean Thrown if the object parameter is not an AMBean.
     */
    ObjectName registerAMBean(Object obj, String name) throws JMException;

    /**
     * Register an object as annotated MBean to the specified type and name.
     * @param obj Object to register.
     * @param type Type of the AMBean.
     * @param name Name of the object.
     * @return JMX Object name onto which the AMBean is registered.
     * @throws NotAnAMBean Thrown if the object parameter is not an AMBean.
     */
    ObjectName registerAMBean(Object obj, String type, String name) throws JMException;

    /**
     * Replace the object registered wi the specified name by another object.
     * New object shall strictly have the same type than the replaced object.
     * @param name Name of the object to replace.
     * @param other New object to set with this name.
     * @return Replaced object.
     */
    Object replaceAMBean(ObjectName name, Object other) throws JMException;

    /**
     * Replace an object by another, keeping exactly the same name.
     * Both object shall strictly have the same type.
     * @param old Object to replace.
     * @param other NEw object to set.
     * @return Object name of the old and new objects.
     */
    ObjectName replaceAMBean(Object old, Object other) throws JMException;

    /**
     * Unregister an AMBean based on its object name.
     * @param objName Object name of the AMBean to unregister.
     */
    void unregisterAMBean(ObjectName objName) throws JMException;

    /**
     * Unregister an AMBean.
     * @param obj AMBean to unregister.
     */
    void unregisterAMBean(Object obj) throws JMException;

    /**
     * Unregister all AMBeans.
     * @throws JMException
     */
    void unregisterAllAMBeans() throws JMException;
}
