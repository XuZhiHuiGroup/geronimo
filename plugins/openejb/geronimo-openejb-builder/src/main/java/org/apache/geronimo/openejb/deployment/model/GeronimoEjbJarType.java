//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.05.05 at 02:26:05 PM PDT 
//


package org.apache.geronimo.openejb.deployment.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.geronimo.deployment.service.plan.EnvironmentType;
import org.apache.geronimo.j2ee.deployment.JndiPlan;
import org.apache.geronimo.j2ee.deployment.model.naming.EjbLocalRefType;
import org.apache.geronimo.j2ee.deployment.model.naming.EjbRefType;
import org.apache.geronimo.j2ee.deployment.model.naming.EnvEntryType;
import org.apache.geronimo.j2ee.deployment.model.naming.GbeanRefType;
import org.apache.geronimo.j2ee.deployment.model.naming.MessageDestinationType;
import org.apache.geronimo.j2ee.deployment.model.naming.PersistenceContextRefType;
import org.apache.geronimo.j2ee.deployment.model.naming.PersistenceUnitRefType;
import org.apache.geronimo.j2ee.deployment.model.naming.ResourceEnvRefType;
import org.apache.geronimo.j2ee.deployment.model.naming.ResourceRefType;
import org.apache.geronimo.j2ee.deployment.model.naming.ServiceRefType;
import org.apache.geronimo.security.deployment.model.security.SecurityRefType;
import org.apache.geronimo.security.deployment.model.security.SecurityType;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.w3c.dom.Element;


