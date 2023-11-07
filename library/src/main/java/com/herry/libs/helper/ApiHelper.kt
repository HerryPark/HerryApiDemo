package com.herry.libs.helper

import android.os.Build

@Suppress("MemberVisibilityCanBePrivate", "unused", "AnnotateVersionCheck", "ObsoleteSdkInt")
object ApiHelper {
    fun hasLollipop(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    fun hasMarshmallow(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    fun hasNougat(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N // 24

    fun hasNougatPlusPlus(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 // 25

    fun hasOreo(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O // 26

    fun hasOSv8(): Boolean = hasOreo()

    fun hasPie(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P // 28

    fun hasOSv9(): Boolean = hasPie()

    fun hasOSv10(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q // 29

    fun hasAPI29(): Boolean = hasOSv10()

    fun hasOSv11(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R // 30

    fun hasAPI30(): Boolean = hasOSv11()

    fun hasOSv12(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S // 31

    fun hasAPI31(): Boolean = hasOSv12()

    fun hasOSv12L(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2 // 32

    fun hasOSv13(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU // 33

    fun hasAPI33(): Boolean = hasOSv13()

    fun hasOSv14(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE // 34

    fun hasAPI34(): Boolean = hasOSv14()

}