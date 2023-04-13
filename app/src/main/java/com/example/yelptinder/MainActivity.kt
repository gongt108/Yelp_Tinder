package com.example.yelptinder

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.yelptinder.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.floor

private const val TAG = "MainActivity"
//private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "y4Gn-ssAjxOyPuRRyCTCI3_hhwDfvvSqLwb5ZOYZ2C26XJTI3G7HZLbodng8chkH_JswxScEivJ6HNcnPEy75anvESzKraVU38IROyAd_CT7SjPpskviU2EE_neZYXYx"
class MainActivity : AppCompatActivity() {
    var position = 0
    private var binding: ActivityMainBinding? = null
    val restaurants = mutableListOf<YelpRestaurant>()
    private val restaurantsSwipedRight = mutableListOf<YelpRestaurant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var intent = intent
        var category = intent.getStringExtra("category")
        var city = intent.getStringExtra("city")

        val yelpService = RetrofitHelper.getInstance().create(YelpService::class.java)

        GlobalScope.launch {
            val result = yelpService.searchRestaurants( "Bearer $API_KEY", category.toString(),
                city.toString()
            ).enqueue(object :
                Callback<YelpSearchResult> {
                override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                    Log.i(TAG, "onResponse $response")
                    val body = response.body()
                    if(body == null) {
                        Log.w(TAG, "Did not receive valid response body from Yelp API... exiting")
                        return
                    }
                    restaurants.addAll(body.restaurants)
                    initView(restaurants, position)
                    binding?.restaurantCard?.visibility = View.VISIBLE
                }

                override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                    Log.i(TAG, "onFailure $t")
                }

            })
        }

        swipeCard()

    }

    fun initView(restaurants: List<YelpRestaurant>, position: Int) {
        if (position == 20) {
            binding?.restaurantCard?.visibility = View.GONE
            binding?.tvRandomize?.visibility = View.VISIBLE

            binding?.tvRandomize?.setOnClickListener(View.OnClickListener {
                randomizeCard()
                binding?.restaurantCard?.visibility = View.VISIBLE
                binding?.tvReroll?.visibility = View.VISIBLE
                binding?.tvReroll?.setOnClickListener {
                    randomizeCard()
                }

                binding?.tvStartOver?.visibility = View.VISIBLE
                binding?.tvStartOver?.setOnClickListener {
                    finish()
                }

                binding?.tvRandomize?.visibility = View.GONE
            })

        } else {
            showCard(restaurants, position)
        }
    }

    private fun showCard(restaurants: List<YelpRestaurant>, position: Int) {

        val restaurant = restaurants[position]

        binding?.tvName?.text = restaurant.name
        binding?.ratingBar?.rating = restaurant.rating.toFloat()
        binding?.tvNumReviews?.text = restaurant.numReviews.toString()
        binding?.tvAddress?.text = restaurant.location.address
        binding?.tvAddress?.text = restaurant.categories[0].title
        binding?.tvDistance?.text = restaurant.displayDistance()
        binding?.tvPrice?.text = restaurant.price
        Glide.with(this).load(restaurant.imageUrl).apply(
            RequestOptions().transforms(
                CenterCrop(), RoundedCorners(20)
            )
        ).into(binding!!.imageView)

        if (restaurant.isOpen) {
            binding?.tvOpen?.visibility = View.VISIBLE
            binding?.tvClosed?.visibility = View.GONE
        } else {
            binding?.tvOpen?.visibility = View.GONE
            binding?.tvClosed?.visibility = View.VISIBLE
        }
    }

    private fun randomizeCard() {
        var chosenPosition = floor(Math.random() * restaurantsSwipedRight.size)
        showCard(restaurantsSwipedRight, chosenPosition.toInt())
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun swipeCard() {
        binding!!.restaurantCard.setOnTouchListener(
            View.OnTouchListener { view, event ->

                // variables to store current configuration of card.
                val displayMetrics = resources.displayMetrics
                val cardWidth = restaurantCard.width
                val cardStart = (displayMetrics.widthPixels.toFloat() / 2) - (cardWidth / 2)

                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        val newX = event.rawX

                        // carry out swipe only if newX - cardWidth < cardStart, that is
                        // the card is swiped to the left side, not to the right
                        if (newX - cardWidth < cardStart) {
                            restaurantCard.animate()
                                .x(
                                    kotlin.math.min(cardStart, newX - (cardWidth / 2))
                                )
                                .setDuration(0)
                                .start()
                        }
                        if (newX - cardWidth > cardStart) {
                            restaurantCard.animate()
                                .x(
                                    kotlin.math.max(cardStart, newX - (cardWidth / 2))
                                )
                                .setDuration(0)
                                .start()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        val currentX = restaurantCard.x
                        when {
                            currentX <= 0 -> {
                                restaurantCard.animate()
                                    .x((0 - cardWidth).toFloat())
                                    .setDuration(0)
                                    .start()
                                position++

                                initView(restaurants, position)

                                restaurantCard.animate()
                                    .x(cardStart).duration = 0

                            }
                            (currentX + cardWidth) > displayMetrics.widthPixels.toFloat() -> {
                                restaurantCard.animate()
                                    .x(displayMetrics.widthPixels.toFloat())
                                    .setDuration(0)
                                    .start()
                                restaurantsSwipedRight.add(restaurants[position])
                                position++

                                initView(restaurants, position)

                                restaurantCard.animate()
                                    .x(cardStart).duration = 0
                            }
                            else -> {
                                restaurantCard.animate()
                                    .x(cardStart)
                                    .setDuration(0)
                                    .start()
                            }
                        }

                    }
                }
                view.performClick()
                return@OnTouchListener true
            }
        )
    }



    override fun onDestroy() {
        super.onDestroy()

        binding = null
    }
}