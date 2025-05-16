package com.herry.test.app.sample.home

import androidx.annotation.IdRes
import com.herry.test.R

enum class HomeTab(@IdRes val id: Int) {
    HOT_FEEDS (R.id.sample_news_navigation),
    FEEDS (R.id.sample_feeds_navigation),
    CREATE (R.id.sample_create_navigation),
    ME (R.id.sample_me_navigation);

    companion object {
        fun generate(id: Int): HomeTab? = entries.firstOrNull { it.id == id }
    }
}