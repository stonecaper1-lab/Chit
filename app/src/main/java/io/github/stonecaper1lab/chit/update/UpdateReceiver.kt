package io.github.stonecaper1lab.chit.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Updater.ACTION_DOWNLOAD) return
        val url = intent.getStringExtra("url") ?: return
        val tag = intent.getStringExtra("tag") ?: return
        if (!isTrustedUrl(url)) return
        Updater.download(context, url, tag)
    }
}
