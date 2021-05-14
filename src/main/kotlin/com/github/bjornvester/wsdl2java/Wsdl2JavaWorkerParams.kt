package com.github.bjornvester.wsdl2java

import org.gradle.workers.WorkParameters

interface Wsdl2JavaWorkerParams : WorkParameters {
    var args: Array<String>
}
