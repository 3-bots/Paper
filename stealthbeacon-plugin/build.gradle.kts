version = "1.0.1"

dependencies {
    compileOnly(project(":paper-api"))
}

tasks.jar {
    archiveBaseName.set("StealthBeacon")
    archiveVersion.set(project.version.toString())
}
