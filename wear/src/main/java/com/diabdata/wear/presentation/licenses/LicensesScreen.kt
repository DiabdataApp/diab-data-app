package com.diabdata.wear.presentation.licenses

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.diabdata.wear.R
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withJson

@Composable
fun LicensesScreen() {
    val transformationSpec = rememberTransformationSpec()
    val transformingLazyColumnState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        modifier = Modifier.fillMaxSize(),
        scrollState = transformingLazyColumnState
    ) { contentPadding ->
        val context = LocalContext.current
        val libs = remember { Libs.Builder().withJson(context, R.raw.aboutlibraries).build() }

        TransformingLazyColumn(
            state = transformingLazyColumnState,
            contentPadding = contentPadding
        ) {
            item {
                ListHeader(
                    modifier = Modifier.transformedHeight(this, transformationSpec)
                ) {
                    Text(
                        stringResource(
                            R.string.show_licenses_button_label
                        )
                    )
                }
            }
            items(libs.libraries) { library ->
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    onClick = {}
                ) {
                    Text(library.name)
                }
            }
        }
    }
}
