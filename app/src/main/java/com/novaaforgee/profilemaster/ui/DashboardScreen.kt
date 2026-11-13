package com.novaaforgee.profilemaster.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novaaforgee.profilemaster.model.InstalledApp
import com.novaaforgee.profilemaster.model.ProvisionedSpaceInfo
import com.novaaforgee.profilemaster.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onRequestWorkProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = BgDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Nova Profile Master",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextLight
                            )
                            Text(
                                "Persistent Multi-Instance Engine",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AccentCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Core Architecture Guarantee Card
            item {
                PersistenceGuaranteeCard()
            }

            // 2. Active Native Spaces Card
            item {
                ActiveSpacesCard(
                    spaces = uiState.activeSpaces,
                    selectedSpaceId = uiState.selectedSpaceId,
                    onSelectSpace = { viewModel.selectSpace(it) },
                    onCreateProfile = onRequestWorkProfile
                )
            }

            // 3. Deployment Modal / Action Bar (If app selected)
            if (uiState.selectedApp != null) {
                item {
                    DeploymentCommandCard(
                        app = uiState.selectedApp!!,
                        targetUserId = uiState.selectedSpaceId,
                        command = uiState.copyAdbCommandText ?: "",
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("ADB Command", uiState.copyAdbCommandText))
                            Toast.makeText(context, "Command copied! Run in terminal or Shizuku", Toast.LENGTH_SHORT).show()
                        },
                        onDismiss = { viewModel.clearSelectedApp() }
                    )
                }
            }

            // 4. Section Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Installed Apps to Clone (${uiState.installedApps.size})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = TextLight
                    )
                    Text(
                        "Tap any app to deploy",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // 5. Installed Apps List
            items(uiState.installedApps) { app ->
                AppDeployRowCard(
                    app = app,
                    isSelected = uiState.selectedApp?.packageName == app.packageName,
                    onSelect = { viewModel.selectApp(app) },
                    onDirectLaunch = {
                        viewModel.profileManager.launchAppInSpace(app.packageName)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
fun PersistenceGuaranteeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Native OS Air-Gapped Persistence",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SuccessGreen
                )
            }
            Text(
                "Cloned applications are provisioned directly in Android OS (/data/user/<id>/). Even if you DELETE Nova Profile Master, your cloned apps and all their data will remain 100% active and untouched.",
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun ActiveSpacesCard(
    spaces: List<ProvisionedSpaceInfo>,
    selectedSpaceId: Int,
    onSelectSpace: (Int) -> Unit,
    onCreateProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Detected Native Spaces",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AccentCyan
                )

                TextButton(onClick = onCreateProfile) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Provision New", fontSize = 12.sp)
                }
            }

            spaces.forEach { space ->
                val isSelected = space.userId == selectedSpaceId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) SurfaceDark else Color.Transparent)
                        .clickable { onSelectSpace(space.userId) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (space.isRunning) SuccessGreen else BorderDark)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(space.userName, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = TextLight)
                            Text(
                                if (space.userId == 0) "Primary System" else "Isolated Native Space",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    if (isSelected) {
                        Text("Active Target", fontSize = 11.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DeploymentCommandCard(
    app: InstalledApp,
    targetUserId: Int,
    command: String,
    onCopy: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Deploy ${app.appName} to Space $targetUserId",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AccentCyan
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                }
            }

            Text(
                "Run this 1-line command in PC Terminal (or Shizuku / Termux) to link the native APK with ZERO duplication:",
                fontSize = 12.sp,
                color = TextMuted
            )

            Surface(
                color = BgDark,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        command,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = AccentCyan,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextLight)
                    }
                }
            }
        }
    }
}

@Composable
fun AppDeployRowCard(
    app: InstalledApp,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDirectLaunch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceDark else CardDark
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BorderDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = null,
                    tint = if (app.packageName.contains("snapchat")) Color(0xFFFFFC00) else AccentCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextLight
                )
                Text(
                    text = app.packageName,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            IconButton(onClick = onDirectLaunch) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Launch", tint = AccentCyan)
            }
        }
    }
}
