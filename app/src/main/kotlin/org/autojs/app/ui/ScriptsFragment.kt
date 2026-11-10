package org.autojs.app.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.autojs.app.R
import org.autojs.app.engine.EngineService
import org.autojs.app.script.FileEntryAdapter
import org.autojs.app.script.WorkspaceManager
import org.autojs.app.script.WorkspaceManager.FileEntry
import java.io.File

class ScriptsFragment : Fragment(), FileEntryAdapter.Listener {

    private lateinit var adapter: FileEntryAdapter
    private lateinit var pathText: TextView
    private lateinit var permHint: View
    private var currentDir: File = WorkspaceManager.root

    private val importLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                val path = WorkspaceManager.importFrom(requireContext(), uri, currentDir)
                toast(if (path != null) "已导入: ${File(path).name}" else "导入失败")
                refresh()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_scripts, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        WorkspaceManager.ensureRoot()
        pathText = view.findViewById(R.id.path_text)
        permHint = view.findViewById(R.id.perm_hint)
        permHint.setOnClickListener { requestAllFilesAccess(requireContext()) }

        val rv = view.findViewById<RecyclerView>(R.id.file_list)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = FileEntryAdapter(this)
        rv.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener { showFabMenu(it) }
        view.findViewById<View>(R.id.btn_up).setOnClickListener { goUp() }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = goUp()
            }
        )
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val granted = WorkspaceManager.hasAllFilesAccess(requireContext())
        permHint.visibility = if (granted) View.GONE else View.VISIBLE
        pathText.text = displayPath(currentDir)
        adapter.submitList(if (granted) WorkspaceManager.list(currentDir) else emptyList())
    }

    private fun goUp() {
        if (currentDir.absolutePath == WorkspaceManager.root.absolutePath) {
            requireActivity().moveTaskToBack(true)
        } else {
            currentDir = currentDir.parentFile ?: WorkspaceManager.root
            refresh()
        }
    }

    // region 打开 / 菜单
    override fun onOpen(entry: FileEntry) {
        if (entry.isDir) {
            currentDir = File(entry.path)
            refresh()
        } else {
            openEditor(entry.path)
        }
    }

    override fun onMenu(anchor: View, entry: FileEntry) {
        val popup = PopupMenu(anchor.context, anchor)
        val canRun = entry.isProject || WorkspaceManager.isJs(entry.name)
        if (canRun) popup.menu.add("运行")
        if (!entry.isDir) popup.menu.add("编辑")
        popup.menu.add("重命名")
        popup.menu.add("删除")
        popup.setOnMenuItemClickListener { mi ->
            when (mi.title) {
                "运行" -> runEntry(entry)
                "编辑" -> openEditor(entry.path)
                "重命名" -> doRename(entry)
                "删除" -> doDelete(entry)
            }
            true
        }
        popup.show()
    }

    private fun runEntry(entry: FileEntry) {
        val path = if (entry.isProject) {
            WorkspaceManager.projectMain(File(entry.path))
        } else if (WorkspaceManager.isJs(entry.name)) {
            entry.path
        } else null
        if (path != null) {
            EngineService.startScript(requireContext(), path)
            toast("已启动: ${entry.name}")
        }
    }
    // endregion

    // region 新建 / 导入
    private fun showFabMenu(anchor: View) {
        val popup = PopupMenu(anchor.context, anchor)
        popup.menu.add(0, 1, 0, "新建项目")
        popup.menu.add(0, 2, 1, "新建文件夹")
        popup.menu.add(0, 3, 2, "新建脚本")
        popup.menu.add(0, 4, 3, "导入脚本")
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                1 -> newProject()
                2 -> newItem("新建文件夹") { WorkspaceManager.createFolder(currentDir, it) != null }
                3 -> newItem("新建脚本") { WorkspaceManager.createFile(currentDir, it) != null }
                4 -> importLauncher.launch(arrayOf("*/*"))
            }
            true
        }
        popup.show()
    }

    private fun newProject() = newItem("新建项目") { name ->
        val dir = WorkspaceManager.createProject(name)
        if (dir != null) currentDir = dir
        dir != null
    }

    private fun newItem(title: String, action: (String) -> Boolean) {
        val input = EditText(requireContext()).apply { hint = "名称" }
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setPositiveButton("确定") { _, _ ->
                val ok = action(input.text.toString().ifBlank { "untitled" })
                toast(if (ok) "已创建" else "创建失败（可能已存在）")
                refresh()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun doRename(entry: FileEntry) {
        val input = EditText(requireContext()).apply { setText(entry.name) }
        AlertDialog.Builder(requireContext())
            .setTitle("重命名")
            .setView(input)
            .setPositiveButton("确定") { _, _ ->
                WorkspaceManager.rename(entry, input.text.toString())
                refresh()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun doDelete(entry: FileEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除")
            .setMessage("确定删除 ${entry.name}？")
            .setPositiveButton("删除") { _, _ ->
                WorkspaceManager.delete(entry)
                refresh()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    // endregion

    private fun openEditor(path: String) {
        startActivity(Intent(requireContext(), EditorActivity::class.java)
            .putExtra("script_path", path))
    }

    private fun displayPath(dir: File): String {
        val root = WorkspaceManager.root.absolutePath
        return if (dir.absolutePath == root) "/${WorkspaceManager.ROOT_NAME}"
        else dir.absolutePath.removePrefix(root).trimStart('/')
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    companion object {
        fun requestAllFilesAccess(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    .setData(Uri.parse("package:${context.packageName}"))
                runCatching { context.startActivity(intent) }
                    .onFailure {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:${context.packageName}"))
                        )
                    }
            }
        }
    }
}
