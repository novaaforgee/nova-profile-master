package com.novaaforgee.profilemaster.provisioner

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class NovaDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }
}
