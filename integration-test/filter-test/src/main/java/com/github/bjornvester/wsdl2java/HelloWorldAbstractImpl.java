package com.github.bjornvester.wsdl2java;

import com.github.bjornvester.example._abstract.HelloWorldAbstract;
import com.github.bjornvester.example._abstract.SayHi;
import com.github.bjornvester.example._abstract.SayHiResponse;

public class HelloWorldAbstractImpl implements HelloWorldAbstract {
	@Override
	public SayHiResponse sayHi(SayHi request) {
		SayHiResponse response = new SayHiResponse();
		response.setReturn("Hi, " + request.getArg0());
		return response;
	}
}
