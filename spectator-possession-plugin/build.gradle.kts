version = "1.2.3"

dependencies {
    compileOnly(project(":paper-api"))
}

tasks.jar {
    archiveBaseName.set("SpectatorPossession")
    archiveVersion.set(project.version.toString())
}
