package com.ebookfrenzy.duvproject.ui.entertainer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.data.remote.dto.ContactDto
import com.ebookfrenzy.duvproject.databinding.EmergencyContactFragmentBinding
import com.ebookfrenzy.duvproject.ui.common.collectWhenStarted
import com.ebookfrenzy.duvproject.ui.common.loadingOverlay
import com.ebookfrenzy.duvproject.ui.entertainer.setup.ContactForm
import com.ebookfrenzy.duvproject.ui.entertainer.setup.EmergencyContactViewModel
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupOptions
import com.ebookfrenzy.duvproject.ui.entertainer.setup.handleSetupEvent
import com.ebookfrenzy.duvproject.ui.entertainer.setup.pickOneOf
import com.ebookfrenzy.duvproject.ui.entertainer.setup.requireFilled
import dagger.hilt.android.AndroidEntryPoint

/** Set-up step 4 (Figma "Entertainer's Account Setup > Emergency Contact"): next of kin and a professional contact. */
@AndroidEntryPoint
class EmergencyContactFragment : Fragment() {

    private var _binding: EmergencyContactFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EmergencyContactViewModel by viewModels()

    /** The five boxes of one contact, in the order of [ContactForm]. */
    private data class ContactFields(
        val firstName: EditText, val lastName: EditText, val email: EditText, val phone: EditText,
        val relationship: EditText,
    ) {
        fun read() = ContactForm(
            firstName.text.toString(), lastName.text.toString(), email.text.toString(), phone.text.toString(),
            relationship.text.toString(),
        )

        fun fill(contact: ContactDto?) {
            contact ?: return
            firstName.setText(contact.firstName)
            lastName.setText(contact.lastName)
            email.setText(contact.email)
            phone.setText(contact.phone)
            relationship.setText(contact.relationship)
        }

        /** Server field names under [prefix] ("nextOfKin" / "professionalContact") → boxes. */
        fun byServerName(prefix: String) = mapOf(
            "$prefix.firstName" to firstName, "$prefix.lastName" to lastName, "$prefix.email" to email,
            "$prefix.phone" to phone, "$prefix.relationship" to relationship,
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = EmergencyContactFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loader = loadingOverlay()
        val kin = with(binding) { ContactFields(kinFirstName, kinLastName, kinEmail, kinPhone, kinRelationship) }
        val pro = with(binding) { ContactFields(proFirstName, proLastName, proEmail, proPhone, proRelationship) }

        with(binding) {
            setupHeader.setupHeaderTitle.setText(R.string.emergency_contact)
            setupHeader.setupBack.setOnClickListener { findNavController().navigateUp() }
            kinRelationship.pickOneOf(SetupOptions.relationships)
            proRelationship.pickOneOf(SetupOptions.relationships)
            emergencyUpdate.setOnClickListener {
                // Next of kin is required; the professional contact is optional (checked by the view model).
                if (requireFilled(kinFirstName, kinLastName, kinPhone, kinRelationship)) {
                    viewModel.saveContacts(kin.read(), pro.read())
                }
            }
        }

        val serverFields = kin.byServerName("nextOfKin") + pro.byServerName("professionalContact")
        collectWhenStarted(viewModel.isLoading, loader::setLoading)
        collectWhenStarted(viewModel.setup) { setup ->
            if (setup != null && !viewModel.prefilled) {
                kin.fill(setup.nextOfKin)
                pro.fill(setup.professionalContact)
                viewModel.prefilled = true
            }
        }
        collectWhenStarted(viewModel.events) { event ->
            if (requireActivity().handleSetupEvent(event, serverFields)) findNavController().navigateUp()
        }
        viewModel.load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
