package com.moon.nugasam.extension

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations

// TODO It will be removed after androidx lifecycle 2.2.0
@MainThread
fun <X, Y> LiveData<X>.map(func: (X) -> Y): LiveData<Y> = Transformations.map(this, func)

//@MainThread
//fun <T> LiveData<T>.distinctUntilChanged(): LiveData<T> {
//    return Transformations.distinctUntilChanged(this)
//}

@MainThread
fun <T> LiveData<T>.filter(predicate: (data: T) -> Boolean): LiveData<T> =
    MediatorLiveData<T>().apply {
        addSource(this@filter) {
            if (predicate(it)) {
                value = it
            } else {
                //Logger.debug(tag = "LiveData.filter") { "onChanged. data($it) is filtered" }
            }
        }
    }


/**
 * Wait until both of LiveData post value.
 *
 * A = LiveData
 * B = LiveData
 * C = A.merge(B) { valueA, valueB ->
 *     valueC // make new value
 * }
 *
 * and C will remember last value A and B.
 * so if valueB is changed valueC will be posted with old value A
 */
@MainThread
fun <T, R, N> LiveData<T>.merge(target: LiveData<R>, newValue: (T, R) -> N): LiveData<N> {
    return MediatorLiveData<N>().apply {
        val source = this@merge
        var sourceReady = false
        var targetReady = false
        fun mergePostValue(t: T?, r: R?) {
            if (t != null && r != null && sourceReady && targetReady) {
                value = newValue(t, r)
            } else {
                Log.d(
                    "LiveData.merge",
                    "may not ready yet. source:$sourceReady, target:$targetReady\""
                )
            }
        }
        addSource(source) {
            sourceReady = true
            mergePostValue(it, target.value)
        }
        addSource(target) {
            targetReady = true
            mergePostValue(source.value, it)
        }
    }
}
