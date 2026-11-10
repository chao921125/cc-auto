package org.autojs.app.script

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.autojs.app.R
import java.text.SimpleDateFormat
import java.util.Locale

class FileEntryAdapter(
    private val listener: Listener
) : ListAdapter<WorkspaceManager.FileEntry, FileEntryAdapter.VH>(DIFF) {

    interface Listener {
        fun onOpen(entry: WorkspaceManager.FileEntry)
        fun onMenu(anchor: View, entry: WorkspaceManager.FileEntry)
    }

    private val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.file_icon)
        private val name: TextView = view.findViewById(R.id.file_name)
        private val info: TextView = view.findViewById(R.id.file_info)
        private val more: ImageButton = view.findViewById(R.id.btn_more)

        fun bind(e: WorkspaceManager.FileEntry) {
            name.text = e.name
            icon.setImageResource(
                when {
                    e.isProject -> android.R.drawable.ic_menu_my_calendar
                    e.isDir -> android.R.drawable.ic_menu_view
                    else -> android.R.drawable.ic_menu_edit
                }
            )
            info.text = when {
                e.isProject -> "项目"
                e.isDir -> "文件夹"
                else -> "%.1f KB · %s".format(e.size / 1024.0, df.format(e.lastModified))
            }
            itemView.setOnClickListener { listener.onOpen(e) }
            more.setOnClickListener { anchor -> listener.onMenu(anchor, e) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<WorkspaceManager.FileEntry>() {
            override fun areItemsTheSame(a: WorkspaceManager.FileEntry, b: WorkspaceManager.FileEntry) = a.path == b.path
            override fun areContentsTheSame(a: WorkspaceManager.FileEntry, b: WorkspaceManager.FileEntry) = a == b
        }
    }
}
