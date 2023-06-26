package com.woleapp.netpos.contactless.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.woleapp.netpos.contactless.BuildConfig
import com.woleapp.netpos.contactless.R
import com.woleapp.netpos.contactless.adapter.BranchAdapter
import com.woleapp.netpos.contactless.adapter.StatesAdapter
import com.woleapp.netpos.contactless.databinding.FragmentRegisterBinding
import com.woleapp.netpos.contactless.model.FBNBranch
import com.woleapp.netpos.contactless.model.FBNState
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.getDeviceId
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.initPartnerId
import com.woleapp.netpos.contactless.viewmodels.ContactlessRegViewModel
import com.woleapp.netpos.contactless.viewmodels.RegistrationViewModel
import java.util.*

class RegisterFragment : BaseFragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegistrationViewModel>()
    private val contactlessViewModel by activityViewModels<ContactlessRegViewModel>()
    private lateinit var dialog: AlertDialog
    private lateinit var failureDialog: AlertDialog
    private lateinit var register: Button
    private lateinit var deviceSerialId: String
    private lateinit var listOfStates: String
    private lateinit var listOfBranches: String
    private lateinit var firstBankStates: AutoCompleteTextView
    private lateinit var firstBankBranches: AutoCompleteTextView
    private lateinit var partnerID : String
    private lateinit var date: Calendar

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

        if (BuildConfig.FLAVOR.contains("zenith")) {
            viewModel.message.observe(viewLifecycleOwner) { event ->
                event.getContentIfNotHandled()?.let {
                    failureDialog = AlertDialog.Builder(requireContext())
                        .setTitle("Registration Status")
                        .setCancelable(false)
                        .setPositiveButton("OK") { _, _ ->
                            failureDialog.dismiss()
                        }
                        .setMessage(it)
                        .create()
                }
                failureDialog.show()
            }
        }else{
            viewModel.message.observe(viewLifecycleOwner) { event ->
                event.getContentIfNotHandled()?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.authDone.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                if (it) {
                    dialog.show()
                }
            }
        }
        if (BuildConfig.FLAVOR.contains("zenith")) {
            dialog = AlertDialog.Builder(requireContext())
                .setTitle("Registration Status")
                .setCancelable(false)
                .setPositiveButton("Continue") { _, _ ->
                    showFragment(
                        LoginFragment(),
                        containerViewId = R.id.auth_container,
                        fragmentName = "Login Fragment",
                    )
                }
                .setMessage("Successful")
                .create()
        }else{
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
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        partnerID = initPartnerId()
        if (BuildConfig.FLAVOR.contains("zenith")) {
            binding.fragmentState.visibility = View.VISIBLE
            binding.fragmentState.hint = "Select D.O.B"
            binding.loginLabel.text = "Account Opening/SIGN UP"
            binding.state.isFocusableInTouchMode = false
            binding.bvnField.visibility = View.VISIBLE
            binding.referenceField.visibility = View.VISIBLE
            binding.passwordField.visibility = View.VISIBLE
            binding.confirmPasswordField.visibility = View.VISIBLE
            binding.textInputBusinessName.hint = "First Name"
            binding.businessName.isFocusableInTouchMode = true
            binding.contactInfo.isFocusableInTouchMode = true
            binding.phone.isFocusableInTouchMode = true
            binding.email.isFocusableInTouchMode = true
            binding.textContactInfo.hint = "Last Name"
            binding.state.setOnClickListener {
                showDateTimePicker()
            }
        }
        deviceSerialId = getDeviceId(requireContext())
        if (BuildConfig.FLAVOR.contains("firstbank")) {
            binding.fragmentState.visibility = View.VISIBLE
            binding.fragmentBranch.visibility = View.VISIBLE
            contactlessViewModel.getStates()

            contactlessViewModel.getStatesResponse.observe(viewLifecycleOwner) {
                val stateAdapter = StatesAdapter(
                    contactlessViewModel.listOfStates, requireContext(),
                    android.R.layout.simple_expandable_list_item_1
                )
                firstBankStates.setAdapter(stateAdapter)
            }

            firstBankStates.onItemClickListener = object : AdapterView.OnItemClickListener {
                override fun onItemClick(adapterView: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val statesList =
                        adapterView?.getItemAtPosition(p2) as FBNState
                    listOfStates = statesList.state
                    viewModel.setSelectedState(listOfStates)
                    contactlessViewModel.getBranches(statesList.id, partnerID, deviceSerialId)
                }
            }

            contactlessViewModel.getBranchResponse.observe(viewLifecycleOwner) {
                val branchAdapter = BranchAdapter(
                    contactlessViewModel.listOfBranches, requireContext(),
                    android.R.layout.simple_expandable_list_item_1
                )
                firstBankBranches.setAdapter(branchAdapter)
            }

            firstBankBranches.onItemClickListener = object : AdapterView.OnItemClickListener {
                override fun onItemClick(adapterView: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val branchList =
                        adapterView?.getItemAtPosition(p2) as FBNBranch
                    listOfBranches = branchList.branch_name
                    viewModel.setSelectedBranch(listOfBranches)
                }
            }
        }




        register = binding.btnLogin
        register.setOnClickListener {
            viewModel.register(requireContext(),partnerID, deviceSerialId)
        }
    }


    private fun showDateTimePicker() {
        val currentDate = Calendar.getInstance()
        date = Calendar.getInstance()
        DatePickerDialog(
            requireContext(), { view, year, monthOfYear, dayOfMonth ->
                date.set(year, monthOfYear, dayOfMonth)
                val month = monthOfYear + 1
                binding.state.setText("$year-$month-$dayOfMonth")
            }, currentDate[Calendar.YEAR], currentDate[Calendar.MONTH], currentDate[Calendar.DATE]
        ).show()
    }
}
