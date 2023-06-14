package com.github.bjornvester.wsdl2java;

import com.github.bjornvester.HelloWorld;
import com.github.bjornvester.SayHi;
import com.github.bjornvester.SayHiResponse;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import jakarta.xml.ws.Endpoint;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HelloWorldTest {
    String serviceAddress = "http://localhost:8803/HelloWorldService";
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
        factory.setServiceClass(HelloWorld.class);
        factory.setAddress(serviceAddress);
        HelloWorld client = (HelloWorld) factory.create();

        // Call the service
        SayHi sayHi = new SayHi();
        sayHi.setArg0(OffsetDateTime.now());
        SayHiResponse response = client.sayHi(sayHi);
        assertTrue(response.getReturn().contains("Hi"));
    }
}
