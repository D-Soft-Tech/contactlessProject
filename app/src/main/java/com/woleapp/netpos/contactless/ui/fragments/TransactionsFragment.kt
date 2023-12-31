package com.woleapp.netpos.contactless.ui.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.danbamitale.epmslib.entities.*
import com.danbamitale.epmslib.extensions.formatCurrencyAmount
import com.danbamitale.epmslib.processors.TransactionProcessor
import com.danbamitale.epmslib.utils.IsoAccountType
import com.dsofttech.dprefs.utils.DPrefs
import com.pixplicity.easyprefs.library.Prefs
import com.woleapp.netpos.contactless.BuildConfig
import com.woleapp.netpos.contactless.R
import com.woleapp.netpos.contactless.adapter.ServiceAdapter
import com.woleapp.netpos.contactless.databinding.FragmentTransactionsBinding
import com.woleapp.netpos.contactless.databinding.LayoutPreauthDialogBinding
import com.woleapp.netpos.contactless.databinding.LayoutVerveCardQrAmountDialogBinding
import com.woleapp.netpos.contactless.databinding.QrAmoutDialogBinding
import com.woleapp.netpos.contactless.model.PostQrToServerModel
import com.woleapp.netpos.contactless.model.QrScannedDataModel
import com.woleapp.netpos.contactless.model.Service
import com.woleapp.netpos.contactless.nibss.NetPosTerminalConfig
import com.woleapp.netpos.contactless.ui.dialog.LoadingDialog
import com.woleapp.netpos.contactless.ui.dialog.QrPasswordPinBlockDialog
import com.woleapp.netpos.contactless.util.*
import com.woleapp.netpos.contactless.util.AppConstants.getGUID
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.observeServerResponse
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.stringToBase64
import com.woleapp.netpos.contactless.viewmodels.NfcCardReaderViewModel
import com.woleapp.netpos.contactless.viewmodels.ScanQrViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

@AndroidEntryPoint
class TransactionsFragment : BaseFragment() {

