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
            throw GradleException("Failed to generate Java sources from WSDL. See the log for details.")
        }

        fixGeneratedAnnotations()
    }

    private fun fixGeneratedAnnotations() {
        if (parameters.switchGeneratedAnnotation || parameters.removeDateFromGeneratedAnnotation) {
            parameters.outputDir.asFileTree.forEach {
                logger.debug("Fixing the @Generated annotation in file $it")
                var source = it.readText()

                if (parameters.switchGeneratedAnnotation) {
                    source = source.replaceFirst("import javax.annotation.Generated", "import javax.annotation.processing.Generated")
                }

                if (parameters.removeDateFromGeneratedAnnotation) {
                    // Remove the "date" part from the @Generated annotation
                    // Input example: @Generated(value = "org.apache.cxf.tools.wsdlto.WSDLToJava", date = "2021-05-15T21:18:42.272+02:00", comments = "Apache CXF 3.4.3")
                    // Note that the 'value' property may contain classes in the 'com.sun.tools.xjc' namespace
                    val generatedPattern = """(@Generated\(value = "[\w\.]*"), date = "[^"]*"([^)]*\))"""
                    source = source.replace(Regex(generatedPattern), "$1$2")
                }

                it.writeText(source)
            }
        }
    }
}
