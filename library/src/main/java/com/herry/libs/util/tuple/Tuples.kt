@file:Suppress("unused")

package com.herry.libs.util.tuple

import java.io.Serializable

data class Tuple1<out T>(val t1: T) : Serializable {
    override fun toString(): String = "($t1)"
}

data class Tuple2<out T1, out T2>(val t1: T1, val t2: T2) : Serializable {
    override fun toString(): String = "($t1, $t2)"
}

infix fun <T1, T2> Tuple1<T1>.then(t2: T2): Tuple2<T1, T2> = Tuple2(this.t1, t2)

fun <T> Tuple2<T, T>.toList(): List<T> = listOf(t1, t2)

data class Tuple3<out T1, out T2, out T3>(val t1: T1, val t2: T2, val t3: T3) : Serializable {
    override fun toString(): String = "($t1, $t2, $t3)"
}

infix fun <T1, T2, T3> Tuple2<T1, T2>.then(t3: T3): Tuple3<T1, T2, T3> = Tuple3(this.t1, this.t2, t3)

fun <T> Tuple3<T, T, T>.toList(): List<T> = listOf(t1, t2, t3)