package com.github.emilienkia.ajmx.examples.scr;

import com.github.emilienkia.ajmx.AjmxAdaptor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.management.JMException;

@Component(immediate = true)
public class ManualPublishing {

    @Reference
    AjmxAdaptor adaptor;

    MyBean bean = new MyBean();

    @Activate
    void start() throws JMException {
        adaptor.registerAMBean(bean, "my-bean");
    }

    @Deactivate
    void stop() throws JMException {
        adaptor.unregisterAMBean(bean);
    }

}
