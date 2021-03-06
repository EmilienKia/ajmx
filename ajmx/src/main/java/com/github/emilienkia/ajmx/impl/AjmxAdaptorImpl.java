package com.github.emilienkia.ajmx.impl;

import com.github.emilienkia.ajmx.AjmxAdaptor;
import com.github.emilienkia.ajmx.annotations.MBean;
import com.github.emilienkia.ajmx.annotations.MBeanAttribute;
import com.github.emilienkia.ajmx.annotations.MBeanOperation;
import com.github.emilienkia.ajmx.annotations.MBeanOperationParam;
import com.github.emilienkia.ajmx.exceptions.AttributeDescriptorException;
import com.github.emilienkia.ajmx.exceptions.NotAnAMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AjmxAdaptorImpl implements AjmxAdaptor {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    MBeanServer mbeanServer = null;

    Map<Object, Instance> ambeans = new HashMap<>();

    Map<Class<?>, ClassDescriptor> classDescs = new HashMap<>();

    public AjmxAdaptorImpl() {
    }

    public AjmxAdaptorImpl(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    public MBeanServer assignMbeanServer(MBeanServer server) {
        MBeanServer old = mbeanServer;
        if(mbeanServer!=null && mbeanServer!=server) {
            for(Instance bean : ambeans.values()) {
                try {
                    mbeanServer.unregisterMBean(bean.getObjectName());
                } catch (JMException ex) {
                    logger.error("Problem when unregistering object '{}'", bean);
                }
            }
        }
        mbeanServer = server;
        if(server!=null) {
            for(Instance bean : ambeans.values()) {
                try {
                    mbeanServer.registerMBean(bean, bean.getObjectName());
                } catch (JMException ex) {
                    logger.error("Problem when registering object '{}'", bean);
                }
            }
        }
        return old;
    }

    public boolean hasMBeanServer() {
        return mbeanServer != null;
    }

    ClassDescriptor getDescriptor(Class<?> clazz) {
        if(!classDescs.containsKey(clazz)) {
            return introspect(clazz);
        } else {
            return classDescs.get(clazz);
        }
    }

    ClassDescriptor introspect(Class<?> clazz) {
        if(clazz==null) {
            return null;
        }
        MBean annot = clazz.getAnnotation(MBean.class);
        if(annot==null) {
            return null;
        }

        ClassDescriptor desc = new ClassDescriptor(clazz, annot);
        classDescs.put(clazz, desc);
        return desc;
    }

    @Override
    public boolean isAMBean(Class<?> clazz) {
        return getDescriptor(clazz) != null;
    }

    @Override
    public boolean hasAMBean(Object obj) {
        return ambeans.get(obj)!=null;
    }

    @Override
    public void registerAMBean(Object obj) throws JMException {
        registerAMBean(obj, null, null);
    }

    @Override
    public void registerAMBean(Object obj, String name) throws JMException {
        registerAMBean(obj, null, name);
    }

    @Override
    public void registerAMBean(Object obj, String type, String name) throws JMException {
        Instance instance = createInstance(obj, type, name);
        logger.info("Register MBean : {}", instance.getObjectName());
        try {
            if(mbeanServer!=null) {
                mbeanServer.registerMBean(instance, instance.getObjectName());
            }
            ambeans.put(obj, instance);
        } catch (MalformedObjectNameException | MBeanRegistrationException | InstanceAlreadyExistsException | NotCompliantMBeanException ex) {
            logger.error("Error while creating new ABean for object {} of type {}", obj, obj.getClass().getName(), ex);
        }
    }

    Instance createInstance(Object obj, String type, String name) {
        ClassDescriptor desc = getDescriptor(obj.getClass());
        if(desc==null) {
            logger.error("Object {} of class {} is not an AMbean", obj, obj.getClass().getName());
            throw new NotAnAMBean();
        }
        return new Instance(obj, desc, type, name);
    }

    @Override
    public void unregisterAMBean(Object obj) throws JMException {
        Instance instance = ambeans.get(obj);
        if (instance!=null) {
            logger.info("Unregister mbean : {}", instance);
            if(mbeanServer!=null) {
                mbeanServer.unregisterMBean(instance.getObjectName());
            }
            ambeans.remove(obj);
        }
    }

    @Override
    public void unregisterAllAMBeans() {
        if(mbeanServer!=null) {
            for (Instance instance : ambeans.values()) {
                try {
                    mbeanServer.unregisterMBean(instance.getObjectName());
                } catch (Exception e) {
                    logger.error("Error while unregistering an ambean {}", instance, e);
                }
            }
        }
        ambeans.clear();
    }

    static String getTypeName(Class<?> clazz) {
        return clazz.getTypeName();
    }

    enum MethodKind{
        Getter, Setter, Other
    }

    static MethodKind detectMethodKind(Method method) {
        if(method.getParameterCount() == 0 && method.getReturnType() != Void.class) {
            return MethodKind.Getter;
        } else if (method.getParameterCount() == 1) {
            return MethodKind.Setter;
        } else {
            return MethodKind.Other;
        }
    }

    private static MBeanParameterInfo introspectParameter(int idx, Class<?> type, Annotation[] annots) {
        MBeanOperationParam paramAnnot = Arrays.stream(annots)
                .map(MBeanOperationParam.class::cast)
                .filter(Objects::nonNull)
                .findAny().orElse(null);

        String name = null;
        String description = null;
        if(paramAnnot!=null) {
            name = paramAnnot.name();
            description = paramAnnot.description();
        }
        if(name==null || name.isEmpty()) {
            name = "param" + idx;
        }

        return new MBeanParameterInfo(name, getTypeName(type), description);
    }

    private static String toCamelCase(final String str) {
        String first = ""+str.charAt(0);
        return first.toLowerCase()+str.substring(1);
    }

    class ClassDescriptor {

        public class AttributeDescriptor {
            Field field;
            MBeanAttribute fieldAttr;

            Method getMethod;
            Method setMethod;
            String name;
            String description;
            Class<?> type;

            MBeanAttributeInfo info;

            public AttributeDescriptor(Field field, MBeanAttribute attr) {
                introspectField(field, attr);
            }

            public AttributeDescriptor(Method method, MBeanAttribute attr) {
                switch(detectMethodKind(method)) {
                    case Getter:
                        introspectGetterMethod(method, attr);
                        break;
                    case Setter:
                        introspectSetterMethod(method, attr);
                        break;
                    default:
                        throw new AttributeDescriptorException("Method '"+method.getName()+"' is not a getter nor a setter.");
                }
            }

            public void merge(AttributeDescriptor other) {
                if(!getName().equals(other.getName())) {
                    throw new IllegalArgumentException("Cant merge attributes having different names");
                }
                if(!getType().equals(other.getType())) {
                    throw new IllegalArgumentException("Cant merge attributes having different types");
                }

                if(other.field!=null) {
                    if(this.field!=null) {
                        throw new IllegalArgumentException("Cant merge attributes having both field");
                    } else {
                        this.field = other.field;
                        this.fieldAttr = other.fieldAttr;
                    }
                }
                if(other.getMethod!=null) {
                    if(this.getMethod!=null) {
                        throw new IllegalArgumentException("Cant merge attributes having both get method");
                    } else {
                        this.getMethod = other.getMethod;
                    }
                }
                if(other.setMethod!=null) {
                    if(this.setMethod!=null) {
                        throw new IllegalArgumentException("Cant merge attributes having both set method");
                    } else {
                        this.setMethod = other.setMethod;
                    }
                }

                if(other.description!=null && this.description==null) {
                    this.description = other.description;
                }

                updateInfo();
            }

            void updateInfo() {
                info = new MBeanAttributeInfo(this.name, getTypeName(type), this.description,
                        canRead(), canWrite(), getType()==Boolean.class && canRead());
            }

            protected void introspectField(Field field, MBeanAttribute attr) {
                this.field = field;
                this.fieldAttr = attr;
                field.setAccessible(true);

                if(!attr.name().isEmpty()) {
                    this.name = attr.name();
                } else {
                    this.name = field.getName();
                }

                if(!attr.description().isEmpty()) {
                    this.description = attr.description();
                }

                type = field.getType();

                updateInfo();
            }

            protected void introspectGetterMethod(Method method, MBeanAttribute attr) {
                getMethod = method;
                getMethod.setAccessible(true);

                if(!attr.name().isEmpty()) {
                    this.name = attr.name();
                } else {
                    String name = getMethod.getName();
                    if(name.startsWith("get") && name.length()>3) {
                        this.name = toCamelCase(name.substring(3));
                    } else if(name.startsWith("is") && name.length()>2 && getMethod.getReturnType()==Boolean.class) {
                        this.name = toCamelCase(name.substring(2));
                    } else {
                        this.name = name;
                    }
                }

                if(!attr.description().isEmpty()) {
                    this.description = attr.description();
                }

                type = getMethod.getReturnType();

                updateInfo();
            }

            protected void introspectSetterMethod(Method method, MBeanAttribute attr) {
                setMethod = method;
                setMethod.setAccessible(true);

                if(!attr.name().isEmpty()) {
                    this.name = attr.name();
                } else {
                    String name = setMethod.getName();
                    if(name.startsWith("set") && name.length()>3) {
                        this.name = toCamelCase(name.substring(3));
                    } else {
                        this.name = name;
                    }
                }

                if(!attr.description().isEmpty()) {
                    this.description = attr.description();
                }

                type = setMethod.getParameterTypes()[0];

                info = new MBeanAttributeInfo(this.name, getTypeName(type), this.description,
                        false, true,false);
            }

            public String getName() {
                return name;
            }

            public String getDescription() {
                return description;
            }

            public Class<?> getType() {
                return type;
            }

            public MBeanAttributeInfo getInfo() {
                return info;
            }

            public boolean canRead() {
                return (field != null && MBeanAttribute.Helpers.canRead(fieldAttr))
                        || (getMethod != null);
            }

            public boolean canWrite() {
                return (field != null && MBeanAttribute.Helpers.canWrite(fieldAttr))
                        || (setMethod != null);
            }

            public Object getValue(Object obj) throws MBeanException, ReflectionException, AttributeNotFoundException {
                if(getMethod!=null) {
                    try {
                        return getMethod.invoke(obj);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new MBeanException(e);
                    }
                } else if(field!=null) {
                    if (MBeanAttribute.Helpers.canRead(fieldAttr)) {
                        try {
                            return field.get(obj);
                        } catch (IllegalArgumentException e) {
                            throw new ReflectionException(e);
                        } catch (Exception e) {
                            throw new MBeanException(e);
                        }
                    }
                }
                throw new AttributeNotFoundException("Attribute " + this.name + " is not readable.");
            }

            public void setValue(Object obj, Object value) throws InvalidAttributeValueException, MBeanException, ReflectionException, AttributeNotFoundException {
                if(setMethod!=null) {
                    try {
                        setMethod.invoke(obj, value);
                        return;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new MBeanException(e);
                    }
                } else if(field!=null) {
                    if (MBeanAttribute.Helpers.canWrite(fieldAttr)) {
                        try {
                            field.set(obj, value);
                            return;
                        } catch (IllegalArgumentException e) {
                            throw new ReflectionException(e);
                        } catch (IllegalAccessException e) {
                            throw new InvalidAttributeValueException();
                        } catch (Exception e) {
                            throw new MBeanException(e);
                        }
                    }
                }
                throw new AttributeNotFoundException("Attribute " + this.name + " is not writable.");
            }
        }

        public class OperationDescriptor {
            MBeanOperation op;
            Method method;
            String name;
            String description;

            MBeanOperationInfo info;

            public OperationDescriptor(Method method, MBeanOperation op) {
                this.method = method;
                this.op = op;
                introspect();
            }

            private MBeanParameterInfo[] introspectSignature() {
                Class<?>[] parameterTypes = method.getParameterTypes();
                Annotation[][] annotations = method.getParameterAnnotations();
                List<MBeanParameterInfo> params = new ArrayList<>();
                for(int p=0; p<method.getParameterCount(); p++) {
                    params.add(introspectParameter(p, parameterTypes[p], annotations[p]));
                }
                return params.toArray(new MBeanParameterInfo[params.size()]);
            }

            protected void introspect() {
                method.setAccessible(true);

                if(!op.name().isEmpty()) {
                    name = op.name();
                } else {
                    name = method.getName();
                }

                if(!op.description().isEmpty()) {
                    description = op.description();
                }

                info = new MBeanOperationInfo(name, description, introspectSignature(),
                        getTypeName(method.getReturnType()), op.impact().value());
            }

            public String getName() {
                return name;
            }

            public String getDescription() {
                return description;
            }

            public MBeanOperationInfo getInfo() {
                return info;
            }

            public Object invoke(Object obj, Object... params) throws InvocationTargetException, IllegalAccessException {
                return method.invoke(obj, params);
            }
        }

        Class<?> clazz;
        MBean annot;
        String domain = null;
        MBeanInfo info = null;

        Map<String, AttributeDescriptor> attributes = new HashMap<>();
        Map<String, OperationDescriptor> operations = new HashMap<>();


        public ClassDescriptor(Class<?> clazz, MBean annot) {
            this.clazz = clazz;
            this.annot = annot;
            introspect();
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public MBean getAnnot() {
            return annot;
        }

        public String getDescription() {
            return annot != null ? annot.description() : "";
        }

        public String getDomain() {
            if(domain==null) {
                if(annot.domain()!=null && !annot.domain().isEmpty()) {
                    domain = annot.domain();
                } else {
                    domain = clazz.getPackage().getName();
                }
            }
            return domain;
        }

        public Collection<String> getAttributeNames() {
            return attributes.keySet();
        }

        public AttributeDescriptor getAttribute(final String name) {
            return attributes.get(name);
        }

        public Object getAttributeValue(String name, Object obj) throws AttributeNotFoundException, MBeanException, ReflectionException {
            AttributeDescriptor attr = attributes.get(name);
            if(attr != null) {
                return attr.getValue(obj);
            } else {
                throw new AttributeNotFoundException();
            }
        }

        public void setAttributeValue(String name, Object obj, Object value) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
            AttributeDescriptor attr = attributes.get(name);
            if(attr != null) {
                attr.setValue(obj, value);
            } else {
                throw new AttributeNotFoundException();
            }
        }

        public Object invoke(String name, Object obj, Object ... params) throws MBeanException {
            OperationDescriptor operation = operations.get(name);
            if(operation==null) {
                throw new IllegalArgumentException("Operation '" + name + "' not found for object of type '" + obj.getClass().getName() + "'");
            }
            try {
                return operation.invoke(obj, params);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new MBeanException(e);
            }
        }

        void introspect() {
            // Look for annotated attributes.
            for (Field field : clazz.getDeclaredFields()) {
                MBeanAttribute attr = field.getAnnotation(MBeanAttribute.class);
                if(attr!=null) {
                    AttributeDescriptor desc = new AttributeDescriptor(field, attr);
                    attributes.merge(desc.getName(), desc, (attr1, attr2)->{attr1.merge(attr2); return attr1;});
                }
            }
            for (Method method : clazz.getDeclaredMethods()) {
                MBeanAttribute attr = method.getAnnotation(MBeanAttribute.class);
                if(attr!=null) {
                    AttributeDescriptor desc = new AttributeDescriptor(method, attr);
                    attributes.merge(desc.getName(), desc, (attr1, attr2)->{attr1.merge(attr2); return attr1;});
                }
            }

            // Look for annotated operations.
            for (Method method : clazz.getDeclaredMethods()) {
                MBeanOperation op = method.getAnnotation(MBeanOperation.class);
                if(op!=null) {
                    OperationDescriptor desc = new OperationDescriptor(method, op);
                    operations.put(desc.getName(), desc);
                }
            }

            info = new MBeanInfo(clazz.getName(), getDescription(),
                    attributes.values().stream().map(AttributeDescriptor::getInfo).toArray(size -> new MBeanAttributeInfo[size]),
                    new MBeanConstructorInfo[]{},
                    operations.values().stream().map(OperationDescriptor::getInfo).toArray(size -> new MBeanOperationInfo[size]),
                    new MBeanNotificationInfo[]{}
            );
        }

        public MBeanInfo getMBeanInfo() {
            if(info == null) {
                introspect();
            }
            return info;
        }
    }

    public class Instance implements DynamicMBean  {

        Object object;
        ClassDescriptor descriptor;
        String type;
        String name;

        public Instance(Object object, ClassDescriptor descriptor, String type, String name) {
            this.object = object;
            this.descriptor = descriptor;
            this.type = type;
            this.name = name;
        }

        public Instance(Object object, ClassDescriptor descriptor, String name) {
            this(object, descriptor, null, name);
        }

        public Instance(Object object, ClassDescriptor descriptor) {
            this(object, descriptor, null, null);
        }

        public String getDomain() {
            return descriptor.getDomain();
        }

        public String getType() {
            if(type == null || type.isEmpty()) {
                type = descriptor.getAnnot().type();
            }
            if(type == null || type.isEmpty()) {
                type = descriptor.getClazz().getSimpleName();
            }
            return type;
        }

        public String getName() {
            if(name==null || name.isEmpty()) {
                name = descriptor.getAnnot().name();
            }
            if(name==null || name.isEmpty()) {
                name = Integer.toString(object.hashCode());
            }
            return name;
        }

        public String getDescription() {
            return descriptor.getDescription();
        }

        String buildObjectName() {
            return getDomain() + ":type=" + getType() + ",name=" + getName();
            // TODO Add additional data to object name
        }

        public ObjectName getObjectName() throws MalformedObjectNameException {
            return new ObjectName(buildObjectName());
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            return descriptor.getMBeanInfo();
        }

        public MBeanAttributeInfo getMBeanAttributeInfo(final String attrName) {
            return descriptor.getAttribute(attrName).getInfo();
        }

        @Override
        public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
            return descriptor.getAttributeValue(attribute, object);
        }

        @Override
        public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
            descriptor.setAttributeValue(attribute.getName(), object, attribute.getValue());
        }

        public void setAttribute(String name, Object value) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
            descriptor.setAttributeValue(name, object, value);
        }

        @Override
        public AttributeList getAttributes(String[] attributes) {
            return new AttributeList(
                    Arrays.stream(attributes)
                        .map(attrName -> {
                                try {
                                    return new Attribute(attrName, getAttribute(attrName));
                                } catch (JMException ex) {
                                    return null;
                                }
                        })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
        }

        @Override
        public AttributeList setAttributes(AttributeList attributes) {
            return new AttributeList(
                    attributes.asList().stream()
                            .map(attribute -> {
                                try {
                                    setAttribute(attribute);
                                    return attribute;
                                } catch (JMException ex) {
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
        }

        @Override
        public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
            // TODO Use signature to find suitable method to invoke
            return descriptor.invoke(actionName, object, params);
        }

        @Override
        public String toString() {
            return buildObjectName();
        }
    }

}
