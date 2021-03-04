package com.github.emilienkia.ajmx.osgi;

import com.github.emilienkia.ajmx.AjmxAdaptor;
import com.github.emilienkia.ajmx.impl.AjmxAdaptorImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.MBeanServer;

public class AjmxActivator  implements BundleActivator {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected BundleContext context;

    protected ServiceTracker<MBeanServer, MBeanServer> mbeanServerServiceTracker;
    protected ServiceTracker<Object, Object> objectServiceTracker;

    protected AjmxAdaptorImpl amBeanServer = new AjmxAdaptorImpl();

    private ServiceRegistration<AjmxAdaptor> registration = null;

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        mbeanServerServiceTracker = new ServiceTracker<>(context, MBeanServer.class, null){
            @Override
            public synchronized MBeanServer addingService(ServiceReference<MBeanServer> reference) {
                MBeanServer server = super.addingService(reference);
                if(server!=null && shouldReplaceCurrentMBeanServer(reference)) {
                    replaceCurrent(server);
                }
                return server;
            }
            @Override
            public void modifiedService(ServiceReference<MBeanServer> reference, MBeanServer service) {
                // TODO
            }
            @Override
            public void removedService(ServiceReference<MBeanServer> reference, MBeanServer service) {
                super.removedService(reference, service);
                MBeanServer server = getService();
                if(server != null) {
                    replaceCurrent(server);
                } else {
                    removeCurrent();
                }
            }

            private void removeCurrent() {
                amBeanServer.assignMbeanServer(null);
                updateServiceRegtistration();
            }

            private void replaceCurrent(MBeanServer server) {
                amBeanServer.assignMbeanServer(server);
                updateServiceRegtistration();
            }

            private void updateServiceRegtistration() {
                boolean has = amBeanServer.hasMBeanServer();
                if(has && registration==null) {
                    registration = context.registerService(AjmxAdaptor.class, amBeanServer, null);
                } else if (!has && registration!=null) {
                    registration.unregister();
                    registration = null;
                }
            }

            private boolean shouldReplaceCurrentMBeanServer(ServiceReference<MBeanServer> newService) {
                ServiceReference<MBeanServer> current = getServiceReference();
                if (current == null) {
                    return true;
                }
                // Compare service ranking
                Object rankCurrentProperty = current.getProperty(Constants.SERVICE_RANKING);
                int currentRanking = (rankCurrentProperty instanceof Integer) ? ((Integer) rankCurrentProperty).intValue() : 0;
                Object rankNewProperty = newService.getProperty(Constants.SERVICE_RANKING);
                int newRanking = (rankNewProperty instanceof Integer) ? ((Integer) rankNewProperty).intValue() : 0;
                if (newRanking != currentRanking) {
                    return newRanking > currentRanking;
                }
                // Compare service id
                long currentId = ((Long) (current.getProperty(Constants.SERVICE_ID))).longValue();
                long newId = ((Long) (newService.getProperty(Constants.SERVICE_ID))).longValue();
                return newId < currentId;
            }

        };
        objectServiceTracker = new ServiceTracker<Object, Object>(context, context.createFilter("("+Constants.SERVICE_ID+"=*)"), null){
            @Override
            public Object addingService(ServiceReference<Object> reference) {
                Object service = super.addingService(reference);
                if(service!=null && amBeanServer.isAMBean(service.getClass())) {
                    // TODO Handle OSGi properties
                    try {
                        amBeanServer.registerAMBean(service);
                        return service;
                    } catch (JMException ex) {
                        logger.error("Error while registering object {}", service, ex);
                    }
                }
                // Do not track not-an-ambean object
                return null;
            }

            @Override
            public void modifiedService(ServiceReference<Object> reference, Object service) {
                // TODO
            }

            @Override
            public void removedService(ServiceReference<Object> reference, Object service) {
                super.removedService(reference, service);
                try {
                    amBeanServer.unregisterAMBean(service);
                } catch (JMException ex) {
                    logger.error("Error while unregistering object {}", service, ex);
                }
            }
        };
        mbeanServerServiceTracker.open(true);
        objectServiceTracker.open(true);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        objectServiceTracker.close();
        objectServiceTracker = null;
        mbeanServerServiceTracker.close();
        mbeanServerServiceTracker = null;
    }

}