    private lateinit var adapter: ServiceAdapter
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val scanQrViewModel by activityViewModels<ScanQrViewModel>()
    private lateinit var qrAmoutDialogBinding: QrAmoutDialogBinding
    private lateinit var verveCardQrAmountDialogBinding: LayoutVerveCardQrAmountDialogBinding
    private lateinit var qrAmountDialog: androidx.appcompat.app.AlertDialog
    private lateinit var qrAmountDialogForVerveCard: androidx.appcompat.app.AlertDialog
    private lateinit var requestNarration: String
    private lateinit var progressDialog: ProgressDialog
    private val nfcCardReaderViewModel by activityViewModels<NfcCardReaderViewModel>()
    private var observer: ((Event<ICCCardHelper>) -> Unit)? = null
    private val qrPinBlock: QrPasswordPinBlockDialog = QrPasswordPinBlockDialog()
    private var amountToPayInDouble: Double? = 0.0
    private var qrData: QrScannedDataModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNarration = Singletons.getCurrentlyLoggedInUser()?.mid?.let {
            "$it:${Singletons.getCurrentlyLoggedInUser()?.terminal_id}:${UtilityParam.STRING_MPGS_TAG}"
        } ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        adapter = ServiceAdapter {
            val nextFrag: Any? = when (it.id) {
                0 -> {
                    showFragment(SalesFragment.newInstance())
                }
                1 -> {
                    showFragment(TransactionHistoryFragment.newInstance(action = HISTORY_ACTION_REFUND))
                }
                8 -> showFragment(TransactionHistoryFragment.newInstance(action = HISTORY_ACTION_REVERSAL))
                7 -> {
                    showFragment(SalesFragment.newInstance(TransactionType.PURCHASE_WITH_CASH_BACK))
                }
                2 -> {
                    showPreAuthDialog()
                    null
                }
                4 -> {
                    getBalance()
                    null
                }
                5 -> showFragment(ReprintFragment())

                3 -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container_main, SettingsFragment())
                        .addToBackStack(null)
                        .commit()
                }
                6 -> showFragment(SalesFragment.newInstance(isVend = true))
                else -> showFragment(SalesFragment.newInstance(TransactionType.CASH_ADVANCE))
            }
        }

        qrAmoutDialogBinding = QrAmoutDialogBinding.inflate(inflater, null, false)
            .apply {
                executePendingBindings()
                lifecycleOwner = viewLifecycleOwner
            }

        verveCardQrAmountDialogBinding =
            LayoutVerveCardQrAmountDialogBinding.inflate(inflater, null, false)
                .apply {
                    executePendingBindings()
                    lifecycleOwner = viewLifecycleOwner
                }

        qrAmountDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext()).apply {
            setView(qrAmoutDialogBinding.root)
        }.create()

        qrAmountDialogForVerveCard =
            androidx.appcompat.app.AlertDialog.Builder(requireContext()).apply {
                setView(verveCardQrAmountDialogBinding.root)
            }.create()

        progressDialog = ProgressDialog(requireContext())

        return binding.root
    }

    override fun onDetach() {
        super.onDetach()
        Timber.e("transaction detached")
    }

    override fun onStop() {
        super.onStop()
        Timber.e("on stop")
        nfcCardReaderViewModel.iccCardHelperLiveData.removeObservers(viewLifecycleOwner)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nfcCardReaderViewModel.iccCardHelperLiveData.removeObservers(viewLifecycleOwner)
        binding.rvTransactions.layoutManager = GridLayoutManager(context, 2)
        binding.rvTransactions.adapter = adapter
        setService()
    }

    private fun setService() {
        val listOfService = ArrayList<Service>()
            .apply {
                add(Service(0, "Purchase", R.drawable.purchase))
                if (!BuildConfig.FLAVOR.contains("providussoftpos") || !BuildConfig.FLAVOR.contains("providus")) {
                    add(Service(7, "Purchase With Cashback", R.drawable.purchase))
                }
                if (BuildConfig.FLAVOR.contains("providuspos")) {
                    remove(Service(7, "Purchase With Cashback", R.drawable.purchase))
                }
                add(Service(3, "Settings", R.drawable.ic_baseline_settings))
                if (BuildConfig.FLAVOR.contains("polaris")) {
                    remove(Service(3, "Settings", R.drawable.ic_baseline_settings))
                }
                add(Service(4, "Balance Enquiry", R.drawable.ic_write))
                if (BuildConfig.FLAVOR.contains("zenith")) {
                    remove(Service(4, "Balance Enquiry", R.drawable.ic_write))
                }
                add(Service(5, "Reprint", R.drawable.ic_print))
            }
        adapter.submitList(listOfService)
    }

    private fun showPreAuthDialog() {
        val dialog = AlertDialog.Builder(context)
            .apply {
                setCancelable(false)
            }.create()
        val preAuthDialogBinding =
            LayoutPreauthDialogBinding.inflate(LayoutInflater.from(context), null, false)
                .apply {
                    lifecycleOwner = viewLifecycleOwner
                    executePendingBindings()
                    preAuthNew.setOnClickListener {
                        dialog.dismiss()
                        addFragmentWithoutRemove(SalesFragment.newInstance(TransactionType.PRE_AUTHORIZATION))
                    }
                    preAuthComplete.setOnClickListener {
                        dialog.dismiss()
                        addFragmentWithoutRemove(
                            TransactionHistoryFragment.newInstance(
                                HISTORY_ACTION_PREAUTH,
                            ),
                        )
                    }
                    cancelButton.setOnClickListener {
                        dialog.dismiss()
                    }
                }
        dialog.setView(preAuthDialogBinding.root)
        dialog.show()
    }

    private fun showAmountDialog(qrData: QrScannedDataModel) {
        qrAmountDialog.show()
        qrAmoutDialogBinding.proceed.setOnClickListener {
            val amountDouble = qrAmoutDialogBinding.amount.text.toString().toDoubleOrNull()
            if (qrAmoutDialogBinding.amount.text.isNullOrEmpty()) {
                qrAmoutDialogBinding.amount.error =
                    getString(R.string.amount_empty)
            }
            amountDouble?.let {
                qrAmoutDialogBinding.amount.text?.clear()
                qrAmountDialog.cancel()
                val qrDataToSendToBackend =
                    PostQrToServerModel(
                        it,
                        qrData.data,
                        merchantId = UtilityParam.STRING_MERCHANT_ID,
                        naration = requestNarration,
                    )
                Log.d("QRDATA", qrDataToSendToBackend.naration)
                scanQrViewModel.setScannedQrIsVerveCard(false)
                scanQrViewModel.postScannedQrRequestToServer(qrDataToSendToBackend)
                observeServerResponse(
                    scanQrViewModel.sendQrToServerResponse,
                    LoadingDialog(),
                    childFragmentManager,
                ) {
                    addFragmentWithoutRemove(
                        CompleteQrPaymentWebViewFragment(),
                    )
                }
            }
        }
    }

    private fun showAmountDialogForVerveCard() {
        qrAmountDialogForVerveCard.show()
        verveCardQrAmountDialogBinding.proceed.setOnClickListener {
            if (verveCardQrAmountDialogBinding.amount.text.isNullOrEmpty()) {
                verveCardQrAmountDialogBinding.amount.error = getString(R.string.amount_empty)
            }
            val amountDouble =
                verveCardQrAmountDialogBinding.amount.text.toString().toDoubleOrNull()
            amountToPayInDouble = amountDouble
            verveCardQrAmountDialogBinding.amount.text?.clear()
            qrAmountDialogForVerveCard.cancel()
            qrAmountDialogForVerveCard.dismiss()
            qrPinBlock.show(requireActivity().supportFragmentManager, STRING_PIN_BLOCK_DIALOG_TAG)
        }
    }

    private fun getBalance() {
        showCardDialog(
            requireActivity(),
            viewLifecycleOwner,
        ).observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                nfcCardReaderViewModel.initiateNfcPayment(10, 0, it)
            }
        }
        observer = { event ->
            event.getContentIfNotHandled()?.let {
                it.error?.let { error ->
                    Timber.e(error)
                    Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                }
                it.cardData?.let { cardData ->
                    checkBalance(cardData, it.accountType!!)
                }
            }
        }
        observer?.let {
            Timber.e("add obs")
            nfcCardReaderViewModel.iccCardHelperLiveData.observe(viewLifecycleOwner, it)
        }
    }

    private fun checkBalance(
        cardData: CardData,
        accountType: IsoAccountType = IsoAccountType.DEFAULT_UNSPECIFIED,
    ) {
        if (NetPosTerminalConfig.getKeyHolder() == null) {
            Toast.makeText(requireContext(), "Terminal not configured", Toast.LENGTH_LONG).show()
            return
        }

        val hostConfig = HostConfig(
            NetPosTerminalConfig.getTerminalId(),
            NetPosTerminalConfig.connectionData,
            NetPosTerminalConfig.getKeyHolder()!!,
            NetPosTerminalConfig.getConfigData()!!,
        )
        val requestData =
            TransactionRequestData(TransactionType.BALANCE, 0L, accountType = accountType)
        progressDialog!!.setMessage("Checking Balance...")
        progressDialog!!.show()
        val processor = TransactionProcessor(hostConfig)
        // processor.
        val disposable = processor.processTransaction(requireContext(), requestData, cardData)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { response, error ->
                if (progressDialog!!.isShowing) {
                    progressDialog!!.dismiss()
                }
                error?.let {
                    it.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        "Error ${it.localizedMessage}",
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                response?.let {
                    if (it.responseCode == "A3") {
                        DPrefs.removePref(PREF_CONFIG_DATA)
                        DPrefs.removePref(PREF_KEYHOLDER)
                        NetPosTerminalConfig.init(
                            requireContext().applicationContext,
                            configureSilently = true,
                        )
                    }

                    val me = it.buildSMSText("Account Balance Check")

                    val messageString = if (it.isApproved) {
                        "Account Balance:\n " + it.accountBalances.joinToString("\n") { accountBalance ->
                            "${accountBalance.accountType}, ${
                                accountBalance.amount.div(100).formatCurrencyAmount()
                            }"
                        }
                    } else {
                        "${it.responseMessage}(${it.responseCode})"
                    }

                    showMessage(
                        if (it.isApproved) "Approved" else "Declined",
                        messageString,
                        me.toString(),
                    )
                }
            }

        // compositeDisposable.add(disposable)
    }

    private fun showMessage(s: String, vararg messageString: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .apply {
                setTitle(s)
                setMessage(messageString.first())
                setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                    nfcCardReaderViewModel.prepareSMS(messageString.reversed().joinToString("\n"))
                }
                create().show()
            }
    }

    private fun formatPinAndSendToServer(
        pin: String,
        amountDouble: Double?,
        qrData: QrScannedDataModel,
    ) {
        val formattedPadding = stringToBase64(pin).removeSuffix('\n'.toString())
        if (pin.length == 4) {
            amountDouble?.let {
                qrAmountDialogForVerveCard.cancel()
                val qrDataToSendToBackend =
                    PostQrToServerModel(
                        it,
                        qrData.data,
                        merchantId = UtilityParam.STRING_MERCHANT_ID,
                        padding = formattedPadding,
                        naration = requestNarration,
                    )
                scanQrViewModel.setScannedQrIsVerveCard(true)
                scanQrViewModel.saveTheQrToSharedPrefs(qrDataToSendToBackend.copy(orderId = getGUID()))
                scanQrViewModel.postScannedQrRequestToServer(qrDataToSendToBackend)
                observeServerResponse(
                    scanQrViewModel.sendQrToServerResponseVerve,
                    LoadingDialog(),
                    requireActivity().supportFragmentManager,
                ) {
                    addFragmentWithoutRemove(
                        EnterOtpFragment(),
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
