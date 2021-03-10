package com.github.emilienkia.ajmx.examples.scr;

import com.github.emilienkia.ajmx.annotations.MBean;
import com.github.emilienkia.ajmx.annotations.MBeanAttribute;
import com.github.emilienkia.ajmx.annotations.MBeanOperation;

@MBean
public class MyBean {

    /**
     * Read-only field view of the accumulator attribute.
     */
    @MBeanAttribute
    int accumulator = 0;

    /**
     * Write method view of the accumulator attribute.
     */
    @MBeanAttribute
    public void setAccumulator(int newValue) {
        accumulator = newValue;
    }

    /**
     * Operation.
     */
    @MBeanOperation
    public int add(int num) {
        accumulator += num;
        return accumulator;
    }

}
