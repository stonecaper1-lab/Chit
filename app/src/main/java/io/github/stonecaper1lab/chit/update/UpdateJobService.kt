package io.github.stonecaper1lab.chit.update

import android.app.job.JobParameters
import android.app.job.JobService
import io.github.stonecaper1lab.chit.data.Prefs

class UpdateJobService : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        val app = applicationContext
        Thread {
            val info = runCatching { Updater.check(app) }.getOrNull()
            if (info != null) {
                val prefs = Prefs(app)
                if (info.tag != prefs.lastNotifiedTag) {
                    Updater.notify(app, info)
                    prefs.lastNotifiedTag = info.tag
                }
            }
            jobFinished(params, false)
        }.start()
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean = false
}
