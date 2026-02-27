val gdxVersion = "1.12.1"

plugins {
    application
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
}

application {
    mainClass.set("td.lwjgl3.Lwjgl3Launcher")
}

tasks.named<JavaExec>("run") {
    jvmArgs("-XstartOnFirstThread")
}
