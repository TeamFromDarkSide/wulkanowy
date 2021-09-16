package io.github.wulkanowy.ui.modules.schoolannouncement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.parseAsHtml
import androidx.fragment.app.DialogFragment
import io.github.wulkanowy.data.db.entities.SchoolAnnouncement
import io.github.wulkanowy.databinding.DialogSchoolAnnouncementBinding
import io.github.wulkanowy.utils.lifecycleAwareVariable
import io.github.wulkanowy.utils.toFormattedString

class SchoolAnnouncementDialog : DialogFragment() {

    private var binding: DialogSchoolAnnouncementBinding by lifecycleAwareVariable()

    private lateinit var announcement: SchoolAnnouncement

    companion object {

        private const val ARGUMENT_KEY = "item"

        fun newInstance(exam: SchoolAnnouncement) = SchoolAnnouncementDialog().apply {
            arguments = Bundle().apply { putSerializable(ARGUMENT_KEY, exam) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
        arguments?.run {
            announcement = getSerializable(ARGUMENT_KEY) as SchoolAnnouncement
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DialogSchoolAnnouncementBinding.inflate(inflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            announcementDialogSubjectValue.text = announcement.subject
            announcementDialogDateValue.text = announcement.date.toFormattedString()
            announcementDialogDescriptionValue.text = announcement.content.parseAsHtml()

            announcementDialogClose.setOnClickListener { dismiss() }
        }
    }
}
