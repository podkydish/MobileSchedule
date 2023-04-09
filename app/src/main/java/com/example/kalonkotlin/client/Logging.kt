package com.example.kalonkotlin.client

import android.content.Context
import android.util.Log
import java.io.IOException

object Logging {

    fun writeToFile(context: Context, filename: String, text: String) {
        try {
            val outputStream = context.openFileOutput(filename, Context.MODE_APPEND)
            outputStream.write(text.toByteArray())
            outputStream.close()
        } catch (e: IOException) {
            Log.e("TAG", "Error writing to file: ${e.message}")
        }
    }

    fun logTo(context: Context, message: String) {
        val logTag = "MY_APP_LOG"
        val logFilename = "mai_pomogator.log"

        // Build the log message
        val logMessage = "$logTag: $message\n"

        // Write the log message to the file
        writeToFile(context, logFilename, logMessage)

        // Also log the message to the system log
        Log.d(logTag, message)
    }

}