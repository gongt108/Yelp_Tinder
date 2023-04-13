package com.example.yelptinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.example.yelptinder.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    var binding: ActivitySearchBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var category: String? = "food"
        binding?.etCategory?.doOnTextChanged { text, _, _, _ ->
            category = text.toString()
        }
        var city : String? = "New York"
        binding?.etCity?.doOnTextChanged { text, _, _, _ ->
            city = text.toString()
        }

        var extras = Bundle()
        extras.putString("category", category)
        extras.putString("city", city)

        binding?.tvSearch?.setOnClickListener {
            val intent = Intent (this, MainActivity::class.java)
            intent.putExtra("category", category)
            intent.putExtra("city", city)
            startActivity(intent)
        }
    }


}