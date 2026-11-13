package com.novaaforgee.profilemaster.model

import android.graphics.drawable.Drawable

data class InstalledApp(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false,
    val isAlreadyClonedInProfile: Boolean = false
)

data class ProvisionedSpaceInfo(
    val userId: Int,
    val userName: String,
    val isRunning: Boolean,
    val isManagedProfile: Boolean,
    val isSecondSpace: Boolean
)
