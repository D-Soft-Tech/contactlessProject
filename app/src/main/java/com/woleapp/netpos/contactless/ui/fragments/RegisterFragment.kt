package com.woleapp.netpos.contactless.ui.fragments

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
import com.woleapp.netpos.contactless.viewmodels.ContactlessRegViewModel
import com.woleapp.netpos.contactless.viewmodels.RegistrationViewModel

class RegisterFragment : BaseFragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegistrationViewModel>()
    private val contactlessViewModel by activityViewModels<ContactlessRegViewModel>()
    private lateinit var dialog: AlertDialog
    private lateinit var register: Button
    private lateinit var deviceSerialId: String
    private lateinit var listOfStates: String
    private lateinit var listOfBranches: String
    private lateinit var firstBankStates: AutoCompleteTextView
    private lateinit var firstBankBranches: AutoCompleteTextView
    private lateinit var partnerID : String

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
        firstBankStates = binding.state
        firstBankBranches = binding.branch
        initPartnerId()
        deviceSerialId = getDeviceId(requireContext())
        if (BuildConfig.FLAVOR.contains("firstbank")){
            contactlessViewModel.getStates()
        }
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

        register = binding.btnLogin
        register.setOnClickListener {
            viewModel.register(requireContext(),partnerID, deviceSerialId)
        }
    }

    private fun initPartnerId(){
        val bankList = mapOf("firstbank" to "7FD43DF1-633F-4250-8C6F-B49DBB9650EA", "easypay" to "1B0E68FD-7676-4F2C-883D-3931C3564190",
            "fcmbeasypay" to "1B0E68FD-7676-4F2C-883D-3931C3564190", "easypayfcmb" to "1B0E68FD-7676-4F2C-883D-3931C3564190",
            "providuspos" to "8B26F328-040F-4F27-A5BC-4414AB9D1EFA","providus" to "8B26F328-040F-4F27-A5BC-4414AB9D1EFA", "providussoftpos" to "8B26F328-040F-4F27-A5BC-4414AB9D1EFA",
            "wemabank" to "1E3D050B-6995-495F-982A-0511114959C8", "zenith" to "3D9B3E2D-5171-4D6A-99CC-E2799D16DD56",
        )

        for (element in bankList) {
            if (element.key == BuildConfig.FLAVOR) {
                partnerID = element.value
            }
        }
    }
}
