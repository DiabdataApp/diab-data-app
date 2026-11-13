package com.diabdata.feature.settings.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diabdata.BuildConfig
import com.diabdata.core.ui.theme.DiabDataTheme
import com.diabdata.core.ui.theme.ExtendedTheme
import com.diabdata.core.ui.theme.GoogleSansFlexFontFamily
import com.diabdata.core.utils.ui.ColoredIconCircle
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.feature.settings.sections.dataSettings.ui.components.locale
import com.diabdata.shared.R as shared

@Composable
fun AppInfoCard(
    isBeta: Boolean,
    versionName: String,
    showChangeLogDialog: () -> Unit,
    showLicensesDialog: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),

    ) {
        Row(
            modifier = Modifier
                .padding(start = 22.dp, top = 22.dp, end = 22.dp, bottom = 12.dp),
            horizontalArrangement = spacedBy(10.dp),
        ) {
            ColoredIconCircle(
                baseColor = if (isBeta) {
                    ExtendedTheme.colors.betaContainer
                } else {
                    ExtendedTheme.colors.releaseVersionContainer
                },
                iconRes = if (isBeta) {
                    shared.drawable.ic_logo_outlined
                } else {
                    shared.drawable.ic_logo_filled
                },
                size = 38.dp,
                iconSize = 28.dp
            )
            Column {
                Text(
                    text = stringResource(shared.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isBeta) {
                        ExtendedTheme.colors.betaContainer
                    } else {
                        ExtendedTheme.colors.releaseVersionContainer
                    },
                    fontFamily = GoogleSansFlexFontFamily,
                    fontWeight = FontWeight(1000),
                    fontStyle = Italic
                )
                Text(
                    text = stringResource(shared.string.app_tagline),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = GoogleSansFlexFontFamily,
                )
            }
        }

        FlowRow(
            modifier = Modifier
                .padding(start = 22.dp, end = 22.dp, bottom = 22.dp)
                .fillMaxWidth(),
            horizontalArrangement = spacedBy(8.dp),
        ) {
            val uriHandler = LocalUriHandler.current

            AssistChip(
                label = {
                    Text(
                        "v$versionName${if (isBeta) "-beta" else ""}",
                        fontFamily = GoogleSansFlexFontFamily,
                        fontWeight = FontWeight(600),
                    )
                },
                leadingIcon = {
                    SvgIcon(
                        resId = shared.drawable.app_version_filled_icon_vector,
                        modifier = Modifier
                            .size(18.dp)
                            .border(
                                0.dp,
                                Color.Transparent,
                                shape = RoundedCornerShape(50)
                            ),
                        color = if (isBeta) {
                            ExtendedTheme.colors.onBetaContainer
                        } else {
                            ExtendedTheme.colors.onReleaseVersionContainer
                        }
                    )
                },
                shape = RoundedCornerShape(50),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isBeta) {
                        ExtendedTheme.colors.betaContainer
                    } else {
                        ExtendedTheme.colors.releaseVersionContainer
                    },
                    labelColor = if (isBeta) {
                        ExtendedTheme.colors.onBetaContainer
                    } else {
                        ExtendedTheme.colors.onReleaseVersionContainer
                    }
                ),
                onClick = {},
            )

            AssistChip(
                onClick = {
                    if (!isBeta) {
                        uriHandler.openUri(
                            "https://github.com/DiabdataApp/diab-data-android"
                        )
                    } else {
                        uriHandler.openUri(
                            "https://github.com/DiabdataApp/diab-data-android/tree/dev"
                        )
                    }
                },
                label = {
                    Text(
                        "Github",
                        fontFamily = GoogleSansFlexFontFamily,
                        fontWeight = FontWeight(600),
                    )
                },
                leadingIcon = {
                    SvgIcon(
                        resId = shared.drawable.github_icon_vector,
                        modifier = Modifier
                            .size(18.dp)
                            .border(
                                0.dp,
                                Color.Transparent,
                                shape = RoundedCornerShape(50)
                            ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                shape = RoundedCornerShape(50),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
            )

            AssistChip(
                onClick = { showChangeLogDialog() },
                label = {
                    Text(
                        stringResource(shared.string.settings_changelogs_chip_label),
                        fontFamily = GoogleSansFlexFontFamily,
                        fontWeight = FontWeight(600),
                    )
                },
                leadingIcon = {
                    SvgIcon(
                        resId = shared.drawable.breaking_new_icon_vector,
                        modifier = Modifier
                            .size(18.dp)
                            .border(
                                0.dp,
                                Color.Transparent,
                                shape = RoundedCornerShape(50)
                            ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                shape = RoundedCornerShape(50),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
            )

            AssistChip(
                onClick = { showLicensesDialog() },
                label = {
                    Text(
                        stringResource(shared.string.settings_licenses_chip_label),
                        fontFamily = GoogleSansFlexFontFamily,
                        fontWeight = FontWeight(600),
                    )
                },
                leadingIcon = {
                    SvgIcon(
                        resId = shared.drawable.information_icon_vector,
                        modifier = Modifier
                            .size(18.dp)
                            .border(
                                0.dp,
                                Color.Transparent,
                                shape = RoundedCornerShape(50)
                            ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                shape = RoundedCornerShape(50),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
            )

            AssistChip(
                onClick = {
                    uriHandler.openUri("https://app.diabdata.fr/")
                },
                label = {
                    Text(
                        stringResource(shared.string.settings_website_chip_label),
                        fontFamily = GoogleSansFlexFontFamily,
                        fontWeight = FontWeight(600),
                    )
                },
                leadingIcon = {
                    SvgIcon(
                        resId = shared.drawable.arrow_outward_icon_vector,
                        modifier = Modifier
                            .size(18.dp)
                            .border(
                                0.dp,
                                Color.Transparent,
                                shape = RoundedCornerShape(50)
                            ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                shape = RoundedCornerShape(50),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
            )
        }
    }
}

@Preview(name = "Interactive", showBackground = true, locale = locale)
@Composable
fun AppInfoCardInteractivePreview() {
    var darkTheme by remember { mutableStateOf(false) }
    var betaMode by remember { mutableStateOf(false) }

    DiabDataTheme(dynamicColor = false, darkTheme = darkTheme) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(24.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(16.dp)
            ) {
                AppInfoCard(
                    isBeta = betaMode,
                    versionName = BuildConfig.VERSION_NAME,
                    showChangeLogDialog = {},
                    showLicensesDialog = {}
                )

                Column(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(vertical = 12.dp, horizontal = 32.dp),
                    verticalArrangement = spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Dark Theme",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = GoogleSansFlexFontFamily,
                            fontWeight = FontWeight(800),
                        )
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { darkTheme = it }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Beta Mode",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = GoogleSansFlexFontFamily,
                            fontWeight = FontWeight(800),
                        )
                        Switch(
                            checked = betaMode,
                            onCheckedChange = { betaMode = it },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = ExtendedTheme.colors.betaContainer
                            )
                        )
                    }
                }
            }
        }
    }
}
