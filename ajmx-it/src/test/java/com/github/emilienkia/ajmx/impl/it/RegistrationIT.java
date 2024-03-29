package com.github.emilienkia.ajmx.impl.it;

import com.github.emilienkia.ajmx.AjmxAdaptor;
import com.github.emilienkia.ajmx.annotations.MBean;
import com.github.emilienkia.ajmx.exceptions.NotAnAMBean;
import com.github.emilienkia.ajmx.impl.entities.DomainAnnot;
import com.github.emilienkia.ajmx.impl.entities.DomainTypeAnnot;
import com.github.emilienkia.ajmx.impl.entities.DomainTypeNameAnnot;
import com.github.emilienkia.ajmx.impl.entities.EmptyAnnot;
import com.github.emilienkia.ajmx.impl.entities.NoAnnot;
import com.github.emilienkia.ajmx.impl.entities.Simple;
import org.apache.karaf.itests.KarafTestSupport;
import org.assertj.core.api.WithAssertions;
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
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.io.File;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class RegistrationIT extends KarafTestSupport implements WithAssertions {

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
    public void unregisterObjectTest() throws Exception {

        DomainTypeNameAnnot obj = new DomainTypeNameAnnot();
        ObjectName name = server.registerAMBean(obj);

        assertThat(mbeanServer.getObjectInstance(name)).isNotNull();

        server.unregisterAMBean(obj);

        Throwable thrown = catchThrowable(() -> mbeanServer.getObjectInstance(name));
        assertThat(thrown).isInstanceOf(JMException.class);

    }

    @Test
    public void unregisterObjectNameTest() throws Exception {

        DomainTypeNameAnnot obj = new DomainTypeNameAnnot();
        ObjectName name = server.registerAMBean(obj);

        assertThat(mbeanServer.getObjectInstance(name)).isNotNull();

        server.unregisterAMBean(name);

        Throwable thrown = catchThrowable(() -> mbeanServer.getObjectInstance(name));
        assertThat(thrown).isInstanceOf(JMException.class);

    }


    @Test
    public void unregisterUnknownObjectNameTest() throws Exception {

        DomainTypeNameAnnot obj = new DomainTypeNameAnnot();
        ObjectName name = server.registerAMBean(obj);

        assertThat(mbeanServer.getObjectInstance(name)).isNotNull();

        Throwable thrown = catchThrowable(() -> server.unregisterAMBean(new ObjectName("this.is.test", "MyType", "AnotherName")));
        assertThat(thrown).isInstanceOf(InstanceNotFoundException.class);

    }

    @Test
    public void unregisterObjectNamePatternTest() throws Exception {
        Simple simple1 = new Simple();
        simple1.value = 1;
        Simple simple2 = new Simple();
        simple2.value = 2;
        Simple simple3 = new Simple();
        simple3.value = 3;

        ObjectName name1 = server.registerAMBean(simple1, "simple1");
        ObjectName name2 = server.registerAMBean(simple2, "simple2");
        ObjectName name3 = server.registerAMBean(simple3, "notsimple");

        System.out.println("name1 = " + name1.toString());
        System.out.println("name2 = " + name2.toString());
        System.out.println("name3 = " + name3.toString());

        assertThat(mbeanServer.getObjectInstance(name1)).isNotNull();
        assertThat(mbeanServer.getObjectInstance(name2)).isNotNull();
        assertThat(mbeanServer.getObjectInstance(name3)).isNotNull();

        server.unregisterAMBeans(new ObjectName("com.github.emilienkia.ajmx.impl.entities:type=Simple,name=simple*"));

        Throwable thrown1 = catchThrowable(() -> mbeanServer.getObjectInstance(name1));
        assertThat(thrown1).isInstanceOf(InstanceNotFoundException.class);

        Throwable thrown2 = catchThrowable(() -> mbeanServer.getObjectInstance(name2));
        assertThat(thrown2).isInstanceOf(InstanceNotFoundException.class);

        assertThat(mbeanServer.getObjectInstance(name3)).isNotNull();
    }


    @Test
    public void testReplaceByName() throws JMException {
        Simple simple1 = new Simple();
        simple1.value = 1;
        Simple simple2 = new Simple();
        simple2.value = 2;

        ObjectName name = server.registerAMBean(simple1, "simple1");
        assertThat(name).isNotNull();
        assertThat(mbeanServer.getObjectInstance(name)).isNotNull();

        Object value = mbeanServer.getAttribute(name, "value");
        assertThat(value).isNotNull().isInstanceOf(Integer.class).isEqualTo(simple1.value);

        Object ret = server.replaceAMBean(name, simple2);

        assertThat(ret).isNotNull().isEqualTo(simple1);

        value = mbeanServer.getAttribute(name, "value");
        assertThat(value).isNotNull().isInstanceOf(Integer.class).isEqualTo(simple2.value);

    }

    @Test
    public void testReplaceByObject() throws JMException {
        Simple simple1 = new Simple();
        simple1.value = 1;
        Simple simple2 = new Simple();
        simple2.value = 2;

        ObjectName name = server.registerAMBean(simple1, "simple1");
        assertThat(name).isNotNull();
        assertThat(mbeanServer.getObjectInstance(name)).isNotNull();

        Object value = mbeanServer.getAttribute(name, "value");
        assertThat(value).isNotNull().isInstanceOf(Integer.class).isEqualTo(simple1.value);

        server.replaceAMBean(simple1, simple2);

        value = mbeanServer.getAttribute(name, "value");
        assertThat(value).isNotNull().isInstanceOf(Integer.class).isEqualTo(simple2.value);

    }

}

