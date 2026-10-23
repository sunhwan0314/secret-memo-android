package com.dev.secretmemo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.dev.secretmemo.MainActivity
import com.dev.secretmemo.R
import com.dev.secretmemo.data.repository.MemoRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MemoAppWidget : AppWidgetProvider(), KoinComponent {

    private val memoRepository: MemoRepository by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_memo)

        val memos = try {
            memoRepository.getMemos()
        } catch (_: Exception) {
            emptyList()
        }

        if (memos.isNotEmpty()) {
            val topMemo = memos.first()
            views.setTextViewText(R.id.widgetTitle, topMemo.title)
            views.setTextViewText(R.id.widgetContent, topMemo.content)
        } else {
            views.setTextViewText(R.id.widgetTitle, "저장된 메모가 없습니다")
            views.setTextViewText(R.id.widgetContent, "앱을 실행하여 메모를 작성해 보세요.")
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, MemoAppWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, MemoAppWidget::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
