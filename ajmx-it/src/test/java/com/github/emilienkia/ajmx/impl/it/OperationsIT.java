package com.github.emilienkia.ajmx.impl.it;

import com.github.emilienkia.ajmx.AjmxAdaptor;
import com.github.emilienkia.ajmx.annotations.MBeanAttribute;
import com.github.emilienkia.ajmx.impl.entities.DomainAnnot;
import com.github.emilienkia.ajmx.impl.entities.DomainTypeAnnot;
import com.github.emilienkia.ajmx.impl.entities.EmptyAnnot;
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

import javax.inject.Inject;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
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
public class OperationsIT extends KarafTestSupport implements WithAssertions {

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

