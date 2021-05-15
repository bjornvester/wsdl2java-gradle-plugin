package com.github.bjornvester.wsdl2java

import org.apache.cxf.tools.common.ToolContext
import org.apache.cxf.tools.wsdlto.WSDLToJava
import org.gradle.api.GradleException
import org.gradle.workers.WorkAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Wsdl2JavaWorker : WorkAction<Wsdl2JavaWorkerParams> {
    private val logger: Logger = LoggerFactory.getLogger(Wsdl2JavaWorker::class.java)

    override fun execute() {
        logger.info("Running WSDLToJava tool with args: {}", parameters.args)

        try {
            WSDLToJava(parameters.args).run(ToolContext())
        } catch (e: Exception) {
            // We can't propagate the exception as it might contain classes from CXF which are not available outside the worker execution context

            logger.error("Failed to generate sources from WSDL", e)
            throw GradleException("Failed to generate Java sources from WSDL. See the log for details. Error message is: ${e.message}")
        }
    }
}
