# AJMX with OSGi Declarative Service annotations

This example demonstrates how to simply annotate and provides your AMbeans using OSGi Declarative Services annotations.

``DirectPublishing.java`` will show how an OSGi component can ben decalred as an AMBean. It will be automatically registered and published
if a JMX server is available.

``ManualPublishing.java`` will show how to manually register and publish an AMBean by referencing an ``AjmxAdaptor`` with 
DS ``@Reference`` annotation.

To run these example, you can type these following lines in a Karaf CLI:

    karaf@root()> feature:repo-add mvn:com.github.emilienkia.ajmx.examples/ajmx-examples-scr/LATEST/xml/features
    Adding feature url mvn:com.github.emilienkia.ajmx.examples/ajmx-examples-scr/LATEST/xml/features

    karaf@root()> feature:install ajmx-examples-scr


