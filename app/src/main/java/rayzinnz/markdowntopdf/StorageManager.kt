package rayzinnz.markdowntopdf

import android.content.Context
import android.content.SharedPreferences

class StorageManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("markdown_prefs", Context.MODE_PRIVATE)

    fun getLastFileName(): String {
        return prefs.getString("last_file_name", "Converted.pdf") ?: "Converted.pdf"
    }

    fun setLastFileName(name: String) {
        prefs.edit().putString("last_file_name", name).apply()
    }
}
