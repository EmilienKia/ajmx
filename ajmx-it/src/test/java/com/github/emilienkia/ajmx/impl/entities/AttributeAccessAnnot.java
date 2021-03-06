package com.github.emilienkia.ajmx.impl.entities;

import com.github.emilienkia.ajmx.annotations.MBean;
import com.github.emilienkia.ajmx.annotations.MBeanAttribute;

import static com.github.emilienkia.ajmx.annotations.MBeanAttribute.AccessMode.READ_ONLY;
import static com.github.emilienkia.ajmx.annotations.MBeanAttribute.AccessMode.WRITE_ONLY;

@MBean(domain = "this.is.test", description = "Attribute testings")
public class AttributeAccessAnnot {

    public AttributeAccessAnnot() {
    }


    public int intValue = 42;

    // Following attribute "methodReadOnly" is only readable

    @MBeanAttribute
    public int methodReadOnly() {
        return intValue;
    }

    // Following attrribute "methodWriteOnly" is only writable
    @MBeanAttribute
    public void methodWriteOnly(int val) {
        intValue = val;
    }


    // Following attribute "methodReadWrite" is read-write with methods

    @MBeanAttribute
    public int getMethodReadWrite() {
        return intValue;
    }

    @MBeanAttribute
    public void setMethodReadWrite(int newVal) {
        intValue = newVal;
    }


    // Following attribute "methodMixedReadWrite" is readable with method and writable with field

    @MBeanAttribute(accessMode = WRITE_ONLY)
    public int methodMixedReadWrite = 42;

    @MBeanAttribute
    public int getMethodMixedReadWrite() {
        return methodMixedReadWrite;
    }

    // Following attribute "methodMixedWriteRead" is readable with method and writable with field

    @MBeanAttribute(accessMode = READ_ONLY)
    public int methodMixedWriteRead = 42; // Following attribute "methodMixedWriteRead" is readable with method and writable with field

    @MBeanAttribute
    public void setMethodMixedWriteRead(int val) {
        methodMixedWriteRead = val;
    }



}
