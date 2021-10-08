package io.github.wulkanowy.services.messaging

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import io.github.wulkanowy.data.db.entities.Notification
import io.github.wulkanowy.data.repositories.NotificationRepository
import io.github.wulkanowy.services.sync.notifications.NotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
@AndroidEntryPoint
class AppMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val job = Job()

    private val serviceScope = CoroutineScope(Dispatchers.Main + job)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val remoteMessageData = remoteMessage.data
        val title = remoteMessageData["title"] ?: return
        val content = remoteMessageData["content"] ?: return
        val customData = remoteMessageData["custom_data"]

        val notification = Notification(
            title = title,
            content = content,
            data = customData,
            date = LocalDateTime.now(),
            type = NotificationType.PUSH,
            studentId = -1
        )

        serviceScope.launch {
            try {
                notificationRepository.saveNotification(notification)
            } catch (e: Throwable) {
                Timber.e(e)
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}