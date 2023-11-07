package com.herry.test.data

/**
 * Created by herry.park on 2020/06/18.
 **/

class GifMediaFileInfoData (
    id: String,
    mimeType: String,
    path: String,
    name: String,
    size: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    date: Long = 0
) : MediaFileInfoData(id = id, mimeType = mimeType, path = path, name = name, size = size, date = date)