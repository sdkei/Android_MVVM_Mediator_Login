package io.github.sdkei.loginmvvm.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * メンバ関数 [observe] を呼び出すことで指定された [LiveData] の監視を開始し、
 * メンバ関数 [close] を呼び出すことですべての監視を解除する、
 * 監視管理クラス。
 */
class CloseableObservingManager : AutoCloseable {

    private val observingSet = mutableSetOf<Pair<LiveData<*>, Observer<*>>>()

    /** [observe] メンバ関数で開始した監視をすべて解除する。*/
    override fun close() {
        observingSet.forEach { (liveData, observer) ->
            @Suppress("UNCHECKED_CAST")
            liveData.removeObserver(observer as Observer<Any?>)
        }

        observingSet.clear()
    }

    /**
     * [liveData] の監視を開始する。
     * [close] メンバ関数が呼び出されると監視が解除される。
     */
    fun <T> observe(liveData: LiveData<T>, observer: Observer<T>) {
        observingSet += liveData to observer

        liveData.observeForever(observer)
    }

    /**
     * [liveData] の監視を開始する。
     * [close] メンバ関数が呼び出されると監視が解除される。
     */
    fun <T> observe(liveData: LiveData<T>, observer: (T) -> Unit) {
        observe(liveData, Observer { observer(it) })
    }
}

/**
 * レシーバーの監視を開始する。
 * [manager] の [CloseableObservingManager.close] メンバ関数が呼び出されると監視が解除される。
 */
fun <T> LiveData<T>.observe(manager: CloseableObservingManager, observer: Observer<T>) {
    manager.observe(this, observer)
}

/**
 * レシーバーの監視を開始する。
 * [manager] の [CloseableObservingManager.close] メンバ関数が呼び出されると監視が解除される。
 */
fun <T> LiveData<T>.observe(manager: CloseableObservingManager, observer: (T) -> Unit) {
    manager.observe(this, observer)
}
