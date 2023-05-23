package com.woleapp.netpos.contactless.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import com.woleapp.netpos.contactless.R
import com.woleapp.netpos.contactless.databinding.FragmentScanQrCodeLandingPageBinding

class ScanQrCodeLandingPage : BaseFragment() {
    private lateinit var binding: FragmentScanQrCodeLandingPageBinding
    private lateinit var proceedBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_scan_qr_code_landing_page,
            container,
            false,
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onResume() {
        super.onResume()
        proceedBtn.setOnClickListener {
            showFragment(FragmentBarCodeScannerRefactored())
        }
    }

    private fun initViews() {
        with(binding) {
            proceedBtn = proceedButton
        }
    }
}
