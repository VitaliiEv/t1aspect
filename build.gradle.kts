plugins {
	java
	id("org.springframework.boot") version "3.2.4"
	id("io.spring.dependency-management") version "1.1.4"
	id("org.openapi.generator") version "7.4.0"
}

group = "com.github.vitaliiev"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-security")

	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

val projectBuildDir = layout.buildDirectory.asFile.get();
val openApiOutputDir = "$projectBuildDir/generated"

sourceSets {
    main {
		java.srcDir("$openApiOutputDir/src/main/java")
    }
}

openApiGenerate {
    generatorName = "spring"
    inputSpec = "$rootDir/src/main/resources/api.yaml"
    outputDir = openApiOutputDir
    apiPackage = "com.github.vitaliiev.t1aspect.api"
    modelPackage = "com.github.vitaliiev.t1aspect.model"
    apiFilesConstrainedTo.add("")
    modelFilesConstrainedTo.add("")
    supportingFilesConstrainedTo.add("ApiUtil.java")
	configOptions = mapOf(
		"delegatePattern" to "true",
		"title" to "t1aspect",
		"useJakartaEe" to "true",
		"openApiNullable" to "false",
	)

    validateSpec = true

    typeMappings = mapOf(
		"OffsetDateTime" to "java.time.LocalDateTime",
	)
}

tasks.compileJava {
    dependsOn("openApiGenerate")
}