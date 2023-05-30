package com.woleapp.netpos.contactless.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.android.material.textfield.TextInputEditText
import com.pixplicity.easyprefs.library.Prefs
import com.woleapp.netpos.contactless.BuildConfig
import com.woleapp.netpos.contactless.R
import com.woleapp.netpos.contactless.adapter.BranchAdapter
import com.woleapp.netpos.contactless.adapter.StatesAdapter
import com.woleapp.netpos.contactless.databinding.FragmentExisitingCustomersRegistrationBinding
import com.woleapp.netpos.contactless.model.ExistingAccountRegisterRequest
import com.woleapp.netpos.contactless.model.FBNBranch
import com.woleapp.netpos.contactless.model.FBNState
import com.woleapp.netpos.contactless.model.RegistrationForExistingFBNUsersRequest
import com.woleapp.netpos.contactless.util.*
import com.woleapp.netpos.contactless.util.AppConstants.SAVED_ACCOUNT_NUM_SIGNED_UP
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.alertDialog
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.getDeviceId
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.observeServerResponse
import com.woleapp.netpos.contactless.viewmodels.ContactlessRegViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_terms_and_conditions.view.*
import kotlinx.android.synthetic.main.fragment_exisiting_customers_registration.*

@AndroidEntryPoint
class ExistingCustomersRegistrationFragment : BaseFragment() {

    private lateinit var binding: FragmentExisitingCustomersRegistrationBinding
    private val viewModel by activityViewModels<ContactlessRegViewModel>()
    private lateinit var loader: AlertDialog
    private lateinit var businessNameView: TextInputEditText
    private lateinit var contactName: TextInputEditText
    private lateinit var addressView: TextInputEditText
    private lateinit var emailView: TextInputEditText
    private lateinit var passwordView: TextInputEditText
    private lateinit var firstBankStates: AutoCompleteTextView
    private lateinit var firstBankBranches: AutoCompleteTextView
    private lateinit var confirmPasswordView: TextInputEditText
    private lateinit var phoneNumber: TextInputEditText
    private lateinit var submitBtn: Button
    private lateinit var partnerID: String
    private lateinit var actNumber: String
    private lateinit var deviceSerialID: String
    private lateinit var listOfStates: String
    private lateinit var listOfBranches: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_exisiting_customers_registration,
            container,
            false,
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getStates()
        deviceSerialID = getDeviceId(requireContext()).toString()
        viewModel.registerMessage.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
        if (BuildConfig.FLAVOR.contains("firstbank")){
            binding.fragmentState.visibility = View.VISIBLE
            binding.fragmentBranch.visibility = View.VISIBLE
        }
        initViews()
        initPartnerID()
        val newActNumber =
            Prefs.getString(AppConstants.SAVED_ACCOUNT_NUM_SIGNED_UP, "")
        // val phoneNumber = newPoneNumber.replace("^\"|\"$", "")
        actNumber = newActNumber.substring(1, newActNumber.length - 1)

        val newBusinessName =
            Prefs.getString(AppConstants.BUSINESS_NAME, "")
        val businessName = newBusinessName.substring(1, newBusinessName.length - 1)

        val newBusinessAddress =
            Prefs.getString(AppConstants.BUSINESS_ADDRESS, "")
        val businessAddress = newBusinessAddress.substring(1, newBusinessAddress.length - 1)

        val newEmail =
            Prefs.getString(AppConstants.EMAIL_ADDRESS, "")
        val email = newEmail.substring(1, newEmail.length - 1)

        val newPhone =
            Prefs.getString(AppConstants.PHONE_NUMBER, "")
        val phone = newPhone.substring(1, newPhone.length - 1)

        val newContactInfo =
            Prefs.getString(AppConstants.FULL_NAME, "")
        val contactInfo = newContactInfo.substring(1, newContactInfo.length - 1).replace("\\u0026", "&")

        binding.businessName.setText(businessName.replace("\\u0026", "&"))
        binding.contactInfo.setText(contactInfo)
        binding.address.setText(businessAddress.replace("\\u0026", "&"))
        binding.phone.setText(phone)
        binding.email.setText(email.replace("\\u0026", "&"))

        loader = alertDialog(requireContext())


        viewModel.getStatesResponse.observe(viewLifecycleOwner) {
            val stateAdapter = StatesAdapter(
                viewModel.listOfStates, requireContext(),
                android.R.layout.simple_expandable_list_item_1
            )
            firstBankStates.setAdapter(stateAdapter)
        }

