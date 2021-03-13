package com.github.emilienkia.ajmx.impl.entities;

import com.github.emilienkia.ajmx.annotations.MBean;
import com.github.emilienkia.ajmx.annotations.MBeanAttribute;

@MBean
public class Simple {

    @MBeanAttribute
    public int value;
}
