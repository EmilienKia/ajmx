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
import org.junit.Before;
import org.junit.Test;

import javax.management.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class AjmxAdaptorImplTest implements WithAssertions {

    AjmxAdaptorImpl server;

    @Before
    public void setup() {
        server = new AjmxAdaptorImpl();
        assertThat(server).isNotNull();
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
    public void domainAndTypeandNameAnnotTest() throws JMException {
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

    @Test
    public void noAttributeTest() throws JMException {
        EmptyAnnot obj = new EmptyAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        assertThat(inst.getMBeanInfo().getAttributes())
                .isNullOrEmpty();
    }

    @Test
    public void attributeTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        assertThat(inst.getMBeanInfo().getAttributes())
                .isNotNull().isNotEmpty();
    }

    @Test
    public void attributeBooleanTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String testedAttribute = "boolAttr";
        Class<?> testedClass = Boolean.class;

        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo(Boolean.TRUE);

        inst.setAttribute(testedAttribute, false);
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo(Boolean.FALSE);

        inst.setAttribute(testedAttribute, true);
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void attributeByteTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String testedAttribute = "byteAttr";
        Class<?> testedClass = Byte.class;

        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((byte)2);

        inst.setAttribute(testedAttribute, (byte)25);
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((byte)25);

        inst.setAttribute(testedAttribute, (byte)2);
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((byte)2);
    }

    @Test
    public void attributeCharTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String testedAttribute = "charAttr";
        Class<?> testedClass = Character.class;

        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((char)'A');

        inst.setAttribute(testedAttribute, (char)'a');
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((char)'a');

        inst.setAttribute(testedAttribute, (char)'A');
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((char)'A');
    }

    @Test
    public void attributeShortTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String testedAttribute = "shortAttr";
        Class<?> testedClass = Short.class;

        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((short)512);

        inst.setAttribute(testedAttribute, (short)1024);
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((short)1024);

        inst.setAttribute(testedAttribute, (short)512);
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((short)512);
    }

    @Test
    public void attributeIntegerTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String testedAttribute = "intAttr";
        Class<?> testedClass = Integer.class;

        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((int)25);

        inst.setAttribute(testedAttribute, (int)65538);
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((int)65538);

        inst.setAttribute(testedAttribute, (int)25);
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((int)25);
    }

    @Test
    public void attributeLongTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String testedAttribute = "longAttr";
        Class<?> testedClass = Long.class;

        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((long)123456789l);

        inst.setAttribute(testedAttribute, (long)987654321l);
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((long)987654321l);

        inst.setAttribute(testedAttribute, (long)123456789l);
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((long)123456789l);
    }

    @Test
    public void attributeFloatTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String testedAttribute = "floatAttr";
        Class<?> testedClass = Float.class;

        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((float) 1234.5f);

        inst.setAttribute(testedAttribute, (float) 654.321f);
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((float) 654.321f);

        inst.setAttribute(testedAttribute, (float) 1234.5f);
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((float) 1234.5f);
    }

    @Test
    public void attributeDoubleTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String testedAttribute = "doubleAttr";
        Class<?> testedClass = Double.class;

        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((double)1234567.89);

        inst.setAttribute(testedAttribute, (double)98765.4321);
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((double)98765.4321);

        inst.setAttribute(testedAttribute, (double)1234567.89);
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo((double)1234567.89);
    }

    @Test
    public void attributeStringTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String testedAttribute = "strAttr";
        Class<?> testedClass = String.class;

        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo("Toto");

        inst.setAttribute(testedAttribute, "Paf");
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo("Paf");

        inst.setAttribute(testedAttribute, "Toto");
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo("Toto");
    }

    @Test
    public void attributeBigIntTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String testedAttribute = "biAttr";
        Class<?> testedClass = BigInteger.class;

        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo(new BigInteger("1234567890123456789"));

        inst.setAttribute(testedAttribute, new BigInteger("9876543210123456789"));
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo(new BigInteger("9876543210123456789"));

        inst.setAttribute(testedAttribute, new BigInteger("1234567890123456789"));
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo(new BigInteger("1234567890123456789"));
    }

    @Test
    public void attributeBigDecTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String testedAttribute = "bdAttr";
        Class<?> testedClass = BigDecimal.class;

        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo(new BigDecimal("123456789.0123456789"));

        inst.setAttribute(testedAttribute, new BigDecimal("987654321.0123456789"));
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo(new BigDecimal("987654321.0123456789"));

        inst.setAttribute(testedAttribute, new BigDecimal("123456789.0123456789"));
        assertThat(inst.getAttribute(testedAttribute)).isNotNull()
                .isInstanceOf(testedClass).isEqualTo(new BigDecimal("123456789.0123456789"));
    }

    @Test
    public void attributeDescriptionTest() throws JMException, NoSuchFieldException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String attributeName = "boolAttr";
        MBeanAttributeInfo attributeInfo = inst.getMBeanAttributeInfo(attributeName);

        assertThat(attributeInfo).isNotNull();
        assertThat(attributeInfo.getDescription()).isNotNull()
                .isEqualTo(DomainTypeAnnot.class.getDeclaredField(attributeName).getAnnotation(MBeanAttribute.class).description());
    }

    @Test
    public void attributeAccessReadOnlyTest() throws JMException {
        DomainAnnot obj = new DomainAnnot();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String attributeName = "readOnly";

        {
            assertThat(inst.getAttribute(attributeName))
                    .isNotNull()
                    .asInstanceOf(STRING).isEqualTo(DomainAnnot.READ_ONLY);
        }

        {
            final String OTHER_VALUE = "Other value";
            obj.setReadOnly(OTHER_VALUE);
            assertThat(inst.getAttribute(attributeName))
                    .isNotNull()
                    .asInstanceOf(STRING).isEqualTo(OTHER_VALUE);

        }

        {
            Throwable thrown = catchThrowable(() -> inst.setAttribute(attributeName, "Another value"));
            assertThat(thrown).isInstanceOf(AttributeNotFoundException.class);
        }
    }

    @Test
    public void attributeAccessWriteOnlyTest() throws JMException {
        DomainAnnot obj = new DomainAnnot();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String attributeName = "writeOnly";

        {
            assertThat(obj.getWriteOnly())
                    .isNotNull()
                    .isEqualTo(DomainAnnot.WRITE_ONLY);

            final String OTHER_VALUE = "Other value";
            inst.setAttribute(attributeName, OTHER_VALUE);

            assertThat(obj.getWriteOnly())
                    .isNotNull()
                    .isEqualTo(OTHER_VALUE);
        }

        {
            Throwable thrown = catchThrowable(() -> inst.getAttribute(attributeName));
            assertThat(thrown).isInstanceOf(AttributeNotFoundException.class);
        }
    }

    @Test
    public void attributeAccessReadWriteTest() throws JMException {
        DomainAnnot obj = new DomainAnnot();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        final String attributeName = "readWrite";

        {
            assertThat(inst.getAttribute(attributeName))
                    .isNotNull()
                    .asInstanceOf(STRING).isEqualTo(DomainAnnot.READ_WRITE);

            final String OTHER_VALUE = "Other value";
            inst.setAttribute(attributeName, OTHER_VALUE);

            assertThat(inst.getAttribute(attributeName))
                    .isNotNull()
                    .asInstanceOf(STRING).isEqualTo(OTHER_VALUE);
        }
    }

    @Test
    public void getAttributesTest() {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);

        AttributeList attributes = inst.getAttributes(new String[]{"intAttr", "strAttr", "unknownAttr"});
        assertThat(attributes).isNotNull().isNotEmpty()
                .containsExactlyInAnyOrder(
                        new Attribute("intAttr", 25),
                        new Attribute("strAttr", "Toto")
                );
    }

    @Test
    public void setAttributesTest() {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        Class<?> clazz = obj.getClass();
        AjmxAdaptorImpl.Instance inst = server.createInstance(obj, null, null);
        AttributeList attributes = inst.setAttributes(new AttributeList(List.of(
                new Attribute("intAttr", 42),
                new Attribute("strAttr", "Paf"),
                new Attribute("unknownAttr", 28),
                new Attribute("longAttr", "Bad value type")
                )));
        assertThat(attributes).isNotNull().isNotEmpty()
                .containsExactlyInAnyOrder(
                        new Attribute("intAttr", 42),
                        new Attribute("strAttr", "Paf")
                );
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