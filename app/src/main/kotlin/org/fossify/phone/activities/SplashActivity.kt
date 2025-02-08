package org.fossify.phone.activities

import android.content.Intent
import org.fossify.commons.activities.BaseSplashActivity
import org.fossify.phone.dialogs.PasswordDialogFragment
import org.fossify.phone.extensions.config

class SplashActivity : BaseSplashActivity() {
    override fun initActivity() {
        val hidePhoneNumberPassword = config.hidePhoneNumberPassword
        if (hidePhoneNumberPassword == null) {
            showPasswordDialog()
        } else {
            openMainActivity()
        }
    }

    private fun openMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showPasswordDialog() {
        PasswordDialogFragment { newPassword ->
            config.hidePhoneNumberPassword = newPassword
            openMainActivity()
        }.show(supportFragmentManager, "password_dialog")
    }
}
