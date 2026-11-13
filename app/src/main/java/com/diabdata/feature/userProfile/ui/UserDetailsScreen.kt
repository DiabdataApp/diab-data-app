package com.diabdata.feature.userProfile.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diabdata.core.model.UserDetails
import com.diabdata.core.model.nowMillis
import com.diabdata.core.ui.components.actionInput.EnumDropdown
import com.diabdata.core.ui.components.date_components.DateSelector
import com.diabdata.feature.userProfile.UserProfileViewModel
import com.diabdata.shared.utils.dataTypes.BloodType
import com.diabdata.shared.utils.dataTypes.DiabetesType
import com.diabdata.shared.utils.dataTypes.Gender
import com.diabdata.shared.utils.dataTypes.GlucoseUnit
import java.time.LocalDate
import com.diabdata.shared.R as shared

@Composable
fun UserDetailsScreen(
    onNavigateBack: () -> Unit
) {
    val userProfileViewModel: UserProfileViewModel = hiltViewModel()
    val userDetails by userProfileViewModel.userDetails.collectAsStateWithLifecycle(initialValue = null)

    UserDetailsView(
        userDetails = userDetails ?: UserDetails(),
        onSave = { updated ->
            userProfileViewModel.updateUserDetails(updated)
            onNavigateBack()
        },
        onProfilePhotoPicked = { uri ->
            userProfileViewModel.saveProfilePhoto(uri)
        },
        onNavigateBack = onNavigateBack
    )
}

