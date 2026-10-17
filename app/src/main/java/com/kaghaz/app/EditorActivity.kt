package com.kaghaz.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.io.File

class EditorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NAME = "name"
    }

    private lateinit var titleInput: EditText
    private lateinit var bodyInput: EditText
    private lateinit var preview: WebView
    private var fileName: String? = null
    private var showingPreview = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        titleInput = findViewById(R.id.titleInput)
        bodyInput = findViewById(R.id.bodyInput)
        preview = findViewById(R.id.preview)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.saveButton).setOnClickListener { save() }
        findViewById<MaterialButton>(R.id.previewButton).setOnClickListener { togglePreview() }

        fileName = intent.getStringExtra(EXTRA_NAME)
        fileName?.let { load(File(File(filesDir, "notes"), it)) }
    }

    private fun load(file: File) {
        if (!file.exists()) return
        val text = try { file.readText() } catch (e: Exception) { "" }
        val firstLine = text.lines().firstOrNull() ?: ""
        if (firstLine.startsWith("#")) {
            titleInput.setText(firstLine.trimStart('#', ' ').trim())
            val idx = text.indexOf("\n\n")
            bodyInput.setText(if (idx >= 0) text.substring(idx + 2) else "")
        } else {
            bodyInput.setText(text)
        }
    }

    private fun save() {
        val dir = File(filesDir, "notes").apply { mkdirs() }
        val title = titleInput.text.toString().trim()
            .ifEmpty { getString(R.string.untitled) }
        val content = "# $title\n\n" + bodyInput.text.toString()
        val target = fileName ?: (System.currentTimeMillis().toString() + ".md")
            .also { fileName = it }
        try {
            File(dir, target).writeText(content)
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.save_error, Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun togglePreview() {
        val btn = findViewById<MaterialButton>(R.id.previewButton)
        if (!showingPreview) {
            preview.visibility = View.VISIBLE
            bodyInput.visibility = View.GONE
            preview.settings.javaScriptEnabled = true
            preview.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    val md = bodyInput.text.toString()
                    val b64 = Base64.encodeToString(md.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                    view.evaluateJavascript(
                        "render(decodeURIComponent(escape(window.atob('" + b64 + "'))))", null
                    )
                }
            }
            preview.loadUrl("file:///android_asset/preview.html")
            btn.setText(R.string.edit)
        } else {
            preview.visibility = View.GONE
            bodyInput.visibility = View.VISIBLE
            btn.setText(R.string.preview)
        }
        showingPreview = !showingPreview
    }
}
