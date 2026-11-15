package com.diabdata.shared.utils.dataTypes

import android.content.Context
import androidx.annotation.StringRes
import com.diabdata.shared.R as R

enum class DiabetesType(
    @param:StringRes
    val displayNameRes: Int
) {
    TYPE_1(
        displayNameRes = R.string.profile_user_diabetes_type_1
    ),
    TYPE_2(
        displayNameRes = R.string.profile_user_diabetes_type_2
    ),
    GESTATIONAL(
        displayNameRes = R.string.profile_user_diabetes_type_gestational
    ),
    MODY(
        displayNameRes = R.string.profile_user_diabetes_type_mody
    ),
    NEONATAL(
        displayNameRes = R.string.profile_user_diabetes_type_neonatal
    ),
    WOLFRAM(
        displayNameRes = R.string.profile_user_diabetes_type_wolfram
    ),
    LADA(
        displayNameRes = R.string.profile_user_diabetes_type_lada
    ),
    TYPE_3C(
        displayNameRes = R.string.profile_user_diabetes_type_3c
    ),
    STEROID_INDUCED(
        displayNameRes = R.string.profile_user_diabetes_type_steroid_induced
    ),
    CYSTIC_FIBROSIS(
        displayNameRes = R.string.profile_user_diabetes_type_cystic_fibrosis
    );

    fun displayName(context: Context): String = context.getString(displayNameRes)
}
