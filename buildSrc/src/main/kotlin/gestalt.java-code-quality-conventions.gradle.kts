import net.ltgt.gradle.errorprone.errorprone

/**
 * Apply to all modules to include multiple code quality plugins to your module.
 */
plugins {
    id("net.ltgt.errorprone")
    checkstyle
    pmd
}

dependencies {
    errorprone(org.github.gestalt.config.Plugins.errorProne)
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone.disableWarningsInGeneratedCode.set(true)
}

checkstyle {
    toolVersion = "8.42"
    configFile = file(rootDir.path + "/config/checkstyle/google_checks.xml")
    isIgnoreFailures = true
}

pmd {
    isConsoleOutput = true
    toolVersion = "6.34.0"
    ruleSets = listOf(rootDir.path + "/config/pmd/custom_ruleset.xml")
}
