version = "1.2.2"

dependencies {
    compileOnly(project(":paper-api"))
}

tasks.jar {
    archiveBaseName.set("SpectatorPossession")
    archiveVersion.set(project.version.toString())
}