// ══════════════════════════════════════════════
//  VUE PURE (stateless, previewable)
// ══════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsView(
    userDetails: UserDetails,
    onSave: (UserDetails) -> Unit,
    onProfilePhotoPicked: (Uri) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()

    var firstName by remember(userDetails) { mutableStateOf(userDetails.firstName.orEmpty()) }
    var lastName by remember(userDetails) { mutableStateOf(userDetails.lastName.orEmpty()) }
    var birthdate by remember(userDetails) { mutableStateOf(userDetails.birthdate) }
    var gender by remember(userDetails) { mutableStateOf(userDetails.gender) }
    var bloodType by remember(userDetails) { mutableStateOf(userDetails.bloodType) }
    var diabetesType by remember(userDetails) { mutableStateOf(userDetails.diabetesType) }
    var diagnosisDate by remember(userDetails) { mutableStateOf(userDetails.diagnosisDate) }
    var endocrinologist by remember(userDetails) { mutableStateOf(userDetails.endocrinologist.orEmpty()) }
    var generalPractitioner by remember(userDetails) { mutableStateOf(userDetails.generalPractitioner.orEmpty()) }
    var ophthalmologist by remember(userDetails) { mutableStateOf(userDetails.ophthalmologist.orEmpty()) }
    var cardiologist by remember(userDetails) { mutableStateOf(userDetails.cardiologist.orEmpty()) }
    var nephrologist by remember(userDetails) { mutableStateOf(userDetails.nephrologist.orEmpty()) }
    var insulinPumpModel by remember(userDetails) { mutableStateOf(userDetails.insulinPumpModel.orEmpty()) }
    var cgmModel by remember(userDetails) { mutableStateOf(userDetails.cgmModel.orEmpty()) }
    var insulinType by remember(userDetails) { mutableStateOf(userDetails.insulinType.orEmpty()) }
    var basalInsulinType by remember(userDetails) { mutableStateOf(userDetails.basalInsulinType.orEmpty()) }
    var targetGlucoseMin by remember(userDetails) { mutableStateOf(userDetails.targetGlucoseMin?.toString().orEmpty()) }
    var targetGlucoseMax by remember(userDetails) { mutableStateOf(userDetails.targetGlucoseMax?.toString().orEmpty()) }
    var glucoseUnit by remember(userDetails) { mutableStateOf(userDetails.glucoseUnit) }
    var emergencyContactName by remember(userDetails) { mutableStateOf(userDetails.emergencyContactName.orEmpty()) }
    var emergencyContactPhone by remember(userDetails) { mutableStateOf(userDetails.emergencyContactPhone.orEmpty()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { onProfilePhotoPicked(it) } }

    fun buildUpdatedUserDetails() = userDetails.copy(
        firstName = firstName.ifBlank { null },
        lastName = lastName.ifBlank { null },
        birthdate = birthdate,
        gender = gender,
        bloodType = bloodType,
        diabetesType = diabetesType,
        diagnosisDate = diagnosisDate,
        endocrinologist = endocrinologist.ifBlank { null },
        generalPractitioner = generalPractitioner.ifBlank { null },
        ophthalmologist = ophthalmologist.ifBlank { null },
        cardiologist = cardiologist.ifBlank { null },
        nephrologist = nephrologist.ifBlank { null },
        insulinPumpModel = insulinPumpModel.ifBlank { null },
        cgmModel = cgmModel.ifBlank { null },
        insulinType = insulinType.ifBlank { null },
        basalInsulinType = basalInsulinType.ifBlank { null },
        targetGlucoseMin = targetGlucoseMin.toFloatOrNull(),
        targetGlucoseMax = targetGlucoseMax.toFloatOrNull(),
        glucoseUnit = glucoseUnit,
        emergencyContactName = emergencyContactName.ifBlank { null },
        emergencyContactPhone = emergencyContactPhone.ifBlank { null },
        updatedAt = nowMillis()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(shared.string.edit_profile)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(onClick = { onSave(
                        buildUpdatedUserDetails().copy(
                            updatedAt = nowMillis()
                        )
                    ) }) {
                        Text(stringResource(shared.string.common_save))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ═══════════════════════════
            //  PROFILE PICTURE
            // ═══════════════════════════
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(
                        firstName = firstName.ifBlank { null },
                        lastName = lastName.ifBlank { null },
                        profilePhotoPath = userDetails.profilePhotoPath,
                        size = 96.dp,
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(shared.string.profile_change_photo),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ═══════════════════════════
            //  IDENTITY
            // ═══════════════════════════
            item { SectionHeader(stringResource(shared.string.profile_user_identity_section)) }

            item {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text(stringResource(shared.string.profile_user_first_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )
            }

            item {
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text(stringResource(shared.string.profile_user_last_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )
            }

            item {
                DateSelector(
                    date = birthdate ?: today,
                    onDateSelected = { birthdate = it },
                    labelRes = shared.string.profile_user_birth_date_label
                )
            }

            item {
                EnumDropdown(
                    label = stringResource(shared.string.profile_user_gender_label),
                    options = Gender.entries,
                    selected = gender,
                    displayName = { it.displayName(context) },
                    onSelectedChange = { gender = it },
                    iconRes = { it.iconRes }
                )
            }

            item {
                EnumDropdown(
                    label = stringResource(shared.string.profile_user_blood_type_label),
                    options = BloodType.entries,
                    selected = bloodType,
                    displayName = { it.displayName(context) },
                    onSelectedChange = { bloodType = it },
                )
            }

            // ═══════════════════════════
            //  ABOUT DIABETES
            // ═══════════════════════════
            item { SectionHeader(stringResource(shared.string.profile_user_diabetes_details_section)) }

            item {
                EnumDropdown(
                    label = stringResource(shared.string.profile_user_diabetes_type),
                    options = DiabetesType.entries,
                    selected = diabetesType,
                    displayName = { it.displayName(context) },
                    onSelectedChange = { diabetesType = it },
                )
            }

            item {
                DateSelector(
                    date = diagnosisDate ?: today,
                    onDateSelected = { diagnosisDate = it },
                    labelRes = shared.string.profile_user_diagnosis_date_label
                )
            }

            // ═══════════════════════════
            //  DOCTORS
            // ═══════════════════════════
            item { SectionHeader(stringResource(shared.string.profile_user_medical_team_section)) }

            item {
                OutlinedTextField(
                    value = endocrinologist,
                    onValueChange = { endocrinologist = it },
                    label = { Text(stringResource(shared.string.profile_user_endocrinologist_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )
            }

            item {
                OutlinedTextField(
                    value = generalPractitioner,
                    onValueChange = { generalPractitioner = it },
                    label = { Text(stringResource(shared.string.profile_user_general_practitioner_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )
            }

            item {
                OutlinedTextField(
                    value = ophthalmologist,
                    onValueChange = { ophthalmologist = it },
                    label = { Text(stringResource(shared.string.profile_user_ophthalmologist_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )
            }

            item {
                OutlinedTextField(
                    value = cardiologist,
                    onValueChange = { cardiologist = it },
                    label = { Text(stringResource(shared.string.profile_user_cardiologist_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )
            }

            item {
                OutlinedTextField(
                    value = nephrologist,
                    onValueChange = { nephrologist = it },
                    label = { Text(stringResource(shared.string.profile_user_nephrologist_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )
            }

            // ═══════════════════════════
            //  TREATMENT
            // ═══════════════════════════
            item { SectionHeader(stringResource(shared.string.profile_user_treatments_section)) }

            item {
                OutlinedTextField(
                    value = insulinPumpModel,
                    onValueChange = { insulinPumpModel = it },
                    label = { Text(stringResource(shared.string.profile_user_insulin_pump_model_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )
            }

            item {
                OutlinedTextField(
                    value = cgmModel,
                    onValueChange = { cgmModel = it },
                    label = { Text(stringResource(shared.string.profile_user_cgm_model_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )
            }

            item {
                OutlinedTextField(
                    value = insulinType,
                    onValueChange = { insulinType = it },
                    label = { Text(stringResource(shared.string.profile_user_insulin_type_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )
            }

            item {
                OutlinedTextField(
                    value = basalInsulinType,
                    onValueChange = { basalInsulinType = it },
                    label = { Text(stringResource(shared.string.profile_user_basal_insulin_type_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )
            }

            // ═══════════════════════════
            //  GLYCEMIA TARGETS
            // ═══════════════════════════
            item { SectionHeader(stringResource(shared.string.profile_user_glucose_goals_section)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetGlucoseMin,
                        onValueChange = { targetGlucoseMin = it },
                        label = { Text(stringResource(shared.string.profile_user_target_glucose_min_label)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = MaterialTheme.shapes.small,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = targetGlucoseMax,
                        onValueChange = { targetGlucoseMax = it },
                        label = { Text(stringResource(shared.string.profile_user_target_glucose_max_label)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = MaterialTheme.shapes.small,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }

            item {
                EnumDropdown(
                    label = stringResource(shared.string.profile_user_glucose_unit_label),
                    options = GlucoseUnit.entries,
                    selected = glucoseUnit,
                    displayName = { it.displayName(context) },
                    onSelectedChange = { glucoseUnit = it },
                )
            }

            // ═══════════════════════════
            //  EMERGENCY CONTACT
            // ═══════════════════════════
            item { SectionHeader(stringResource(shared.string.profile_user_emergency_contact_section)) }

            item {
                OutlinedTextField(
                    value = emergencyContactName,
                    onValueChange = { emergencyContactName = it },
                    label = { Text(stringResource(shared.string.profile_user_emergency_contact_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small
                )
            }

            item {
                OutlinedTextField(
                    value = emergencyContactPhone,
                    onValueChange = { emergencyContactPhone = it },
                    label = { Text(stringResource(shared.string.profile_user_emergency_contact_phone_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ══════════════════════════════════════════════
//  SECTION HEADER
// ══════════════════════════════════════════════

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

// ══════════════════════════════════════════════
//  PREVIEW
// ══════════════════════════════════════════════

@Preview(showBackground = true)
@Composable
fun UserDetailsViewEmptyPreview() {
    UserDetailsView(
        userDetails = UserDetails(),
        onSave = {},
        onProfilePhotoPicked = {},
        onNavigateBack = {},
    )
}

@Preview(showBackground = true)
@Composable
fun UserDetailsViewFilledPreview() {
    UserDetailsView(
        userDetails = UserDetails(
            firstName = "Jean",
            lastName = "Dupont",
            birthdate = LocalDate.of(1990, 5, 15),
            gender = Gender.MALE,
            bloodType = BloodType.A_POSITIVE,
            diabetesType = DiabetesType.TYPE_1,
            diagnosisDate = LocalDate.of(2010, 3, 20),
            endocrinologist = "Dr. Martin",
            generalPractitioner = "Dr. Bernard",
            ophthalmologist = "Dr. Petit",
            cardiologist = "Dr. Moreau",
            nephrologist = "Dr. Laurent",
            insulinPumpModel = "Omnipod 5",
            cgmModel = "Dexcom G7",
            insulinType = "Novorapid",
            basalInsulinType = "Tresiba",
            targetGlucoseMin = 70f,
            targetGlucoseMax = 180f,
            glucoseUnit = GlucoseUnit.MG_DL,
            emergencyContactName = "Marie Dupont",
            emergencyContactPhone = "06 12 34 56 78"
        ),
        onSave = {},
        onProfilePhotoPicked = {},
        onNavigateBack = {},
    )
}