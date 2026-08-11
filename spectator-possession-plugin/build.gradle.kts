version = "1.5.0"

dependencies {
    compileOnly(project(":paper-api"))
}

tasks.jar {
    archiveBaseName.set("SpectatorPossession")
    archiveVersion.set(project.version.toString())
}
