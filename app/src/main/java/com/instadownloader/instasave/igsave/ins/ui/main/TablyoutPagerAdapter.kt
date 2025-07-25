package com.instadownloader.instasave.igsave.ins.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.instadownloader.instasave.igsave.ins.MyUtils
import com.instadownloader.instasave.igsave.ins.R
import com.instadownloader.instasave.igsave.ins.ui.main.browser.BrowserFragment

private val TAB_TITLES_BROWER = arrayOf(
    R.string.Home,
    R.string.browser,
    R.string.downloads
)

private val TAB_TITLES = arrayOf(
    R.string.Home,
    R.string.downloads
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).

        if (MyUtils.isUseBrowserMode){
            if (position == 0){
                return LoadFragment.newInstance()
            }
            else if(position == 1){
                return BrowserFragment.newInstance()
            }
            else if(position == 2){
                return DownloadsFragment.newInstance()
            }
        }else{
            if (position == 0){
                return LoadFragment.newInstance()
            }
            else if(position == 1){
                return DownloadsFragment.newInstance()
            }
        }


        return LoadFragment.newInstance()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        if (MyUtils.isUseBrowserMode){
            return context.resources.getString(TAB_TITLES_BROWER[position])

        }else{
            return context.resources.getString(TAB_TITLES[position])
        }
    }

    override fun getCount(): Int {
        if (MyUtils.isUseBrowserMode){
            return 3
        }
        return 2
    }
}