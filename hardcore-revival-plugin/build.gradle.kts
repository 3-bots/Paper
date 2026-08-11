version = "1.1.0"

dependencies {
    compileOnly(project(":paper-api"))
}

tasks.jar {
    archiveBaseName.set("HardcoreRevival")
    archiveVersion.set(project.version.toString())
}
