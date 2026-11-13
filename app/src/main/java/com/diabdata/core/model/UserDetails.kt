package com.diabdata.core.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diabdata.shared.utils.dataTypes.BloodType
import com.diabdata.shared.utils.dataTypes.DiabetesType
import com.diabdata.shared.utils.dataTypes.Gender
import com.diabdata.shared.utils.dataTypes.GlucoseUnit
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import kotlin.time.Instant

@Entity(tableName = "user_details")
data class UserDetails(
    @PrimaryKey val id: Int = 0,

    // --- Identity ---
    @SerializedName("first_name", alternate = ["firstName"])
    val firstName: String? = null,
    @SerializedName("last_name", alternate = ["lastName"])
    val lastName: String? = null,
    @SerializedName("profile_photo_path", alternate = ["profilePhotoPath"])
    val profilePhotoPath: String? = null,
    @SerializedName("birth_date", alternate = ["birthdate"])
    val birthdate: LocalDate? = null,
    val gender: Gender? = null,
    @SerializedName("blood_type", alternate = ["bloodType"])
    val bloodType: BloodType? = null,

    // --- Diabetes ---
    @SerializedName("diabetes_type", alternate = ["diabetesType"])
    val diabetesType: DiabetesType? = null,
    @SerializedName("diabetes_diagnosis_date", alternate = ["diabetesDiagnosisDate"])
    val diagnosisDate: LocalDate? = null,

    // --- Medical team ---
    @SerializedName("endocrinologist_name", alternate = ["endocrinologist"])
    val endocrinologist: String? = null,
    @SerializedName("general_practitioner_name", alternate = ["generalPractitioner"])
    val generalPractitioner: String? = null,
    @SerializedName("ophthalmologist_name", alternate = ["ophthalmologist"])
    val ophthalmologist: String? = null,
    @SerializedName("cardiologist_name", alternate = ["cardiologist"])
    val cardiologist: String? = null,
    @SerializedName("nephrologist_name", alternate = ["nephrologist"])
    val nephrologist: String? = null,

    // --- Treatments ---
    @SerializedName("insulin_pump_model", alternate = ["insulinPumpModel"])
    val insulinPumpModel: String? = null,
    @SerializedName("cgm_model", alternate = ["cgmModel"])
    val cgmModel: String? = null,
    @SerializedName("insulin_type", alternate = ["insulinType"])
    val insulinType: String? = null,
    @SerializedName("basal_insulin_type", alternate = ["basalInsulinType"])
    val basalInsulinType: String? = null,

    // --- Blood sugar levels goals ---
    @SerializedName("target_glucose_min", alternate = ["targetGlucoseMin"])
    val targetGlucoseMin: Float? = null,
    @SerializedName("target_glucose_max", alternate = ["targetGlucoseMax"])
    val targetGlucoseMax: Float? = null,
    @SerializedName("glucose_unit", alternate = ["glucoseUnit"])
    val glucoseUnit: GlucoseUnit? = null,

    // --- Emergency contact ---
    @SerializedName("emergency_contact_name", alternate = ["emergencyContactName"])
    val emergencyContactName: String? = null,
    @SerializedName("emergency_contact_phone", alternate = ["emergencyContactPhone"])
    val emergencyContactPhone: String? = null,

    @SerializedName("updated_at")
    @ColumnInfo(defaultValue = "0")
    val updatedAt: Instant = UNKNOWN_TIME_DEFAULT,
)