/**
 * <p>Java class for geronimo-ejb-jarType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="geronimo-ejb-jarType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://geronimo.apache.org/xml/ns/deployment-1.2}environment" minOccurs="0"/>
 *         &lt;element ref="{http://geronimo.apache.org/xml/ns/j2ee/application-2.0}clustering" minOccurs="0"/>
 *         &lt;element name="openejb-jar" type="{http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0}openejb-jarType" minOccurs="0"/>
 *         &lt;group ref="{http://geronimo.apache.org/xml/ns/naming-1.2}jndiEnvironmentRefsGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://geronimo.apache.org/xml/ns/naming-1.2}message-destination" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="tss-link" type="{http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0}tss-linkType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="web-service-binding" type="{http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0}web-service-bindingType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{http://geronimo.apache.org/xml/ns/security-2.0}security"/>
 *           &lt;element ref="{http://geronimo.apache.org/xml/ns/security-2.0}security-ref"/>
 *         &lt;/choice>
 *         &lt;any processContents='lax' namespace='http://java.sun.com/xml/ns/persistence' maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "geronimo-ejb-jarType", propOrder = {
    "environment",
    "clustering",
    "openejbJar",
        "envEntry",
        "ejbRef",
        "ejbLocalRef",
        "gbeanRef",
        "persistenceContextRef",
         "persistenceUnitRef",
         "resourceEnvRef",
          "resourceRef",
        "serviceRef",
    "messageDestination",
    "tssLink",
    "webServiceBinding",
    "security",
    "securityRef",
    "persistence"
})
public class GeronimoEjbJarType implements JndiPlan {

    @XmlElement(namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2")
    protected EnvironmentType environment;
    @XmlElement(namespace = "http://geronimo.apache.org/xml/ns/j2ee/application-2.0")
    protected AbstractClusteringType clustering;
    @XmlElement(name = "openejb-jar")
    protected OpenejbJarType openejbJar;

    @XmlElement(name = "env-entry", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<EnvEntryType> envEntry;

    @XmlElement(name = "ejb-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<EjbRefType> ejbRef;

    @XmlElement(name = "ejb-local-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<EjbLocalRefType> ejbLocalRef;

    @XmlElement(name = "gbean-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<GbeanRefType> gbeanRef;

    @XmlElement(name = "persistence-context-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<PersistenceContextRefType> persistenceContextRef;

    @XmlElement(name = "persistence-unit-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<PersistenceUnitRefType> persistenceUnitRef;

    @XmlElement(name = "resource-env-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<ResourceEnvRefType> resourceEnvRef;

    @XmlElement(name = "resource-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<ResourceRefType> resourceRef;

    @XmlElement(name = "service-ref", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<ServiceRefType> serviceRef;

    @XmlElement(name = "message-destination", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected List<MessageDestinationType> messageDestination;
    @XmlElement(name = "tss-link")
    protected List<TssLinkType> tssLink;
    @XmlElement(name = "web-service-binding")
    protected List<WebServiceBindingType> webServiceBinding;
    @XmlElement(namespace = "http://geronimo.apache.org/xml/ns/security-2.0")
    protected SecurityType security;
    @XmlElement(name = "security-ref", namespace = "http://geronimo.apache.org/xml/ns/security-2.0")
    protected SecurityRefType securityRef;
    @XmlElement(name = "persistence", namespace = "http://java.sun.com/xml/ns/persistence")
    protected List<Persistence> persistence;

    /**
     * Gets the value of the environment property.
     * 
     * @return
     *     possible object is
     *     {@link EnvironmentType }
     *     
     */
    public EnvironmentType getEnvironment() {
        return environment;
    }

    /**
     * Sets the value of the environment property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnvironmentType }
     *     
     */
    public void setEnvironment(EnvironmentType value) {
        this.environment = value;
    }

    /**
     * 
     *                         Reference to abstract clustering element defined in
     *                         imported "geronimo-application-2.0.xsd"
     *                     
     * 
     * @return
     *     possible object is
     *     {@link AbstractClusteringType }
     *     
     */
    public AbstractClusteringType getClustering() {
        return clustering;
    }

    /**
     * Sets the value of the clustering property.
     * 
     * @param value
     *     allowed object is
     *     {@link AbstractClusteringType }
     *     
     */
    public void setClustering(AbstractClusteringType value) {
        this.clustering = value;
    }

    /**
     * Gets the value of the openejbJar property.
     * 
     * @return
     *     possible object is
     *     {@link OpenejbJarType }
     *     
     */
    public OpenejbJarType getOpenejbJar() {
        return openejbJar;
    }

    /**
     * Sets the value of the openejbJar property.
     * 
     * @param value
     *     allowed object is
     *     {@link OpenejbJarType }
     *     
     */
    public void setOpenejbJar(OpenejbJarType value) {
        this.openejbJar = value;
    }

    //JndiPlan methods

    public List<EnvEntryType> getEnvEntry() {
        if (envEntry == null) {
            envEntry = new ArrayList<EnvEntryType>();
        }
        return envEntry;
    }

    public List<EjbRefType> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new ArrayList<EjbRefType>();
        }
        return ejbRef;
    }

    public List<EjbLocalRefType> getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new ArrayList<EjbLocalRefType>();
        }
        return ejbLocalRef;
    }

    public List<ResourceRefType> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new ArrayList<ResourceRefType>();
        }
        return resourceRef;
    }

    public List<ResourceEnvRefType> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new ArrayList<ResourceEnvRefType>();
        }
        return resourceEnvRef;
    }

    public List<PersistenceContextRefType> getPersistenceContextRef() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new ArrayList<PersistenceContextRefType>();
        }
        return persistenceContextRef;
    }

    public List<PersistenceUnitRefType> getPersistenceUnitRef() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new ArrayList<PersistenceUnitRefType>();
        }
        return persistenceUnitRef;
    }

    public List<GbeanRefType> getGBeanRef() {
        if (gbeanRef == null) {
            gbeanRef = new ArrayList<GbeanRefType>();
        }
        return gbeanRef;
    }

    public List<ServiceRefType> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new ArrayList<ServiceRefType>();
        }
        return serviceRef;
    }
    /**
     * Gets the value of the messageDestination property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageDestination property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessageDestination().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MessageDestinationType }
     * 
     * 
     */
    public List<MessageDestinationType> getMessageDestination() {
        if (messageDestination == null) {
            messageDestination = new ArrayList<MessageDestinationType>();
        }
        return this.messageDestination;
    }

    /**
     * Gets the value of the tssLink property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tssLink property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTssLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TssLinkType }
     * 
     * 
     */
    public List<TssLinkType> getTssLink() {
        if (tssLink == null) {
            tssLink = new ArrayList<TssLinkType>();
        }
        return this.tssLink;
    }

    /**
     * Gets the value of the webServiceBinding property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the webServiceBinding property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWebServiceBinding().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WebServiceBindingType }
     * 
     * 
     */
    public List<WebServiceBindingType> getWebServiceBinding() {
        if (webServiceBinding == null) {
            webServiceBinding = new ArrayList<WebServiceBindingType>();
        }
        return this.webServiceBinding;
    }

    /**
     * Gets the value of the security property.
     * 
     * @return
     *     possible object is
     *     {@link SecurityType }
     *     
     */
    public SecurityType getSecurity() {
        return security;
    }

    /**
     * Sets the value of the security property.
     * 
     * @param value
     *     allowed object is
     *     {@link SecurityType }
     *     
     */
    public void setSecurity(SecurityType value) {
        this.security = value;
    }

    /**
     * Gets the value of the securityRef property.
     * 
     * @return
     *     possible object is
     *     {@link SecurityRefType }
     *     
     */
    public SecurityRefType getSecurityRef() {
        return securityRef;
    }

    /**
     * Sets the value of the securityRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link SecurityRefType }
     *     
     */
    public void setSecurityRef(SecurityRefType value) {
        this.securityRef = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * {@link Object }
     * 
     * 
     */
    public List<Persistence> getPersistence() {
        if (persistence == null) {
            persistence = new ArrayList<Persistence>();
        }
        return this.persistence;
    }

}