package com.instadownloader.instasave.igsave.ins


import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.instadownloader.instasave.igsave.ins.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {

    private lateinit var binding:ActivityHelpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)

        setContentView(binding.root)
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if(mode != Configuration.UI_MODE_NIGHT_YES) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.helpCardview.helpCardviewStory.howToUseStory.text = String.format(getString(R.string.how_to_download_something),
            getString(R.string.story))
        binding.helpCardview.helpCardview.howToUsePost.text = String.format(getString(R.string.how_to_download_something),
            "")
    }

    override fun onStart() {
        super.onStart()
        MyUtils.isFromTheAppOtherActivityBack  = true
    }

}