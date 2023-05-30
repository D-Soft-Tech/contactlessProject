package com.woleapp.netpos.contactless.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.woleapp.netpos.contactless.BuildConfig
import com.woleapp.netpos.contactless.R
import com.woleapp.netpos.contactless.adapter.StatesAdapter
import com.woleapp.netpos.contactless.databinding.FragmentNewOrExistingBinding
import com.woleapp.netpos.contactless.model.FBNState
import com.woleapp.netpos.contactless.util.RandomPurposeUtil
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.getDeviceId
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.observeServerResponse
import com.woleapp.netpos.contactless.viewmodels.ContactlessRegViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_account_number_layout.view.*
import timber.log.Timber

@AndroidEntryPoint
class NewOrExistingFragment : BaseFragment() {

    private lateinit var binding: FragmentNewOrExistingBinding
    private val viewModel by activityViewModels<ContactlessRegViewModel>()
    private lateinit var loader: AlertDialog
    private lateinit var newPartnerId: String
    private lateinit var deviceSerialID: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_new_or_existing, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPartnerID()
        deviceSerialID = getDeviceId(requireContext()).toString()
        viewModel.message.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }


        loader = RandomPurposeUtil.alertDialog(requireContext())

        binding.confirmationTvNo.setOnClickListener {
            showFragment(
                RegisterFragment(),
                containerViewId = R.id.auth_container,
                fragmentName = "Register Fragment",
            )
        }

        binding.confirmationTvYes.setOnClickListener {
            activity?.getFragmentManager()?.popBackStack()
            val dialogView: View = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_account_number_layout, null)
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            dialogBuilder.setView(dialogView)

            val alertDialog: AlertDialog = dialogBuilder.create()
            alertDialog.show()

            dialogView.dialog_account_number_proceed.setOnClickListener {
                alertDialog.dismiss()
                val account = dialogView.dialog_account_number_editText.text.toString()
                if (account.isNullOrEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Please, enter your account number",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else if (account.length < 10) {
                    Toast.makeText(
                        requireContext(),
                        "Please, enter valid account number",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    if (BuildConfig.FLAVOR.contains("firstbank")){
                        viewModel.findAccountForFirstBankUser(account, newPartnerId, deviceSerialID)
                        observeServerResponse(viewModel.firstBankAccountNumberResponse, loader, requireActivity().supportFragmentManager){
                            showFragment(
                                ExistingCustomersRegistrationFragment(),
                                containerViewId = R.id.auth_container,
                                fragmentName = "RegisterOTP Fragment"
                            )
                        }
                    }else{
                        viewModel.accountLookUp(account, newPartnerId, deviceSerialID)
                        observeServerResponse(viewModel.accountNumberResponse, loader, requireActivity().supportFragmentManager){
                            showFragment(
                                RegistrationOTPFragment(),
                                containerViewId = R.id.auth_container,
                                fragmentName = "RegisterOTP Fragment"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun initPartnerID() {
        val bankList = mutableMapOf("firstbank" to "7FD43DF1-633F-4250-8C6F-B49DBB9650EA", "fcmb" to "1B0E68FD-7676-4F2C-883D-3931C3564190", "easypay" to "1B0E68FD-7676-4F2C-883D-3931C3564190",
            "fcmbeasypay" to "1B0E68FD-7676-4F2C-883D-3931C3564190", "easypayfcmb" to "1B0E68FD-7676-4F2C-883D-3931C3564190","easyfcmb" to "1B0E68FD-7676-4F2C-883D-3931C3564190",
            "providus" to "8B26F328-040F-4F27-A5BC-4414AB9D1EFA", "providussoftpos" to "8B26F328-040F-4F27-A5BC-4414AB9D1EFA",
            "wemabank" to "1E3D050B-6995-495F-982A-0511114959C8", "zenith" to "3D9B3E2D-5171-4D6A-99CC-E2799D16DD56",
        )

        for (element in bankList) {
            if (element.key == BuildConfig.FLAVOR) {
                Timber.d("ACCOUNTBANK---->${element.value}")
                newPartnerId = element.value
                Timber.d("PARTNERID---->$newPartnerId")
            }
        }
    }
}
