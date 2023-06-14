package com.github.bjornvester.wsdl2java

import org.gradle.api.file.Directory
import org.gradle.workers.WorkParameters

interface Wsdl2JavaWorkerParams : WorkParameters {
    var wsdlToArgs: Map<String, List<String>>
    var outputDir: Directory
    var removeDateFromGeneratedAnnotation: Boolean
    var generatedStyle: String
}