        firstBankStates.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(adapterView: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val statesList =
                    adapterView?.getItemAtPosition(p2) as FBNState
                listOfStates = statesList.state
                viewModel.getBranches(statesList.id, partnerID, deviceSerialID)
            }
        }


        viewModel.getBranchResponse.observe(viewLifecycleOwner) {
            val branchAdapter = BranchAdapter(
                viewModel.listOfBranches, requireContext(),
                android.R.layout.simple_expandable_list_item_1
            )
            firstBankBranches.setAdapter(branchAdapter)
        }

        firstBankBranches.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(adapterView: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val branchList =
                    adapterView?.getItemAtPosition(p2) as FBNBranch
                listOfBranches = branchList.branch_name
            //    binding.address.setText(branchList.address)
            }
        }

        submitBtn.setOnClickListener {
            register()
        }
    }

    private fun initPartnerID() {
        val bankList = mapOf("firstbank" to "7FD43DF1-633F-4250-8C6F-B49DBB9650EA", "fcmb" to "1B0E68FD-7676-4F2C-883D-3931C3564190",
            "easypay" to "1B0E68FD-7676-4F2C-883D-3931C3564190",
            "fcmbeasypay" to "1B0E68FD-7676-4F2C-883D-3931C3564190", "easyfcmb" to "1B0E68FD-7676-4F2C-883D-3931C3564190",
            "providus" to "8B26F328-040F-4F27-A5BC-4414AB9D1EFA", "providussoftpos" to "8B26F328-040F-4F27-A5BC-4414AB9D1EFA",
            "wemabank" to "1E3D050B-6995-495F-982A-0511114959C8", "zenith" to "3D9B3E2D-5171-4D6A-99CC-E2799D16DD56",
        )

        for (element in bankList) {
            if (element.key == BuildConfig.FLAVOR) {
                partnerID = element.value
            }
        }
    }

    private fun initViews() {
        with(binding) {
            businessNameView = businessName
            contactName = contactInfo
            addressView = address
            emailView = email
            passwordView = password
            firstBankStates = state
            firstBankBranches = branch
            confirmPasswordView = confirmPassword
            phoneNumber = phone
            submitBtn = btnLogin
        }
    }

    private fun register() {
        when {
            businessNameView.text.toString().isEmpty() -> {
                showToast(getString(R.string.all_please_enter_business_name))
            }
            contactName.text.toString().isEmpty() -> {
                showToast(getString(R.string.all_please_enter_full_name))
            }
            addressView.text.toString().isEmpty() -> {
                showToast(getString(R.string.all_please_enter_location))
            }
            phoneNumber.text.toString().isEmpty() -> {
                showToast(getString(R.string.all_please_enter_phone_number))
            }
            emailView.text.toString().isEmpty() -> {
                showToast(getString(R.string.all_please_enter_email_address))
            }
            passwordView.text.toString().isEmpty() -> {
                showToast(getString(R.string.hint_enter_password))
            }
            firstBankStates.text.toString().isEmpty() -> {
                showToast(getString(R.string.select_your_state))
            }
            firstBankBranches.text.toString().isEmpty() -> {
                showToast(getString(R.string.select_your_branch))
            }
            confirmPasswordView.text.toString().isEmpty() -> {
                showToast(getString(R.string.all_please_enter_confirm_password))
            }
            !validatePasswordMismatch(
                passwordView.text.toString(),
                confirmPasswordView.text.toString(),
            ) -> {
                showToast(getString(R.string.all_password_mismatch))
            }
            else -> {
                if (validateSignUpFieldsOnTextChange()) {
                    if (BuildConfig.FLAVOR.contains("providus") || BuildConfig.FLAVOR.contains("providussoftpos")) {
                        activity?.getFragmentManager()?.popBackStack()
                        val dialogView: View = LayoutInflater.from(requireContext())
                            .inflate(R.layout.dialog_terms_and_conditions, null)
                        val dialogBuilder: AlertDialog.Builder =
                            AlertDialog.Builder(requireContext())
                        dialogBuilder.setView(dialogView)

                        val alertDialog: AlertDialog = dialogBuilder.create()
                        alertDialog.show()
                        if (BuildConfig.FLAVOR.contains("providus") || BuildConfig.FLAVOR.contains("providussoftpos")) {
                            dialogView.pdf.fromAsset("providus.pdf").load()
                        } else if (BuildConfig.FLAVOR.contains("fcmb")) {
                            dialogView.pdf.fromAsset("qlick.pdf").load()
                        } else if (BuildConfig.FLAVOR.contains("easypay")) {
                            dialogView.pdf.fromAsset("qlick.pdf").load()
                        } else if (BuildConfig.FLAVOR.contains("fcmbeasypay")) {
                            dialogView.pdf.fromAsset("qlick.pdf").load()
                        } else if (BuildConfig.FLAVOR.contains("easyfcmb")) {
                            dialogView.pdf.fromAsset("qlick.pdf").load()
                        } else if (BuildConfig.FLAVOR.contains("easypayfcmb")) {
                            dialogView.pdf.fromAsset("qlick.pdf").load()
                        }
                        dialogView.accept_button.setOnClickListener {
                            alertDialog.dismiss()
                            registerExistingCustomer()
                        }
                    } else {
                        registerExistingCustomer()
                    }
                }
            }
        }
    }

    private fun validateSignUpFieldsOnTextChange(): Boolean {
        var isValidated = true

        businessNameView.doOnTextChanged { _, _, _, _ ->
            when {
                businessNameView.text.toString().trim().isEmpty() -> {
                    showToast(getString(R.string.all_please_enter_business_name))
                    isValidated = false
                }
                else -> {
                    binding.existingCustomersBusinessName.error = null
                    isValidated = true
                }
            }
        }
        contactName.doOnTextChanged { _, _, _, _ ->
            when {
                contactName.text.toString().trim().isEmpty() -> {
                    showToast(getString(R.string.all_please_enter_full_name))
                    isValidated = false
                }
                else -> {
                    binding.existingCustomersContactInfo.error = null
                    isValidated = true
                }
            }
        }
        addressView.doOnTextChanged { _, _, _, _ ->
            when {
                addressView.text.toString().trim().isEmpty() -> {
                    binding.existingCustomersTextInputAddress.error =
                        getString(R.string.all_please_enter_location)
                    isValidated = false
                }
                else -> {
                    binding.existingCustomersTextInputAddress.error = null
                    isValidated = true
                }
            }
        }
        emailView.doOnTextChanged { _, _, _, _ ->
            when {
                emailView.text.toString().trim().isEmpty() -> {
                    binding.emailField.error =
                        getString(R.string.all_please_enter_email_address)
                    isValidated = false
                }
                else -> {
                    binding.emailField.error = null
                    isValidated = true
                }
            }
        }
        firstBankStates.doOnTextChanged { _, _, _, _ ->
            when {
                firstBankStates.text.toString().trim().isEmpty() -> {
                    binding.fragmentState.error =
                        getString(R.string.select_your_state)
                    isValidated = false
                }
                else -> {
                    binding.fragmentState.error = null
                    isValidated = true
                }
            }
        }
        firstBankBranches.doOnTextChanged { _, _, _, _ ->
            when {
                firstBankBranches.text.toString().trim().isEmpty() -> {
                    binding.fragmentBranch.error =
                        getString(R.string.select_your_branch)
                    isValidated = false
                }
                else -> {
                    binding.fragmentBranch.error = null
                    isValidated = true
                }
            }
        }
        binding.password.doOnTextChanged { _, _, _, _ ->
            when {
                binding.password.text.toString().trim().isEmpty() -> {
                    binding.passwordField.error =
                        getString(R.string.all_please_enter_password)
                    binding.passwordField.errorIconDrawable = null
                    isValidated = false
                }
                else -> {
                    binding.passwordField.error = null
                    isValidated = true
                }
            }
        }
        binding.confirmPassword.doOnTextChanged { _, _, _, _ ->
            when {
                binding.confirmPassword.text.toString().trim().isEmpty() -> {
                    binding.confirmPasswordField.error =
                        getString(R.string.all_please_enter_confirm_password)
                    binding.confirmPasswordField.errorIconDrawable =
                        null
                    isValidated = false
                }
                !validatePasswordMismatch(
                    binding.password.text.toString().trim(),
                    binding.confirmPassword.text.toString().trim(),
                ) -> {
                    binding.confirmPasswordField.error =
                        getString(R.string.all_password_mismatch)
                    binding.confirmPasswordField.errorIconDrawable =
                        null
                    isValidated = false
                }
                else -> {
                    binding.confirmPasswordField.error = null
                    isValidated = true
                }
            }
        }
        return isValidated
    }

    private fun registerExistingCustomer() {
        if (BuildConfig.FLAVOR.contains("firstbank")) {
            val existingAccountRegReq = RegistrationForExistingFBNUsersRequest(
                accountNumber = actNumber,
                businessAddress = addressView.text.toString().trim(),
                businessName = businessNameView.text.toString().trim(),
                contactInformation = contactName.text.toString().trim(),
                username = emailView.text.toString().trim(),
                password = passwordView.text.toString().trim(),
                state = listOfStates,
                branch_name = listOfBranches,
                phoneNumber = phoneNumber.text.toString().trim(),
            )
            viewModel.registerExistingAccountForFBN(
                existingAccountRegReq,
                partnerId = partnerID,
                deviceSerialId = deviceSerialID,
            )
        }else{
            val existingAccountRegReq = ExistingAccountRegisterRequest(
                accountNumber = actNumber,
                businessAddress = addressView.text.toString().trim(),
                businessName = businessNameView.text.toString().trim(),
                contactInformation = contactName.text.toString().trim(),
                username = emailView.text.toString().trim(),
                password = passwordView.text.toString().trim(),
                phoneNumber = phoneNumber.text.toString().trim(),
            )
            viewModel.registerExistingAccount(
                existingAccountRegReq,
                partnerId = partnerID,
                deviceSerialId = deviceSerialID,
            )

        }
        observeServerResponse(
            viewModel.existingRegRequestResponse,
            loader,
            requireActivity().supportFragmentManager,
        ) {
            showFragment(
                LoginFragment(),
                containerViewId = R.id.auth_container,
                fragmentName = "Login Fragment",
            )
        }
    }
}
