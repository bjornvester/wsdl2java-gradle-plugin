package com.github.bjornvester.wsdl2java

import org.apache.cxf.tools.common.ToolContext
import org.apache.cxf.tools.wsdlto.WSDLToJava
import org.gradle.workers.WorkAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Wsdl2JavaWorker : WorkAction<Wsdl2JavaWorkerParams> {
    private val logger: Logger = LoggerFactory.getLogger(Wsdl2JavaWorker::class.java)

    override fun execute() {
        logger.info("Running WSDLToJava tool with args: {}", parameters.args)
        WSDLToJava(parameters.args).run(ToolContext())
    }
}
