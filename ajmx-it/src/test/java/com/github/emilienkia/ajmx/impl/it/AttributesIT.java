package com.github.emilienkia.ajmx.impl.it;

import com.github.emilienkia.ajmx.AjmxAdaptor;
import com.github.emilienkia.ajmx.annotations.MBean;
import com.github.emilienkia.ajmx.annotations.MBeanAttribute;
import com.github.emilienkia.ajmx.exceptions.NotAnAMBean;
import com.github.emilienkia.ajmx.impl.AjmxAdaptorImpl;
import com.github.emilienkia.ajmx.impl.entities.AttributeAccessAnnot;
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
public class AttributesIT extends KarafTestSupport implements WithAssertions {

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
                mavenBundle().groupId("com.github.emilienkia.ajmx").artifactId("ajmx-osgi").versionAsInProject()
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
    public void getMethodReadOnlyAttributeTest() throws JMException {
        AttributeAccessAnnot obj = new AttributeAccessAnnot();
        server.registerAMBean(obj, "readOnlyAttribute");
        final ObjectName name = new ObjectName("this.is.test:type=AttributeAccessAnnot,name=readOnlyAttribute");

        assertThat(mbeanServer.getAttribute(name, "methodReadOnly"))
                .isNotNull()
                .asInstanceOf(INTEGER).isEqualTo(42);


        Throwable thrown = catchThrowable(() -> mbeanServer.setAttribute(name, new Attribute("methodReadOnly", 28)));
        assertThat(thrown).isInstanceOf(AttributeNotFoundException.class).hasMessageContaining("is not writable");
    }


    @Test
    public void getMethodWriteOnlyAttributeTest() throws JMException {
        AttributeAccessAnnot obj = new AttributeAccessAnnot();
        server.registerAMBean(obj, "writeOnlyAttribute");
        final ObjectName name = new ObjectName("this.is.test:type=AttributeAccessAnnot,name=writeOnlyAttribute");

        assertThat(obj.intValue).isEqualTo(42);
        mbeanServer.setAttribute(name, new Attribute("methodWriteOnly", 28));
        assertThat(obj.intValue).isEqualTo(28);


        Throwable thrown = catchThrowable(() -> mbeanServer.getAttribute(name, "methodWriteOnly"));
        assertThat(thrown).isInstanceOf(AttributeNotFoundException.class).hasMessageContaining("is not readable");
    }

    @Test
    public void getMethodReadWriteAttributeTest() throws JMException {
        AttributeAccessAnnot obj = new AttributeAccessAnnot();
        server.registerAMBean(obj, "readWriteAttribute");
        final ObjectName name = new ObjectName("this.is.test:type=AttributeAccessAnnot,name=readWriteAttribute");

        assertThat(obj.intValue).isEqualTo(42);
        assertThat(mbeanServer.getAttribute(name, "methodReadWrite"))
                .isNotNull()
                .asInstanceOf(INTEGER).isEqualTo(42);

        mbeanServer.setAttribute(name, new Attribute("methodReadWrite", 28));
        assertThat(obj.intValue).isEqualTo(28);

        assertThat(mbeanServer.getAttribute(name, "methodReadWrite"))
                .isNotNull()
                .asInstanceOf(INTEGER).isEqualTo(28);
    }

    @Test
    public void getMethodMixedReadWriteAttributeTest() throws JMException {
        AttributeAccessAnnot obj = new AttributeAccessAnnot();
        server.registerAMBean(obj, "mixedReadWriteAttribute");
        final ObjectName name = new ObjectName("this.is.test:type=AttributeAccessAnnot,name=mixedReadWriteAttribute");

        assertThat(obj.methodMixedReadWrite).isEqualTo(42);
        assertThat(mbeanServer.getAttribute(name, "methodMixedReadWrite"))
                .isNotNull()
                .asInstanceOf(INTEGER).isEqualTo(42);

        mbeanServer.setAttribute(name, new Attribute("methodMixedReadWrite", 28));
        assertThat(obj.methodMixedReadWrite).isEqualTo(28);

        assertThat(mbeanServer.getAttribute(name, "methodMixedReadWrite"))
                .isNotNull()
                .asInstanceOf(INTEGER).isEqualTo(28);
    }

    @Test
    public void getMethodMixedWriteReadAttributeTest() throws JMException {
        AttributeAccessAnnot obj = new AttributeAccessAnnot();
        server.registerAMBean(obj, "mixedWriteWriteAttribute");
        final ObjectName name = new ObjectName("this.is.test:type=AttributeAccessAnnot,name=mixedWriteWriteAttribute");

        assertThat(obj.methodMixedWriteRead).isEqualTo(42);
        assertThat(mbeanServer.getAttribute(name, "methodMixedWriteRead"))
                .isNotNull()
                .asInstanceOf(INTEGER).isEqualTo(42);

        mbeanServer.setAttribute(name, new Attribute("methodMixedWriteRead", 28));
        assertThat(obj.methodMixedWriteRead).isEqualTo(28);

        assertThat(mbeanServer.getAttribute(name, "methodMixedWriteRead"))
                .isNotNull()
                .asInstanceOf(INTEGER).isEqualTo(28);
    }

}

