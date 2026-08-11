version = "1.3.1"

dependencies {
    compileOnly(project(":paper-api"))
}

tasks.jar {
    archiveBaseName.set("SpectatorPossession")
    archiveVersion.set(project.version.toString())
}
