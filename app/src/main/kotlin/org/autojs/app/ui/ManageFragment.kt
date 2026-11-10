package org.autojs.app.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.autojs.app.R
import org.autojs.app.engine.EngineManager
import org.autojs.app.engine.EngineService
import org.autojs.core.runtime.ScriptRuntime

class ManageFragment : Fragment() {

    private lateinit var manager: EngineManager
    private lateinit var list: RecyclerView
    private lateinit var emptyView: View
    private lateinit var countText: TextView
    private val handler = Handler(Looper.getMainLooper())

    private val ticker = object : Runnable {
        override fun run() {
            render()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_manage, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        manager = EngineManager.get(requireContext())
        list = view.findViewById(R.id.runtime_list)
        emptyView = view.findViewById(R.id.empty_view)
        countText = view.findViewById(R.id.running_count)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = RuntimeAdapter()

        view.findViewById<Button>(R.id.btn_stop_all).setOnClickListener {
            EngineService.stopAll(requireContext())
            handler.post { render() }
        }

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.log_container, LogFragment())
                .commit()
        }
        render()
    }

    override fun onResume() {
        super.onResume()
        handler.post(ticker)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(ticker)
    }

    private fun render() {
        val runtimes = manager.getAllRuntimes().toList()
        countText.text = "运行中 (${runtimes.size})"
        emptyView.visibility = if (runtimes.isEmpty()) View.VISIBLE else View.GONE
        (list.adapter as? RuntimeAdapter)?.submit(runtimes)
    }

    private inner class RuntimeAdapter : RecyclerView.Adapter<RuntimeAdapter.VH>() {
        private var items: List<ScriptRuntime> = emptyList()

        fun submit(newItems: List<ScriptRuntime>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_runtime, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

        override fun getItemCount(): Int = items.size

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            private val name: TextView = view.findViewById(R.id.runtime_name)
            private val info: TextView = view.findViewById(R.id.runtime_info)
            private val stop: Button = view.findViewById(R.id.btn_stop)

            fun bind(rt: ScriptRuntime) {
                name.text = rt.source.name
                val elapsed = if (rt.startedAt() > 0)
                    (System.currentTimeMillis() - rt.startedAt()) / 1000 else 0
                info.text = "${rt.id}  ·  已运行 ${elapsed}s  ·  ${if (rt.isAlive()) "运行中" else "已结束"}"
                stop.setOnClickListener {
                    manager.stopScript(rt.id)
                    handler.post { render() }
                }
            }
        }
    }
}
