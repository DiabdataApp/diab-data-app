package com.diabdata.shared.utils.utils

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import com.diabdata.shared.R as Shared

fun Uri.toReadablePath(context: Context): String {
    val docId = try {
        DocumentsContract.getTreeDocumentId(this)
    } catch (e: Exception) {
        this.lastPathSegment
    }

    return docId
        ?.replace("primary:", "${context.getString(Shared.string.common_internal_storage)} > ")
        ?.replace("home:", "${context.getString(Shared.string.common_storage_home)} > ")
        ?.replace(
            Regex("^([0-9A-F]{4}-[0-9A-F]{4}):"),
            "${context.getString(Shared.string.common_storage_sd_card)} > "
        )
        ?: this.toString()
}

fun String.uriStringToReadablePath(context: Context): String = this.toUri().toReadablePath(context)
