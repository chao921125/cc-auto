package org.autojs.app.script

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import org.json.JSONObject
import java.io.File

/**
 * 工作区管理：脚本与项目存放于外部公共目录 /sdcard/CcAuto。
 *
 * 目录结构：
 *   CcAuto/
 *     projects/<项目名>/project.json + main.js + 任意子目录/文件
 *     samples/            教程示例运行时复制到此
 *     *.js                根级散落脚本
 */
object WorkspaceManager {

    const val ROOT_NAME = "CcAuto"
    const val PROJECTS = "projects"
    const val SAMPLES = "samples"
    const val PROJECT_FILE = "project.json"

    data class FileEntry(
        val name: String,
        val path: String,
        val isDir: Boolean,
        val isProject: Boolean,
        val size: Long,
        val lastModified: Long
    )

    val root: File get() = File(Environment.getExternalStorageDirectory(), ROOT_NAME)

    fun ensureRoot() {
        if (!root.exists()) root.mkdirs()
        File(root, PROJECTS).mkdirs()
    }

    fun hasAllFilesAccess(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    /** 列出目录内容：文件夹在前、文件在后，各自按名称排序。无权限时返回空。 */
    fun list(dir: File): List<FileEntry> {
        val children = dir.listFiles() ?: return emptyList()
        val entries = children.filter { it.name.startsWith(".").not() }.map { f ->
            FileEntry(
                name = f.name,
                path = f.absolutePath,
                isDir = f.isDirectory,
                isProject = f.isDirectory && File(f, PROJECT_FILE).exists(),
                size = if (f.isFile) f.length() else 0L,
                lastModified = f.lastModified()
            )
        }
        return entries.sortedWith(
            compareByDescending<FileEntry> { it.isDir }.thenBy { it.name.lowercase() }
        )
    }

    fun isJs(name: String): Boolean = name.endsWith(".js", ignoreCase = true)

    fun createProject(name: String): File? {
        ensureRoot()
        val dir = File(File(root, PROJECTS), name.safeName())
        if (dir.exists()) return null
        if (!dir.mkdirs()) return null
        val main = File(dir, "main.js")
        main.writeText("// 项目 ${name}\n// 入口脚本\n\n\"auto\";\n\nconsole.show();\nlog(\"Hello from $name\");\n")
        JSONObject()
            .put("name", name)
            .put("main", "main.js")
            .put("packageName", "org.autojs.project." + name.safeName().replace("-", "_"))
            .put("versionName", "1.0.0")
            .put("versionCode", 1)
            .let { File(dir, PROJECT_FILE).writeText(it.toString(2)) }
        return dir
    }

    fun createFolder(dir: File, name: String): File? {
        val f = File(dir, name.safeName())
        return if (!f.exists() && f.mkdirs()) f else null
    }

    fun createFile(dir: File, name: String): File? {
        val fileName = if (isJs(name)) name.safeName() else name.safeName() + ".js"
        val f = File(dir, fileName)
        if (f.exists()) return null
        f.writeText("// ${f.name}\n\n\"auto\";\n\n")
        return f
    }

    /** 删除文件或目录（目录递归）。 */
    fun delete(entry: FileEntry): Boolean = deletePath(File(entry.path))

    private fun deletePath(f: File): Boolean {
        if (f.isDirectory) f.listFiles()?.forEach { deletePath(it) }
        return f.delete()
    }

    fun rename(entry: FileEntry, newName: String): String? {
        val old = File(entry.path)
        val target = if (entry.isDir) newName.safeName()
                     else if (isJs(newName)) newName.safeName()
                     else newName.safeName() + ".js"
        val dest = File(old.parentFile, target)
        return if (old.renameTo(dest)) dest.absolutePath else null
    }

    /** 项目主脚本绝对路径，非项目返回 null。 */
    fun projectMain(projectDir: File): String? {
        val pf = File(projectDir, PROJECT_FILE)
        if (!pf.exists()) return null
        return try {
            val main = JSONObject(pf.readText()).optString("main", "main.js")
            File(projectDir, main).absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /** 从 SAF content URI 导入文件到目标目录，返回落地路径。 */
    fun importFrom(context: Context, uri: Uri, destDir: File): String? {
        ensureRoot()
        val name = queryDisplayName(context, uri) ?: "imported_${System.currentTimeMillis()}.js"
        val dest = File(destDir, name)
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            dest.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /** 将 assets/scripts 下的示例复制到工作区 samples 目录，返回落地路径。 */
    fun stageSample(context: Context, assetName: String): String? {
        ensureRoot()
        val samplesDir = File(root, SAMPLES).apply { mkdirs() }
        val dest = File(samplesDir, assetName)
        return try {
            context.assets.open("scripts/$assetName").use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            dest.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = c.getString(idx)
            }
        }
        return name
    }

    private fun String.safeName(): String =
        trim().replace(Regex("[/\\\\:*?\"<>|]"), "_").ifBlank { "untitled" }
}
