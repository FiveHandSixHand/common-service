package com.fhsh.daitda;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the build.gradle configuration introduced in the "configure common module gradle" commit.
 *
 * The PR converts the project from a runnable Spring Boot application to a reusable library
 * module and wires up Maven publishing to GitHub Packages.  These tests validate every
 * intentional change that was made:
 *
 *  - java-library plugin replaces java + spring-boot plugins
 *  - maven-publish plugin is present
 *  - JPA starter is declared with the `api` scope (transitive exposure to consumers)
 *  - Publishing block targets GitHub Packages with artifactId "common"
 *  - Credentials are resolved from GPR_USER / GPR_TOKEN environment variables
 *  - Removed artefacts (spring-boot plugin, spring-cloud BOM, web/feign/postgresql deps) are absent
 */
@DisplayName("build.gradle configuration")
class BuildConfigurationTest {

    private static String buildScript;
    private static List<String> lines;

    @BeforeAll
    static void readBuildGradle() throws IOException {
        // Walk up from the compiled test-classes directory to the project root.
        // The build file sits at <project-root>/build.gradle.
        Path projectRoot = resolveProjectRoot();
        Path buildFile = projectRoot.resolve("build.gradle");
        assertThat(buildFile).as("build.gradle must exist at project root").exists();

        buildScript = Files.readString(buildFile);
        lines = Files.readAllLines(buildFile);
    }

    // -----------------------------------------------------------------------
    // Plugin declarations
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("applies the java-library plugin (not plain java)")
    void appliesJavaLibraryPlugin() {
        assertThat(buildScript).contains("id 'java-library'");
    }

    @Test
    @DisplayName("applies the maven-publish plugin")
    void appliesMavenPublishPlugin() {
        assertThat(buildScript).contains("id 'maven-publish'");
    }

    @Test
    @DisplayName("does NOT apply the org.springframework.boot plugin")
    void doesNotApplySpringBootPlugin() {
        assertThat(buildScript).doesNotContain("id 'org.springframework.boot'");
    }

    @Test
    @DisplayName("does NOT apply the plain java plugin")
    void doesNotApplyPlainJavaPlugin() {
        // 'java-library' is fine; the bare 'java' plugin must not appear
        assertThat(buildScript).doesNotContain("id 'java'");
    }

    // -----------------------------------------------------------------------
    // Dependency declarations
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("declares spring-boot-starter-data-jpa with api scope")
    void jpaStarterDeclaredWithApiScope() {
        assertThat(buildScript)
                .containsPattern(Pattern.compile(
                        "api\\s+'org\\.springframework\\.boot:spring-boot-starter-data-jpa'"));
    }

    @Test
    @DisplayName("JPA starter is NOT declared with implementation scope")
    void jpaStarterNotDeclaredWithImplementationScope() {
        assertThat(buildScript).doesNotContain(
                "implementation 'org.springframework.boot:spring-boot-starter-data-jpa'");
    }

    @Test
    @DisplayName("spring-boot-starter-test is present for tests")
    void testDependencyPresent() {
        assertThat(buildScript).contains(
                "testImplementation 'org.springframework.boot:spring-boot-starter-test'");
    }

    @Test
    @DisplayName("lombok is declared as compileOnly")
    void lombokCompileOnly() {
        assertThat(buildScript).contains("compileOnly 'org.projectlombok:lombok'");
    }

    @Test
    @DisplayName("lombok annotation processor is declared")
    void lombokAnnotationProcessor() {
        assertThat(buildScript).contains("annotationProcessor 'org.projectlombok:lombok'");
    }

    @Test
    @DisplayName("spring-boot-starter-web is removed")
    void webStarterAbsent() {
        assertThat(buildScript).doesNotContain("spring-boot-starter-web");
    }

    @Test
    @DisplayName("spring-cloud-starter-openfeign is removed")
    void openfeignAbsent() {
        assertThat(buildScript).doesNotContain("spring-cloud-starter-openfeign");
    }

    @Test
    @DisplayName("postgresql runtime dependency is removed")
    void postgresqlAbsent() {
        assertThat(buildScript).doesNotContain("org.postgresql:postgresql");
    }

    @Test
    @DisplayName("junit-platform-launcher testRuntimeOnly is removed")
    void junitPlatformLauncherAbsent() {
        assertThat(buildScript).doesNotContain("junit-platform-launcher");
    }

    // -----------------------------------------------------------------------
    // Spring Cloud BOM / ext block removal
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("springCloudVersion ext property is removed")
    void springCloudVersionAbsent() {
        assertThat(buildScript).doesNotContain("springCloudVersion");
    }

    @Test
    @DisplayName("Spring Cloud dependency management BOM is removed")
    void springCloudDependencyManagementAbsent() {
        assertThat(buildScript).doesNotContain("spring-cloud-dependencies");
    }

