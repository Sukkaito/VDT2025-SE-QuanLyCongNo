plugins {
	java
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "vn.viettel"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
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
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	// https://mvnrepository.com/artifact/org.modelmapper/modelmapper
	implementation("org.modelmapper:modelmapper:3.2.3")
	// https://mvnrepository.com/artifact/org.apache.poi/poi
	implementation("org.apache.poi:poi:5.4.1")
	implementation("org.apache.poi:poi-ooxml:5.4.1")
	// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-impl
	implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
	// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-jackson
	implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
	// https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine
	implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

ext["spring-security.version"]="6.4.5"