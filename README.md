# AJMX - Annotation-based JMX adaptor

[AJMX](https://github.com/EmilienKia/ajmx) is a library providing an adaptor to allow publishing simply annotated POJO
as JMX monitored beans without having to implement any MBean interface nor deal with Dynamic, Model or Open MBean complexity.

## Quick start

Just annotate your Java class:

    @MBean(domain = "this.is.test", type="MyType", name = "AName")
    public class MyClass {
    
        @MBeanAttribute(name = "attrib", description = "This is an integer attribute")
        int anAttribute = 25;

        @MBeanOperation(name="hello", description = "Say 'hello' to someone.")
        String sayHello(
            @MBeanOperationParam(name = "name", description = "Who to say hello.")
            String name
            ) {
                return "Hello " + name + " !";
        }
    }

Then, using an existing ``MBeanServer`` instantiate a ``AjmxAdaptor``and register an instance of your object:

    MBeanServer mbeanServer = ...;
    AjmxAdaptor adaptor = new AjmxAdaptorImpl(mbeanServer);
    
    MyClass obj1 = new MyClass();
    adaptor.registerAMBean(obj1);
    
    MyClass obj2 = new MyClass();
    adaptor.registerAMBean(obj2, "AnotherName"); // Register another instance with an alternative name

## How to build
AJMX is a pure maven-based Java project.
Just type ``mvn install`` to build, self test and install the library locally.

At runtime, AJMX only depends on SLF4J API.
At unit test time, AJMX requires JUnit and AssertJ. At integration test time, it also requires Pax-Exam and few other OSGi and test-related packages.
All these dependencies are fetched by maven.

## How to annotate your classes

### Declare your AMBeans
To let your classes be recognized as annoted management beans (AMBean), just put the ``@MBean`` annotation to your class.
You can specify the domain, the type and the name of your AMBean directly in the annotation.

    @MBean(domain = "this.is.test", type="MyType", name = "AName")
    public class MyClass

If not present, the adaptor will use the class package as domain, the class name as type and the object hash code as name.
Moreover, you can specify a name and/or a type directly when registering your object. When explicitly specified at registration, types and names take precedence over annotation declaration. This is particularly usefull to distingusih names or contexts when registering many instances of the same class, as JMX requires MBeans have strictly different ObjectName.

### Declare attributes
To add a JMX attribute, just annotate a class field with ``@MBeanAttribute``.

	@MBeanAttribute(name = "attrib", description = "This is an integer attribute", accessMode = READ_WRITE)
	int intAttr = 25;

This annotation allows to specify the name and the description of the attribute. If the ``name`` annotation property is not specified, the attribute takes the name of the Java field.
You can also specify the access mode to the attribute. By default, an attribute is read-only, but you can set it at read-only, read-write or write-only.

### Declare operations
To add a JMX operation, just annotate the method to invoke with ``@MBeanOperation``. Method return and parameters types will be automatically mapped to the operation. You can specify the method name and description. As attributes, If name parameter is ommited, the operation will use the Java method name. You may also specify the impact of the operation on the bean by specifying if the operation is an action, an information, both or if impact is unknwon.
You can also add name and description meta-data to parameters by annotating them with ``@MBeanOperationParam``.

    @MBeanOperation(name="hello", description = "Say hello to someone", impact = MBeanOperation.Impact.ACTION)
    String sayHello(
            @MBeanOperationParam(name = "name", description = "Who to say hello.")
                    String name
    ) {
        return "Hello " + name + " !";
    }

## Limitations

### Wrapped types
Currently, AMBeans can only use simple types for attributes and methods return and parameters types.
Theses types are:
* Primitive types and their wrappers
* String and Date
* BigInteger and BigDecimal
* Arrays and Collections of all previous types

### Supported features
Currently AJMX only support attributes and operations but not events nor advanced meta-data.
AJMX also only support one MBeanServer at a time. If you want to publish AMbeans on many MBeanServers, you have to create one AjmxAdaptor per MBeanServer and register your object on both.

## OSGi support
AJMX also provides an OSGi-specific package. It advantageously replaces the standard package and don't need the standard package to run.
To use AJMX on a bundle, you can build with standard AJMX package at compile time, and use the OSGi package at runtime.

AJMX-OSGi bundle provides an ``AjmxAdaptor`` service you can require to declare your AMBeans.
The bundle also listen for an ``MBeanServer`` service. If no MBeanServer is supposed to be registered into OSGi service registry, you can also set one manually at your convenience.
Finally AJMX-OSGi bundle is listening for all registred services and, if they are annotated correctly, will register them as all other AMBeans.

In consequence, if you are in a full OSGi environment providing an MBeanServer, and you just want to expose services objects, you just have to annotate them and publish them as services.

This AJMX(OSGi have been design for and tested with [Apache Karaf](https://karaf.apache.org) runtime with ``management`` feature.

### Karaf support
AJMX provides a Karaf features.xml file to easily provision it.
You can reference it on your own features repository file.

Or you can install it, through command-line by adding the features.xml as features repository and install the feature:

    karaf@root()> feature:repo-add mvn:com.github.emilienkia/ajmx-osgi/LATEST/xml/features
    Adding feature url mvn:com.github.emilienkia/ajmx-osgi/LATEST/xml/features
    
    karaf@root()> feature:install ajmx-osgi 





