package com.kaghaz.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: NotesAdapter
    private lateinit var emptyView: TextView
    private lateinit var countView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notesDir().mkdirs()

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        emptyView = findViewById(R.id.emptyView)
        countView = findViewById(R.id.countView)

        adapter = NotesAdapter { file -> openEditor(file) }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            openEditor(null)
        }

        reload()
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun notesDir(): File = File(filesDir, "notes")

    private fun reload() {
        val notes = notesDir().listFiles { f -> f.name.endsWith(".md") }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
        adapter.submit(notes)
        emptyView.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
        countView.text = getString(R.string.notes_count, notes.size)
    }

    private fun openEditor(file: File?) {
        val intent = Intent(this, EditorActivity::class.java)
        file?.let { intent.putExtra(EditorActivity.EXTRA_NAME, it.name) }
        startActivity(intent)
    }

    data class Note(val file: File, val title: String, val snippet: String, val date: String)

    inner class NotesAdapter(private val onClick: (File) -> Unit) :
        RecyclerView.Adapter<NotesAdapter.VH>() {

        private val items = mutableListOf<Note>()
        private val fmt = SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.getDefault())

        fun submit(list: List<File>) {
            items.clear()
            list.forEach { f ->
                val text = try { f.readText() } catch (e: Exception) { "" }
                val lines = text.lines().filter { it.isNotBlank() }
                val title = lines.firstOrNull()
                    ?.trimStart('#', ' ')
                    ?.trim()
                    ?.ifEmpty { getString(R.string.untitled) }
                    ?: getString(R.string.untitled)
                val snippet = lines.drop(1).joinToString(" ").take(120)
                items.add(Note(f, title, snippet, fmt.format(Date(f.lastModified()))))
            }
            notifyDataSetChanged()
        }

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.noteTitle)
            val snippet: TextView = v.findViewById(R.id.noteSnippet)
            val date: TextView = v.findViewById(R.id.noteDate)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note, parent, false)
            return VH(v)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val n = items[position]
            holder.title.text = n.title
            holder.snippet.text = n.snippet
            holder.date.text = n.date
            holder.itemView.setOnClickListener { onClick(n.file) }
            holder.itemView.setOnLongClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle(R.string.delete_title)
                    .setMessage(getString(R.string.delete_msg, n.title))
                    .setPositiveButton(R.string.delete) { _, _ ->
                        n.file.delete()
                        reload()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }
        }
    }
}
