package com.diabdata.core.ui.components.cardsList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diabdata.core.ui.theme.DiabDataTheme
import com.diabdata.core.utils.ui.ColoredIconCircleProps
import com.diabdata.shared.R.drawable as icons
import com.diabdata.shared.utils.dataTypes.AddableType

@Preview(showBackground = true, name = "CardsList - Simple")
@Composable
fun CardsListSimplePreview() {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    DiabDataTheme {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            CardsList(
                header = "Paramètres",
                cards = listOf(
                    CardItem(
                        leadingIcon = icons.notification_active_icon_vector,
                        content = { Text("Notifications") },
                        switchState = notificationsEnabled,
                        onSwitchChange = { notificationsEnabled = it }
                    ),
                    CardItem(
                        leadingIcon = icons.app_version_icon_vector,
                        content = {
                            Column {
                                Text("Version", style = MaterialTheme.typography.bodyLarge)
                                Text("1.0.0", style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        trailingIcon = icons.arrow_right_icon_vector,
                        onTrailingIconClick = { }
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "CardsList - Carte unique")
@Composable
fun CardsListSinglePreview() {
    DiabDataTheme {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            CardsList(
                header = "Prochain rendez-vous",
                cards = listOf(
                    CardItem(
                        leadingColoredCircleIcon = ColoredIconCircleProps(
                            iconRes = icons.stethoscope_icon_vector,
                            baseColor = AddableType.APPOINTMENT.baseColor,
                            size = null,
                            iconSize = null
                        ),
                        content = {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    "Dr. Martin — Diabétologue",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    "15 juillet 2025 à 14h30",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        trailingIcon = icons.arrow_right_icon_vector,
                        onTrailingIconClick = { }
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "CardsList - Avec pagination")
@Composable
fun CardsListPaginatedPreview() {
    DiabDataTheme {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            CardsList(
                header = "Historique HBA1C",
                pageSize = 3,
                cards = listOf(
                    CardItem(
                        leadingColoredCircleIcon = ColoredIconCircleProps(
                            iconRes = icons.weight_icon_vector,
                            baseColor = AddableType.WEIGHT.baseColor,
                            size = null,
                            iconSize = null
                        ),
                        content = {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text("Janvier 2025 — 6.8%")
                            }
                        }
                    ),
                    CardItem(
                        leadingColoredCircleIcon = ColoredIconCircleProps(
                            iconRes = icons.weight_icon_vector,
                            baseColor = AddableType.WEIGHT.baseColor,
                            size = null,
                            iconSize = null
                        ),
                        content = {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text("Octobre 2024 — 7.1%")
                            }
                        }
                    ),
                    CardItem(
                        leadingColoredCircleIcon = ColoredIconCircleProps(
                            iconRes = icons.weight_icon_vector,
                            baseColor = AddableType.WEIGHT.baseColor,
                            size = null,
                            iconSize = null
                        ),
                        content = {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text("Juillet 2024 — 7.3%")
                            }
                        }
                    ),
                    CardItem(
                        leadingColoredCircleIcon = ColoredIconCircleProps(
                            iconRes = icons.weight_icon_vector,
                            baseColor = AddableType.WEIGHT.baseColor,
                            size = null,
                            iconSize = null
                        ),
                        content = {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text("Avril 2024 — 6.9%")
                            }
                        }
                    ),
                    CardItem(
                        leadingColoredCircleIcon = ColoredIconCircleProps(
                            iconRes = icons.weight_icon_vector,
                            baseColor = AddableType.WEIGHT.baseColor,
                            size = null,
                            iconSize = null
                        ),
                        content = {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text("Janvier 2024 — 7.0%")
                            }
                        }
                    ),
                    CardItem(
                        leadingColoredCircleIcon = ColoredIconCircleProps(
                            iconRes = icons.weight_icon_vector,
                            baseColor = AddableType.WEIGHT.baseColor,
                            size = null,
                            iconSize = null
                        ),
                        content = {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text("Octobre 2023 — 7.4%")
                            }
                        }
                    ),
                    CardItem(
                        leadingColoredCircleIcon = ColoredIconCircleProps(
                            iconRes = icons.weight_icon_vector,
                            baseColor = AddableType.WEIGHT.baseColor,
                            size = null,
                            iconSize = null
                        ),
                        content = {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text("Juillet 2023 — 7.2%")
                            }
                        }
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "CardsList - Complet")
@Composable
fun CardsListFullPreview() {
    var rappelCapteur by remember { mutableStateOf(true) }
    var rappelCatheter by remember { mutableStateOf(false) }
    var rappelPeremption by remember { mutableStateOf(true) }

    DiabDataTheme {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // Stack 1 — Rappels
                CardsList(
                    header = "Rappels",
                    cards = listOf(
                        CardItem(
                            leadingIcon = icons
                                .continuous_glucose_monitoring_system_sensor_icon_vector,
                            content = {
                                Column {
                                    Text(
                                        "Capteur de glycémie",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "Rappel tous les 14 jours",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            switchState = rappelCapteur,
                            onSwitchChange = { rappelCapteur = it }
                        ),
                        CardItem(
                            leadingIcon = icons.wired_patch_icon_vector,
                            content = {
                                Column {
                                    Text("Cathéter", style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "Rappel tous les 3 jours",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            switchState = rappelCatheter,
                            onSwitchChange = { rappelCatheter = it }
                        ),
                        CardItem(
                            leadingIcon = icons.fast_acting_insulin_syringe_icon_vector,
                            content = {
                                Column {
                                    Text(
                                        "Péremption insuline",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "Novorapid — expire le 20/08/2025",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            switchState = rappelPeremption,
                            onSwitchChange = { rappelPeremption = it }
                        )
                    )
                )

                // Stack 2 — Poids
                CardsList(
                    header = "Suivi du poids",
                    pageSize = 2,
                    cards = listOf(
                        CardItem(
                            leadingIcon = icons.weight_icon_vector,
                            content = {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text("01/06/2025 — 72.5 kg")
                                }
                            }
                        ),
                        CardItem(
                            leadingIcon = icons.weight_icon_vector,
                            content = {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text("01/05/2025 — 73.0 kg")
                                }
                            }
                        ),
                        CardItem(
                            leadingIcon = icons.weight_icon_vector,
                            content = {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text("01/04/2025 — 73.8 kg")
                                }
                            }
                        ),
                        CardItem(
                            leadingIcon = icons.weight_icon_vector,
                            content = {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text("01/03/2025 — 74.2 kg")
                                }
                            }
                        )
                    )
                )
            }
        }
    }
}
