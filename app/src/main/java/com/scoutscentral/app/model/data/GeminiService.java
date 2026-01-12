package com.scoutscentral.app.model.data;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.scoutscentral.app.BuildConfig;
import com.scoutscentral.app.model.Scout;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiService {
    private static final String TAG = "GeminiService";
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
            
    private final Gson gson = new Gson();

    public String generateProgressPlan(Scout scout, String positiveTraits, String negativeTraits) throws IOException {
        String prompt = buildPrompt(scout, positiveTraits, negativeTraits);
        
        try {
            return callGeminiApi("v1beta", "gemma-3-12b", prompt);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                Log.w(TAG, "gemma-3-12b not found, falling back to gemini-2.5-flash");
                try {
                    return callGeminiApi("v1", "gemini-2.5-flash", prompt);
                } catch (IOException e2) {
                    Log.w(TAG, "gemini-2.5-flash failed, falling back to gemini-2.0-flash");
                    return callGeminiApi("v1beta", "gemini-2.0-flash", prompt);
                }
            }
            throw e;
        }
    }

    private String callGeminiApi(String version, String model, String prompt) throws IOException {
        String url = String.format("https://generativelanguage.googleapis.com/%s/models/%s:generateContent?key=%s", 
                version, model, API_KEY);

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject root = new JsonObject();
        root.add("contents", contents);

        RequestBody body = RequestBody.create(gson.toJson(root), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseString = response.body() != null ? response.body().string() : "";
            
            if (!response.isSuccessful()) {
                int code = response.code();
                Log.e(TAG, "Gemini API " + version + "/" + model + " failed: " + code);
                Log.e(TAG, "Full error response: " + responseString);
                
                if (code == 429) {
                    throw new IOException("מכסת הבקשות של Gemini AI הסתיימה (429). אנא נסו שוב בעוד דקה.");
                }
                if (code == 404) {
                    throw new IOException("Gemini API error 404");
                }
                throw new IOException("Gemini API error " + code);
            }

            try {
                JsonObject jsonResponse = JsonParser.parseString(responseString).getAsJsonObject();
                return jsonResponse.getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse successful Gemini response: " + responseString, e);
                throw new IOException("שגיאה בפענוח תשובת ה-AI");
            }
        }
    }

    private String buildPrompt(Scout scout, String positiveTraits, String negativeTraits) {
        return String.format(
            "אתה יועץ חכם ומנוסה בתנועת הצופים. המשימה שלך היא ליצור תוכנית התקדמות אישית, מקצועית ומעוררת השראה עבור חניך בשם %s, שמטרתה לחזק את התכונות החיוביות שלו ולסייע לו להתמודד עם התכונות השליליות.\n\n" +
            "פרטי החניך:\n" +
            "📈 דרגה: %s\n" +
            "👍 תכונות חיוביות לחיזוק: %s\n" +
            "👎 תכונות שליליות לשיפור: %s\n\n" +
            "הנחיות לבניית התוכנית:\n" +
            "1.  **מבנה ויזואלי**: עצב את התוכנית בצורה ויזואלית יפה וקלה לקריאה ב-TextView של אנדרואיד. השתמש בכותרות ברורות, אייקונים (Emojis) ורווחים בין הפסקאות.\n" +
            "2.  **תוכן התוכנית**:\n" +
            "    - **🌟 משימות מעשיות**: הצע לפחות 3 משימות מעשיות ומאתגרות. כל משימה צריכה להתבסס על התכונות החיוביות של החניך כדי לחזק אותן, ובו זמנית לאתגר אותו בתחומים הקשורים לתכונות השליליות שלו.\n" +
            "    - **🏅 תג מומחיות מוצע**: הצע תג מומחיות חדש שמתאים לחניך. הסבר בקצרה איך השגת התג תסייע לו לחזק את עצמו.\n" +
            "    - **💪 טיפ להצלחה**: סיים עם עצה קצרה ומחזקת, המעודדת את החניך ומעניקה לו מוטיבציה.\n\n" +
            "כתוב את כל התשובה בעברית רהוטה ומושכת, בגוף שני (פנייה ישירה לחניך).",
            scout.getName(),
            scout.getLevel().name(),
            positiveTraits,
            negativeTraits
        );
    }
}
