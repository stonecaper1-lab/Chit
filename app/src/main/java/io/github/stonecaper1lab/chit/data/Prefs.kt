package io.github.stonecaper1lab.chit.data

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("chit", Context.MODE_PRIVATE)

    var lockEnabled: Boolean
        get() = sp.getBoolean("lock", false)
        set(v) = sp.edit().putBoolean("lock", v).apply()

    var mirrorUri: String?
        get() = sp.getString("mirror", null)
        set(v) = sp.edit().putString("mirror", v).apply()

    var updateCheck: Boolean
        get() = sp.getBoolean("update_check", false)
        set(v) = sp.edit().putBoolean("update_check", v).apply()

    var lastNotifiedTag: String?
        get() = sp.getString("last_tag", null)
        set(v) = sp.edit().putString("last_tag", v).apply()
}
