package circlet.plugins.pipelines.services

import circlet.pipelines.config.dsl.scriptdefinition.*
import circlet.plugins.pipelines.utils.*
import klogging.*
import java.io.*
import kotlin.script.experimental.intellij.*

class CircletScriptDefinitionsProvider : ScriptDefinitionsProvider {
    override val id: String = "CircletScriptDefinitionsProvider"

    override fun getDefinitionClasses(): Iterable<String> {
        return listOf(ProjectScriptDefinition::class.qualifiedName!!)
    }

    override fun getDefinitionsClassPath(): Iterable<File> {
        val url = find(CircletScriptDefinitionsProvider::class, "pipelines-config-dsl-scriptdefinition")
        val file = File(url.file)
        if (!file.exists()) {
            throw Exception("File with ProjectScriptDefinition doesn't exist")
        }
        return listOf(file)
    }

    override fun useDiscovery(): Boolean {
        return true
    }
}
