package com.github.bjornvester.wsdl2java;

import com.github.bjornvester.HelloWorld;
import com.github.bjornvester.SayHi;
import com.github.bjornvester.SayHiResponse;

public class HelloWorldImpl implements HelloWorld {
	@Override
	public SayHiResponse sayHi(SayHi request) {
		SayHiResponse response = new SayHiResponse();
		response.setReturn("Hi, " + request.getArg0());
		return response;
	}
}
