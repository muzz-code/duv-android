package com.ebookfrenzy.duvproject.ui.entertainer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ebookfrenzy.duvproject.R
import com.ebookfrenzy.duvproject.data.remote.dto.EntertainerSetupDto
import com.ebookfrenzy.duvproject.databinding.EntertainerProfileFragmentBinding
import com.ebookfrenzy.duvproject.ui.common.collectWhenStarted
import com.ebookfrenzy.duvproject.ui.common.loadingOverlay
import com.ebookfrenzy.duvproject.ui.entertainer.setup.NigeriaLocations
import com.ebookfrenzy.duvproject.ui.entertainer.setup.ProfileForm
import com.ebookfrenzy.duvproject.ui.entertainer.setup.ProfileSetupViewModel
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupEvent
import com.ebookfrenzy.duvproject.ui.entertainer.setup.SetupOptions
import com.ebookfrenzy.duvproject.ui.entertainer.setup.compressForUpload
import com.ebookfrenzy.duvproject.ui.entertainer.setup.decodePhoto
import com.ebookfrenzy.duvproject.ui.entertainer.setup.handleSetupEvent
import com.ebookfrenzy.duvproject.ui.entertainer.setup.labelFor
import com.ebookfrenzy.duvproject.ui.entertainer.setup.labels
import com.ebookfrenzy.duvproject.ui.entertainer.setup.pickManyOf
import com.ebookfrenzy.duvproject.ui.entertainer.setup.pickOneOf
import com.ebookfrenzy.duvproject.ui.entertainer.setup.requireFilled
import com.ebookfrenzy.duvproject.ui.entertainer.setup.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Set-up step 1 (Figma "Entertainer's Account Setup > Entertainer Profile"). */
@AndroidEntryPoint
class EntertainerProfileFragment : Fragment() {

    private var _binding: EntertainerProfileFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileSetupViewModel by viewModels()

    /** System photo picker: no storage permission needed (falls back to the file chooser on older phones). */
    private val pickPhoto = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri == null) return@registerForActivityResult
        val context = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            val jpeg = withContext(Dispatchers.IO) { compressForUpload(context, uri) }
            if (jpeg == null) requireActivity().showSnackbar(R.string.setup_photo_failed)
            else viewModel.uploadPhoto(jpeg)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = EntertainerProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        val loader = loadingOverlay()
        with(binding) {
            setupHeader.setupHeaderTitle.setText(R.string.entertainer_profile)
            setupHeader.setupBack.setOnClickListener { findNavController().navigateUp() }

            val openPicker = View.OnClickListener {
                pickPhoto.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            }
            profilePhoto.setOnClickListener(openPicker)
            profilePhotoCamera.setOnClickListener(openPicker)

            profileType.pickOneOf(SetupOptions.entertainmentTypes.labels)
            // State first, then the LGAs of that state. Changing the state clears an LGA from another state.
            profileLocation.pickOneOf(onPicked = { state ->
                if (profileLga.text.toString() !in NigeriaLocations.lgas(context, state)) profileLga.text = null
            }) { NigeriaLocations.states(context) }
            profileLga.pickOneOf {
                val state = profileLocation.text.toString()
                if (state.isBlank()) {
                    profileLocation.error = getString(R.string.setup_pick_state_first)
                    Toast.makeText(context, R.string.setup_pick_state_first, Toast.LENGTH_SHORT).show()
                    null
                } else {
                    NigeriaLocations.lgas(context, state)
                }
            }
            profileBaseCharge.pickOneOf(SetupOptions.chargeLabels)
            profileHighestCharge.pickOneOf(SetupOptions.chargeLabels)
            profileTravel.pickOneOf(SetupOptions.yesNo)
            profileYear.pickOneOf(SetupOptions.years)
            profileAvailableFor.pickManyOf(SetupOptions.eventTypes.labels)

            profileUpdate.setOnClickListener {
                val filled = requireFilled(
                    profileStageName, profileType, profileLocation, profileLga, profileBaseCharge,
                    profileHighestCharge, profileTravel, profileYear, profileAvailableFor,
                )
                if (filled) viewModel.saveProfile(form())
            }
        }

        val serverFields = with(binding) {
            mapOf(
                "stageName" to profileStageName, "entertainerType" to profileType, "yearStarted" to profileYear,
                "baseCharge" to profileBaseCharge, "highestCharge" to profileHighestCharge,
                "state" to profileLocation, "cityLga" to profileLga, "availableFor" to profileAvailableFor,
                "about" to profileAbout,
            )
        }
        collectWhenStarted(viewModel.isLoading, loader::setLoading)
        collectWhenStarted(viewModel.setup) { setup ->
            if (setup != null && !viewModel.prefilled) {
                prefill(setup)
                viewModel.prefilled = true
            }
        }
        collectWhenStarted(viewModel.photo) { bytes ->
            val size = resources.getDimensionPixelSize(R.dimen.setup_photo_size)
            bytes?.let { decodePhoto(it, size) }?.let(binding.profilePhoto::setImageBitmap)
        }
        collectWhenStarted(viewModel.events) { event ->
            val saved = requireActivity().handleSetupEvent(event, serverFields)
            if (saved && event == SetupEvent.Saved(R.string.setup_saved_profile)) findNavController().navigateUp()
        }
        viewModel.load()
    }

    private fun form() = with(binding) {
        ProfileForm(
            stageName = profileStageName.text.toString(),
            type = profileType.text.toString(),
            state = profileLocation.text.toString(),
            lga = profileLga.text.toString(),
            baseCharge = profileBaseCharge.text.toString(),
            highestCharge = profileHighestCharge.text.toString(),
            travel = profileTravel.text.toString(),
            year = profileYear.text.toString(),
            availableFor = profileAvailableFor.text.toString(),
            about = profileAbout.text.toString(),
        )
    }

    /** Fills the form with what's already saved (fields never saved stay empty, showing their hints). */
    private fun prefill(setup: EntertainerSetupDto) = with(binding) {
        if (setup.stageName == null) return@with
        profileStageName.setText(setup.stageName)
        profileType.setText(SetupOptions.entertainmentTypes.labelFor(setup.entertainerType))
        profileLocation.setText(setup.state)
        profileLga.setText(setup.cityLga)
        setup.baseCharge?.let { profileBaseCharge.setText(SetupOptions.naira(it.toLong())) }
        setup.highestCharge?.let { profileHighestCharge.setText(SetupOptions.naira(it.toLong())) }
        profileTravel.setText(if (setup.travelsToOtherStates) SetupOptions.YES else SetupOptions.NO)
        profileYear.setText(setup.yearStarted?.toString())
        // Shown in the list's own order (the server keeps them as an unordered set).
        val chosen = setup.availableFor.orEmpty()
        profileAvailableFor.setText(
            SetupOptions.eventTypes.filter { it.value in chosen }.labels.joinToString(SetupOptions.SEPARATOR),
        )
        profileAbout.setText(setup.about)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
