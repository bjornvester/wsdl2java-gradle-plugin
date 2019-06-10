package com.github.bjornvester.wsdl2java;

import com.github.bjornvester.HelloWorld;
import com.github.bjornvester.SayHi;
import com.github.bjornvester.SayHiResponse;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.xml.ws.Endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloWorldTest {
	String serviceAddress = "http://localhost:8899/HellowWorldService";
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
		sayHi.setArg0("Joe");
		SayHiResponse response = client.sayHi(sayHi);
		assertEquals(response.getReturn(), "Hi, Joe");
	}
}
