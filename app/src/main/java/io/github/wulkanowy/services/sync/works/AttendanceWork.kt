package io.github.wulkanowy.services.sync.works

import io.github.wulkanowy.data.db.entities.Semester
import io.github.wulkanowy.data.db.entities.Student
import io.github.wulkanowy.data.repositories.AttendanceRepository
import io.github.wulkanowy.services.sync.notifications.NewAttendanceNotification
import io.github.wulkanowy.utils.monday
import io.github.wulkanowy.utils.sunday
import io.github.wulkanowy.utils.waitForResult
import kotlinx.coroutines.flow.first
import java.time.LocalDate.now
import javax.inject.Inject

class AttendanceWork @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val newAttendanceNotification: NewAttendanceNotification
) : Work {

    override suspend fun doWork(student: Student, semester: Semester) {
        attendanceRepository.getAttendance(student, semester, now().monday, now().sunday, true)
            .waitForResult()

        attendanceRepository.getAttendanceFromDatabase(semester, now().monday, now().sunday)
            .first()
            .filterNot { it.isNotified }
            .let {
                if (it.isNotEmpty()) newAttendanceNotification.notify(it, student)

                attendanceRepository.updateTimetable(it.onEach { attendance ->
                    attendance.isNotified = true
                })
            }
    }
}
