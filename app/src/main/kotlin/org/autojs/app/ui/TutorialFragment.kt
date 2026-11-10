package org.autojs.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.autojs.app.R

class TutorialFragment : Fragment() {

    private sealed interface Row {
        data class Header(val title: String) : Row
        data class Topic(val index: Int, val topic: TutorialTopic) : Row
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_tutorial, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rows = buildRows()
        val rv = view.findViewById<RecyclerView>(R.id.tutorial_list)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = Adapter(rows)
    }

    private fun buildRows(): List<Row> {
        val out = ArrayList<Row>()
        TutorialData.grouped.forEach { (category, list) ->
            out.add(Row.Header(category))
            list.forEach { topic -> out.add(Row.Topic(TutorialData.topics.indexOf(topic), topic)) }
        }
        return out
    }

    private class Adapter(val rows: List<Row>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(position: Int) = when (rows[position]) {
            is Row.Header -> TYPE_HEADER else -> TYPE_TOPIC
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == TYPE_HEADER) {
                HeaderVH(inflater.inflate(R.layout.item_tutorial_header, parent, false))
            } else {
                TopicVH(inflater.inflate(R.layout.item_tutorial, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val row = rows[position]) {
                is Row.Header -> (holder as HeaderVH).bind(row.title)
                is Row.Topic -> (holder as TopicVH).bind(row.index, row.topic)
            }
        }

        override fun getItemCount(): Int = rows.size

        private class HeaderVH(v: View) : RecyclerView.ViewHolder(v) {
            private val title: TextView = v.findViewById(R.id.category_title)
            fun bind(t: String) { title.text = t }
        }

        private class TopicVH(v: View) : RecyclerView.ViewHolder(v) {
            private val title: TextView = v.findViewById(R.id.tutorial_title)
            private val summary: TextView = v.findViewById(R.id.tutorial_summary)
            private val tag: TextView = v.findViewById(R.id.tutorial_sample_tag)
            fun bind(index: Int, topic: TutorialTopic) {
                title.text = topic.title
                summary.text = topic.summary
                tag.visibility = if (topic.sample != null) View.VISIBLE else View.GONE
                itemView.setOnClickListener {
                    itemView.context.startActivity(
                        Intent(itemView.context, TutorialActivity::class.java)
                            .putExtra("topic_index", index)
                    )
                }
            }
        }

        companion object {
            const val TYPE_HEADER = 0
            const val TYPE_TOPIC = 1
        }
    }
}
