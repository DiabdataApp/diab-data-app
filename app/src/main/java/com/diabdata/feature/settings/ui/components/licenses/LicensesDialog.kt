package com.diabdata.feature.settings.ui.components.licenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.core.utils.ui.SvgIcon
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import com.mikepenz.aboutlibraries.ui.compose.m3.style.m3VariantColors
import com.mikepenz.aboutlibraries.ui.compose.style.LicenseHueResolver
import com.mikepenz.aboutlibraries.ui.compose.variant.LibrariesVariant
import com.diabdata.shared.R as shared

@Composable
fun LicensesDialog(onDismiss: () -> Unit) {
    val closeButtonText = stringResource(shared.string.common_close)
    val libraries by produceLibraries(R.raw.aboutlibraries)
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val maxHeight = with(density) { windowInfo.containerSize.height.toDp() * 0.6f }
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryLicenseHueResolver = remember(primaryColor) { LicenseHueResolver { primaryColor } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { }
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SvgIcon(
                        resId = shared.drawable.information_icon_vector,
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(shared.string.settings_licenses_dialog_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LibrariesContainer(
                    libraries = libraries,
                    variant = LibrariesVariant.Refined,
                    variantColors = LibraryDefaults.m3VariantColors(
                        headerBackground = Color.Transparent,
                        rowBackground = Color.Transparent,
                        headerDivider = MaterialTheme.colorScheme.outline,
                        licenseBadgeContainer = MaterialTheme.colorScheme.primary,
                        licenseBadgeContent = MaterialTheme.colorScheme.onPrimary,
                        licenseHueResolver = primaryLicenseHueResolver,
                    ),
                    colors = LibraryDefaults.libraryColors(
                        libraryBackgroundColor = Color.Transparent,
                        libraryContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxHeight),
                    licenseDialogConfirmText = closeButtonText,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(closeButtonText, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
