package com.intellij.settingsSync

import com.intellij.util.io.isFile
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

internal class MockSettingsSyncIdeMediator : SettingsSyncIdeMediator {
  private val files = mutableMapOf<String, String>()

  override fun applyToIde(snapshot: SettingsSnapshot) {
    for (fileState in snapshot.fileStates) {
      if (fileState is FileState.Modified) {
        files[fileState.file] = String(fileState.content, Charset.defaultCharset())
      }
      else {
        files.remove(fileState.file)
      }
    }
  }

  override fun activateStreamProvider() {
  }

  override fun removeStreamProvider() {
  }

  override fun collectFilesToExportFromSettings(appConfigPath: Path): () -> Collection<Path> {
    return getAllFilesFromSettings(appConfigPath)
  }

  companion object {
    fun getAllFilesFromSettings(appConfigPath: Path): () -> Collection<Path> {
      return {
        Files.walk(appConfigPath).filter { it.isFile() }.toList()
      }
    }
  }
}
