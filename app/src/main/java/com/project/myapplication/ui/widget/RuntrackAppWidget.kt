package com.project.myapplication.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.project.myapplication.R

/**
 * Implementation of App Widget functionality.
 */
class RuntrackAppWidget : AppWidgetProvider() {

    private var currentNumberStepsCount = 0
    private var distanceTravelled = 0f
    private var timerCount = 0

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context,
                appWidgetManager,
                appWidgetId,
                currentNumberStepsCount,
                timerCount,
                distanceTravelled)

        }
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.getIntExtra("current_steps", 0) != -1) {
            currentNumberStepsCount = intent.getIntExtra("current_steps", 0)
        }
        if (intent.getIntExtra("timer_counter", 0) != -1) {
            timerCount = intent.getIntExtra("timer_counter", 0)
        }
        if (intent.getFloatExtra("total_distance", 0f) != -1f) {
            distanceTravelled = intent.getFloatExtra("total_distance", 0f)
        }
        super.onReceive(context, intent)
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            currentNumberStepsCount: Int, timerCounter: Int, distanceTravelled: Float,
        ) {
            val views = RemoteViews(context.packageName, R.layout.runtrack_app_widget)

            val currentNumberSteps: String =
                StringBuilder("$currentNumberStepsCount Steps").toString()
            val distance: String = StringBuilder("$distanceTravelled Meter").toString()
            val countTime: String = StringBuilder("$timerCounter Seconds").toString()

            views.setTextViewText(R.id.tv_number_step, currentNumberSteps)
            views.setTextViewText(R.id.tv_total_distance, distance)
            views.setTextViewText(R.id.tv_average_pace, countTime)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}