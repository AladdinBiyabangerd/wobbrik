package com.aladdin.wobbrik

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import org.json.JSONObject

internal const val SupportEmail = "aladdinbiyabangerd@gmail.com"
internal data class LegalDocument(val id: String, val title: String, val text: String)

/** Bundled copy of AppBackend's canonical Wobbrik content. Works before deployment and offline. */
internal fun loadLegalDocuments(context: Context): List<LegalDocument> {
    val app = context.assets.open("wobbrik-legal.json").bufferedReader().use { JSONObject(it.readText()) }
    require(app.getString("id") == "wobbrik" && app.getString("package") == "com.aladdin.wobbrik")
    val documents = app.getJSONObject("locales").getJSONObject("az").getJSONObject("documents")
    return listOf("privacy", "terms", "delete-account").map { id ->
        val doc = documents.getJSONObject(id)
        val sections = doc.getJSONArray("sections")
        LegalDocument(id, doc.getString("title"), buildString {
            appendLine(doc.getString("intro"))
            appendLine("\n${app.getString("developer")} · ${app.getString("updated")}")
            for (index in 0 until sections.length()) {
                val section = sections.getJSONObject(index)
                appendLine("\n${section.getString("title")}\n${section.getString("body")}")
            }
        })
    }
}

@Composable
internal fun LegalDocumentsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val documents = remember(context) { runCatching { loadLegalDocuments(context) }.getOrNull() }
    var selected by remember { mutableStateOf("privacy") }
    var choosing by remember { mutableStateOf(false) }
    val current = documents?.first { it.id == selected }
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(current?.title ?: "Məxfilik") },
        text = {
            key(selected) {
                Text(current?.text ?: "Sənəd oxunmadı. Dəstək: $SupportEmail",
                    Modifier.verticalScroll(rememberScrollState()), fontSize = GameTokens.Body)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Bağla", fontWeight = FontWeight.Bold) } },
        dismissButton = {
            Box {
                TextButton(onClick = { choosing = true }, enabled = documents != null) { Text("Sənədi seç") }
                DropdownMenu(expanded = choosing, onDismissRequest = { choosing = false }) {
                    documents?.forEach { doc ->
                        DropdownMenuItem(text = { Text(doc.title) }, onClick = { selected = doc.id; choosing = false })
                    }
                }
            }
        }, containerColor = GameTokens.Surface, titleContentColor = GameTokens.Text, textContentColor = GameTokens.Text)
}
