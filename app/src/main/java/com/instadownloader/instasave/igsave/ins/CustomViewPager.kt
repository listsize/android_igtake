package com.instadownloader.instasave.igsave.ins

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class CustomViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {

    var mIsCanScroll = true

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if(!mIsCanScroll){
            return false
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if(!mIsCanScroll){
            return false
        }
        return super.onTouchEvent(ev)
    }
}