    @Test
    @DisplayName("dependencyManagement imports block is removed")
    void dependencyManagementImportsAbsent() {
        assertThat(buildScript)
                .as("The dependencyManagement { imports { ... } } block should be gone")
                .doesNotContainPattern(Pattern.compile("dependencyManagement\\s*\\{[^}]*imports"));
    }

    // -----------------------------------------------------------------------
    // Publishing configuration
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("publishing block is present")
    void publishingBlockPresent() {
        assertThat(buildScript).containsPattern(Pattern.compile("publishing\\s*\\{"));
    }

    @Test
    @DisplayName("publication is named gpr and uses MavenPublication")
    void publicationNamedGpr() {
        assertThat(buildScript).containsPattern(Pattern.compile(
                "gpr\\s*\\(\\s*MavenPublication\\s*\\)"));
    }

    @Test
    @DisplayName("publication includes all java components")
    void publicationFromComponents() {
        assertThat(buildScript).contains("from components.java");
    }

    @Test
    @DisplayName("publication artifactId is 'common'")
    void publicationArtifactId() {
        assertThat(buildScript).containsPattern(Pattern.compile(
                "artifactId\\s*=\\s*['\"]common['\"]"));
    }

    @Test
    @DisplayName("publishing repository is named GitHubPackages")
    void repositoryNamedGitHubPackages() {
        assertThat(buildScript).containsPattern(Pattern.compile(
                "name\\s*=\\s*['\"]GitHubPackages['\"]"));
    }

    @Test
    @DisplayName("publishing repository URL points to the correct GitHub Packages path")
    void repositoryUrl() {
        assertThat(buildScript).contains(
                "https://maven.pkg.github.com/FiveHandSixHand/common-service");
    }

    @Test
    @DisplayName("publishing credentials use GPR_USER environment variable")
    void credentialsUsernameFromEnv() {
        assertThat(buildScript).containsPattern(Pattern.compile(
                "username\\s*=\\s*System\\.getenv\\(['\"]GPR_USER['\"]\\)"));
    }

    @Test
    @DisplayName("publishing credentials use GPR_TOKEN environment variable")
    void credentialsPasswordFromEnv() {
        assertThat(buildScript).containsPattern(Pattern.compile(
                "password\\s*=\\s*System\\.getenv\\(['\"]GPR_TOKEN['\"]\\)"));
    }

    @Test
    @DisplayName("credentials do NOT hard-code a username or password value")
    void credentialsNotHardCoded() {
        // Make sure neither field is assigned a literal string that looks like a credential
        assertThat(buildScript)
                .doesNotContainPattern(Pattern.compile("username\\s*=\\s*['\"][a-zA-Z0-9_-]{4,}['\"]"))
                .doesNotContainPattern(Pattern.compile("password\\s*=\\s*['\"][a-zA-Z0-9_-]{4,}['\"]"));
    }

    // -----------------------------------------------------------------------
    // Structural integrity / regression guards
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("group is set to com.fhsh")
    void groupIsSet() {
        assertThat(buildScript).containsPattern(Pattern.compile(
                "group\\s*=\\s*['\"]com\\.fhsh['\"]"));
    }

    @Test
    @DisplayName("Java toolchain targets Java 17")
    void javaToolchainVersion() {
        assertThat(buildScript).containsPattern(Pattern.compile(
                "languageVersion\\s*=\\s*JavaLanguageVersion\\.of\\(\\s*17\\s*\\)"));
    }

    @Test
    @DisplayName("io.spring.dependency-management plugin is still present")
    void springDependencyManagementPluginPresent() {
        assertThat(buildScript).contains("id 'io.spring.dependency-management'");
    }

    @Test
    @DisplayName("mavenCentral is the only dependency repository")
    void mavenCentralRepository() {
        assertThat(buildScript).contains("mavenCentral()");
    }

    @Test
    @DisplayName("build script is non-empty and structurally sound")
    void buildScriptNonEmpty() {
        assertThat(lines)
                .as("build.gradle should have a meaningful number of lines")
                .hasSizeGreaterThan(10);
        assertThat(buildScript)
                .startsWith("plugins")
                .contains("dependencies")
                .contains("publishing");
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /**
     * Resolves the Gradle project root from the working directory or class-path.
     * Works both when running via {@code ./gradlew test} (cwd = project root) and
     * when running from an IDE (cwd may vary).
     */
    private static Path resolveProjectRoot() {
        // 1. Try the current working directory first (most reliable under Gradle).
        Path cwd = Paths.get("").toAbsolutePath();
        if (cwd.resolve("build.gradle").toFile().exists()) {
            return cwd;
        }

        // 2. Walk up from the class-file location until we find build.gradle.
        Path candidate = Paths.get(
                BuildConfigurationTest.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .getPath());
        while (candidate != null) {
            if (candidate.resolve("build.gradle").toFile().exists()) {
                return candidate;
            }
            candidate = candidate.getParent();
        }

        throw new IllegalStateException(
                "Could not locate the project root containing build.gradle. cwd=" + cwd);
    }
}