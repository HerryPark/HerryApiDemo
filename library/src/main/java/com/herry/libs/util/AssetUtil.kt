package com.herry.libs.util

import android.content.Context

object AssetUtil {

    /**
     * Read asset file content
     *
     * @param context Application Context
     * @param fileName Asset file name
     * @return String file content
     */
    fun readAssetFile(context: Context, fileName: String): String {
        return context.assets.open(fileName)
            .bufferedReader().use {
                it.readText()
            }
    }
}