package com.github.emilienkia.ajmx.impl;

import com.github.emilienkia.ajmx.annotations.MBeanAttribute;
import com.github.emilienkia.ajmx.impl.entities.DomainAnnot;
import com.github.emilienkia.ajmx.impl.entities.DomainTypeAnnot;
import com.github.emilienkia.ajmx.impl.entities.EmptyAnnot;
import org.assertj.core.api.WithAssertions;
import org.assertj.core.data.Offset;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class OperationsTest implements WithAssertions {

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
    public void voidVoidOperationTest() throws ReflectionException, MBeanException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        Object res = inst.invoke("voidVoidOperation", new Object[0], new String[0]);
        assertThat(res).isNull();
    }

    @Test
    public void stringStringOperationTest() throws ReflectionException, MBeanException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);

        Object[] params = new Object[] {
                "World"
        };
        String[] signature = new String[] {
                String.class.getName()
        };

        Object res = inst.invoke("hello", params, signature);
        assertThat(res).isNotNull().isInstanceOf(String.class).asString().isNotEmpty();
    }

    @Test
    public void integerOperationTest() throws ReflectionException, MBeanException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);

        Object[] params = new Object[] {
                Boolean.TRUE,
                Byte.valueOf("1"),
                Short.valueOf("2"),
                3,
                4l,
                BigInteger.TEN
        };
        String[] signature = new String[] {
                Boolean.class.getName(),
                Byte.class.getName(),
                Short.class.getName(),
                Integer.class.getName(),
                Long.class.getName(),
                BigInteger.class.getName()
        };

        Object res = inst.invoke("sumIntegers", params, signature);
        assertThat(res).isNotNull().isInstanceOf(BigInteger.class)
                .asInstanceOf(BIG_INTEGER).isEqualTo(-20);
    }

    @Test
    public void decimalOperationTest() throws ReflectionException, MBeanException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);

        Object[] params = new Object[] {
                1.2f,
                3.4,
                new BigDecimal("5.6")
        };
        String[] signature = new String[] {
                Float.class.getName(),
                Double.class.getName(),
                BigDecimal.class.getName()
        };

        Object res = inst.invoke("sumDecimals", params, signature);
        assertThat(res).isNotNull().isInstanceOf(Double.class)
                .asInstanceOf(DOUBLE).isEqualTo( 10.2 , Offset.offset(0.0001) );
    }
}
