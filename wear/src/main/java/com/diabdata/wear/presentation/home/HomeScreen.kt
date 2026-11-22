package com.diabdata.wear.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.diabdata.wear.BuildConfig
import com.diabdata.wear.R

@Composable
fun HomeScreen(onLicensesClick: () -> Unit) {
    val versionNumber = BuildConfig.VERSION_NAME
    val buildType = BuildConfig.BUILD_TYPE

    val transformationSpec = rememberTransformationSpec()
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        modifier = Modifier.fillMaxSize(),
        scrollState = listState,
        edgeButton = {
            EdgeButton(
                onClick = { onLicensesClick() },
                buttonSize = EdgeButtonSize.ExtraSmall
            ) {
                Text(stringResource(R.string.show_licenses_button_label))
            }
        }
    ) { contentPadding ->
        TransformingLazyColumn(
            state = listState,
            verticalArrangement = spacedBy(8.dp, Alignment.CenterVertically),
            contentPadding = contentPadding
        ) {
            item {
                Image(
                    painter = painterResource(
                        com.diabdata.shared.R.drawable.ic_logo_outlined
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .transformedHeight(this, transformationSpec)
                )
            }

            item {
                Text(stringResource(R.string.version_label, versionNumber, buildType))
            }
        }
    }
}
