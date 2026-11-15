package com.diabdata.feature.settings.ui.components.changelog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun LazyListScope.changelogSection(title: String, contents: List<String>) {
    item {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
    item { Spacer(Modifier.height(4.dp)) }
    items(contents) { line ->
        Text("\t• $line")
    }
    item { Spacer(Modifier.height(8.dp)) }
}
