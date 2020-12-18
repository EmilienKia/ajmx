package com.github.emilienkia.ajmx.annotations;

import javax.management.MBeanOperationInfo;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface MBeanOperation {

    String name() default "";

    String description() default "";

    enum Impact {
        UNKNOWN(MBeanOperationInfo.UNKNOWN),
        ACTION(MBeanOperationInfo.ACTION),
        INFO(MBeanOperationInfo.INFO),
        ACTION_INFO(MBeanOperationInfo.ACTION_INFO);

        int val;
        Impact(int val) {
            this.val = val;
        }

        public int value() {
            return this.val;
        }

        public static Impact fromVal(int val) {
            switch(val)
            {
                case MBeanOperationInfo.ACTION_INFO:
                    return ACTION_INFO;
                case MBeanOperationInfo.ACTION:
                    return ACTION;
                case MBeanOperationInfo.INFO:
                    return INFO;
                default:
                    return UNKNOWN;
            }
        }
    }

    Impact impact() default Impact.UNKNOWN;
}
