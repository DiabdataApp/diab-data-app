package com.diabdata.shared.utils.dataTypes

import android.content.Context
import androidx.annotation.StringRes
import com.diabdata.shared.R as R

enum class BloodType(
    @param:StringRes
    val displayNameRes: Int
) {
    A_POSITIVE(R.string.profile_user_blood_type_label_a_positive),
    A_NEGATIVE(R.string.profile_user_blood_type_label_a_negative),
    B_POSITIVE(R.string.profile_user_blood_type_label_b_positive),
    B_NEGATIVE(R.string.profile_user_blood_type_label_b_negative),
    AB_POSITIVE(R.string.profile_user_blood_type_label_ab_positive),
    AB_NEGATIVE(R.string.profile_user_blood_type_label_ab_negative),
    O_POSITIVE(R.string.profile_user_blood_type_label_o_positive),
    O_NEGATIVE(R.string.profile_user_blood_type_label_o_negative);

    fun displayName(context: Context): String = context.getString(displayNameRes)
}
