package com.chaibet.app

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class LedgerItem(
    val time: String,
    val event: String,
    val amount: Int,
    val friendCut: Int,
    val appCut: Int
)

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("chai_bet_prefs", Context.MODE_PRIVATE)

    var buddyName: String
        get() = prefs.getString("buddy_name", "Rahul Verma") ?: "Rahul Verma"
        set(value) = prefs.edit().putString("buddy_name", value).apply()

    var buddyUpi: String
        get() = prefs.getString("buddy_upi", "rahul.verma@oksbi") ?: "rahul.verma@oksbi"
        set(value) = prefs.edit().putString("buddy_upi", value).apply()

    var snackPrice: Int
        get() = prefs.getInt("snack_price", 25)
        set(value) = prefs.edit().putInt("snack_price", value).apply()

    var snackName: String
        get() = prefs.getString("snack_name", "Cutting Chai") ?: "Cutting Chai"
        set(value) = prefs.edit().putString("snack_name", value).apply()

    var startHour: Int
        get() = prefs.getInt("start_hour", 9)
        set(value) = prefs.edit().putInt("start_hour", value).apply()

    var endHour: Int
        get() = prefs.getInt("end_hour", 18)
        set(value) = prefs.edit().putInt("end_hour", value).apply()

    var totalChaisWon: Int
        get() = prefs.getInt("total_chais_won", 8)
        set(value) = prefs.edit().putInt("total_chais_won", value).apply()

    var totalChaisGiven: Int
        get() = prefs.getInt("total_chais_given", 0)
        set(value) = prefs.edit().putInt("total_chais_given", value).apply()

    var unlockedUntil: Long
        get() = prefs.getLong("unlocked_until", 0L)
        set(value) = prefs.edit().putLong("unlocked_until", value).apply()

    fun isGraceSessionActive(): Boolean {
        return System.currentTimeMillis() < unlockedUntil
    }

    fun grantUnlockGrace(durationMinutes: Int = 15) {
        unlockedUntil = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
    }

    fun recordChaiGiven(appName: String, amount: Int) {
        totalChaisGiven += 1
        val friendCut = (amount * 0.8).toInt()
        val appCut = amount - friendCut
        addLedgerEntry("Sponsored Chai ($appName)", amount, friendCut, appCut)
    }

    fun recordChaiWon(friendName: String, amount: Int = 25) {
        totalChaisWon += 1
        val friendCut = (amount * 0.8).toInt()
        val appCut = amount - friendCut
        addLedgerEntry("$friendName slipped on Reels", amount, friendCut, appCut)
    }

    fun addLedgerEntry(event: String, amount: Int, friendCut: Int, appCut: Int) {
        val list = getLedgerEntries().toMutableList()
        val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        list.add(0, LedgerItem(timeStr, event, amount, friendCut, appCut))
        if (list.size > 20) list.removeAt(list.size - 1)

        val array = JSONArray()
        list.forEach {
            val obj = JSONObject().apply {
                put("time", it.time)
                put("event", it.event)
                put("amount", it.amount)
                put("friendCut", it.friendCut)
                put("appCut", it.appCut)
            }
            array.put(obj)
        }
        prefs.edit().putString("ledger_json", array.toString()).apply()
    }

    fun getLedgerEntries(): List<LedgerItem> {
        val raw = prefs.getString("ledger_json", null) ?: return listOf(
            LedgerItem("10:45 AM", "Vikram slipped on Shorts", 25, 20, 5),
            LedgerItem("09:15 AM", "Sneha slipped on Reels", 25, 20, 5)
        )
        val list = mutableListOf<LedgerItem>()
        val array = JSONArray(raw)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                LedgerItem(
                    obj.getString("time"),
                    obj.getString("event"),
                    obj.getInt("amount"),
                    obj.getInt("friendCut"),
                    obj.getInt("appCut")
                )
            )
        }
        return list
    }
}
