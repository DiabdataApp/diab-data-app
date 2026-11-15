package com.diabdata.core.ui.components.noDataView

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.core.ui.theme.DiabDataTheme
import com.diabdata.core.utils.ui.SvgIcon
import com.diabdata.shared.R as shared

/**
 * Composable function that displays a screen with a message indicating that no data is available.
 *
 * @param modifier Modifier to be applied to the layout.
 * @param iconType The type of icon to display. Defaults to [IconTypes.DEFAULT].
 */
@Composable
fun NoDataView(modifier: Modifier = Modifier, iconType: IconTypes = IconTypes.DEFAULT) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.wrapContentWidth()
        ) {
            SvgIcon(
                resId = iconType.iconRes,
                modifier = Modifier
                    .width((LocalWindowInfo.current.containerSize.width * 0.15f).dp)
                    .aspectRatio(1f),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Text(
                text = stringResource(iconType.displayNameRes),
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Enum class representing different types of icons for displaying no data messages.
 */
enum class IconTypes(
    @param:StringRes val displayNameRes: Int,
    @param:DrawableRes val iconRes: Int
) {
    DEFAULT(
        displayNameRes = shared.string.homescreen_no_data_text,
        iconRes = shared.drawable.inbox_icon_vector
    ),
    DEVICES(
        displayNameRes = shared.string.homescreen_no_data_text,
        iconRes = shared.drawable.no_devices_icon_vector
    );

    fun displayName(context: Context): String = context.getString(displayNameRes)
}

@Preview(showBackground = true)
@Composable
fun NoDataPreview() {
    DiabDataTheme {
        NoDataView()
    }
}
