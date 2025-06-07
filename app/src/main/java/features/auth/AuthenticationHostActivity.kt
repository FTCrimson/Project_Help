package com.example.project_helper.features.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.project_helper.R
import com.example.project_helper.features.fragments.LoginFragment

class AuthenticationHostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication_host)
        if (supportFragmentManager.findFragmentById(R.id.nav_host) == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.nav_host, LoginFragment())
                .commit()
        }
    }
}