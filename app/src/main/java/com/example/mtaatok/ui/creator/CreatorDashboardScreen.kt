package com.example.mtaatok.ui.creator

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mtaatok.data.repository.MtaaTokRepository
import com.example.mtaatok.model.VideoPost

@Composable
fun CreatorDashboardScreen(
    repository: MtaaTokRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val posts by repository.getAllPosts().collectAsState(initial = emptyList())
    val monetizationConfig by repository.monetizationConfig.collectAsState()
    val currentUser by repository.sessionManager.currentUser.collectAsState()

    var showPayoutDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }

    // Calculate aggregated metrics from real posts
    val totalViews = remember(posts) { posts.sumOf { it.viewsCount } }
    val totalLikes = remember(posts) { posts.sumOf { it.likesCount } }
    val totalComments = remember(posts) { posts.sumOf { it.commentsCount } }
    val totalShares = remember(posts) { posts.sumOf { it.sharesCount } }

    // Dynamic estimated earnings formula based on views & RPM
    val estimatedEarnings = remember(totalViews, monetizationConfig) {
        (totalViews / 1000.0) * monetizationConfig.estimatedRpmRate
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C10))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("creator_dashboard_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Text(
                    text = "Creator Studio & Earnings",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                IconButton(onClick = { showConfigDialog = true }) {
                    Icon(Icons.Default.Tune, contentDescription = "Config Rules", tint = Color(0xFF00E5FF))
                }
            }

            // Overview Cards (Earnings Banner)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF231633)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimated Mtaa Balance",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Verified Creator", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$%.2f USD".format(estimatedEarnings),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "≈ KES %.0f (Local Payout)".format(estimatedEarnings * 130),
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Min Payout Threshold: $%.0f".format(monetizationConfig.minimumWithdrawalAmount),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        Button(
                            onClick = {
                                if (estimatedEarnings >= monetizationConfig.minimumWithdrawalAmount) {
                                    showPayoutDialog = true
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Minimum withdrawal is $%.0f. Current balance: $%.2f".format(
                                            monetizationConfig.minimumWithdrawalAmount,
                                            estimatedEarnings
                                        ),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (estimatedEarnings >= monetizationConfig.minimumWithdrawalAmount) Color(0xFFFF5722) else Color(0xFF333344)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Withdraw", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Analytics Grid
            Text(
                text = "Key Performance Metrics (All Time)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Video Views",
                    value = formatNumber(totalViews),
                    icon = Icons.Default.Visibility,
                    accentColor = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Likes",
                    value = formatNumber(totalLikes),
                    icon = Icons.Default.Favorite,
                    accentColor = Color(0xFFFF2A6D),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Comments",
                    value = formatNumber(totalComments),
                    icon = Icons.Default.ModeComment,
                    accentColor = Color(0xFFFF5722),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Shares",
                    value = formatNumber(totalShares),
                    icon = Icons.Default.Share,
                    accentColor = Color(0xFFFFD700),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Monetization Eligibility Checklist
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Monetization Eligibility Checklist",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    EligibilityCheckRow(
                        title = "At least 1,000 followers",
                        current = "${currentUser?.followersCount ?: 420}/1000",
                        isMet = (currentUser?.followersCount ?: 0) >= 1000
                    )

                    EligibilityCheckRow(
                        title = "At least 10,000 video views (30 days)",
                        current = "${formatNumber(totalViews)}/10K",
                        isMet = totalViews >= 10000
                    )

                    EligibilityCheckRow(
                        title = "At least 3 published original videos",
                        current = "${posts.size}/3",
                        isMet = posts.size >= 3
                    )

                    EligibilityCheckRow(
                        title = "Account in good community standing",
                        current = "Verified",
                        isMet = true
                    )

                    EligibilityCheckRow(
                        title = "18+ Age & Tax info verified",
                        current = "Approved",
                        isMet = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Video Performance Breakdown Table
            Text(
                text = "Video Performance Breakdown",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                posts.take(6).forEach { post ->
                    VideoPerformanceRow(post = post)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Payout Request Dialog
        if (showPayoutDialog) {
            PayoutRequestDialog(
                balance = estimatedEarnings,
                onDismiss = { showPayoutDialog = false },
                onSubmit = { method, account ->
                    showPayoutDialog = false
                    Toast.makeText(
                        context,
                        "Payout request of $%.2f via $method to $account submitted for processing!".format(estimatedEarnings),
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }

        // Admin / Creator Monetization Rules Dialog
        if (showConfigDialog) {
            MonetizationConfigDialog(
                currentConfig = monetizationConfig,
                onDismiss = { showConfigDialog = false },
                onSave = { minAmount, milestone, rpm ->
                    repository.updateMonetizationConfig(minAmount, milestone, rpm)
                    showConfigDialog = false
                    Toast.makeText(context, "Monetization & Milestone rules updated!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color.Gray, fontSize = 12.sp)
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EligibilityCheckRow(
    title: String,
    current: String,
    isMet: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isMet) Color(0xFF00E5FF) else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = if (isMet) Color.White else Color.Gray, fontSize = 13.sp)
        }
        Text(current, color = if (isMet) Color(0xFF00E5FF) else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun VideoPerformanceRow(post: VideoPost) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14141E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.caption,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("👁️ ${formatNumber(post.viewsCount)}", color = Color.Gray, fontSize = 11.sp)
                    Text("❤️ ${formatNumber(post.likesCount)}", color = Color.Gray, fontSize = 11.sp)
                    Text("💬 ${formatNumber(post.commentsCount)}", color = Color.Gray, fontSize = 11.sp)
                }
            }

            Text(
                text = "$%.2f".format((post.viewsCount / 1000.0) * 0.45),
                color = Color(0xFFFFD700),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PayoutRequestDialog(
    balance: Double,
    onDismiss: () -> Unit,
    onSubmit: (method: String, account: String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("M-Pesa") }
    var accountIdentifier by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Earnings Payout", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Available Balance: $%.2f USD".format(balance), color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Payout Method:", color = Color.LightGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("M-Pesa", "Bank Transfer", "PayPal").forEach { method ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedMethod == method) Color(0xFFFF5722) else Color(0xFF242436))
                                .clickable { selectedMethod = method }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(method, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = accountIdentifier,
                    onValueChange = { accountIdentifier = it },
                    label = { Text(if (selectedMethod == "M-Pesa") "Phone Number (+254...)" else "Account / Email") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF5722),
                        unfocusedBorderColor = Color(0xFF333346),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (accountIdentifier.isNotBlank()) {
                        onSubmit(selectedMethod, accountIdentifier)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
            ) {
                Text("Confirm Payout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF181826)
    )
}

@Composable
private fun MonetizationConfigDialog(
    currentConfig: com.example.mtaatok.model.MonetizationConfig,
    onDismiss: () -> Unit,
    onSave: (minAmount: Double, milestone: Int, rpm: Double) -> Unit
) {
    var minAmountStr by remember { mutableStateOf(currentConfig.minimumWithdrawalAmount.toString()) }
    var milestoneStr by remember { mutableStateOf(currentConfig.liveMilestoneTarget.toString()) }
    var rpmStr by remember { mutableStateOf(currentConfig.estimatedRpmRate.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monetization & Rules Settings", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Configure administrator rules for creator earnings and milestones.", color = Color.LightGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = minAmountStr,
                    onValueChange = { minAmountStr = it },
                    label = { Text("Min Withdrawal Threshold (USD)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF5722),
                        unfocusedBorderColor = Color(0xFF333346),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = milestoneStr,
                    onValueChange = { milestoneStr = it },
                    label = { Text("Live Viewer Milestone Alert Target (e.g. 999)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF333346),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rpmStr,
                    onValueChange = { rpmStr = it },
                    label = { Text("Estimated RPM Rate ($ per 1K views)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFF333346),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val minAmount = minAmountStr.toDoubleOrNull() ?: 50.0
                    val milestone = milestoneStr.toIntOrNull() ?: 999
                    val rpm = rpmStr.toDoubleOrNull() ?: 0.45
                    onSave(minAmount, milestone, rpm)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
            ) {
                Text("Save Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF181826)
    )
}

private fun formatNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> "%.1fM".format(number / 1_000_000.0)
        number >= 1_000 -> "%.1fK".format(number / 1000.0)
        else -> number.toString()
    }
}
