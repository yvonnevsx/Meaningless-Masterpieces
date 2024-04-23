package com.example.myapplication.ui.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import kotlin.math.abs
import android.animation.ValueAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import profileFragment

class ArtworkDetailActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector
    private lateinit var cardView: CardView
    private var originalX: Float = 0f
    private lateinit var mediaPlayerCash: MediaPlayer
    private lateinit var mediaPlayerCat: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artwork_detail)

        val imageResId = intent.getIntExtra("imageResId", 0)
        val title = intent.getStringExtra("title")

        val imageView: ImageView = findViewById(R.id.imageView_artwork)
//        val titleView: TextView = findViewById(R.id.textView_artwork_title)

        val buttonProfile = findViewById<Button>(R.id.button_profile)
        buttonProfile.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }

        mediaPlayerCash = MediaPlayer.create(this, R.raw.cash_sound)
        mediaPlayerCat = MediaPlayer.create(this, R.raw.cat_sound)

        imageView.setImageResource(imageResId)
//        titleView.text = title
        Log.d("ArtworkDetailActivity", "Title: $title")

        cardView = findViewById(R.id.cardView_artwork)
        originalX = cardView.x

        gestureDetector = GestureDetector(this, GestureListener())

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            onBackPressed()
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }


    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val distanceX = e2?.x?.minus(e1?.x ?: 0f) ?: 0f
            val distanceY = e2?.y?.minus(e1?.y ?: 0f) ?: 0f

            if (abs(distanceX) > abs(distanceY) &&
                abs(distanceX) > SWIPE_THRESHOLD &&
                abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
            ) {
                if (distanceX > 0) {
                    // RIGHT SWIPE
                    val animationViewMoney =
                        findViewById<LottieAnimationView>(R.id.animationViewRightSwipe)
                    animationViewMoney.setAnimation(R.raw.animation_money3)
                    animationViewMoney.playAnimation()
                    mediaPlayerCash.start()

                    val valueTextView = findViewById<TextView>(R.id.textView_artwork_value)
                    val currentValueText = valueTextView.text.toString().substringAfter("$").replace(",", ".")
                    val currentValue = currentValueText.toDouble()
                    val newValue =
                        currentValue * (1.05 + Math.random() * 0.1)

                    animateCountingText(valueTextView, currentValue, newValue)

                    animateCardView(true)

                    Log.d("ArtworkDetailActivity", "Swipe right")
                } else {
                    // LEFT SWIPE

                    val animationViewPoop =
                        findViewById<LottieAnimationView>(R.id.animationViewLeftSwipe)
                    animationViewPoop.setAnimation(R.raw.animation_scratch)
                    animationViewPoop.playAnimation()
                    mediaPlayerCat.start()

                    val valueTextView = findViewById<TextView>(R.id.textView_artwork_value)
                    val currentValueText = valueTextView.text.toString().substringAfter("$").replace(",", ".")
                    val currentValue = currentValueText.toDouble()
                    val randomChance = (0..9).random()

                    // Check if the random number is 0 (1/20 chance)
                    val newValue = if (randomChance == 0) {
                        currentValue * 3 // Increase the value by 300%
                    } else {
                        currentValue * (0.85 + Math.random() * 0.1) // Decrease the value by a factor between 0.85 and 0.95
                    }

                    animateCountingText(valueTextView, currentValue, newValue)

                    animateCardView(false)

                    Log.d("ArtworkDetailActivity", "Swipe left")
                }
                return true
            }
            return false
        }

        private fun animateCountingText(textView: TextView, startValue: Double, endValue: Double) {
            val valueAnimator = ValueAnimator.ofFloat(startValue.toFloat(), endValue.toFloat())
            valueAnimator.duration = 1000 // Set the duration of animation in milliseconds
            valueAnimator.interpolator =
                AccelerateInterpolator() // Optional: Set interpolator for acceleration
            valueAnimator.addUpdateListener {
                val animatedValue = it.animatedValue as Float
                textView.text = String.format("$%.2f", animatedValue)
            }
            valueAnimator.start()
        }

        private fun animateCardView(isSwipeRight: Boolean) {
            val cardView = findViewById<CardView>(R.id.cardView_artwork)
            val originalX = cardView.x
            val translationX = if (isSwipeRight) 200f else -200f
            val maxTiltAngle = 10f

            val translationAnimator = ObjectAnimator.ofFloat(cardView, View.TRANSLATION_X, translationX)
            val tiltAngle = translationX / (cardView.width / 2) * maxTiltAngle
            val rotationAnimator = ObjectAnimator.ofFloat(cardView, View.ROTATION, 0f, tiltAngle)

            val animatorSet = AnimatorSet().apply {
                playTogether(translationAnimator, rotationAnimator)
                duration = 300
                interpolator = AccelerateInterpolator()
                translationAnimator.addUpdateListener {
                    val animatedValue = it.animatedValue as Float
                    cardView.translationX = animatedValue
                }
                doOnEnd {
                    // Bounce back animation
                    val bounceBackTranslationAnimator = ObjectAnimator.ofFloat(cardView, View.TRANSLATION_X, 0f)
                    val bounceBackRotationAnimator = ObjectAnimator.ofFloat(cardView, View.ROTATION, tiltAngle, 0f)

                    val bounceBackAnimatorSet = AnimatorSet().apply {
                        playTogether(bounceBackTranslationAnimator, bounceBackRotationAnimator)
                        duration = 300
                        interpolator = OvershootInterpolator()
                        start()
                    }
                }
                start()
            }
        }
    }
}


