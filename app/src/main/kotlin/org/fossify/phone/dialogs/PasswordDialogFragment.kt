package org.fossify.phone.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.fossify.commons.extensions.beGone
import org.fossify.commons.extensions.beGoneIf
import org.fossify.phone.databinding.DialogPasswordBinding
import org.fossify.phone.extensions.config

class PasswordDialogFragment(
    private val isVerifyMode: Boolean = false,
    private val onPasswordSet: (String?) -> Unit
) : DialogFragment() {

    enum class Mode { FIRST_TIME, VERIFY, SETTINGS }

    private lateinit var binding: DialogPasswordBinding
    private lateinit var mode: Mode
    private var currentPassword: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogPasswordBinding.inflate(layoutInflater)
        currentPassword = requireActivity().config.hidePhoneNumberPassword
        mode = if (isVerifyMode) {
            Mode.VERIFY
        } else {
            if (currentPassword == null || currentPassword == "-1") Mode.FIRST_TIME else Mode.SETTINGS
        }

        val builder = AlertDialog.Builder(requireContext()).setView(binding.root)
        isCancelable = mode != Mode.FIRST_TIME // First-time setup is mandatory

        when (mode) {
            Mode.FIRST_TIME -> setupFirstTimeUI()
            Mode.VERIFY -> setupVerifyUI()
            Mode.SETTINGS -> setupSettingsUI()
        }

        return builder.create()
    }

    private fun setupFirstTimeUI() {
        binding.layoutCurrentPassword.beGone()
        binding.btnRemovePassword.text = "Skip"

        binding.btnRemovePassword.setOnClickListener {
            onPasswordSet("-1") // Skip password
            dismiss()
        }
        binding.btnSave.setOnClickListener {
            val newPass = binding.etNewPassword.text.toString()
            val confirmPass = binding.etConfirmNewPassword.text.toString()

            if (newPass.isEmpty() || newPass.length < 6) {
                binding.layoutNewPassword.error = "Must be more than 6 characters! "
                return@setOnClickListener
            }
            if (newPass != confirmPass) {
                binding.etConfirmNewPassword.error = "Passwords do not match!"
                return@setOnClickListener
            }

            onPasswordSet(newPass)
            dismiss()
        }
    }

    private fun setupVerifyUI() {
        binding.layoutCurrentPassword.beGoneIf(currentPassword.isNullOrEmpty()) // Show only if password exists
        binding.layoutNewPassword.beGone()
        binding.layoutConfirmNewPassword.beGone()
        binding.btnRemovePassword.beGone()
        binding.btnSave.text = "Show"

        binding.btnSave.setOnClickListener {
            val enteredPassword = binding.etCurrentPassword.text.toString()
            if (enteredPassword != currentPassword) {
                binding.etCurrentPassword.error = "Incorrect password!"
                return@setOnClickListener
            }

            onPasswordSet(enteredPassword)
            dismiss()
        }
    }

    private fun setupSettingsUI() {
        val hasPassword = !currentPassword.isNullOrEmpty()
        binding.layoutCurrentPassword.beGoneIf(!hasPassword)
        binding.btnRemovePassword.beGoneIf(!hasPassword)

        binding.btnSave.setOnClickListener {
            if (hasPassword) {
                val currentPassInput = binding.etCurrentPassword.text.toString()
                if (currentPassInput != currentPassword) {
                    binding.etCurrentPassword.error = "Incorrect current password!"
                    return@setOnClickListener
                }
                binding.etCurrentPassword.error = null
            }

            val newPass = binding.etNewPassword.text.toString()
            val confirmPass = binding.etConfirmNewPassword.text.toString()

            if (newPass.isEmpty() || newPass.length < 6) {
                binding.layoutNewPassword.error = "Must be more than 6 characters! "
                return@setOnClickListener
            }
            if (newPass != confirmPass) {
                binding.etConfirmNewPassword.error = "Passwords do not match!"
                return@setOnClickListener
            }

            onPasswordSet(newPass)
            dismiss()
        }

        binding.btnRemovePassword.setOnClickListener {
            val currentPassInput = binding.etCurrentPassword.text.toString()
            if (currentPassInput != currentPassword) {
                binding.etCurrentPassword.error = "Incorrect current password!"
                return@setOnClickListener
            }

            onPasswordSet("-1") // Remove password
            dismiss()
        }
    }
}

