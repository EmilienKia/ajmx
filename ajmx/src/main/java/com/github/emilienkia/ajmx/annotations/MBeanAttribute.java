package com.github.emilienkia.ajmx.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface MBeanAttribute {

    enum AccessMode {
        DEFAULT,
        READ_ONLY,
        WRITE_ONLY,
        READ_WRITE
    }

    String name() default "";

    String description() default "";

    AccessMode accessMode() default AccessMode.DEFAULT;

    class Helpers {

        private Helpers() {
            // Prevent construct helper class
        }

        public static boolean canRead(final MBeanAttribute attr) {
            if (attr == null) {
                throw new IllegalArgumentException("MBean attribute annotation shall be specified");
            }
            return attr.accessMode() == AccessMode.DEFAULT || attr.accessMode() == MBeanAttribute.AccessMode.READ_ONLY || attr.accessMode() == MBeanAttribute.AccessMode.READ_WRITE;
        }

        public static boolean canWrite(final MBeanAttribute attr) {
            if (attr == null) {
                throw new IllegalArgumentException("MBean attribute annotation shall be specified");
            }
            return attr.accessMode() == MBeanAttribute.AccessMode.WRITE_ONLY || attr.accessMode() == MBeanAttribute.AccessMode.READ_WRITE;
        }
    }

}
