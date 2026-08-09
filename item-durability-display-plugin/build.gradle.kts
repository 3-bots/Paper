version = "1.0.0"

dependencies {
    compileOnly(project(":paper-api"))
}

tasks.jar {
    archiveBaseName.set("ItemDurabilityDisplay")
    archiveVersion.set(project.version.toString())
}
