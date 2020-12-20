package io.github.sdkei.loginmvvm.view.converter

import androidx.annotation.IdRes
import androidx.databinding.InverseMethod
import io.github.sdkei.loginmvvm.R
import io.github.sdkei.loginmvvm.model.UserType
import io.github.sdkei.loginmvvm.utils.exhaustive

/** [UserType] とリソース ID を相互変換するコンバーター。 */
object UserTypeToRadioButtonIdResConverter {
    @JvmStatic
    @IdRes
    fun convert(userType: UserType?): Int =
        when (userType) {
            UserType.GUEST -> R.id.radio_button_guest_user
            UserType.REGISTERED -> R.id.radio_button_registered_user
            null -> 0
        }.exhaustive

    @InverseMethod("convert")
    @JvmStatic
    fun inverseConvert(@IdRes idRes: Int): UserType? =
        when (idRes) {
            R.id.radio_button_guest_user -> UserType.GUEST
            R.id.radio_button_registered_user -> UserType.REGISTERED
            else -> null
        }.exhaustive
}