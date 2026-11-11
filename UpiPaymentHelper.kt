package com.chaibet.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object UpiPaymentHelper {

    fun launchUpiPayment(context: Context, vpa: String, name: String, amount: Int, note: String) {
        val uri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", vpa)
            .appendQueryParameter("pn", name)
            .appendQueryParameter("am", amount.toString())
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", note)
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri)
        val chooser = Intent.createChooser(intent, "Sponsor Chai via UPI")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No UPI application found on this device", Toast.LENGTH_LONG).show()
        }
    }
}
