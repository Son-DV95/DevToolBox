package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun analyzeCode(code: String, language: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Vui lòng cấu hình GEMINI_API_KEY trong mục Secrets của AI Studio."
        }

        val prompt = """
            Hãy đóng vai là một Trình Biên Dịch & Chuyên Gia Phát Triển Android (Android Compiler & Dev Expert).
            Hãy biên dịch thử và phân tích đoạn code $language dưới đây:
            
            ```$language
            $code
            ```
            
            Vui lòng trả về phản hồi chi tiết, được cấu trúc rõ ràng bằng tiếng Việt gồm các phần sau:
            1. 🔍 **Kết quả chạy thử (Simulated Output)**: Kết quả xuất ra màn hình (ví dụ logcat, toast, hoặc kết quả hàm) nếu đoạn code được chạy trên thiết bị Android thực tế.
            2. 🛠️ **Phân tích lỗi (Syntax/Logic Check)**: Phát hiện bất kỳ lỗi cú pháp, lỗi logic, hoặc các lỗi có thể gây crash ứng dụng trên các phiên bản Android khác nhau (đặc biệt chú ý về thread chính, quyền hạn permissions, hoặc null safety).
            3. 💡 **Đề xuất tối ưu (Android Best Practices)**: Gợi ý cách cải tiến hoặc tối ưu đoạn code này theo tiêu chuẩn Material 3, Jetpack Compose, Kotlin Coroutines, v.v. kèm theo một phiên bản mã đã tối ưu hóa nếu cần.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = "Bạn là một trợ lý đắc lực hỗ trợ lập trình viên Android di động.")))
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Không nhận được phản hồi từ Gemini API."
        } catch (e: Exception) {
            "Lỗi kết nối hoặc API: ${e.localizedMessage ?: e.message}"
        }
    }
}
