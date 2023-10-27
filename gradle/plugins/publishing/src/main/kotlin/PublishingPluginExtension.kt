import org.gradle.api.provider.Property

interface PublishingPluginExtension {
    val name: Property<String>
    val description: Property<String>
    val version: Property<String>
}
