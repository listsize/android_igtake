package com.instadownloader.instasave.igsave.ins.ui.main.browser

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import com.instadownloader.instasave.igsave.ins.MyApp
import com.instadownloader.instasave.igsave.ins.MyRepository

class BrowserViewModel(app: Application) : AndroidViewModel(app) {
    private var mRepository: MyRepository = (app as MyApp).appContainer.myRepository

    var liveDataToBrowserWithLink = mRepository.liveDataToBrowserWithString.map{
        it
    }

    var livedataLoginSucWithDialog = mRepository.livedataLoginSucWithDialog.map{
        it
    }


    fun resetLivedataBrowserWithLink() {
        mRepository.liveDataToBrowserWithString.value = ""
    }

    fun resetLivedataLoginSuc() {
        mRepository.livedataLoginSucWithDialog.postValue(false)
    }

}