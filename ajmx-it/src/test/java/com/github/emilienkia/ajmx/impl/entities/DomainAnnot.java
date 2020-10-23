package com.github.emilienkia.ajmx.impl.entities;

import com.github.emilienkia.ajmx.annotations.MBean;
import com.github.emilienkia.ajmx.annotations.MBeanAttribute;

@MBean(domain = "this.is.test")
public class DomainAnnot {

    public DomainAnnot() {
    }

    public static final String READ_ONLY = "ReadOnly";
    public static final String READ_WRITE = "ReadWrite";
    public static final String WRITE_ONLY = "WriteOnly";

    @MBeanAttribute
    String readOnly = READ_ONLY;

    @MBeanAttribute(accessMode = MBeanAttribute.AccessMode.READ_WRITE)
    String readWrite = READ_WRITE;

    @MBeanAttribute(accessMode = MBeanAttribute.AccessMode.WRITE_ONLY)
    String writeOnly = WRITE_ONLY;


    public String getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(String readOnly) {
        this.readOnly = readOnly;
    }

    public String getReadWrite() {
        return readWrite;
    }

    public void setReadWrite(String readWrite) {
        this.readWrite = readWrite;
    }

    public String getWriteOnly() {
        return writeOnly;
    }

    public void setWriteOnly(String writeOnly) {
        this.writeOnly = writeOnly;
    }
}
