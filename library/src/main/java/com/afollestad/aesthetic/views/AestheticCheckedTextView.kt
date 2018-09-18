/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.aesthetic.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckedTextView
import com.afollestad.aesthetic.Aesthetic
import com.afollestad.aesthetic.Aesthetic.Companion.get
import com.afollestad.aesthetic.ColorIsDarkState
import com.afollestad.aesthetic.actions.ViewTextColorAction
import com.afollestad.aesthetic.utils.TintHelper
import com.afollestad.aesthetic.utils.distinctToMainThread
import com.afollestad.aesthetic.utils.onErrorLogAndRethrow
import com.afollestad.aesthetic.utils.plusAssign
import com.afollestad.aesthetic.utils.resId
import com.afollestad.aesthetic.utils.watchColor
import io.reactivex.Observable.combineLatest
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer

/** @author Aidan Follestad (afollestad) */
class AestheticCheckedTextView(
  context: Context,
  attrs: AttributeSet? = null
) : AppCompatCheckedTextView(context, attrs) {

  private var subs: CompositeDisposable? = null
  private var backgroundResId: Int = 0

  init {
    if (attrs != null) {
      backgroundResId = context.resId(attrs, android.R.attr.background)
    }
  }

  private fun invalidateColors(state: ColorIsDarkState) =
    TintHelper.setTint(this, state.color, state.isDark)

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    subs = CompositeDisposable()
    subs +=
        combineLatest(
            watchColor(
                context,
                backgroundResId,
                get().colorAccent()
            )!!,
            Aesthetic.get().isDark,
            ColorIsDarkState.creator()
        ).distinctToMainThread()
            .subscribe(
                Consumer { this.invalidateColors(it) },
                onErrorLogAndRethrow()
            )
    subs += get().textColorPrimary()
        .distinctToMainThread()
        .subscribe(ViewTextColorAction(this))
  }

  override fun onDetachedFromWindow() {
    subs?.clear()
    super.onDetachedFromWindow()
  }
}
