/**
 * SOAPArrayStruct.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package org.apache.geronimo.axis.builder.interop.types;

public class SOAPArrayStruct  implements java.io.Serializable {
    private java.lang.String varString;
    private int varInt;
    private float varFloat;
    private java.lang.String[] varArray;

    public SOAPArrayStruct() {
    }

    public java.lang.String getVarString() {
        return varString;
    }

    public void setVarString(java.lang.String varString) {
        this.varString = varString;
    }

    public int getVarInt() {
        return varInt;
    }

    public void setVarInt(int varInt) {
        this.varInt = varInt;
    }

    public float getVarFloat() {
        return varFloat;
    }

    public void setVarFloat(float varFloat) {
        this.varFloat = varFloat;
    }

    public java.lang.String[] getVarArray() {
        return varArray;
    }

    public void setVarArray(java.lang.String[] varArray) {
        this.varArray = varArray;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SOAPArrayStruct)) return false;
        SOAPArrayStruct other = (SOAPArrayStruct) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.varString==null && other.getVarString()==null) || 
             (this.varString!=null &&
              this.varString.equals(other.getVarString()))) &&
            this.varInt == other.getVarInt() &&
            this.varFloat == other.getVarFloat() &&
            ((this.varArray==null && other.getVarArray()==null) || 
             (this.varArray!=null &&
              java.util.Arrays.equals(this.varArray, other.getVarArray())));
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
        if (getVarString() != null) {
            _hashCode += getVarString().hashCode();
        }
        _hashCode += getVarInt();
        _hashCode += new Float(getVarFloat()).hashCode();
        if (getVarArray() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getVarArray());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getVarArray(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SOAPArrayStruct.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/4s4c/1/3/wsdl/types/", "SOAPArrayStruct"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("varString");
        elemField.setXmlName(new javax.xml.namespace.QName("", "varString"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("varInt");
        elemField.setXmlName(new javax.xml.namespace.QName("", "varInt"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("varFloat");
        elemField.setXmlName(new javax.xml.namespace.QName("", "varFloat"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("varArray");
        elemField.setXmlName(new javax.xml.namespace.QName("", "varArray"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
