package com.diabdata.shared.utils.dataTypes

import android.content.Context
import androidx.annotation.StringRes
import com.diabdata.shared.R as R

enum class GlucoseUnit(
    @param:StringRes
    val displayNameRes: Int
) {
    MG_DL(
        displayNameRes = R.string.profile_user_glucose_unit_label_mg_dl
    ),
    MMOL_L(
        displayNameRes = R.string.profile_user_glucose_unit_label_mmol_l
    );

    fun displayName(context: Context): String = context.getString(displayNameRes)
}
