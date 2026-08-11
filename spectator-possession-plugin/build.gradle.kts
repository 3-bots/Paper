version = "1.4.0"

dependencies {
    compileOnly(project(":paper-api"))
}

tasks.jar {
    archiveBaseName.set("SpectatorPossession")
    archiveVersion.set(project.version.toString())
}
