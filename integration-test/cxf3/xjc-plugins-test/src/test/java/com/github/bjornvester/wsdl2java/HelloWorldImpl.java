package com.github.bjornvester.wsdl2java;

import com.github.bjornvester.example.xjcplugins.HelloWorld;
import com.github.bjornvester.example.xjcplugins.SayHi;
import com.github.bjornvester.example.xjcplugins.SayHiResponse;

public class HelloWorldImpl implements HelloWorld {
	@Override
	public SayHiResponse sayHi(SayHi request) {
		SayHiResponse response = new SayHiResponse();
		response.setReturn("Hi, " + request.getArg0());
		return response;
	}
}
