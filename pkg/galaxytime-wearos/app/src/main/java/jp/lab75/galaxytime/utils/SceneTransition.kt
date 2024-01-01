//
//	manage fade in/out
//

package jp.lab75.galaxytime.utils

import android.R

import android.content.Context
import android.content.res.Resources
import android.util.Log

import android.animation.ValueAnimator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter

// public fun fadeOut() {

// 	Log.d(TAG, "FADEOUT $watchMode")
// 	transitionMode = TransitionMode.OUT

// 	val animator = ValueAnimator.ofFloat(0.0f, 255f)
// 	animator.duration = 50

// 	animator.addUpdateListener { valueAnimator ->
// 		transitionAlpha = valueAnimator.animatedValue as Float
// 		invalidate()
// 	}
// 	animator.start()
// 	animator.addListener(object : AnimatorListenerAdapter() {
// 		override fun onAnimationEnd(animation: Animator) {
// 			super.onAnimationEnd(animation)
// 			transitionAlpha = 255f
// 			transitionMode = TransitionMode.IDLE
// 			watchMode = nextWatchMode
// 			Log.d(TAG, "FADEOUT complete")
// 			fadeIn()
// 			invalidate()
// 		}
// 	})
// 	animator.start()
// }

// public fun fadeIn() {

// 	Log.d(TAG, "FADEIN $watchMode")
// 	transitionMode = TransitionMode.IN

// 	val animator = ValueAnimator.ofFloat(255f, 0.0f)
// 	animator.duration = 250

// 	animator.addUpdateListener { valueAnimator ->
// 		transitionAlpha = valueAnimator.animatedValue as Float
// 		invalidate()
// 	}
// 	animator.start()
// 	animator.addListener(object : AnimatorListenerAdapter() {
// 		override fun onAnimationEnd(animation: Animator) {
// 			super.onAnimationEnd(animation)
// 			transitionAlpha = 0f
// 			transitionMode = TransitionMode.IDLE
// 			Log.d(TAG, "FADEIN complete")
// 			invalidate()
// 		}
// 	})
// 	animator.start()
// }
