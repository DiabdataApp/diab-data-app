package com.diabdata.shared.utils.dataTypes

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.diabdata.shared.R as R

enum class Gender(
    @param:StringRes
    val displayNameRes: Int,
    @param:DrawableRes val iconRes: Int
) {
    MALE(
        R.string.profile_user_gender_male_label,
        R.drawable.male_icon_vector
    ),
    FEMALE(
        R.string.profile_user_gender_female_label,
        R.drawable.female_icon_vector
    ),
    OTHER(
        R.string.profile_user_gender_other_label,
        R.drawable.question_mark_icon_vector
    ),
    PREFER_NOT_TO_SAY(
        R.string.profile_user_gender_prefer_not_to_say_label,
        R.drawable.back_hand_icon_vector
    );

    fun displayName(context: Context): String = context.getString(displayNameRes)
}
