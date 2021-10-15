package io.github.wulkanowy.ui.modules.settings.ads

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.wulkanowy.R
import io.github.wulkanowy.ui.modules.main.MainView

@AndroidEntryPoint
class AdsFragment : PreferenceFragmentCompat(), MainView.TitledView {

    override val titleStringId = R.string.pref_settings_ads_title

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.scheme_preferences_ads, rootKey)
    }
}