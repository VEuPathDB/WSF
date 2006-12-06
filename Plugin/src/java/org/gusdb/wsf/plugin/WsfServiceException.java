package org.gusdb.wsf.plugin;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.ser.BeanDeserializer;
import org.apache.axis.encoding.ser.BeanSerializer;

/**
 * 
 */

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WsfServiceException extends AxisFault implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6228934705413257448L;

    private Object __equalsCalc = null;

    // Type metadata
    private static TypeDesc typeDesc = new TypeDesc(WsfServiceException.class,
            true);

    static {
        typeDesc.setXmlType(new QName("http://plugin.wsf.gusdb.org",
                "WsfServiceException"));
    }

    /**
     * Return type metadata object
     */
    public static TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static Serializer getSerializer(String mechType, Class _javaType,
            QName _xmlType) {
        return new BeanSerializer(_javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static Deserializer getDeserializer(String mechType,
            Class _javaType, QName _xmlType) {
        return new BeanDeserializer(_javaType, _xmlType, typeDesc);
    }

    /**
     * 
     */
    public WsfServiceException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public WsfServiceException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public WsfServiceException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public WsfServiceException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    // TODO Auto-generated constructor stub
    // }
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof WsfServiceException)) return false;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true;
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        __hashCodeCalc = false;
        return _hashCode;
    }

    /**
     * Writes the exception data to the faultDetails
     */
    public void writeDetails(QName qname, SerializationContext context)
            throws java.io.IOException {
        context.serialize(qname, null, this);
    }
}
