package com.github.emilienkia.ajmx.impl.it;

import com.github.emilienkia.ajmx.AjmxAdaptor;
import com.github.emilienkia.ajmx.annotations.MBean;
import com.github.emilienkia.ajmx.annotations.MBeanAttribute;
import com.github.emilienkia.ajmx.exceptions.NotAnAMBean;
import com.github.emilienkia.ajmx.impl.entities.DomainAnnot;
import com.github.emilienkia.ajmx.impl.entities.DomainTypeAnnot;
import com.github.emilienkia.ajmx.impl.entities.DomainTypeNameAnnot;
import com.github.emilienkia.ajmx.impl.entities.EmptyAnnot;
import com.github.emilienkia.ajmx.impl.entities.NoAnnot;
import org.apache.karaf.itests.KarafTestSupport;
import org.assertj.core.api.WithAssertions;
import org.assertj.core.data.Offset;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

import javax.inject.Inject;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class AjmxAdaptorIT extends KarafTestSupport implements WithAssertions {

    @ProbeBuilder
    public final TestProbeBuilder probeConfiguration(final TestProbeBuilder probe) {
        probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*,org.apache.felix.service.*;status=provisional");
        return probe;
    }

    @Configuration
    public final Option[] config() {
        return OptionUtils.combine(
                super.config(),
                karafDistributionConfiguration()
                        .frameworkUrl(maven().groupId("org.apache.karaf").artifactId("apache-karaf").type("tar.gz").versionAsInProject())
                        .useDeployFolder(false)
                        .unpackDirectory(new File("target/pax-exam")),
                logLevel(LogLevelOption.LogLevel.INFO),
                keepRuntimeFolder(),

                // Install runtime features
                features(maven().groupId("org.apache.karaf.features").artifactId("standard").type("xml").classifier("features").versionAsInProject(), "standard"),
                features(maven().groupId("org.apache.karaf.features").artifactId("standard").type("xml").classifier("features").versionAsInProject(), "management"),

                // Test tooling
                mavenBundle().groupId("org.assertj").artifactId("assertj-core").versionAsInProject(),

                // Tested bundle
                mavenBundle().groupId("com.github.emilienkia").artifactId("ajmx-osgi").versionAsInProject()
        );
    }


    @Inject
    AjmxAdaptor server;

    @Inject
    MBeanServer mbeanServer;

    @Before
    public void setup() {
        assertThat(server).isNotNull();
        assertThat(mbeanServer).isNotNull();
    }

    @After
    public void cleanup() throws Exception {
        server.unregisterAllAMBeans();
    }

    @Test
    public void testOsgiServiceWithAnnot() throws Exception {
        DomainTypeNameAnnot obj = new DomainTypeNameAnnot();
        ServiceRegistration<DomainTypeNameAnnot> registration = bundleContext.registerService(DomainTypeNameAnnot.class, obj, null);

        ObjectName name = new ObjectName("this.is.test:type=MyType,name=AName");

        // Object is found and published
        ObjectInstance instance = mbeanServer.getObjectInstance(name);
        assertThat(instance).isNotNull();

        registration.unregister();

        // Object is not found anymore.
        Throwable thrown = catchThrowable(() -> mbeanServer.getObjectInstance(name));
        assertThat(thrown).isInstanceOf(InstanceNotFoundException.class);
    }

    @Test
    public void noAnnotTest() {
        NoAnnot obj = new NoAnnot();
        Throwable thrown = catchThrowable(() -> server.registerAMBean(obj));
        assertThat(thrown).isInstanceOf(NotAnAMBean.class);
    }

    @Test
    public void emptyAnnotTest() throws Exception {
        EmptyAnnot obj = new EmptyAnnot();
        server.registerAMBean(obj, "test");

        {
            // Bad name
            Throwable thrown = catchThrowable(() ->
                    mbeanServer.getObjectInstance(new ObjectName("com.github.emilienkia.ajmx.impl.entities:type=EmptyAnnot,name=unknownObject"))
            );
            assertThat(thrown).isInstanceOf(InstanceNotFoundException.class);
        }

        {
            // Bad type
            Throwable thrown = catchThrowable(() ->
                    mbeanServer.getObjectInstance(new ObjectName("com.github.emilienkia.ajmx.impl.entities:type=BadAnnot,name=test"))
            );
            assertThat(thrown).isInstanceOf(InstanceNotFoundException.class);
        }

        {
            // Bad namespace
            Throwable thrown = catchThrowable(() ->
                    mbeanServer.getObjectInstance(new ObjectName("com.github.emilienkia.badns:type=EmptyAnnot,name=test"))
            );
            assertThat(thrown).isInstanceOf(InstanceNotFoundException.class);
        }

        {
            ObjectInstance instance = mbeanServer.getObjectInstance(new ObjectName("com.github.emilienkia.ajmx.impl.entities:type=EmptyAnnot,name=test"));
            assertThat(instance).isNotNull();
        }

    }


    @Test
    public void domainAnnotTest() throws Exception {
        DomainAnnot obj = new DomainAnnot();
        server.registerAMBean(obj, "test");

        {
            // Bad name
            Throwable thrown = catchThrowable(() ->
                    mbeanServer.getObjectInstance(new ObjectName("this.is.test:type=DomainAnnot,name=unknownObject"))
            );
            assertThat(thrown).isInstanceOf(InstanceNotFoundException.class);
        }

        {
            // Bad type
            Throwable thrown = catchThrowable(() ->
                    mbeanServer.getObjectInstance(new ObjectName("this.is.test:type=BadAnnot,name=test"))
            );
            assertThat(thrown).isInstanceOf(InstanceNotFoundException.class);
        }

        {
            // Bad namespace
            Throwable thrown = catchThrowable(() ->
                    mbeanServer.getObjectInstance(new ObjectName("this.is.test.badns:type=DomainAnnot,name=test"))
            );
            assertThat(thrown).isInstanceOf(InstanceNotFoundException.class);
        }

        {
            ObjectName name = new ObjectName("this.is.test:type=DomainAnnot,name=test");
            ObjectInstance instance = mbeanServer.getObjectInstance(name);
            assertThat(instance).isNotNull();

            MBeanInfo info = mbeanServer.getMBeanInfo(name);
            assertThat(info.getDescription()).isNullOrEmpty();
        }
    }


    @Test
    public void domainTypeAnnotTest() throws Exception {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");

        {
            // Bad name
            Throwable thrown = catchThrowable(() ->
                    mbeanServer.getObjectInstance(new ObjectName("this.is.test:type=MyType,name=unknownObject"))
            );
            assertThat(thrown).isInstanceOf(InstanceNotFoundException.class);
        }

        {
            // Bad type
            Throwable thrown = catchThrowable(() ->
                    mbeanServer.getObjectInstance(new ObjectName("this.is.test:type=BadAnnot,name=test"))
            );
            assertThat(thrown).isInstanceOf(InstanceNotFoundException.class);
        }

        {
            // Bad namespace
            Throwable thrown = catchThrowable(() ->
                    mbeanServer.getObjectInstance(new ObjectName("this.is.test.badns:type=MyType,name=test"))
            );
            assertThat(thrown).isInstanceOf(InstanceNotFoundException.class);
        }

        {
            ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");
            ObjectInstance instance = mbeanServer.getObjectInstance(name);
            assertThat(instance).isNotNull();

            MBeanInfo info = mbeanServer.getMBeanInfo(name);
            assertThat(info.getDescription()).isNotNull()
                .isEqualTo(DomainTypeAnnot.class.getAnnotation(MBean.class).description());
        }
    }



    @Test
    public void domainTypeNameAnnotTest() throws Exception {
        DomainTypeNameAnnot obj = new DomainTypeNameAnnot();
        server.registerAMBean(obj);

        {
            // Bad name
            Throwable thrown = catchThrowable(() ->
                    mbeanServer.getObjectInstance(new ObjectName("this.is.test:type=MyType,name=unknownObject"))
            );
            assertThat(thrown).isInstanceOf(InstanceNotFoundException.class);
        }

        {
            // Bad type
            Throwable thrown = catchThrowable(() ->
                    mbeanServer.getObjectInstance(new ObjectName("this.is.test:type=BadAnnot,name=AName"))
            );
            assertThat(thrown).isInstanceOf(InstanceNotFoundException.class);
        }

        {
            // Bad namespace
            Throwable thrown = catchThrowable(() ->
                    mbeanServer.getObjectInstance(new ObjectName("this.is.test.badns:type=MyType,name=AName"))
            );
            assertThat(thrown).isInstanceOf(InstanceNotFoundException.class);
        }

        {
            ObjectInstance instance = mbeanServer.getObjectInstance(new ObjectName("this.is.test:type=MyType,name=AName"));
            assertThat(instance).isNotNull();
        }
    }

    @Test
    public void noAttributeTest() throws JMException {
        EmptyAnnot obj = new EmptyAnnot();
        server.registerAMBean(obj, "test");

        ObjectName name = new ObjectName("com.github.emilienkia.ajmx.impl.entities:type=EmptyAnnot,name=test");

        ObjectInstance instance = mbeanServer.getObjectInstance(name);
        assertThat(instance).isNotNull();

        MBeanInfo info = mbeanServer.getMBeanInfo(name);
        assertThat(info).isNotNull();
        assertThat(info.getAttributes()).isNotNull().isEmpty();
    }

    @Test
    public void attributeTest() throws Exception {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");

        ObjectInstance instance = mbeanServer.getObjectInstance(name);
        assertThat(instance).isNotNull();

        MBeanInfo info = mbeanServer.getMBeanInfo(name);
        assertThat(info).isNotNull();
        assertThat(info.getAttributes()).isNotNull().isNotEmpty();

    }

    @Test
    public void attributeBooleanTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");
        final String testedAttribute = "boolAttr";
        final Class<?> testedClass = Boolean.class;

        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass);
            assertThat((boolean) value).isTrue();
        }

        mbeanServer.setAttribute(name, new Attribute(testedAttribute, false));

        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass);
            assertThat((boolean) value).isFalse();
        }

        mbeanServer.setAttribute(name, new Attribute(testedAttribute, true));

        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass);
            assertThat((boolean) value).isTrue();
        }
    }

    @Test
    public void attributeByteTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");
        final String testedAttribute = "byteAttr";
        final Class<?> testedClass = Byte.class;

        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo((byte)2);
        }

        mbeanServer.setAttribute(name, new Attribute(testedAttribute, (byte)25));

        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo((byte)25);
        }
    }

    @Test
    public void attributeCharTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");
        final String testedAttribute = "charAttr";
        final Class<?> testedClass = Character.class;


        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo((char)'A');
        }

        mbeanServer.setAttribute(name, new Attribute(testedAttribute, (char)'a'));

        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo((char)'a');
        }
    }

    @Test
    public void attributeShortTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");
        final String testedAttribute = "shortAttr";
        final Class<?> testedClass = Short.class;


        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo((short)512);
        }

        mbeanServer.setAttribute(name, new Attribute(testedAttribute, (short)1024));

        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo((short)1024);
        }
    }

    @Test
    public void attributeIntegerTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");
        final String testedAttribute = "intAttr";
        final Class<?> testedClass = Integer.class;


        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo((int)25);
        }

        mbeanServer.setAttribute(name, new Attribute(testedAttribute, (int)65538));

        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo((int)65538);
        }
    }

    @Test
    public void attributeLongTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");
        final String testedAttribute = "longAttr";
        final Class<?> testedClass = Long.class;


        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo((long)123456789l);
        }

        mbeanServer.setAttribute(name, new Attribute(testedAttribute, (long)987654321l));

        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo((long)987654321l);
        }
    }

    @Test
    public void attributeFloatTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");
        final String testedAttribute = "floatAttr";
        final Class<?> testedClass = Float.class;


        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo((float) 1234.5f);
        }

        mbeanServer.setAttribute(name, new Attribute(testedAttribute, (float) 654.321f));

        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo((float) 654.321f);
        }
    }

    @Test
    public void attributeDoubleTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");
        final String testedAttribute = "doubleAttr";
        final Class<?> testedClass = Double.class;


        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo((double)1234567.89);
        }

        mbeanServer.setAttribute(name, new Attribute(testedAttribute, (double)98765.4321));

        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo((double)98765.4321);
        }
    }

    @Test
    public void attributeStringTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");
        final String testedAttribute = "strAttr";
        final Class<?> testedClass = String.class;


        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo("Toto");
        }

        mbeanServer.setAttribute(name, new Attribute(testedAttribute, "Paf"));

        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo("Paf");
        }
    }

    @Test
    public void attributeBigIntTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");
        final String testedAttribute = "biAttr";
        final Class<?> testedClass = BigInteger.class;


        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo(new BigInteger("1234567890123456789"));
        }

        mbeanServer.setAttribute(name, new Attribute(testedAttribute, new BigInteger("9876543210123456789")));

        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo(new BigInteger("9876543210123456789"));
        }
    }

    @Test
    public void attributeBigDecTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");
        final String testedAttribute = "bdAttr";
        final Class<?> testedClass = BigDecimal.class;


        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo(new BigDecimal("123456789.0123456789"));
        }

        mbeanServer.setAttribute(name, new Attribute(testedAttribute, new BigDecimal("987654321.0123456789")));

        {
            Object value = mbeanServer.getAttribute(name, testedAttribute);
            assertThat(value).isNotNull().isInstanceOf(testedClass).isEqualTo(new BigDecimal("987654321.0123456789"));
        }
    }

    @Test
    public void attributeDescriptionTest() throws JMException, NoSuchFieldException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");
        final String attributeName = "boolAttr";

        MBeanInfo mBeanInfo = mbeanServer.getMBeanInfo(name);
        assertThat(mBeanInfo).isNotNull();

        assertThat(
            Arrays.stream(mBeanInfo.getAttributes())
                    .filter(a -> a.getName().equals(attributeName))
                    .findAny()
                    .map( MBeanAttributeInfo::getDescription )
        ).isNotEmpty()
                .hasValue(DomainTypeAnnot.class.getDeclaredField(attributeName).getAnnotation(MBeanAttribute.class).description());
    }


    @Test
    public void attributeAccessReadOnlyTest() throws JMException {
        DomainAnnot obj = new DomainAnnot();
        server.registerAMBean(obj, "MyType", "accessRO");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=accessRO");
        final String attributeName = "readOnly";

        {
            assertThat(mbeanServer.getAttribute(name, attributeName))
                    .isNotNull()
                    .asInstanceOf(STRING).isEqualTo(DomainAnnot.READ_ONLY);
        }

        {
            final String OTHER_VALUE = "Other value";
            obj.setReadOnly(OTHER_VALUE);
            assertThat(mbeanServer.getAttribute(name, attributeName))
                    .isNotNull()
                    .asInstanceOf(STRING).isEqualTo(OTHER_VALUE);
        }

        {
            Throwable thrown = catchThrowable(() -> mbeanServer.setAttribute(name, new Attribute(attributeName, "Another value")));
            assertThat(thrown).isInstanceOf(AttributeNotFoundException.class);
        }
    }

    @Test
    public void attributeAccessWriteOnlyTest() throws JMException {
        DomainAnnot obj = new DomainAnnot();
        server.registerAMBean(obj, "MyType", "accessWO");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=accessWO");
        final String attributeName = "writeOnly";

        {
            assertThat(obj.getWriteOnly())
                    .isNotNull()
                    .isEqualTo(DomainAnnot.WRITE_ONLY);

            final String OTHER_VALUE = "Other value";
            mbeanServer.setAttribute(name, new Attribute(attributeName, OTHER_VALUE));

            assertThat(obj.getWriteOnly())
                    .isNotNull()
                    .isEqualTo(OTHER_VALUE);
        }

        {
            Throwable thrown = catchThrowable(() -> mbeanServer.getAttribute(name, attributeName));
            assertThat(thrown).isInstanceOf(AttributeNotFoundException.class);
        }
    }

    @Test
    public void attributeAccessReadWriteTest() throws JMException {
        DomainAnnot obj = new DomainAnnot();
        server.registerAMBean(obj, "MyType", "accessRW");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=accessRW");
        final String attributeName = "readWrite";

        {
            assertThat(mbeanServer.getAttribute(name, attributeName))
                    .isNotNull()
                    .asInstanceOf(STRING).isEqualTo(DomainAnnot.READ_WRITE);

            final String OTHER_VALUE = "Other value";
            mbeanServer.setAttribute(name, new Attribute(attributeName, OTHER_VALUE));

            assertThat(mbeanServer.getAttribute(name, attributeName))
                    .isNotNull()
                    .asInstanceOf(STRING).isEqualTo(OTHER_VALUE);
        }
    }


    @Test
    public void getAttributesTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "readAttributes");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=readAttributes");

        AttributeList attributes = mbeanServer.getAttributes(name, new String[]{"intAttr", "strAttr", "unknownAttr"});
        assertThat(attributes).isNotNull().isNotEmpty()
                .containsExactlyInAnyOrder(
                        new Attribute("intAttr", 25),
                        new Attribute("strAttr", "Toto")
                );
    }

    @Test
    public void setAttributesTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "writeAttributes");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=writeAttributes");

        AttributeList attributes = mbeanServer.setAttributes(name, new AttributeList(List.of(
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
    public void voidVoidOperationTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");
        Object res = mbeanServer.invoke(name, "voidVoidOperation", new Object[0], new String[0]);
        assertThat(res).isNull();
    }

    @Test
    public void stringStringOperationTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");

        Object[] params = new Object[] {
                "World"
        };
        String[] signature = new String[] {
                String.class.getName()
        };

        Object res = mbeanServer.invoke(name, "hello",params, signature);
        assertThat(res).isNotNull().isInstanceOf(String.class).asString().isNotEmpty();
    }


    @Test
    public void integerOperationTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");

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

        Object res = mbeanServer.invoke(name, "sumIntegers", params, signature);
        assertThat(res).isNotNull().isInstanceOf(BigInteger.class)
                .asInstanceOf(BIG_INTEGER).isEqualTo(-20);
    }

    @Test
    public void decimalOperationTest() throws JMException {
        DomainTypeAnnot obj = new DomainTypeAnnot();
        server.registerAMBean(obj, "test");
        final ObjectName name = new ObjectName("this.is.test:type=MyType,name=test");

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

        Object res = mbeanServer.invoke(name, "sumDecimals", params, signature);
        assertThat(res).isNotNull().isInstanceOf(Double.class)
                .asInstanceOf(DOUBLE).isEqualTo( 10.2 , Offset.offset(0.0001) );
    }
}

