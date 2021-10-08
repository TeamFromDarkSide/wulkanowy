package io.github.wulkanowy.services.sync.notifications

import io.github.wulkanowy.R
import io.github.wulkanowy.data.db.entities.Exam
import io.github.wulkanowy.data.db.entities.Student
import io.github.wulkanowy.data.pojos.MultipleNotificationsData
import io.github.wulkanowy.ui.modules.main.MainView
import io.github.wulkanowy.utils.toFormattedString
import java.time.LocalDate
import javax.inject.Inject

class NewExamNotification @Inject constructor(
    private val appNotificationManager: AppNotificationManager
) {

    suspend fun notify(items: List<Exam>, student: Student) {
        val today = LocalDate.now()
        val lines = items.filter { !it.date.isBefore(today) }.map {
            "${it.date.toFormattedString("dd.MM")} - ${it.subject}: ${it.description}"
        }.ifEmpty { return }

        val notification = MultipleNotificationsData(
            type = NotificationType.NEW_EXAM,
            icon = R.drawable.ic_main_exam,
            titleStringRes = R.plurals.exam_notify_new_item_title,
            contentStringRes = R.plurals.exam_notify_new_item_content,
            summaryStringRes = R.plurals.exam_number_item,
            startMenu = MainView.Section.EXAM,
            lines = lines
        )

        appNotificationManager.sendNotification(notification, student)
    }
}
