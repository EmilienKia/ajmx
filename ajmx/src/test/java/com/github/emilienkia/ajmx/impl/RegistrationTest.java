package com.github.emilienkia.ajmx.impl;

import com.github.emilienkia.ajmx.annotations.MBean;
import com.github.emilienkia.ajmx.annotations.MBeanAttribute;
import com.github.emilienkia.ajmx.exceptions.NotAnAMBean;
import com.github.emilienkia.ajmx.impl.entities.DomainAnnot;
import com.github.emilienkia.ajmx.impl.entities.DomainTypeAnnot;
import com.github.emilienkia.ajmx.impl.entities.DomainTypeNameAnnot;
import com.github.emilienkia.ajmx.impl.entities.EmptyAnnot;
import com.github.emilienkia.ajmx.impl.entities.NoAnnot;
import org.assertj.core.api.WithAssertions;
import org.assertj.core.data.Offset;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.management.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class RegistrationTest implements WithAssertions {

    AjmxAdaptorImpl server;

    @Before
    public void setup() {
        server = new AjmxAdaptorImpl();
        assertThat(server).isNotNull();
    }

    @After
    public void after() {
        server = null;
    }

    @Test
    public void noAnnotTest() {
        NoAnnot obj = new NoAnnot();
        Throwable thrown = catchThrowable(() -> server.createInstance(obj, null, null));
        assertThat(thrown).isInstanceOf(NotAnAMBean.class);
    }

    @Test
    public void emptyAnnotTest() throws JMException {
        EmptyAnnot obj = new EmptyAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        assertThat(inst.getDomain()).isEqualTo(clazz.getPackage().getName());
        assertThat(inst.getType()).isEqualTo(clazz.getSimpleName());
        assertThat(inst.getName()).isNotEmpty().isNotBlank();
    }

    @Test
    public void domainAnnotTest() throws JMException {
        DomainAnnot obj = new DomainAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        assertThat(inst.getDomain())
                .isEqualTo(clazz.getAnnotation(MBean.class).domain())
                .isNotEqualTo(clazz.getPackage().getName());
        assertThat(inst.getType())
                .isEqualTo(clazz.getSimpleName());
        assertThat(inst.getName())
                .isNotEmpty().isNotBlank();
        assertThat(inst.getDescription())
                .isNullOrEmpty();
    }

    @Test
    public void domainAndTypeAnnotTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        assertThat(inst.getDomain())
                .isEqualTo(clazz.getAnnotation(MBean.class).domain())
                .isNotEqualTo(clazz.getPackage().getName());
        assertThat(inst.getType())
                .isEqualTo(clazz.getAnnotation(MBean.class).type())
                .isNotEqualTo(clazz.getSimpleName());
        assertThat(inst.getName())
                .isNotEmpty().isNotBlank();
        assertThat(inst.getDescription())
                .isNotNull().isNotEmpty().isNotBlank()
                .isEqualTo(DomainTypeAnnot.class.getAnnotation(MBean.class).description());
    }

    @Test
    public void domainAndTypeAndNameAnnotTest() throws JMException {
        DomainTypeNameAnnot obj = new DomainTypeNameAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        assertThat(inst.getDomain())
                .isEqualTo(clazz.getAnnotation(MBean.class).domain())
                .isNotEqualTo(clazz.getPackage().getName());
        assertThat(inst.getType())
                .isEqualTo(clazz.getAnnotation(MBean.class).type())
                .isNotEqualTo(clazz.getSimpleName());
        assertThat(inst.getName())
                .isEqualTo(clazz.getAnnotation(MBean.class).name());
    }

    @Test
    public void overrideNameTest() throws JMException {
        DomainTypeNameAnnot obj = new DomainTypeNameAnnot();
        Class<?> clazz = obj.getClass();
        String name = "ASpecificName";
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, name);
        assertThat(inst.getDomain())
                .isEqualTo(clazz.getAnnotation(MBean.class).domain())
                .isNotEqualTo(clazz.getPackage().getName());
        assertThat(inst.getType())
                .isEqualTo(clazz.getAnnotation(MBean.class).type())
                .isNotEqualTo(clazz.getSimpleName());
        assertThat(inst.getName())
                .isEqualTo(name)
                .isNotEqualTo(clazz.getAnnotation(MBean.class).name());
    }

    @Test
    public void overrideTypeTest() throws JMException {
        DomainTypeNameAnnot obj = new DomainTypeNameAnnot();
        Class<?> clazz = obj.getClass();
        String type = "ASpecificType";
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, type, null);
        assertThat(inst.getDomain())
                .isEqualTo(clazz.getAnnotation(MBean.class).domain())
                .isNotEqualTo(clazz.getPackage().getName());
        assertThat(inst.getType())
                .isEqualTo(type)
                .isNotEqualTo(clazz.getAnnotation(MBean.class).type())
                .isNotEqualTo(clazz.getSimpleName());
        assertThat(inst.getName())
                .isEqualTo(clazz.getAnnotation(MBean.class).name());
    }

    @Test
    public void overrideTypeAndNameTest() throws JMException {
        DomainTypeNameAnnot obj = new DomainTypeNameAnnot();
        Class<?> clazz = obj.getClass();
        String name = "ASpecificName";
        String type = "ASpecificType";
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, type, name);
        assertThat(inst.getDomain())
                .isEqualTo(clazz.getAnnotation(MBean.class).domain())
                .isNotEqualTo(clazz.getPackage().getName());
        assertThat(inst.getType())
                .isEqualTo(type)
                .isNotEqualTo(clazz.getAnnotation(MBean.class).type())
                .isNotEqualTo(clazz.getSimpleName());
        assertThat(inst.getName())
                .isEqualTo(name)
                .isNotEqualTo(clazz.getAnnotation(MBean.class).name());
    }

}
