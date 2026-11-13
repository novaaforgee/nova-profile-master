package com.novaaforgee.profilemaster.provisioner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import com.novaaforgee.profilemaster.model.InstalledApp
import com.novaaforgee.profilemaster.model.ProvisionedSpaceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SystemProfileManager(private val context: Context) {

    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, NovaDeviceAdminReceiver::class.java)

    // Discover all native isolated spaces (Work Profile, Second Space, Multi-Users)
    fun detectActiveSpaces(): List<ProvisionedSpaceInfo> {
        val spaces = mutableListOf<ProvisionedSpaceInfo>()

        try {
            val userProfiles: List<UserHandle> = userManager.userProfiles
            for (handle in userProfiles) {
                val id = getUserIdFromHandle(handle)
                val isManaged = dpm.isProfileOwnerApp(adminComponent.packageName)
                val isSecondSpace = id == 10 || id == 11 // Standard MIUI Second Space UID allocation
                val name = when {
                    id == 0 -> "Primary Owner (User 0)"
                    isSecondSpace -> "MIUI Second Space (User $id)"
                    isManaged -> "Nova Managed Sandbox (User $id)"
                    else -> "Native Profile (User $id)"
                }

                spaces.add(
                    ProvisionedSpaceInfo(
                        userId = id,
                        userName = name,
                        isRunning = true,
                        isManagedProfile = isManaged,
                        isSecondSpace = isSecondSpace
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: at least show Primary User
            spaces.add(
                ProvisionedSpaceInfo(
                    userId = 0,
                    userName = "Primary System (User 0)",
                    isRunning = true,
                    isManagedProfile = false,
                    isSecondSpace = false
                )
            )
        }

        return spaces
    }

    // Scan all installed apps on the device
    suspend fun getInstalledUserApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val list = mutableListOf<InstalledApp>()

        try {
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in apps) {
                // Focus on user apps or popular messaging/camera apps
                val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val appName = pm.getApplicationLabel(app).toString()
                val pkg = app.packageName

                // Filter out uninteresting Android core packages unless it's user installed
                if (!isSystem || pkg.contains("snapchat") || pkg.contains("whatsapp") || pkg.contains("instagram")) {
                    val icon = try { pm.getApplicationIcon(app) } catch (e: Exception) { null }
                    val ver = try { pm.getPackageInfo(pkg, 0).versionName ?: "1.0" } catch (e: Exception) { "1.0" }

                    list.add(
                        InstalledApp(
                            appName = appName,
                            packageName = pkg,
                            versionName = ver,
                            icon = icon,
                            isSystemApp = isSystem
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Put Snapchat and user messengers at the top
        list.sortedWith(compareByDescending<InstalledApp> {
            it.packageName.contains("snapchat") || it.packageName.contains("whatsapp") || it.packageName.contains("instagram")
        }.thenBy { it.appName })
    }

    // Direct launch intent for provisioned app
    fun launchAppInSpace(packageName: String, userHandle: UserHandle? = null): Boolean {
        return try {
            if (userHandle != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                val activities = launcherApps.getActivityList(packageName, userHandle)
                if (activities.isNotEmpty()) {
                    launcherApps.startMainActivity(activities[0].componentName, userHandle, null, null)
                    return true
                }
            }

            // Normal launch fallback
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Get ADB command string to instantly link app to secondary user profile
    fun getAdbCloneCommand(packageName: String, targetUserId: Int): String {
        return "adb shell pm install-existing --user $targetUserId $packageName"
    }

    // Check if device admin is already enabled
    fun isDeviceAdminActive(): Boolean {
        return dpm.isAdminActive(adminComponent)
    }

    fun getAdminComponent(): ComponentName = adminComponent

    private fun getUserIdFromHandle(userHandle: UserHandle): Int {
        return try {
            val hashCode = userHandle.hashCode()
            // In Android framework, UserHandle identifier or hashCode maps to the UID / 100000
            val method = UserHandle::class.java.getMethod("getIdentifier")
            method.invoke(userHandle) as Int
        } catch (e: Exception) {
            0
        }
    }
}
