package com.daryukim.plancation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.daryukim.plancation.databinding.ActivityLoginBinding
import com.daryukim.plancation.databinding.ActivityLoginmainBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}