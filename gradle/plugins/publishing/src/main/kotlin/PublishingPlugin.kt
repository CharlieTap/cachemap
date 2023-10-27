import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.provideDelegate

class PublishingPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val extension = project.extensions.create<PublishingPluginExtension>("publishingConfig")

        project.plugins.apply("maven-publish")
        project.plugins.apply("signing")

        project.afterEvaluate {

            project.extensions.configure<PublishingExtension>("publishing") {

                repositories {
                    maven {
                        name = "OSSRH"
                        url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

                        credentials {
                            val ossrhUsername: String? by project
                            val ossrhPassword: String? by project

                            username = ossrhUsername
                            password = ossrhPassword
                        }
                    }
                }

                publications {

                    create<MavenPublication>(project.name) {

                        val component = project.components.findByName("kotlinMultiplatform")
                        if (component != null) {
                            from(component)
                        }

                        groupId = "io.github.charlietap"
                        artifactId = project.name
                        version = extension.version.get()

                        pom {
                            name.set(extension.name)
                            description.set(extension.description)
                            url.set("https://github.com/CharlieTap/cachemap")
                            licenses {
                                license {
                                    name.set("Apache-2.0")
                                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                                }
                                license {
                                    name.set("MIT")
                                    url.set("https://opensource.org/licenses/MIT")
                                }
                            }
                            developers {
                                developer {
                                    id.set("CharlieTap")
                                    name.set("Charlie Tapping")
                                }
                            }
                            scm {
                                connection.set("scm:git:https://github.com/CharlieTap/cachemap.git")
                                developerConnection.set("scm:git:ssh://github.com/CharlieTap/cachemap.git")
                                url.set("https://github.com/CharlieTap/cachemap")
                            }
                        }
                    }
                }
            }

            project.extensions.configure<org.gradle.plugins.signing.SigningExtension>("signing") {

                val signingKey: String? by project
                val signingPassword: String? by project

                if(signingKey != null) {
                    useInMemoryPgpKeys(signingKey, signingPassword)
                }

                val mavenPublication = project.extensions.getByType<PublishingExtension>().publications.getByName(project.name) as MavenPublication
                sign(mavenPublication)
            }
        }
    }
}
