package com.example.ui

import android.app.Application
import android.content.Context
import android.provider.Settings
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.app.ActivityManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.CodeSnippet
import com.example.data.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LogLine(
    val raw: String,
    val level: String, // "V", "D", "I", "W", "E"
    val tag: String,
    val message: String,
    val timestamp: String = ""
)

data class SystemShortcut(
    val title: String,
    val description: String,
    val iconName: String,
    val intentAction: String,
    val category: String = "Hệ thống"
)

class DevToolboxViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DatabaseProvider.getRepository(application)

    // Developer Toggles & System Status
    private val _isDeveloperOptionsEnabled = MutableStateFlow(false)
    val isDeveloperOptionsEnabled: StateFlow<Boolean> = _isDeveloperOptionsEnabled.asStateFlow()

    private val _isUsbDebuggingEnabled = MutableStateFlow(false)
    val isUsbDebuggingEnabled: StateFlow<Boolean> = _isUsbDebuggingEnabled.asStateFlow()

    // System Info
    private val _systemInfo = MutableStateFlow<Map<String, String>>(emptyMap())
    val systemInfo: StateFlow<Map<String, String>> = _systemInfo.asStateFlow()

    // Code Lab States
    private val _currentCode = MutableStateFlow("")
    val currentCode: StateFlow<String> = _currentCode.asStateFlow()

    private val _currentLanguage = MutableStateFlow("Kotlin")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _codeSnippetTitle = MutableStateFlow("")
    val codeSnippetTitle: StateFlow<String> = _codeSnippetTitle.asStateFlow()

    private val _codeAnalysisResult = MutableStateFlow("")
    val codeAnalysisResult: StateFlow<String> = _codeAnalysisResult.asStateFlow()

    private val _isAnalyzingCode = MutableStateFlow(false)
    val isAnalyzingCode: StateFlow<Boolean> = _isAnalyzingCode.asStateFlow()

    // Room Code Snippets
    val savedSnippets: StateFlow<List<CodeSnippet>> = repository.allSnippets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Logcat States
    private val _logLines = MutableStateFlow<List<LogLine>>(emptyList())
    val logLines: StateFlow<List<LogLine>> = _logLines.asStateFlow()

    private val _isLogcatRefreshing = MutableStateFlow(false)
    val isLogcatRefreshing: StateFlow<Boolean> = _isLogcatRefreshing.asStateFlow()

    private val _logcatFilterText = MutableStateFlow("")
    val logcatFilterText: StateFlow<String> = _logcatFilterText.asStateFlow()

    private val _logcatFilterLevel = MutableStateFlow("V") // V, D, I, W, E
    val logcatFilterLevel: StateFlow<String> = _logcatFilterLevel.asStateFlow()

    private val _isLogcatPermissionGranted = MutableStateFlow(false)
    val isLogcatPermissionGranted: StateFlow<Boolean> = _isLogcatPermissionGranted.asStateFlow()

    // Shortcut Settings List
    val systemShortcuts = listOf(
        SystemShortcut(
            "Cài đặt nhà phát triển",
            "Mở trực tiếp trang bật tắt gỡ lỗi USB, chế độ nhà phát triển và cấu hình nâng cao.",
            "DeveloperBoard",
            Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS,
            "Lập trình"
        ),
        SystemShortcut(
            "Hỗ trợ tiếp cận (Accessibility)",
            "Cài đặt các dịch vụ hỗ trợ tiếp cận của thiết bị.",
            "Accessibility",
            Settings.ACTION_ACCESSIBILITY_SETTINGS,
            "Tiện ích"
        ),
        SystemShortcut(
            "Mạng & Wi-Fi",
            "Cấu hình kết nối mạng không dây và địa chỉ IP thiết bị.",
            "Wifi",
            Settings.ACTION_WIFI_SETTINGS,
            "Kết nối"
        ),
        SystemShortcut(
            "Quản lý ứng dụng",
            "Xem danh sách ứng dụng đã cài đặt, xóa cache, bộ nhớ hoặc quản lý quyền.",
            "Apps",
            Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS,
            "Hệ thống"
        ),
        SystemShortcut(
            "Ngôn ngữ & Nhập liệu",
            "Bàn phím, ngôn ngữ hệ thống phục vụ test localization ứng dụng.",
            "Language",
            Settings.ACTION_LOCALE_SETTINGS,
            "Hệ thống"
        ),
        SystemShortcut(
            "Cài đặt hệ thống chung",
            "Bảng điều khiển cài đặt thiết bị tổng quát.",
            "Settings",
            Settings.ACTION_SETTINGS,
            "Hệ thống"
        )
    )

    init {
        checkSystemSettings()
        refreshLogcat()
        // Default code snippet
        loadCodeTemplate("Hello World")
        
        // Dynamic system info updater (every 3 seconds)
        viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                loadSystemInfo()
                kotlinx.coroutines.delay(3000)
            }
        }
    }

    fun checkSystemSettings() {
        val resolver = getApplication<Application>().contentResolver
        try {
            val devEnabled = Settings.Global.getInt(resolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
            _isDeveloperOptionsEnabled.value = devEnabled
        } catch (e: Exception) {
            _isDeveloperOptionsEnabled.value = false
        }

        try {
            val adbEnabled = Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED, 0) == 1
            _isUsbDebuggingEnabled.value = adbEnabled
        } catch (e: Exception) {
            _isUsbDebuggingEnabled.value = false
        }
    }

    private fun loadSystemInfo() {
        val application = getApplication<Application>()
        
        // 1. Memory (RAM)
        val ramString = try {
            val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val availableGb = memoryInfo.availMem / (1024.0 * 1024.0 * 1024.0)
            val totalGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            val percentUsed = ((memoryInfo.totalMem - memoryInfo.availMem) * 100.0 / memoryInfo.totalMem).toInt()
            String.format("%.2f GB rảnh / %.2f GB tổng (%d%% đã dùng)", availableGb, totalGb, percentUsed)
        } catch (e: Exception) {
            "N/A"
        }

        // 2. Battery & Temperature
        var batteryPctString = "N/A"
        var tempString = "N/A"
        try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = application.registerReceiver(null, intentFilter)
            
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val pct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else -1
            
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
            val plugType = when (chargePlug) {
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_AC -> "Nguồn AC"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Không dây"
                else -> ""
            }
            
            batteryPctString = if (pct >= 0) {
                "$pct%${if (isCharging) " (Đang sạc${if (plugType.isNotEmpty()) " - $plugType" else ""})" else " (Đang xả)"}"
            } else "N/A"

            val temp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            tempString = if (temp >= 0) {
                String.format("%.1f°C", temp / 10.0)
            } else "N/A"
        } catch (e: Exception) {
            // fallback
        }

        // 3. CPU Core Count & ABI
        val cpuString = try {
            val cores = Runtime.getRuntime().availableProcessors()
            val abi = android.os.Build.SUPPORTED_ABIS.firstOrNull().orEmpty()
            "$cores nhân CPU (${abi})"
        } catch (e: Exception) {
            "N/A"
        }

        val info = mapOf(
            "Mẫu thiết bị" to android.os.Build.MODEL,
            "Nhà sản xuất" to android.os.Build.MANUFACTURER,
            "Hệ điều hành" to "Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})",
            "Dung lượng RAM" to ramString,
            "Trạng thái PIN" to batteryPctString,
            "Nhiệt độ thiết bị" to tempString,
            "Bộ vi xử lý CPU" to cpuString,
            "Mã hiệu Build" to android.os.Build.DISPLAY
        )
        _systemInfo.value = info
    }

    // Code Lab Actions
    fun setCode(code: String) {
        _currentCode.value = code
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    fun setCodeSnippetTitle(title: String) {
        _codeSnippetTitle.value = title
    }

    fun loadCodeTemplate(templateName: String) {
        val template = when (templateName) {
            "Bật Toast" -> """
                import android.widget.Toast
                import android.content.Context

                fun showMyToast(context: Context) {
                    Toast.makeText(
                        context, 
                        "Chào mừng các lập trình viên đến với DevToolbox!", 
                        Toast.LENGTH_LONG
                    ).show()
                }
            """.trimIndent()
            "Gửi Notification" -> """
                import android.app.NotificationChannel
                import android.app.NotificationManager
                import android.content.Context
                import android.os.Build
                import androidx.core.app.NotificationCompat

                fun sendTestNotification(context: Context) {
                    val channelId = "test_channel"
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(channelId, "Test", NotificationManager.IMPORTANCE_DEFAULT)
                        notificationManager.createNotificationChannel(channel)
                    }

                    val builder = NotificationCompat.Builder(context, channelId)
                        .setContentTitle("DevToolbox Alert")
                        .setContentText("Tính năng code trực tiếp đã chạy thành công!")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                    notificationManager.notify(1, builder.build())
                }
            """.trimIndent()
            "Yêu cầu Quyền Camera" -> """
                import android.content.Context
                import android.content.pm.PackageManager
                import androidx.core.content.ContextCompat
                import android.Manifest

                fun checkCameraPermission(context: Context): Boolean {
                    val permissionStatus = ContextCompat.checkSelfPermission(
                        context, 
                        Manifest.permission.CAMERA
                    )
                    return permissionStatus == PackageManager.PERMISSION_GRANTED
                }
            """.trimIndent()
            "Đọc Logcat cục bộ" -> """
                import java.io.BufferedReader
                import java.io.InputStreamReader

                fun readLocalLogs(): List<String> {
                    val logs = mutableListOf<String>()
                    val process = Runtime.getRuntime().exec("logcat -d -v brief")
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        logs.add(line ?: "")
                    }
                    return logs
                }
            """.trimIndent()
            else -> """
                fun main() {
                    val language = "Kotlin"
                    println("Chào mừng bạn đến với trình Code di động!")
                    println("Thiết bị của bạn là: ${android.os.Build.MODEL}")
                    println("API Level: ${android.os.Build.VERSION.SDK_INT}")
                    
                    val features = listOf("USB Debug Quick Access", "Interactive Code Sandbox", "Live Logcat Screen")
                    for (feature in features) {
                        println("✓ Hỗ trợ: " + feature)
                    }
                }
            """.trimIndent()
        }
        _currentCode.value = template
        _codeSnippetTitle.value = templateName
    }

    fun analyzeAndCompileCode() {
        val code = _currentCode.value
        val lang = _currentLanguage.value
        if (code.isBlank()) return

        _isAnalyzingCode.value = true
        _codeAnalysisResult.value = "Đang gửi mã nguồn đến máy chủ AI để mô phỏng biên dịch và phân tích lỗi..."

        viewModelScope.launch {
            val result = GeminiClient.analyzeCode(code, lang)
            _codeAnalysisResult.value = result
            _isAnalyzingCode.value = false
        }
    }

    fun saveSnippet() {
        val title = _codeSnippetTitle.value.ifBlank { "Mẫu code " + System.currentTimeMillis() % 10000 }
        val code = _currentCode.value
        val lang = _currentLanguage.value

        if (code.isBlank()) return

        viewModelScope.launch {
            repository.insert(
                CodeSnippet(
                    title = title,
                    code = code,
                    language = lang
                )
            )
        }
    }

    fun deleteSnippet(snippetId: Long) {
        viewModelScope.launch {
            repository.deleteById(snippetId)
        }
    }

    // Logcat Actions
    fun refreshLogcat() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLogcatRefreshing.value = true
                val process = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-v", "brief"))
                val reader = process.inputStream.bufferedReader()
                val logs = mutableListOf<LogLine>()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val cleanLine = line!!
                    if (cleanLine.startsWith("--------- beginning of")) continue
                    logs.add(parseBriefLogLine(cleanLine))
                }
                process.waitFor()
                
                val limitedLogs = if (logs.size > 800) logs.takeLast(800) else logs
                
                withContext(Dispatchers.Main) {
                    _logLines.value = limitedLogs.reversed()
                    _isLogcatPermissionGranted.value = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _logLines.value = listOf(LogLine(e.message ?: "Lỗi tải logcat", "E", "Hệ thống", e.message ?: "Lỗi tải logcat"))
                    _isLogcatPermissionGranted.value = false
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLogcatRefreshing.value = false
                }
            }
        }
    }

    fun clearDisplayedLogs() {
        _logLines.value = emptyList()
    }

    fun setLogcatFilterText(text: String) {
        _logcatFilterText.value = text
    }

    fun setLogcatFilterLevel(level: String) {
        _logcatFilterLevel.value = level
    }

    private fun parseBriefLogLine(line: String): LogLine {
        try {
            if (line.length >= 3 && line[1] == '/') {
                val level = line[0].toString()
                val tagAndMsg = line.substring(2)
                val colonIndex = tagAndMsg.indexOf(':')
                if (colonIndex != -1) {
                    val tagPart = tagAndMsg.substring(0, colonIndex)
                    val parenIndex = tagPart.indexOf('(')
                    val tag = if (parenIndex != -1) tagPart.substring(0, parenIndex).trim() else tagPart.trim()
                    val message = tagAndMsg.substring(colonIndex + 1).trim()
                    return LogLine(line, level, tag, message)
                }
            }
        } catch (e: Exception) {
            // fallback
        }
        return LogLine(line, "I", "Logcat", line)
    }

    fun exportToAndroidProject(context: Context, onResult: (String?, Uri?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val code = _currentCode.value
                val lang = _currentLanguage.value
                val title = _codeSnippetTitle.value.ifBlank { "DevProject" }
                val cleanTitle = title.replace("[^a-zA-Z0-9]".toRegex(), "")
                val zipFileName = "${cleanTitle}_AndroidProject.zip"
                
                val cacheDir = context.cacheDir
                val zipFile = java.io.File(cacheDir, zipFileName)
                if (zipFile.exists()) {
                    zipFile.delete()
                }

                val zipOutputStream = java.util.zip.ZipOutputStream(java.io.FileOutputStream(zipFile))

                // 1. Settings.gradle.kts
                addZipEntry(zipOutputStream, "settings.gradle.kts", """
                    pluginManagement {
                        repositories {
                            google()
                            mavenCentral()
                            gradlePluginPortal()
                        }
                    }
                    dependencyResolutionManagement {
                        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                        repositories {
                            google()
                            mavenCentral()
                        }
                    }
                    rootProject.name = "$cleanTitle"
                    include(":app")
                """.trimIndent())

                // 2. build.gradle.kts (Project level)
                addZipEntry(zipOutputStream, "build.gradle.kts", """
                    plugins {
                        id("com.android.application") version "8.2.2" apply false
                        id("org.jetbrains.kotlin.android") version "1.9.22" apply false
                    }
                """.trimIndent())

                // 3. app/build.gradle.kts
                addZipEntry(zipOutputStream, "app/build.gradle.kts", """
                    plugins {
                        id("com.android.application")
                        id("org.jetbrains.kotlin.android")
                    }

                    android {
                        namespace = "com.example.generated"
                        compileSdk = 34

                        defaultConfig {
                            applicationId = "com.example.${cleanTitle.lowercase()}"
                            minSdk = 24
                            targetSdk = 34
                            versionCode = 1
                            versionName = "1.0"
                        }

                        buildTypes {
                            release {
                                isMinifyEnabled = false
                                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                            }
                        }
                        compileOptions {
                            sourceCompatibility = JavaVersion.VERSION_1_8
                            targetCompatibility = JavaVersion.VERSION_1_8
                        }
                        kotlinOptions {
                            jvmTarget = "1.8"
                        }
                    }

                    dependencies {
                        implementation("androidx.core:core-ktx:1.12.0")
                        implementation("androidx.appcompat:appcompat:1.6.1")
                        implementation("com.google.android.material:material:1.11.0")
                        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
                    }
                """.trimIndent())

                // 4. app/src/main/AndroidManifest.xml
                addZipEntry(zipOutputStream, "app/src/main/AndroidManifest.xml", """
                    <?xml version="1.0" encoding="utf-8"?>
                    <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                        <uses-permission android:name="android.permission.INTERNET"/>
                        <uses-permission android:name="android.permission.CAMERA"/>
                        <application
                            android:allowBackup="true"
                            android:icon="@android:drawable/sym_def_app_icon"
                            android:label="$title"
                            android:roundIcon="@android:drawable/sym_def_app_icon"
                            android:supportsRtl="true"
                            android:theme="@style/Theme.AppCompat.Light.DarkActionBar">
                            <activity
                                android:name=".MainActivity"
                                android:exported="true">
                                <intent-filter>
                                    <action android:name="android.intent.action.MAIN" />
                                    <category android:name="android.intent.category.LAUNCHER" />
                                </intent-filter>
                            </activity>
                        </application>
                    </manifest>
                """.trimIndent())

                // 5. Code File
                val fileExtension = if (lang == "Kotlin") "kt" else "java"
                val sourceFilePath = "app/src/main/java/com/example/generated/MainActivity.$fileExtension"
                
                val finalCode = if (code.contains("class MainActivity") || code.contains("Activity()")) {
                    code
                } else {
                    if (lang == "Kotlin") {
                        """
                        package com.example.generated

                        import android.os.Bundle
                        import androidx.appcompat.app.AppCompatActivity
                        import android.widget.LinearLayout
                        import android.widget.Button
                        import android.widget.TextView
                        import android.view.Gravity

                        class MainActivity : AppCompatActivity() {
                            override fun onCreate(savedInstanceState: Bundle?) {
                                super.onCreate(savedInstanceState)
                                
                                val layout = LinearLayout(this).apply {
                                    orientation = LinearLayout.VERTICAL
                                    gravity = Gravity.CENTER
                                    setPadding(32, 32, 32, 32)
                                }

                                val titleTextView = TextView(this).apply {
                                    text = "DevToolbox Exported App"
                                    textSize = 24f
                                    gravity = Gravity.CENTER
                                    setPadding(0, 0, 0, 64)
                                }
                                layout.addView(titleTextView)

                                val runButton = Button(this).apply {
                                    text = "Run Custom Code Action"
                                    setOnClickListener {
                                        try {
                                            executeCustomCode()
                                        } catch(e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                layout.addView(runButton)

                                setContentView(layout)
                            }

                            // --- USER CUSTOM CODE START ---
                            ${code.replace("package ", "// package ")}
                            // --- USER CUSTOM CODE END ---
                            
                            private fun executeCustomCode() {
                                // Nếu code của bạn có hàm main hoặc showMyToast, hãy gọi ở đây
                                println("Chạy code thành công!")
                            }
                        }
                        """.trimIndent()
                    } else {
                        """
                        package com.example.generated;

                        import android.os.Bundle;
                        import androidx.appcompat.app.AppCompatActivity;
                        import android.widget.LinearLayout;
                        import android.widget.Button;
                        import android.widget.TextView;
                        import android.view.Gravity;

                        public class MainActivity extends AppCompatActivity {
                            @Override
                            protected void onCreate(Bundle savedInstanceState) {
                                super.onCreate(savedInstanceState);
                                
                                LinearLayout layout = new LinearLayout(this);
                                layout.setOrientation(LinearLayout.VERTICAL);
                                layout.setGravity(Gravity.CENTER);
                                layout.setPadding(32, 32, 32, 32);

                                TextView titleTextView = new TextView(this);
                                titleTextView.setText("DevToolbox Exported App");
                                titleTextView.setTextSize(24f);
                                titleTextView.setGravity(Gravity.CENTER);
                                titleTextView.setPadding(0, 0, 0, 64);
                                layout.addView(titleTextView);

                                Button runButton = new Button(this);
                                runButton.setText("Run Custom Code Action");
                                runButton.setOnClickListener(v -> {
                                    try {
                                        executeCustomCode();
                                    } catch(Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                                layout.addView(runButton);

                                setContentView(layout);
                            }

                            private void executeCustomCode() {
                                // Gọi các hàm của bạn tại đây;
                                System.out.println("Chạy code thành công!");
                            }
                        }
                        """.trimIndent()
                    }
                }

                addZipEntry(zipOutputStream, sourceFilePath, finalCode)
                zipOutputStream.close()

                val contentUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    zipFile
                )

                withContext(Dispatchers.Main) {
                    onResult(zipFileName, contentUri)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(null, null)
                }
            }
        }
    }

    private fun addZipEntry(zipOutputStream: java.util.zip.ZipOutputStream, path: String, content: String) {
        val entry = java.util.zip.ZipEntry(path)
        zipOutputStream.putNextEntry(entry)
        zipOutputStream.write(content.toByteArray())
        zipOutputStream.closeEntry()
    }
}
