package io.github.stonecaper1lab.chit.update

import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import io.github.stonecaper1lab.chit.R
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

/** Opt-in update check against the latest GitHub release. Contacts github.com only. */
object Updater {
    private const val API = "https://api.github.com/repos/stonecaper1-lab/Chit/releases/latest"
    const val ACTION_DOWNLOAD = "io.github.stonecaper1lab.chit.DOWNLOAD_UPDATE"
    private const val CHANNEL = "updates"
    private const val NOTIF_ID = 4200
    private const val JOB_ID = 4201

    data class Info(val tag: String, val url: String)

    @Suppress("DEPRECATION")
    private fun installedVersion(context: Context): String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0"

    /** Blocking network call: run off the main thread. Returns null when up to date. */
    fun check(context: Context): Info? {
        val conn = URL(API).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            if (conn.responseCode != 200) error("HTTP ${conn.responseCode}")
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val tag = json.getString("tag_name")
            if (!isNewer(tag, installedVersion(context))) return null
            val assets = json.getJSONArray("assets")
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                val url = a.getString("browser_download_url")
                if (a.getString("name").endsWith(".apk") && isTrustedUrl(url)) return Info(tag, url)
            }
            error("Release $tag has no APK")
        } finally {
            conn.disconnect()
        }
    }

    fun notify(context: Context, info: Info) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "App updates", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val intent = Intent(context, UpdateReceiver::class.java)
            .setAction(ACTION_DOWNLOAD)
            .putExtra("url", info.url)
            .putExtra("tag", info.tag)
        val pending = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = Notification.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_chit)
            .setContentTitle("Chit ${info.tag} is available")
            .setContentText("Tap to download the update")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIF_ID, n)
    }

    /** Hands the APK to the system DownloadManager; its completion notification opens the installer. */
    fun download(context: Context, url: String, tag: String) {
        val safeTag = tag.filter { it.isLetterOrDigit() || it == '.' || it == '-' }
        val name = "Chit-$safeTag.apk"
        val dm = context.getSystemService(DownloadManager::class.java)
        fun request(): DownloadManager.Request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle("Chit $tag")
                .setMimeType("application/vnd.android.package-archive")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        try {
            dm.enqueue(request().setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name))
        } catch (e: SecurityException) {
            dm.enqueue(request().setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, name))
        }
        context.getSystemService(NotificationManager::class.java).cancel(NOTIF_ID)
        Toast.makeText(context, "Downloading Chit $tag...", Toast.LENGTH_SHORT).show()
    }

    fun ensureScheduled(context: Context) {
        val js = context.getSystemService(JobScheduler::class.java)
        if (js.getPendingJob(JOB_ID) != null) return
        js.schedule(
            JobInfo.Builder(JOB_ID, ComponentName(context, UpdateJobService::class.java))
                .setPeriodic(24L * 60 * 60 * 1000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build()
        )
    }

    fun cancel(context: Context) {
        context.getSystemService(JobScheduler::class.java).cancel(JOB_ID)
    }
}
