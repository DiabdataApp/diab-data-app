package com.diabdata.shared.utils.dataTypes

import androidx.annotation.StringRes
import com.diabdata.shared.R as shared

enum class BackupFrequency(val key: String, @param:StringRes val labelRes: Int, val days: Long) {
    DAILY("daily", shared.string.recurrence_period_dayly, 1),
    WEEKLY("weekly", shared.string.recurrence_period_weekly, 7),
    MONTHLY("monthly", shared.string.recurrence_period_monthly, 30);

    companion object {
        fun fromKey(value: String): BackupFrequency = entries.find { it.key == value } ?: WEEKLY
    }
}
