package com.github.emilienkia.ajmx.impl.entities;

import com.github.emilienkia.ajmx.annotations.MBean;
import com.github.emilienkia.ajmx.annotations.MBeanAttribute;
import com.github.emilienkia.ajmx.annotations.MBeanOperation;
import com.github.emilienkia.ajmx.annotations.MBeanOperationParam;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static com.github.emilienkia.ajmx.annotations.MBeanAttribute.AccessMode.READ_WRITE;

@MBean(domain = "this.is.test", type="MyType")
public class DomainTypeAnnot {

    public DomainTypeAnnot() {
    }

    @MBeanAttribute(description = "This is a boolean attribute", accessMode = READ_WRITE)
    boolean boolAttr = true;

    @MBeanAttribute(description = "This is a byte attribute", accessMode = READ_WRITE)
    byte byteAttr = 2;

    @MBeanAttribute(description = "This is a char attribute", accessMode = READ_WRITE)
    char charAttr = 'A';

    @MBeanAttribute(description = "This is a short attribute", accessMode = READ_WRITE)
    short shortAttr = 512;

    @MBeanAttribute(description = "This is an integer attribute", accessMode = READ_WRITE)
    int intAttr = 25;

    @MBeanAttribute(description = "This is a long attribute", accessMode = READ_WRITE)
    long longAttr = 123456789l;

    @MBeanAttribute(description = "This is a float attribute", accessMode = READ_WRITE)
    float floatAttr = 1234.5f;

    @MBeanAttribute(description = "This is a double attribute", accessMode = READ_WRITE)
    double doubleAttr = 1234567.89;

    @MBeanAttribute(description = "This is a string attribute", accessMode = READ_WRITE)
    String strAttr = "Toto";

    @MBeanAttribute(description = "This is a big integer attribute", accessMode = READ_WRITE)
    BigInteger biAttr = new BigInteger("1234567890123456789");

    @MBeanAttribute(description = "This is a big decimal attribute", accessMode = READ_WRITE)
    BigDecimal bdAttr = new BigDecimal("123456789.0123456789");

    @MBeanAttribute(description = "This is a date attribute", accessMode = READ_WRITE)
    Date dateAttr = new Date(100, 1, 1, 0, 0, 0);

    @MBeanOperation(description = "Method which takes no parameter and return nothing")
    void voidVoidOperation() {
    }

    @MBeanOperation(name="hello", description = "Method taking a string and returning a string")
    String sayHello(
            @MBeanOperationParam(name = "name", description = "Who to say hello.")
                    String name
    ) {
        return "Hello " + name + " !";
    }

    @MBeanOperation(description="Make the sum of various integer types")
    BigInteger sumIntegers(boolean negate, byte b, short s, int i, long l, BigInteger bi) {
        bi = bi.add(new BigInteger(Long.toString(l)));
        bi = bi.add(new BigInteger(Integer.toString(i)));
        bi = bi.add(new BigInteger(Short.toString(s)));
        bi = bi.add(new BigInteger(Byte.toString(b)));
        if(negate) {
            bi = bi.negate();
        }
        return bi;
    }

    @MBeanOperation(description="Make the sum of various decimal types")
    double sumDecimals(float f, double d, BigDecimal bd) {
        bd = bd.add(new BigDecimal(f));
        bd = bd.add(new BigDecimal(d));
        return bd.doubleValue();
    }

}
