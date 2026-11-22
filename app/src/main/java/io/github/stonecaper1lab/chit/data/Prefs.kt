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
}
