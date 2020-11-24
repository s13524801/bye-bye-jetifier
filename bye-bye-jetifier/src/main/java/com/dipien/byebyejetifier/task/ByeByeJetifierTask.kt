package com.dipien.byebyejetifier.task

import com.dipien.byebyejetifier.ProjectAnalyzer
import com.dipien.byebyejetifier.ProjectAnalyzerResult
import com.dipien.byebyejetifier.scanner.ScannerProcessor
import com.dipien.byebyejetifier.common.AbstractTask
import com.dipien.byebyejetifier.common.LoggerHelper
import com.dipien.byebyejetifier.scanner.ScannerHelper
import com.dipien.byebyejetifier.scanner.bytecode.BytecodeScanner
import com.dipien.byebyejetifier.scanner.resource.XmlResourceScanner
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import java.lang.RuntimeException

open class ByeByeJetifierTask : AbstractTask() {

    companion object {
        const val TASK_NAME = "canISayByeByeJetifier"
        const val ENABLE_JETIFIER_PROPERTY = "android.enableJetifier"
    }

    @get:Input
    @get:Optional
    var legacyGroupIdPrefixes: List<String> = emptyList()

    @get:Input
    @get:Optional
    var legacyPackagesPrefixes: List<String> = emptyList()

    @get:Input
    @get:Optional
    var ignoredPackages: List<String> = emptyList()

    @get:Input
    @get:Optional
    var ignoredConfigurations: List<String> = emptyList()

    private val scannerProcessor by lazy {
        val scannerHelper = ScannerHelper(legacyPackagesPrefixes, ignoredPackages)
        val scannerList = listOf(BytecodeScanner(scannerHelper), XmlResourceScanner(scannerHelper))
        ScannerProcessor(scannerList)
    }

    init {
        group = "Verification"
        description = "Verifies if you can keep Android Jetifier disabled"
    }

    override fun onExecute() {

        if (project.hasProperty(ENABLE_JETIFIER_PROPERTY) && project.property(ENABLE_JETIFIER_PROPERTY) == "true") {
            throw GradleException(
                "This task needs to be run with Jetifier disabled: ./gradlew $TASK_NAME -P$ENABLE_JETIFIER_PROPERTY=false"
            )
        }

        LoggerHelper.log("ignoredPackages: $ignoredPackages")
        LoggerHelper.log("ignoredConfigurations: $ignoredConfigurations")

        project.allprojects.forEach {
            ProjectAnalyzer(it, ignoredConfigurations, legacyGroupIdPrefixes, scannerProcessor).analyze()
        }

        if (ProjectAnalyzerResult.thereAreSupportLibraryDependencies || ProjectAnalyzerResult.includeSupportLibrary) {
            throw RuntimeException("You can not say Bye Bye Jetifier")
        } else {
            LoggerHelper.lifeCycle("No dependencies with legacy android support usages! You can say Bye Bye Jetifier.")
        }
    }
}
