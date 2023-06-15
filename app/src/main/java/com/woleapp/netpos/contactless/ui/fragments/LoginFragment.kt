package com.woleapp.netpos.contactless.ui.fragments

import android.content.Intent
import android.nfc.NfcManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.gson.JsonObject
import com.pixplicity.easyprefs.library.Prefs
import com.woleapp.netpos.contactless.BuildConfig
import com.woleapp.netpos.contactless.R
import com.woleapp.netpos.contactless.databinding.DialogPasswordResetBinding
import com.woleapp.netpos.contactless.databinding.DialogSetNewPasswordBinding
import com.woleapp.netpos.contactless.databinding.FragmentLoginBinding
import com.woleapp.netpos.contactless.databinding.LayoutSetPasswordBinding
import com.woleapp.netpos.contactless.model.AuthenticationEventData
import com.woleapp.netpos.contactless.model.MqttEvent
import com.woleapp.netpos.contactless.model.MqttEvents
import com.woleapp.netpos.contactless.network.StormApiClient
import com.woleapp.netpos.contactless.nibss.NetPosTerminalConfig
import com.woleapp.netpos.contactless.ui.activities.MainActivity
import com.woleapp.netpos.contactless.util.*
import com.woleapp.netpos.contactless.util.AppConstants.RESET_USERNAME
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.alertDialog
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.closeSoftKeyboard
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.getDeviceId
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.initPartnerId
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.isLettersOrDigits
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.observeServerResponse
import com.woleapp.netpos.contactless.viewmodels.AuthViewModel
import com.woleapp.netpos.contactless.viewmodels.ContactlessRegViewModel

class LoginFragment : BaseFragment() {

