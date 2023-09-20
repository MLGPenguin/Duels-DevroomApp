package me.superpenguin.duels.files

import me.superpenguin.duels.Duels
import java.io.File


/**
 * Handles Json Config files
 * @param relativePath the path beginning after your plugins data folder
 * @param isResource whether the file is located in the resources directory of the [plugin]
 */
abstract class JsonConfig<T>(val plugin: Duels, val file: File, val relativePath: String, val isResource: Boolean) {
    constructor(plugin: Duels, fileName: String, isResource: Boolean = false): this(plugin, File(plugin.dataFolder, fileName), fileName, isResource)

    init {
        if (!file.exists()) runCatching {
            file.parentFile.mkdirs()
            if (isResource) plugin.saveResource(relativePath, false)
            else file.createNewFile()
        }
    }

}
