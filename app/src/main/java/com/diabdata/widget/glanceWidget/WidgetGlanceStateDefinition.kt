package com.diabdata.widget.glanceWidget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.glance.state.GlanceStateDefinition
import com.diabdata.widget.WidgetState
import com.diabdata.widget.widgetDataStore
import java.io.File

object WidgetGlanceStateDefinition :
    GlanceStateDefinition<WidgetState> {

    override fun getLocation(context: Context, fileKey: String): File {
        val dir = File(context.filesDir, "glance")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "widget_state.json")
    }

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<WidgetState> =
        context.widgetDataStore
}
