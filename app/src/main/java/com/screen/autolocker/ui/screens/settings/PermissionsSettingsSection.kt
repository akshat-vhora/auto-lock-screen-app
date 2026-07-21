package com.screen.autolocker.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.screen.autolocker.ui.components.PermissionItemModel
import com.screen.autolocker.ui.components.PermissionList
import com.screen.autolocker.ui.theme.AppPalette

@Composable
fun PermissionsSettingsSection(
    palette: AppPalette,
    items: List<PermissionItemModel>,
    modifier: Modifier = Modifier
) {
    SectionTitle("Permissions", palette)
    PermissionList(
        palette = palette,
        items = items,
        modifier = modifier
    )
}
