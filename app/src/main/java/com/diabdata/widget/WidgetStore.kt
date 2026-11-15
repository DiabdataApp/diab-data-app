package com.diabdata.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore

val Context.widgetDataStore: DataStore<WidgetState> by dataStore(
    fileName = "widget_state.json",
    serializer = WidgetStateSerializer
)
