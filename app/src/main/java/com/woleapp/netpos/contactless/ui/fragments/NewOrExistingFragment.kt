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
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.initPartnerId
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
        newPartnerId = initPartnerId()
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
                    if (BuildConfig.FLAVOR.contains("firstbank") || BuildConfig.FLAVOR.contains("zenith")){
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

}
