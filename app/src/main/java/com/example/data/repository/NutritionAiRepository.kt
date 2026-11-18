package com.example.data.repository

import android.util.Log
import com.example.core.formatVnd
import com.example.data.local.DishEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "NutritionAiRepo"

data class AiDishRecommendation(
    val dishId: Int,
    val reason: String
)

data class AiNutritionResponse(
    val dailyCalories: Int,
    val macroSummary: String,
    val reply: String,
    val recommendations: List<AiDishRecommendation>
)

class NutritionAiRepository {

    private val responseSchema = Schema.obj(
        properties = mapOf(
            "dailyCalories" to Schema.integer(
                description = "Ước tính lượng calo mục tiêu mỗi ngày (kcal)"
            ),
            "macroSummary" to Schema.string(
                description = "Tóm tắt tỷ lệ dinh dưỡng ngắn gọn, ví dụ: Protein 30% • Carb 40% • Fat 30%"
            ),
            "reply" to Schema.string(
                description = "Lời tư vấn ngắn gọn, thân thiện bằng tiếng Việt (tối đa 3-4 câu), không đưa lời khuyên y tế chuyên sâu"
            ),
            "recommendations" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "dishId" to Schema.integer(description = "ID chính xác của món ăn có trong thực đơn"),
                        "reason" to Schema.string(description = "Lý do ngắn gọn món này phù hợp với mục tiêu")
                    )
                ),
                description = "Danh sách 2 đến 3 món gợi ý từ thực đơn hiện có của GreenBite"
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = this@NutritionAiRepository.responseSchema
                temperature = 0.6f
            },
            systemInstruction = content {
                text(
                    """
                    Bạn là Trợ lý Dinh dưỡng thân thiện của GreenBite (ứng dụng đồ ăn Healthy).
                    Quy tắc bắt buộc:
                    1. Trả lời 100% bằng tiếng Việt tự nhiên, ngắn gọn, tích cực và thân thiện.
                    2. Tuyệt đối KHÔNG đưa ra chẩn đoán hoặc lời khuyên y tế/bệnh lý chuyên sâu. Nếu người dùng có dị ứng, hãy tránh tuyệt đối các món chứa thành phần đó.
                    3. Tính mức calo ước tính hàng ngày (kcal) hợp lý dựa trên chiều cao, cân nặng, tuổi và mục tiêu (giảm cân / giữ dáng / tăng cơ).
                    4. Chỉ gợi ý các món CÓ TRONG DANH SÁCH THỰC ĐƠN được cung cấp trong prompt và sử dụng đúng trường `dishId`. Chọn 2-3 món phù hợp nhất.
                    """.trimIndent()
                )
            }
        )
    }

    suspend fun askNutritionAdvice(
        heightCm: Int?,
        weightKg: Int?,
        age: Int?,
        goal: String,
        allergies: String,
        userMessage: String,
        availableDishes: List<DishEntity>
    ): AiNutritionResponse {
        val menuCatalog = availableDishes.joinToString(separator = "\n") { dish ->
            "- dishId=${dish.id}: ${dish.name} | Nhóm: ${dish.category} | Giá: ${formatVnd(dish.price)} | ${dish.calories} kcal (P:${dish.protein}g, C:${dish.carbs}g, F:${dish.fat}g) | Nhãn: ${dish.tagsCsv} | Thành phần: ${dish.ingredientsCsv}"
        }

        val profileSummary = buildString {
            appendLine("Thông tin khách hàng:")
            appendLine("- Chiều cao: ${heightCm?.let { "$it cm" } ?: "Chưa cung cấp"}")
            appendLine("- Cân nặng: ${weightKg?.let { "$it kg" } ?: "Chưa cung cấp"}")
            appendLine("- Tuổi: ${age?.let { "$it tuổi" } ?: "Chưa cung cấp"}")
            appendLine("- Mục tiêu: $goal")
            appendLine("- Dị ứng / Lưu ý: ${allergies.ifBlank { "Không có" }}")
            if (userMessage.isNotBlank()) {
                appendLine("- Câu hỏi thêm của khách: $userMessage")
            }
        }

        val fullPrompt = """
            $profileSummary

            Danh sách món ăn hiện có tại GreenBite:
            $menuCatalog

            Hãy ước tính calo mỗi ngày, tóm tắt macro gợi ý, trả lời thân thiện và chọn 2-3 món phù hợp nhất từ thực đơn trên.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                model.generateContent(fullPrompt)
            }
            val rawJson = response.text?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Trợ lý AI không trả về nội dung.")

            val json = JSONObject(rawJson)
            val dailyCalories = json.optInt("dailyCalories", 1800)
            val macroSummary = json.optString("macroSummary", "Cân bằng Protein • Carb chậm • Chất béo tốt")
            val reply = json.optString("reply", "Mình đã chọn một vài món lành mạnh phù hợp với mục tiêu của bạn!")
            val recArray = json.optJSONArray("recommendations")
            val recommendations = buildList {
                if (recArray != null) {
                    for (i in 0 until recArray.length()) {
                        val obj = recArray.optJSONObject(i) ?: continue
                        val dishId = obj.optInt("dishId", -1)
                        val reason = obj.optString("reason", "Phù hợp với mục tiêu dinh dưỡng của bạn")
                        if (availableDishes.any { it.id == dishId }) {
                            add(AiDishRecommendation(dishId = dishId, reason = reason))
                        }
                    }
                }
            }

            AiNutritionResponse(
                dailyCalories = dailyCalories,
                macroSummary = macroSummary,
                reply = reply,
                recommendations = recommendations
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini nutrition inference timed out", e)
            throw IllegalStateException("Kết nối AI quá thời gian 60 giây. Vui lòng thử lại.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini nutrition inference failed", e)
            throw e
        }
    }
}
