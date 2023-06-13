package com.daryukim.plancation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.daryukim.plancation.databinding.ActivityLoginmainBinding

class LoginMainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginmainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginmainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnJoin.setOnClickListener {
            Toast.makeText(this, "Join!", Toast.LENGTH_SHORT).show()
        }
    }
}