package com.ebookfrenzy.duvproject.ui.entertainer.setup

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.data.remote.dto.EntertainerSetupDto
import com.ebookfrenzy.duvproject.ui.common.LoadingOverlay
import com.ebookfrenzy.duvproject.ui.common.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint

/**
 * The three set-up steps Figma draws as cards rather than screens: Bank Account Details, Youtube Channel and
 * Valid Identification (layouts dialog_setup_*.xml). Pre-filled with what's saved; Update saves to the backend.
 * The bank card asks the bank for the account holder's name as soon as a bank and 10-digit number are in.
 */
@AndroidEntryPoint
class SetupDialogFragment : DialogFragment() {

    enum class Step(val layout: Int) {
        BANK(R.layout.dialog_setup_bank),
        YOUTUBE(R.layout.dialog_setup_youtube),
        IDENTIFICATION(R.layout.dialog_setup_identification),
    }

    private val step get() = Step.valueOf(requireArguments().getString(ARG_STEP)!!)
    private val viewModel: SetupCardViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(step.layout, container, false)

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // Card width: the screen minus @dimen/setup_dialog_margin on each side (311dp on Figma's 360dp phone).
            val margin = resources.getDimensionPixelSize(R.dimen.setup_dialog_margin)
            setLayout(resources.displayMetrics.widthPixels - 2 * margin, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val loader = LoadingOverlay(requireContext())
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) = loader.hide()
        })
        fun field(id: Int): EditText = view.findViewById(id)

        val required: List<EditText>
        val serverFields: Map<String, EditText>
        val save: () -> Unit
        val prefill: (EntertainerSetupDto) -> Unit
        when (step) {
            Step.BANK -> {
                val bank = field(R.id.bank_name)
                val number = field(R.id.bank_account_number)
                val name = field(R.id.bank_account_name)
                val status = view.findViewById<TextView>(R.id.bank_lookup_status)
                val lookUp = { viewModel.lookUp(bank.text.toString(), number.text.toString()) }
                bank.pickSearchable(onPicked = { lookUp() }) {
                    val banks = viewModel.banks.value?.map { it.name }
                    if (banks == null) {
                        Toast.makeText(requireContext(), R.string.setup_bank_loading, Toast.LENGTH_SHORT).show()
                        viewModel.loadBanks()
                    }
                    banks
                }
                number.doAfterTextChanged { lookUp() }
                collectWhenStarted(viewModel.lookup) { showLookup(it, name, number, status) }
                viewModel.loadBanks()

                required = listOf(bank, number)
                serverFields = mapOf("bankCode" to bank, "accountNumber" to number)
                save = { viewModel.saveBank(bank.text.toString(), number.text.toString()) }
                prefill = { setup ->
                    setup.bankAccount?.let {
                        bank.setText(it.bankName)
                        number.setText(it.accountNumber)
                        name.setText(it.accountName)
                        lookUp()
                    }
                }
            }
            Step.YOUTUBE -> {
                val url = field(R.id.youtube_url)
                required = listOf(url)
                serverFields = mapOf("youtubeUrl" to url)
                save = { viewModel.saveYoutube(url.text.toString()) }
                prefill = { setup -> setup.youtubeUrl?.let(url::setText) }
            }
            Step.IDENTIFICATION -> {
                val type = field(R.id.id_type).apply { pickOneOf(SetupOptions.idTypes.labels) }
                val number = field(R.id.id_number)
                val issued = field(R.id.id_issue_date).apply { pickDate() }
                val expires = field(R.id.id_expiry_date).apply { pickDate() }
                // Expiry stays optional: NIN slips and voter's cards don't expire.
                required = listOf(type, number, issued)
                serverFields = mapOf(
                    "documentType" to type, "idNumber" to number, "issueDate" to issued, "expiryDate" to expires,
                )
                save = {
                    viewModel.saveIdentification(
                        type.text.toString(), number.text.toString(), issued.text.toString(), expires.text.toString(),
                    )
                }
                prefill = { setup ->
                    setup.identification?.let {
                        type.setText(SetupOptions.idTypes.labelFor(it.documentType))
                        number.setText(it.idNumber)
                        issued.setText(SetupOptions.isoToUi(it.issueDate))
                        expires.setText(SetupOptions.isoToUi(it.expiryDate))
                    }
                }
            }
        }

        view.findViewById<View>(R.id.dialog_back).setOnClickListener { dismiss() }
        view.findViewById<View>(R.id.dialog_update).setOnClickListener {
            if (requireFilled(*required.toTypedArray())) save()
        }

        collectWhenStarted(viewModel.isLoading, loader::setLoading)
        collectWhenStarted(viewModel.setup) { setup ->
            if (setup != null && !viewModel.prefilled) {
                prefill(setup)
                viewModel.prefilled = true
            }
        }
        collectWhenStarted(viewModel.events) { event ->
            if (requireActivity().handleSetupEvent(event, serverFields)) {
                setFragmentResult(RESULT_SAVED, bundleOf())
                dismiss()
            }
        }
        viewModel.load()
    }

    /** Shows the bank's answer: the holder's name in the (read-only) name field, plus a status line. */
    private fun showLookup(lookup: AccountLookup, name: EditText, number: EditText, status: TextView) {
        val context = requireContext()
        fun status(@StringRes text: Int?, message: String? = null, @ColorRes color: Int = R.color.setup_field_hint) {
            status.isVisible = text != null || message != null
            status.text = message ?: text?.let(context::getString)
            status.setTextColor(ContextCompat.getColor(context, color))
        }
        when (lookup) {
            AccountLookup.Idle -> {
                name.text = null
                status(null)
            }
            AccountLookup.Checking -> {
                name.text = null
                status(R.string.setup_bank_checking)
            }
            is AccountLookup.Found -> {
                name.setText(lookup.accountName)
                number.error = null
                status(R.string.setup_bank_verified, color = R.color.setup_status_ok)
            }
            is AccountLookup.Failed -> {
                name.text = null
                status(null, lookup.message, R.color.setup_status_error)
            }
        }
    }

    companion object {
        private const val ARG_STEP = "step"

        /** Sent to the fragment manager that showed the card after a successful save. */
        const val RESULT_SAVED = "setup_step_saved"

        fun show(fragmentManager: FragmentManager, step: Step) {
            SetupDialogFragment().apply { arguments = bundleOf(ARG_STEP to step.name) }
                .show(fragmentManager, "setup_${step.name.lowercase()}")
        }
    }
}