    private val viewModel by viewModels<AuthViewModel>()
    private val contactlessViewModel by activityViewModels<ContactlessRegViewModel>()
    private lateinit var binding: FragmentLoginBinding
    private lateinit var resetPasswordBinding: DialogPasswordResetBinding
    private lateinit var passwordResetDialog: AlertDialog
    private lateinit var confirmOTPBinding : DialogSetNewPasswordBinding
    private lateinit var confirmOTPDialog: AlertDialog
    private lateinit var setPasswordBinding : LayoutSetPasswordBinding
    private lateinit var setPasswordDialog: AlertDialog
    private lateinit var deviceId: String
    private lateinit var loader: android.app.AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            executePendingBindings()
            viewmodel = viewModel
        }
        resetPasswordBinding = DialogPasswordResetBinding.inflate(inflater, null, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                executePendingBindings()
                viewmodel = viewModel
            }
        passwordResetDialog = AlertDialog.Builder(requireContext())
            .apply {
                setView(resetPasswordBinding.root)
                setCancelable(false)
            }.create()

        confirmOTPBinding = DialogSetNewPasswordBinding.inflate(inflater, null, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                executePendingBindings()
            }
        confirmOTPDialog = AlertDialog.Builder(requireContext())
            .apply {
                setView(confirmOTPBinding.root)
                setCancelable(false)
            }.create()

        setPasswordBinding = LayoutSetPasswordBinding.inflate(inflater, null, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
                executePendingBindings()
            }
        setPasswordDialog = AlertDialog.Builder(requireContext())
            .apply {
                setView(setPasswordBinding.root)
                setCancelable(false)
            }.create()

        confirmOTPBinding.closeDialog.setOnClickListener {
            confirmOTPDialog.dismiss()
        }
        resetPasswordBinding.closeDialog.setOnClickListener {
            passwordResetDialog.dismiss()
        }
        val credentials = JsonObject()
        credentials.addProperty("appname", UtilityParam.APP_NAME)
        credentials.addProperty("password", UtilityParam.APP_PASSWORD)
        viewModel.apply {
            stormApiService = StormApiClient.getInstance()
            appCredentials = credentials
        }
         deviceId = getDeviceId(requireContext()).toString()
        //   Log.d("IMEI", uniqueNumber)
        binding.btnLogin.setOnClickListener {
            viewModel.login(deviceId)
        }

        binding.register.setOnClickListener {
            if (BuildConfig.FLAVOR.contains("firstbank") || BuildConfig.FLAVOR.contains("providuspos") || BuildConfig.FLAVOR.contains("providus") || BuildConfig.FLAVOR.contains("providussoftpos") ||
                BuildConfig.FLAVOR.contains("wemabank") ||
                BuildConfig.FLAVOR.contains("easypay") || BuildConfig.FLAVOR.contains("fcmbeasypay") ||
                BuildConfig.FLAVOR.contains("easypayfcmb") ||
                BuildConfig.FLAVOR.contains("zenith")
            ) {
                showFragment(
                    NewOrExistingFragment(),
                    containerViewId = R.id.auth_container,
                    fragmentName = "NewOrExisting Fragment",
                )
            } else {
                showFragment(
                    RegisterFragment(),
                    containerViewId = R.id.auth_container,
                    fragmentName = "Register Fragment",
                )
            }
        }

        binding.testMyPhone.setOnClickListener {
            testNfcSupport()
        }
        binding.contactlessLogo.setOnClickListener {
            testNfcSupport()
        }
        return binding.root
    }

    private fun testNfcSupport() {
        val nfcManager: NfcManager =
            requireActivity().getSystemService(AppCompatActivity.NFC_SERVICE) as NfcManager
        val nfcAdapter = nfcManager.defaultAdapter
        if (nfcAdapter != null) {
            // Toast.makeText(this, "Device has NFC support", Toast.LENGTH_SHORT).show()
            if (nfcAdapter.isEnabled) {
                Toast.makeText(requireContext(), "NFC enabled", Toast.LENGTH_SHORT).show()
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("NFC Message")
                    .setMessage("Device has NFC support and is enabled")
                    .setPositiveButton("Close") { dialog, _ ->
                        dialog.dismiss()
                    }.show()
            } else {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("NFC Message")
                    .setMessage("NFC is not enabled, goto device settings to enable")
                    .setPositiveButton("Settings") { dialog, _ ->
                        dialog.dismiss()
                        startActivityForResult(
                            Intent(android.provider.Settings.ACTION_NFC_SETTINGS),
                            0,
                        )
                    }.show()
            }
        } else {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("NFC Message")
                .setMessage("Device dose not have NFC support")
                .setPositiveButton("Close") { dialog, _ ->
                    dialog.dismiss()
                    // finish()
                }.show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader = alertDialog(requireContext())
        viewModel.message.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
        contactlessViewModel.message.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
        binding.forgotPassword.setOnClickListener {
            if (BuildConfig.FLAVOR.contains("firstbank")) {
                confirmOTPDialog.show()
            }else{
                passwordResetDialog.show()
            }
        }
        viewModel.authDone.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { authenticated ->
                if (authenticated) {
                    activity?.apply {
                        startActivity(
                            Intent(this, MainActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            },
                        )
                        NetPosTerminalConfig.init(applicationContext)
                        val event = MqttEvent<AuthenticationEventData>().apply {
                            this.event = MqttEvents.AUTHENTICATION.event
                            this.code = "00"
                            this.timestamp = System.currentTimeMillis()
                            this.data = AuthenticationEventData(
                                this.business_name!!,
                                this.storm_id!!,
                                this.deviceSerial!!,
                            )
                            this.status = "SUCCESS"
                        }
                        finish()
                    }
                }
            }
        }

        viewModel.passwordResetSent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                if (it && passwordResetDialog.isShowing) {
                    passwordResetDialog.cancel()
                }
            }
        }

            setPasswordBinding.save.setOnClickListener {
                val email = setPasswordBinding.email.text?.trim().toString()
                val password = setPasswordBinding.passwordEdittext.text?.trim().toString()
                val confirmPassword = setPasswordBinding.confirmPasswordEdittext.text?.trim().toString()
                if (password != confirmPassword){
                    showToast("Password mismatch")
                    return@setOnClickListener
                }
                if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                    showToast("Enter your new password")
                    return@setOnClickListener
                }
                val payload = JsonObject().apply {
                    addProperty("email", email)
                    addProperty("newPassword", password)
                }
                setPasswordBinding.authProgress.visibility = View.VISIBLE
                contactlessViewModel.setNewPassword(payload, initPartnerId(), deviceId)
                observeServerResponse(contactlessViewModel.newPasswordResponse, loader, requireActivity().supportFragmentManager){
                    setPasswordDialog.dismiss()
                }
            }

            confirmOTPBinding.btnResetPassword.setOnClickListener {
                activity?.getFragmentManager()?.popBackStack()
                if (confirmOTPBinding.fragmentOtp.isVisible){
                    val email = confirmOTPBinding.etEmail.text?.trim().toString()
                    val otp = confirmOTPBinding.otp.text?.trim().toString()
                    if (email.isNullOrEmpty() || otp.isNullOrEmpty()) {
                        showToast("All fields are required required")
                        return@setOnClickListener
                    }
                    val payload = JsonObject().apply {
                        addProperty("email", email)
                        addProperty("otp", otp)
                    }
                    contactlessViewModel.confirmOTPToSetPassword(payload, initPartnerId(), deviceId)
                    observeServerResponse(contactlessViewModel.confirmOtpToResetPasswordResponse, loader, requireActivity().supportFragmentManager){
                        setPasswordBinding.email.setText(email)
                        confirmOTPBinding.fragmentOtp.visibility = View.VISIBLE
                        confirmOTPDialog.dismiss()
                        setPasswordDialog.show()
                    }
                }else{
                    val email = confirmOTPBinding.etEmail.text?.trim().toString()
                    if (email.isNullOrEmpty()) {
                        showToast("Email is required")
                        return@setOnClickListener
                    }
                    val payload = JsonObject().apply {
                        addProperty("email", email)
                    }
         //           closeSoftKeyboard(requireContext(), this.requireActivity())
                    contactlessViewModel.resetPassword(payload, initPartnerId(), deviceId)
                    observeServerResponse(contactlessViewModel.resetPasswordResponse, loader, requireActivity().supportFragmentManager){
                        confirmOTPBinding.etEmail.setText(email)
                        confirmOTPBinding.fragmentOtp.visibility = View.VISIBLE
                        confirmOTPBinding.resetPasswordHeader.text = "ENTER OTP"
                    }
                }
            }
    }
}
