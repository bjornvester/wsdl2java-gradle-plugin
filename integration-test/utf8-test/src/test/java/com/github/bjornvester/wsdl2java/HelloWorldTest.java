package com.github.bjornvester.wsdl2java;

import com.github.bjornvester.HelloUtf8ÆØÅPortType;
import com.github.bjornvester.RequestÆØÅ;
import com.github.bjornvester.ResponseÆØÅ;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.xml.ws.Endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloWorldTest {
    String serviceAddress = "http://localhost:8899/HelloWorldService";
    Endpoint endpoint;

    @AfterEach
    void stopEndpoint() {
        if (endpoint != null) {
            endpoint.stop();
        }
    }

    @Test
    void testServerClient() {
        // Create server
        endpoint = Endpoint.publish(serviceAddress, new HelloWorldImpl());

        // Create client
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(HelloUtf8ÆØÅPortType.class);
        factory.setAddress(serviceAddress);
        HelloUtf8ÆØÅPortType client = (HelloUtf8ÆØÅPortType) factory.create();

        // Call the service
        RequestÆØÅ param = new RequestÆØÅ();
        param.setArg0("ÆØÅ");
        ResponseÆØÅ response = client.utf8OperationWithCharsÆØÅ(param);
        assertEquals(response.getReturn(), "Hi, ÆØÅ");
    }
}
