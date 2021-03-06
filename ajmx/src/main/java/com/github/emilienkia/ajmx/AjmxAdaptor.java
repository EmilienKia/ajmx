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
