package com.diabdata.wear.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.diabdata.wear.BuildConfig
import com.diabdata.wear.R

@Composable
fun HomeScreen(onLicensesClick: () -> Unit) {
    val versionNumber = BuildConfig.VERSION_NAME
    val buildType = BuildConfig.BUILD_TYPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        TimeText()
        Column(
            Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(
                    com.diabdata.shared.R.drawable.ic_logo_outlined
                ),
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
            Text(stringResource(R.string.version_label, versionNumber, buildType))
            Button(
                onClick = {
                    onLicensesClick()
                },
                label = { Text(stringResource(R.string.show_licenses_button_label)) }
            )
        }
    }
}
