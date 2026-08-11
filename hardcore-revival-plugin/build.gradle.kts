version = "1.3.0"

dependencies {
    compileOnly(project(":paper-api"))
}

tasks.jar {
    archiveBaseName.set("HardcoreRevival")
    archiveVersion.set(project.version.toString())
}
