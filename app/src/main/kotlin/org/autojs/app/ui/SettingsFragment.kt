package org.autojs.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.autojs.app.R
import org.autojs.app.script.WorkspaceManager

class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.item_accessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        view.findViewById<View>(R.id.item_overlay).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${requireContext().packageName}"))
                )
            }
        }

        view.findViewById<View>(R.id.item_storage).setOnClickListener {
            ScriptsFragment.requestAllFilesAccess(requireContext())
        }

        view.findViewById<View>(R.id.item_keep_alive).setOnClickListener {
            Toast.makeText(requireContext(), "引擎运行期间已自动前台保活", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.item_about).setOnClickListener {
            Toast.makeText(requireContext(), "CcAuto v1.0.0", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val granted = WorkspaceManager.hasAllFilesAccess(requireContext())
        view?.findViewById<android.widget.TextView>(R.id.item_storage_summary)?.text =
            if (granted) "已授权 · 可读写 /sdcard/CcAuto 工作目录"
            else "未授权 · 点此前往开启「所有文件访问」"
    }
}
