/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.axis.builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;

import junit.framework.TestCase;
import org.apache.axis.enum.Style;
import org.apache.axis.soap.SOAPConstants;
import org.apache.geronimo.axis.builder.bookquote.BookQuote;
import org.apache.geronimo.axis.builder.bookquote.BookQuoteService;
import org.apache.geronimo.axis.builder.interop.InteropLab;
import org.apache.geronimo.axis.builder.interop.InteropTestPortType;
import org.apache.geronimo.axis.builder.mock.MockPort;
import org.apache.geronimo.axis.builder.mock.MockSEIFactory;
import org.apache.geronimo.axis.builder.mock.MockService;
import org.apache.geronimo.axis.client.OperationInfo;
import org.apache.geronimo.axis.client.SEIFactory;
import org.apache.geronimo.axis.client.ServiceImpl;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.xbeans.j2ee.JavaWsdlMappingDocument;
import org.apache.geronimo.xbeans.j2ee.JavaWsdlMappingType;
import org.apache.geronimo.xbeans.j2ee.PackageMappingType;

/**
 * @version $Rev:  $ $Date:  $
 */
public class ServiceRefTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));

    public final static String NAMESPACE = "http://geronimo.apache.org/axis/mock";
    private File tmpbasedir;
    private URI configID = URI.create("test");
    private DeploymentContext context;
    private ClassLoader isolatedCl = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
    private final String operationName = "doMockOperation";

    private boolean runExternalWSTest;

    protected void setUp() throws Exception {
        tmpbasedir = File.createTempFile("car", "tmp");
        tmpbasedir.delete();
        tmpbasedir.mkdirs();
        context = new DeploymentContext(tmpbasedir, configID, ConfigurationModuleType.CAR, null, null);

        runExternalWSTest = System.getProperty("geronimo.run.external.webservicetest", "false").equals("true");
    }

    protected void tearDown() throws Exception {
        recursiveDelete(tmpbasedir);
    }

    public void testServiceRefCreation() throws Exception {
        AxisBuilder builder = new AxisBuilder();

        Reference ref = builder.createServiceReference(MockService.class, null, null, null, null, null, context, isolatedCl);
        RefAddr refAddr = ref.get(0);
        Object instance = refAddr.getContent();
        assertTrue(instance instanceof Service);

        ClassLoader cl = context.getClassLoader(null);
        Class loadedType = cl.loadClass(instance.getClass().getName());
        assertTrue(Service.class.isAssignableFrom(loadedType));
        assertTrue(instance.getClass() != loadedType);
    }

    public void testServiceProxy() throws Exception {
        //construct the SEI proxy
        Map portMap = new HashMap();
        portMap.put("MockPort", new MockSEIFactory());
        AxisBuilder builder = new AxisBuilder();
        org.apache.axis.client.Service delegate = new org.apache.axis.client.Service();
        Service service = builder.createService(MockService.class, portMap, context, isolatedCl);
        assertTrue(service instanceof MockService);
        MockService mockService = (MockService) service;
        MockPort mockPort = mockService.getMockPort();
        assertNotNull(mockPort);
    }

    public void testServiceEndpointProxy() throws Exception {
        AxisBuilder builder = new AxisBuilder();

        ServiceImpl serviceInstance = new ServiceImpl(null);
        List typeMappings = new ArrayList();

        URL location = new URL("http://geronimo.apache.org/ws");

        OperationInfo op = buildOperationInfoForMockOperation(builder);
        OperationInfo[] operationInfos = new OperationInfo[]{op};
        Class serviceEndpointClass = builder.enhanceServiceEndpointInterface(isolatedCl, MockPort.class, context);
        SEIFactory serviceInterfaceFactory = builder.createSEIFactory(serviceEndpointClass, serviceInstance, typeMappings, location, operationInfos, context, isolatedCl);
        assertNotNull(serviceInterfaceFactory);
        Remote serviceInterface = serviceInterfaceFactory.createServiceEndpoint();
        assertTrue(serviceInterface instanceof MockPort);
//        MockPort mockServiceInterface = (MockPort) serviceInterface;
//        mockServiceInterface.doMockOperation(null);
    }

    public void testBuildOperationInfo() throws Exception {
        AxisBuilder builder = new AxisBuilder();
        OperationInfo operationInfo = buildOperationInfoForMockOperation(builder);
        assertNotNull(operationInfo);
    }

    public void testBuildFullServiceProxy() throws Exception {
        Definition definition = buildDefinition();
        JavaWsdlMappingType mapping = buildLightweightMappingType();
        QName serviceQName = new QName(NAMESPACE, "MockService");
        AxisBuilder builder = new AxisBuilder();
        Object proxy = builder.createService(MockService.class, definition, mapping, serviceQName, SOAPConstants.SOAP11_CONSTANTS, context, isolatedCl);
        assertNotNull(proxy);
        assertTrue(proxy instanceof MockService);
        MockPort mockPort = ((MockService) proxy).getMockPort();
        assertNotNull(mockPort);
    }

    public void testBuildBookQuoteProxy() throws Exception {
        File wsdl = new File(basedir, "src/test-resources/BookQuote.wsdl");
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        Definition definition = reader.readWSDL(wsdl.toURI().toString());
        File jaxrpcMapping = new File(basedir, "src/test-resources/BookQuote.xml");
        JavaWsdlMappingDocument mappingDocument = JavaWsdlMappingDocument.Factory.parse(jaxrpcMapping);
        JavaWsdlMappingType mapping = mappingDocument.getJavaWsdlMapping();
        QName serviceQName = new QName("http://www.Monson-Haefel.com/jwsbook/BookQuote", "BookQuoteService");
        AxisBuilder builder = new AxisBuilder();
        Object proxy = builder.createService(BookQuoteService.class, definition, mapping, serviceQName, SOAPConstants.SOAP11_CONSTANTS, context, isolatedCl);
        assertNotNull(proxy);
        assertTrue(proxy instanceof BookQuoteService);
        BookQuote bookQuote = ((BookQuoteService) proxy).getBookQuotePort();
        assertNotNull(bookQuote);
    }

    public void testBuildInteropProxy() throws Exception {
        File wsdl = new File(basedir, "src/test-resources/interop/interop.wsdl");
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        Definition definition = reader.readWSDL(wsdl.toURI().toString());
        File jaxrpcMapping = new File(basedir, "src/test-resources/interop/interop-jaxrpcmapping.xml");
        JavaWsdlMappingDocument mappingDocument = JavaWsdlMappingDocument.Factory.parse(jaxrpcMapping);
        JavaWsdlMappingType mapping = mappingDocument.getJavaWsdlMapping();
        QName serviceQName = new QName("http://tempuri.org/4s4c/1/3/wsdl/def/interopLab", "interopLab");
        AxisBuilder builder = new AxisBuilder();
        Object proxy = builder.createService(InteropLab.class, definition, mapping, serviceQName, SOAPConstants.SOAP11_CONSTANTS, context, isolatedCl);
        assertNotNull(proxy);
        assertTrue(proxy instanceof InteropLab);
        InteropTestPortType interopTestPort = ((InteropLab) proxy).getinteropTestPort();
        assertNotNull(interopTestPort);
        if (runExternalWSTest) {
            System.out.println("Running external ws test");
            int result = interopTestPort.echoInteger(1);
            assertEquals(result, 1);
        } else {
            System.out.println("Skipping external ws test");
        }
    }

    public void testBuildInteropProxyFromURIs() throws Exception {
        File wsdldir = new File(basedir, "src/test-resources/interop");
        ClassLoader cl = new URLClassLoader(new URL[]{wsdldir.toURL()}, isolatedCl);
        URI wsdlURI = new URI("interop.wsdl");
        URI jaxrpcmappingURI = new URI("interop-jaxrpcmapping.xml");
        QName serviceQName = new QName("http://tempuri.org/4s4c/1/3/wsdl/def/interopLab", "interopLab");
        AxisBuilder builder = new AxisBuilder();
        Map portComponentRefMap = null;
        List handlers = null;
        Object proxy = builder.createService(InteropLab.class, wsdlURI, jaxrpcmappingURI, serviceQName, portComponentRefMap, handlers, context, cl);
        assertNotNull(proxy);
        ClassLoader contextCl = context.getClassLoader(null);
        proxy = reserialize(proxy, contextCl);
        assertTrue(proxy instanceof InteropLab);
        InteropTestPortType interopTestPort = ((InteropLab) proxy).getinteropTestPort();
        assertNotNull(interopTestPort);
        if (runExternalWSTest) {
            System.out.println("Running external ws test");
            int result = interopTestPort.echoInteger(1);
            assertEquals(result, 1);
        } else {
            System.out.println("Skipping external ws test");
        }
    }

    private Object reserialize(Object object, ClassLoader cl) throws Exception {
        if (!(object instanceof Serializable)) {
            fail("object is not serializable, " + object);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.flush();
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ConfigInputStream(bais, cl);
        Object result = ois.readObject();
        return result;
    }

    private static class ConfigInputStream extends ObjectInputStream {
        private final ClassLoader cl;

        public ConfigInputStream(InputStream in, ClassLoader cl) throws IOException {
            super(in);
            this.cl = cl;
        }

        protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            try {
                return cl.loadClass(desc.getName());
            } catch (ClassNotFoundException e) {
                // let the parent try
                return super.resolveClass(desc);
            }
        }
    }

    private OperationInfo buildOperationInfoForMockOperation(AxisBuilder builder) throws NoSuchMethodException, DeploymentException, WSDLException {
        Class portClass = MockPort.class;
        Method method = portClass.getDeclaredMethod("doMockOperation", new Class[]{String.class});
        WSDLFactory factory = WSDLFactory.newInstance();
        Definition definition = factory.newDefinition();
        ExtensionRegistry extensionRegistry = factory.newPopulatedExtensionRegistry();
        BindingOperation bindingOperation = buildBindingOperation(definition, extensionRegistry);

        Style defaultStyle = Style.DOCUMENT;

        OperationInfo operationInfo = builder.buildOperationInfo(method, bindingOperation, defaultStyle, SOAPConstants.SOAP11_CONSTANTS);
        return operationInfo;
    }

    private Definition buildDefinition() throws WSDLException {
        WSDLFactory factory = WSDLFactory.newInstance();
        Definition definition = factory.newDefinition();
        ExtensionRegistry extensionRegistry = factory.newPopulatedExtensionRegistry();
        BindingOperation bindingOperation = buildBindingOperation(definition, extensionRegistry);
        Binding binding = definition.createBinding();
        binding.setQName(new QName(NAMESPACE, "MockPortBinding"));
        //add soap:binding
        SOAPBinding soapBinding = (SOAPBinding) extensionRegistry.createExtension(Binding.class, new QName("http://schemas.xmlsoap.org/wsdl/soap/", "binding"));
        soapBinding.setTransportURI("http://schemas.xmlsoap.org/soap/http");
        soapBinding.setStyle("rpc");
        binding.addExtensibilityElement(soapBinding);
        binding.addBindingOperation(bindingOperation);
        PortType portType = definition.createPortType();
        portType.setQName(new QName(NAMESPACE, "MockPort"));
        portType.addOperation(bindingOperation.getOperation());
        binding.setPortType(portType);
        Port port = definition.createPort();
        port.setName("MockPort");
        //add soap:address
        SOAPAddress soapAddress = (SOAPAddress) extensionRegistry.createExtension(Port.class, new QName("http://schemas.xmlsoap.org/wsdl/soap/", "address"));
        soapAddress.setLocationURI("http://127.0.0.1:8080/foo");
        port.addExtensibilityElement(soapAddress);
        port.setBinding(binding);
        javax.wsdl.Service service = definition.createService();
        service.setQName(new QName(NAMESPACE, "MockService"));
        service.addPort(port);
        definition.addService(service);
        return definition;
    }

    private BindingOperation buildBindingOperation(Definition definition, ExtensionRegistry extensionRegistry) throws WSDLException {
        Operation operation = definition.createOperation();
        operation.setName(operationName);
        operation.setStyle(OperationType.REQUEST_RESPONSE);
        Input input = definition.createInput();
        Message inputMessage = definition.createMessage();
        Part inputPart = definition.createPart();
        inputPart.setName("string");
        inputPart.setTypeName(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        inputMessage.addPart(inputPart);
        operation.setInput(input);
        input.setMessage(inputMessage);
        Output output = definition.createOutput();
        Message outputMessage = definition.createMessage();
        operation.setOutput(output);
        output.setMessage(outputMessage);
        BindingOperation bindingOperation = definition.createBindingOperation();
        SOAPOperation soapOperation = (SOAPOperation) extensionRegistry.createExtension(BindingOperation.class, new QName("http://schemas.xmlsoap.org/wsdl/soap/", "operation"));
        soapOperation.setSoapActionURI("actionURI");
        soapOperation.setStyle("rpc");
        bindingOperation.addExtensibilityElement(soapOperation);
        bindingOperation.setOperation(operation);
        bindingOperation.setName(operation.getName());
        BindingInput bindingInput = definition.createBindingInput();
        SOAPBody inputBody = (SOAPBody) extensionRegistry.createExtension(BindingInput.class, new QName("http://schemas.xmlsoap.org/wsdl/soap/", "body"));
        inputBody.setUse("encoded");
        bindingInput.addExtensibilityElement(inputBody);
        bindingOperation.setBindingInput(bindingInput);
        BindingOutput bindingOutput = definition.createBindingOutput();
        bindingOutput.addExtensibilityElement(inputBody);
        bindingOperation.setBindingOutput(bindingOutput);
        return bindingOperation;
    }

    private JavaWsdlMappingType buildLightweightMappingType() {
        JavaWsdlMappingType mapping = JavaWsdlMappingType.Factory.newInstance();
        PackageMappingType packageMapping = mapping.addNewPackageMapping();
        packageMapping.addNewNamespaceURI().setStringValue(NAMESPACE);
        packageMapping.addNewPackageType().setStringValue("org.apache.geronimo.axis.builder.mock");
        return mapping;
    }


    private void recursiveDelete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                recursiveDelete(files[i]);
            }
        }
        file.delete();
    }

}
