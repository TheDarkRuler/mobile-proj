package com.example.mobile_proj.ui.screens.workoutChatBot

import android.content.Context
import androidx.lifecycle.ViewModel
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.mobile_proj.BuildConfig
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

interface ApiCallback {
    fun onSuccess(result: String)
    fun onError(error: VolleyError)
}

class WorkoutChatBotViewModel : ViewModel() {
    val stringAPIKey = BuildConfig.BOT_API_KEY
    val stringURLEndPoint = "https://api.llama-api.com/chat/completions"

    fun buttonLlamaAPI(exercise: String, context: Context, callback: ApiCallback) {

        val jsonObject =  JSONObject()
        val jsonObjectMessage = JSONObject()
        val jsonObjectMessageArray = JSONArray()

        try {
            jsonObjectMessage.put("role", "user")
            jsonObjectMessage.put("content", "Help me with this exercise $exercise")
            jsonObjectMessageArray.put(0, jsonObjectMessage)
            jsonObject.put("messages", jsonObjectMessageArray)
        } catch (e: JSONException) {
            throw java.lang.RuntimeException(e)
        }
        println(jsonObject)
        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            stringURLEndPoint,
            jsonObject,
            { response ->
                try {
                    val stringOutput = response.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    callback.onSuccess(stringOutput)
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
            },
            { error ->
                callback.onError(error)
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val mapHeader = HashMap<String, String>()
                mapHeader["Content-Type"] = "application/json"
                mapHeader["Authorization"] = "Bearer $stringAPIKey"
                return mapHeader
            }
        }
        val intTimeoutPeriod = 60000 // 60 seconds
        val retryPolicy = DefaultRetryPolicy(
            intTimeoutPeriod,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        jsonObjectRequest.setRetryPolicy(retryPolicy)
        Volley.newRequestQueue(context).add(jsonObjectRequest)
    }
}