package com.example.image_to_text.ui.helpers.utils

import com.example.image_to_text.ui.adsconfig.constants.FbAdsConstants
import com.example.image_to_text.ui.helpers.firebase.RemoteConstants

object CleanMemory {

    fun clean() {
        RemoteConstants.reset()
        FbAdsConstants.reset()
    }

}