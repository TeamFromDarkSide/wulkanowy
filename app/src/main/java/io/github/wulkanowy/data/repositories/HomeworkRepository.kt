package io.github.wulkanowy.data.repositories

import io.github.wulkanowy.data.db.dao.HomeworkDao
import io.github.wulkanowy.data.db.entities.Homework
import io.github.wulkanowy.data.db.entities.Semester
import io.github.wulkanowy.data.db.entities.Student
import io.github.wulkanowy.data.mappers.mapToEntities
import io.github.wulkanowy.sdk.Sdk
import io.github.wulkanowy.utils.AutoRefreshHelper
import io.github.wulkanowy.utils.getRefreshKey
import io.github.wulkanowy.utils.init
import io.github.wulkanowy.utils.monday
import io.github.wulkanowy.utils.networkBoundResource
import io.github.wulkanowy.utils.sunday
import io.github.wulkanowy.utils.uniqueSubtract
import kotlinx.coroutines.sync.Mutex
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeworkRepository @Inject constructor(
    private val homeworkDb: HomeworkDao,
    private val sdk: Sdk,
    private val refreshHelper: AutoRefreshHelper,
) {

    private val saveFetchResultMutex = Mutex()

    private val cacheKey = "homework"

    fun getHomework(
        student: Student,
        semester: Semester,
        start: LocalDate,
        end: LocalDate,
        forceRefresh: Boolean,
        notify: Boolean = false,
    ) = networkBoundResource(
        mutex = saveFetchResultMutex,
        shouldFetch = {
            val isExpired = refreshHelper.shouldBeRefreshed(
                key = getRefreshKey(cacheKey, semester, start, end)
            )
            it.isEmpty() || forceRefresh || isExpired
        },
        query = {
            homeworkDb.loadAll(
                semesterId = semester.semesterId,
                studentId = semester.studentId,
                from = start.monday,
                end = end.sunday
            )
        },
        fetch = {
            sdk.init(student).switchDiary(semester.diaryId, semester.schoolYear)
                .getHomework(start.monday, end.sunday)
                .mapToEntities(semester)
        },
        saveFetchResult = { old, new ->
            val homeWorkToSave = (new uniqueSubtract old).onEach {
                if (notify) it.isNotified = false
            }
            val filteredOld = old.filterNot { it.isAddedByUser }

            homeworkDb.deleteAll(filteredOld uniqueSubtract new)
            homeworkDb.insertAll(homeWorkToSave)

            refreshHelper.updateLastRefreshTimestamp(getRefreshKey(cacheKey, semester, start, end))
        }
    )

    suspend fun toggleDone(homework: Homework) {
        homeworkDb.updateAll(listOf(homework.apply {
            isDone = !isDone
        }))
    }

    fun getHomeworkFromDatabase(semester: Semester, start: LocalDate, end: LocalDate) =
        homeworkDb.loadAll(semester.semesterId, semester.studentId, start.monday, end.sunday)

    suspend fun updateHomework(homework: List<Homework>) = homeworkDb.updateAll(homework)

    suspend fun saveHomework(homework: Homework) = homeworkDb.insertAll(listOf(homework))

    suspend fun deleteHomework(homework: Homework) = homeworkDb.deleteAll(listOf(homework))
}
