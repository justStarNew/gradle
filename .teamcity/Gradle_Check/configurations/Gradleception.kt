package configurations

import jetbrains.buildServer.configs.kotlin.v10.BuildType
import jetbrains.buildServer.configs.kotlin.v10.buildSteps.gradle
import model.CIBuildModel

object Gradleception : BuildType({
    uuid = "${CIBuildModel.projectPrefix}Gradleception"
    extId = uuid
    name = "Gradleception - Java8 Linux"
    description = "Builds Gradle with the version of Gradle which is currently under development (twice)"

    params {
        param("env.JAVA_HOME", "%linux.java8.oracle.64bit%")
    }

    applyDefaults(this, "install", extraParameters = "-Pgradle_installPath=dogfood-first", extraSteps = {
        gradle {
            name = "BUILD_WITH_BUILT_GRADLE"
            tasks = "clean install"
            gradleHome = "%teamcity.build.checkoutDir%/dogfood-first"
            gradleParams = "-Pgradle_installPath=dogfood-second " + gradleParameters.joinToString(separator = " ")
            param("ui.gradleRunner.gradle.wrapper.useWrapper", "false")
        }
        gradle {
            name = "QUICKCHECK_WITH_GRADLE_BUILT_BY_GRADLE"
            tasks = "clean quickCheck"
            gradleHome = "%teamcity.build.checkoutDir%/dogfood-second"
            gradleParams = gradleParameters.joinToString(separator = " ")
            param("ui.gradleRunner.gradle.wrapper.useWrapper", "false")
        }
    }, requiresDistribution = true)
})