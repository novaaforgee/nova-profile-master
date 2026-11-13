package com.novaaforgee.profilemaster

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.novaaforgee.profilemaster.ui.DashboardScreen
import com.novaaforgee.profilemaster.ui.MainViewModel
import com.novaaforgee.profilemaster.ui.theme.NovaProfileMasterTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NovaProfileMasterTheme {
                DashboardScreen(
                    viewModel = viewModel,
                    onRequestWorkProfile = { triggerProfileProvisioning() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshAll()
    }

    private fun triggerProfileProvisioning() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = viewModel.profileManager.getAdminComponent()

        if (!dpm.isProfileOwnerApp(packageName)) {
            val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
                putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, admin)
            }
            try {
                startActivityForResult(intent, 100)
            } catch (e: Exception) {
                Toast.makeText(this, "Native Managed Profile triggered via system settings", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Nova Profile Master is already Profile Owner!", Toast.LENGTH_SHORT).show()
        }
    }
}
