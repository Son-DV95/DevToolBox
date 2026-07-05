package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.DevToolboxViewModel
import com.example.ui.LogLine
import com.example.ui.SystemShortcut

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: DevToolboxViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(0) }

    // Recheck system settings whenever screen is active
    DisposableEffect(Unit) {
        viewModel.checkSystemSettings()
        onDispose {}
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeveloperMode,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "DevToolbox",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(
                            onClick = {
                                viewModel.checkSystemSettings()
                                Toast.makeText(context, "Đã cập nhật trạng thái hệ thống!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("refresh_status_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Tải lại")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 1.dp)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Hệ thống") },
                        label = { Text("Trạng thái") },
                        modifier = Modifier.testTag("tab_status")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Code, contentDescription = "Playground") },
                        label = { Text("Code Lab") },
                        modifier = Modifier.testTag("tab_playground")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Terminal, contentDescription = "Logcat") },
                        label = { Text("Logcat") },
                        modifier = Modifier.testTag("tab_logcat")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> StatusAndShortcutsScreen(viewModel)
                1 -> CodePlaygroundScreen(viewModel)
                2 -> LogcatViewerScreen(viewModel)
            }
        }
    }
}

@Composable
fun StatusAndShortcutsScreen(viewModel: DevToolboxViewModel) {
    val context = LocalContext.current
    val isDevEnabled by viewModel.isDeveloperOptionsEnabled.collectAsStateWithLifecycle()
    val isUsbEnabled by viewModel.isUsbDebuggingEnabled.collectAsStateWithLifecycle()
    val systemInfo by viewModel.systemInfo.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card Banner with Canvas Background Art
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                // Subtle graphic background
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = size.width / 3f,
                        center = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = size.width / 2f,
                        center = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "DevToolbox Trợ Lý",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tối ưu hóa quy trình làm việc cho các lập trình viên di động nhanh chóng.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // System Toggles & Status Block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Trạng Thái Hệ Thống",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusIndicator(
                            title = "Tùy chọn nhà phát triển",
                            isEnabled = isDevEnabled,
                            modifier = Modifier.weight(1f)
                        )
                        StatusIndicator(
                            title = "Gỡ lỗi qua USB (USB Debug)",
                            isEnabled = isUsbEnabled,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Không thể mở Cài đặt nhà phát triển: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_dev_settings_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Launch, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đi Đến Cài Đặt Phát Triển", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Shortcuts Grid
        item {
            Text(
                text = "Phím Tắt Hệ Thống Quan Trọng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(viewModel.systemShortcuts.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShortcutCard(shortcut = pair[0], modifier = Modifier.weight(1f))
                if (pair.size > 1) {
                    ShortcutCard(shortcut = pair[1], modifier = Modifier.weight(1f))
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Monospace Terminal System Info Block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E) // Dark terminal background
                ),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF4CAF50))
                        )
                        Text(
                            text = "Thông Tin Thiết Bị",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFE0E0E0),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    systemInfo.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$key:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB0B0B0),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(
    title: String,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isEnabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    val contentColor = if (isEnabled) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val iconBgColor = if (isEnabled) {
        Color.White.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(24.dp),
        border = if (!isEnabled) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isEnabled) "Đang hoạt động" else "Chưa kích hoạt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun ShortcutCard(
    shortcut: SystemShortcut,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val icon = when (shortcut.iconName) {
        "DeveloperBoard" -> Icons.Default.DeveloperMode
        "Accessibility" -> Icons.Default.Accessibility
        "Wifi" -> Icons.Default.Wifi
        "Apps" -> Icons.Default.Apps
        "Language" -> Icons.Default.Language
        "Settings" -> Icons.Default.Settings
        else -> Icons.Default.Settings
    }

    Card(
        modifier = modifier
            .height(130.dp)
            .clickable {
                try {
                    val intent = Intent(shortcut.intentAction)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Không thể mở mục: ${shortcut.title}", Toast.LENGTH_SHORT).show()
                }
            }
            .testTag("shortcut_card_${shortcut.title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.Center)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = shortcut.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = shortcut.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 11.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodePlaygroundScreen(viewModel: DevToolboxViewModel) {
    val context = LocalContext.current
    val currentCode by viewModel.currentCode.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val snippetTitle by viewModel.codeSnippetTitle.collectAsStateWithLifecycle()
    val analysisResult by viewModel.codeAnalysisResult.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzingCode.collectAsStateWithLifecycle()
    val savedSnippets by viewModel.savedSnippets.collectAsStateWithLifecycle()

    val templates = listOf("Trống", "Bật Toast", "Yêu cầu Quyền Camera", "Gửi Notification", "Đọc Logcat cục bộ")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome tutorial & info card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lập trình & Xuất Dự Án Thật",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Bạn có thể lựa chọn mẫu nhanh hoặc viết mã nguồn Kotlin/Java trực tiếp. Hãy bấm 'Chạy & Phân tích AI' để kiểm tra lỗi, và bấm 'Xuất dự án (.zip)' để tải về cấu trúc dự án Gradle hoàn chỉnh có thể mở ngay bằng Android Studio trên PC của bạn!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Code Template pills selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Mẫu Mã Nguồn Nhanh (Templates)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(templates) { template ->
                        FilterChip(
                            selected = snippetTitle == template,
                            onClick = { viewModel.loadCodeTemplate(template) },
                            label = { Text(template) },
                            modifier = Modifier.testTag("template_pill_$template")
                        )
                    }
                }
            }
        }

        // Language Segmented Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ngôn ngữ:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                listOf("Kotlin", "Java").forEach { lang ->
                    ElevatedFilterChip(
                        selected = currentLanguage == lang,
                        onClick = { viewModel.setLanguage(lang) },
                        label = { Text(lang) },
                        modifier = Modifier.testTag("lang_chip_$lang")
                    )
                }
            }
        }

        // Code Editor area with title input
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = snippetTitle,
                        onValueChange = { viewModel.setCodeSnippetTitle(it) },
                        label = { Text("Tiêu đề mẫu Code") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("snippet_title_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = currentCode,
                        onValueChange = { viewModel.setCode(it) },
                        placeholder = { Text("// Nhập mã Android của bạn tại đây...", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .testTag("code_editor_field"),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFFEADDFF)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1C1B1F),
                            unfocusedContainerColor = Color(0xFF1C1B1F),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF333333)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.analyzeAndCompileCode()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("run_analyze_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = !isAnalyzing
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Chạy & Phân tích AI", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.saveSnippet()
                                Toast.makeText(context, "Đã lưu mã nguồn thành công!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("save_snippet_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lưu nháp")
                        }

                        var isExporting by remember { mutableStateOf(false) }

                        Button(
                            onClick = {
                                isExporting = true
                                viewModel.exportToAndroidProject(context) { fileName, uri ->
                                    isExporting = false
                                    if (uri != null) {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/zip"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            putExtra(Intent.EXTRA_SUBJECT, "Dự án Android: $fileName")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Xuất dự án Android Studio"))
                                    } else {
                                        Toast.makeText(context, "Lỗi xuất dự án!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1.4f)
                                .height(46.dp)
                                .testTag("export_project_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            enabled = !isExporting
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Xuất dự án (.zip)", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        // Compilation / Analysis Result Output Console
        item {
            AnimatedVisibility(
                visible = analysisResult.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1C1B1F) // terminal black matching editor
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartButton,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Bảng Kết Quả & Phân Tích AI",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Code Analysis", analysisResult)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Đã sao chép phân tích!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Sao chép",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = analysisResult,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = if (isAnalyzing) Color.Gray else Color(0xFFECEFF1),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Saved Code snippets list
        item {
            if (savedSnippets.isNotEmpty()) {
                Text(
                    text = "Mã Nguồn Đã Lưu (${savedSnippets.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        items(savedSnippets) { snippet ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("snippet_item_${snippet.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = snippet.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(snippet.language, fontSize = 10.sp) },
                                modifier = Modifier.height(22.dp)
                            )
                            Text(
                                text = android.text.format.DateFormat.format("HH:mm dd/MM/yyyy", snippet.timestamp).toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                viewModel.setCode(snippet.code)
                                viewModel.setLanguage(snippet.language)
                                viewModel.setCodeSnippetTitle(snippet.title)
                                Toast.makeText(context, "Đã tải mẫu code lên trình soạn thảo!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("load_snippet_${snippet.id}")
                        ) {
                            Icon(
                                Icons.Default.CloudDownload,
                                contentDescription = "Tải lên",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.deleteSnippet(snippet.id)
                                Toast.makeText(context, "Đã xóa mẫu code!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("delete_snippet_${snippet.id}")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Xóa",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogcatViewerScreen(viewModel: DevToolboxViewModel) {
    val context = LocalContext.current
    val logLines by viewModel.logLines.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isLogcatRefreshing.collectAsStateWithLifecycle()
    val filterText by viewModel.logcatFilterText.collectAsStateWithLifecycle()
    val filterLevel by viewModel.logcatFilterLevel.collectAsStateWithLifecycle()
    val isPermissionGranted by viewModel.isLogcatPermissionGranted.collectAsStateWithLifecycle()

    val levelFilters = listOf(
        "V" to "Tất cả",
        "D" to "Debug+",
        "I" to "Info+",
        "W" to "Warn+",
        "E" to "Error"
    )

    // Filter logs client-side
    val filteredLogs = remember(logLines, filterText, filterLevel) {
        logLines.filter { line ->
            // Level Filter check
            val levelWeight = mapOf("V" to 0, "D" to 1, "I" to 2, "W" to 3, "E" to 4)
            val lineWeight = levelWeight[line.level] ?: 0
            val selectedWeight = levelWeight[filterLevel] ?: 0

            val matchesLevel = lineWeight >= selectedWeight
            val matchesSearch = if (filterText.isBlank()) {
                true
            } else {
                line.tag.contains(filterText, ignoreCase = true) || line.message.contains(filterText, ignoreCase = true)
            }

            matchesLevel && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Logcat Actions & Guide Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Trình Quản Lý Logcat",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { viewModel.refreshLogcat() },
                            enabled = !isRefreshing,
                            modifier = Modifier.testTag("logcat_refresh_btn")
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Tải lại")
                            }
                        }

                        IconButton(
                            onClick = { viewModel.clearDisplayedLogs() },
                            modifier = Modifier.testTag("logcat_clear_btn")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Dọn sạch hiển thị")
                        }

                        IconButton(
                            onClick = {
                                val logText = filteredLogs.joinToString("\n") { "[${it.level}] ${it.tag}: ${it.message}" }
                                if (logText.isNotEmpty()) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Logcat Export", logText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Đã sao chép ${filteredLogs.size} dòng log!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Không có dòng log nào để sao chép!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Sao chép toàn bộ log")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Mẹo: Mặc định ứng dụng chỉ hiển thị logs của chính nó. Để hiển thị logs của toàn bộ hệ thống Android, hãy chạy lệnh ADB sau trên máy tính của bạn:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val adbCommand = "adb shell pm grant com.aistudio.devtoolbox.qzkpmw android.permission.READ_LOGS"
                    Text(
                        text = adbCommand,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("ADB Command", adbCommand)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Đã sao chép lệnh ADB!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Sao chép lệnh adb",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Search & Filters row
        OutlinedTextField(
            value = filterText,
            onValueChange = { viewModel.setLogcatFilterText(it) },
            label = { Text("Tìm kiếm theo Tag hoặc Message") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("logcat_search_field"),
            singleLine = true,
            trailingIcon = {
                if (filterText.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setLogcatFilterText("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Xóa")
                    }
                }
            }
        )

        // Horizontal filter buttons
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(levelFilters) { (level, name) ->
                FilterChip(
                    selected = filterLevel == level,
                    onClick = { viewModel.setLogcatFilterLevel(level) },
                    label = { Text(name) },
                    modifier = Modifier.testTag("logcat_filter_pill_$level")
                )
            }
        }

        // Logs terminal list
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF121212) // Pure dark ide terminal
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(40.dp))
                        Text(
                            text = "Không tìm thấy logs nào phù hợp.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredLogs) { log ->
                        LogLineItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun LogLineItem(log: LogLine) {
    val context = LocalContext.current
    // Colors inspired by professional Android Studio logcat
    val (bgColor, textColor, label) = when (log.level) {
        "E" -> Triple(Color(0xFF2D1616), Color(0xFFFF8A80), "ERR")
        "W" -> Triple(Color(0xFF2C2216), Color(0xFFFFD54F), "WRN")
        "I" -> Triple(Color(0xFF142416), Color(0xFF81C784), "INF")
        "D" -> Triple(Color(0xFF16202B), Color(0xFF64B5F6), "DBG")
        else -> Triple(Color(0xFF1A1A1A), Color(0xFFB0BEC5), "VRB")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .clickable {
                val fullLog = "[${log.level}] ${log.tag}: ${log.message}"
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Log Line", fullLog)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Đã sao chép dòng log!", Toast.LENGTH_SHORT).show()
            }
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Level Badge
        Box(
            modifier = Modifier
                .width(34.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(textColor.copy(alpha = 0.2f))
                .padding(vertical = 1.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontFamily = FontFamily.Monospace
            )
        }

        // Tag & Message
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.tag,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 120.dp)
                )
                
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = log.message,
                fontSize = 11.sp,
                color = Color(0xFFECEFF1),
                fontFamily = FontFamily.Monospace,
                lineHeight = 14.sp
            )
        }
    }
}
