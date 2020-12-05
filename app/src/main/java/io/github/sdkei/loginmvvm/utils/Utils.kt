package io.github.sdkei.loginmvvm.utils

/**
 * when や if-else がすべての分岐を網羅していることをコンパイル時に保証するための拡張プロパティ。
 *
 * 次のように使用する。
 *
 * ```
 * when(...) {
 *     ...
 * }.exhaustive
 * ```
 */
inline val <T> T.exhaustive: T
    get() = this