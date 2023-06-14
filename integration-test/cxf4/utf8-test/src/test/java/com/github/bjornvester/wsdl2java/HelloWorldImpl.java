package com.github.bjornvester.wsdl2java;

import com.github.bjornvester.HelloUtf8ÆØÅPortType;
import com.github.bjornvester.RequestÆØÅ;
import com.github.bjornvester.ResponseÆØÅ;

public class HelloWorldImpl implements HelloUtf8ÆØÅPortType {

    @Override
    public ResponseÆØÅ utf8OperationWithCharsÆØÅ(final RequestÆØÅ parameters) {
        ResponseÆØÅ response = new ResponseÆØÅ();
        response.setReturn("Hi, " + parameters.getArg0());
        return response;
    }
}
