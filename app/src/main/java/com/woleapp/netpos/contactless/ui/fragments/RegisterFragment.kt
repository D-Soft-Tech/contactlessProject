package com.woleapp.netpos.contactless.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.woleapp.netpos.contactless.BuildConfig
import com.woleapp.netpos.contactless.R
import com.woleapp.netpos.contactless.databinding.FragmentRegisterBinding
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.getDeviceId
import com.woleapp.netpos.contactless.viewmodels.RegistrationViewModel

class RegisterFragment : BaseFragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegistrationViewModel>()
    private lateinit var dialog: AlertDialog
    private lateinit var register: Button
    private lateinit var deviceSerialId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater).apply {
            executePendingBindings()
            lifecycleOwner = viewLifecycleOwner
            viewmodel = this@RegisterFragment.viewModel
        }
        viewModel.message.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.authDone.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                if (it) {
                    dialog.show()
                }
            }
        }
        dialog = AlertDialog.Builder(requireContext())
            .setTitle("Registration Status")
            .setCancelable(false)
            .setPositiveButton("Continue") { _, _ ->
                // d.cancel()
                // requireActivity().onBackPressed()
                showFragment(
                    LoginFragment(),
                    containerViewId = R.id.auth_container,
                    fragmentName = "Login Fragment",
                )
            }
            .setMessage("Business Info Received, our team will contact you in 2 Business Days")
            .create()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // setSpinner()
        deviceSerialId = getDeviceId(requireContext()).toString()
        register = binding.btnLogin
        val bankList = mapOf("firstbank" to "7FD43DF1-633F-4250-8C6F-B49DBB9650EA", "fcmb" to "1B0E68FD-7676-4F2C-883D-3931C3564190", "easypay" to "1B0E68FD-7676-4F2C-883D-3931C3564190",
            "fcmbeasypay" to "1B0E68FD-7676-4F2C-883D-3931C3564190", "easypayfcmb" to "1B0E68FD-7676-4F2C-883D-3931C3564190", "easyfcmb" to "1B0E68FD-7676-4F2C-883D-3931C3564190",
            "providus" to "8B26F328-040F-4F27-A5BC-4414AB9D1EFA", "providussoftpos" to "8B26F328-040F-4F27-A5BC-4414AB9D1EFA",
            "wemabank" to "1E3D050B-6995-495F-982A-0511114959C8", "zenith" to "3D9B3E2D-5171-4D6A-99CC-E2799D16DD56",
        )

        for (element in bankList) {
            if (element.key == BuildConfig.FLAVOR) {
                // Timber.d("CODEBANK---->${element.value}")
                viewModel.setSelectedBank(element.value)
            }
        }

        register.setOnClickListener {
            viewModel.register(requireContext(), deviceSerialId)
        }
    }
}
