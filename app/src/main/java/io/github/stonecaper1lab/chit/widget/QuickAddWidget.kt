package io.github.stonecaper1lab.chit.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.github.stonecaper1lab.chit.MainActivity
import io.github.stonecaper1lab.chit.R

class QuickAddWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val intent = Intent(context, MainActivity::class.java)
            .setAction(ACTION_NEW)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val views = RemoteViews(context.packageName, R.layout.widget_quick_add)
        views.setOnClickPendingIntent(R.id.widget_root, pending)
        for (id in ids) manager.updateAppWidget(id, views)
    }

    companion object {
        const val ACTION_NEW = "io.github.stonecaper1lab.chit.NEW_NOTE"
    }
}
