package com.woleapp.netpos.contactless.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.dsofttech.dprefs.utils.DPrefs
import com.woleapp.netpos.contactless.R
import com.woleapp.netpos.contactless.databinding.ActivityAuthenticationBinding
import com.woleapp.netpos.contactless.nibss.NetPosTerminalConfig
import com.woleapp.netpos.contactless.ui.fragments.LoginFragment
import com.woleapp.netpos.contactless.util.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        if (RootUtil.isDeviceRooted) {
            Toast.makeText(this, getString(R.string.device_is_rooted), Toast.LENGTH_SHORT).show()
            finish()
        }
        if (DPrefs.getBoolean(PREF_AUTHENTICATED, false) && tokenValid()) {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
            )
            NetPosTerminalConfig.init(applicationContext)
            finish()
        }
        showFragment(LoginFragment())
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                AppConstants.READ_PHONE_STATE_REQUEST_CODE,
            )
        }
    }

    private fun tokenValid(): Boolean {
        val token = if (DPrefs.getString(PREF_USER_TOKEN).isNotEmpty()) {
            DPrefs.getString(
                PREF_USER_TOKEN,
            )
        } else {
            null
        }
        return !(token.isNullOrEmpty() || JWTHelper.isExpired(token))
    }

    private fun showFragment(targetFragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .apply {
                    replace(
                        R.id.auth_container,
                        targetFragment,
                        targetFragment.javaClass.simpleName,
                    )
                    setCustomAnimations(R.anim.right_to_left, android.R.anim.fade_out)
                    commit()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
