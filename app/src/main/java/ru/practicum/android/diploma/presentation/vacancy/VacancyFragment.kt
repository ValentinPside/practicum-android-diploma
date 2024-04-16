package ru.practicum.android.diploma.presentation.vacancy

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.app.App
import ru.practicum.android.diploma.databinding.FragmentVacancyBinding
import ru.practicum.android.diploma.domain.models.VacancyDetail
import ru.practicum.android.diploma.presentation.Factory
import ru.practicum.android.diploma.presentation.vacancy.models.VacancyViewState
import ru.practicum.android.diploma.util.SalaryUtil

class VacancyFragment : Fragment(R.layout.fragment_vacancy) {

    companion object {
        private const val RADIUS = 8
    }

    private val vacancyId: String? by lazy { requireArguments().getString("id") }

    private var _binding: FragmentVacancyBinding? = null
    private val binding get() = _binding!!
    var name: String = "Имя"
    private var phones: String = "+7 (985) 000-00-00"
    private var email: String = "user@example.com"
    private var comment: String = "Заполнить анкету по форме можно на нашем сайте"
    private var skillLength = 0
    private var skillsListLength = 0
    private var activityRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$phones")
            startActivity(intent)
        }
    }

    private val viewModel by viewModels<VacancyViewModel> {
        Factory {
            (requireContext().applicationContext as App).appComponent.vacancyComponent()
                .create(requireNotNull(vacancyId)).viewModel()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVacancyBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAddToFavorites.setOnClickListener {
            viewModel.clickFavorite()
        }

        binding.vacancyToolbars.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.buttonShare.setOnClickListener {
            viewModel.shareVacancy()
        }

        viewModel.observeUi().observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun render(state: VacancyViewState) {
        binding.fragmentNotifications.isVisible = when (state) {
            is VacancyViewState.Content -> true
            else -> false
        }
        binding.clPlaceholderTrouble.isVisible = when (state) {
            is VacancyViewState.ServerError -> true
            else -> false
        }
        binding.progressBar.isVisible = when (state) {
            is VacancyViewState.Loading -> true
            else -> false
        }

        when (state) {
            is VacancyViewState.Content -> {
                renderVacancyDetail(state.vacancyDetail)

                if (state.isFavorite) {
                    binding.buttonAddToFavorites.setImageResource(R.drawable.favorite_vacancy_drawable_fill)
                } else {
                    binding.buttonAddToFavorites.setImageResource(R.drawable.favorite_vacancy_drawable_empty)
                }
            }

            else -> null
        }
    }

    private fun renderVacancyDetail(vacancy: VacancyDetail) {
        binding.jobName.text = vacancy.name
        vacancy.salary?.let {
            binding.jobSalary.text = SalaryUtil.formatSalary(requireContext(), vacancy.salary)
        }
        binding.companyName.text = vacancy.employer?.name ?: ""
        binding.neededExperience.text = vacancy.experience ?: ""
        binding.companyCity.text = vacancy.area ?: ""
        binding.jobTime.text = vacancy.employment ?: ""
        binding.contactPersonEmailData.text = email
        binding.contactPersonData.text = name
        binding.contactPersonEmailData.setOnClickListener {
            val i = Intent(Intent.ACTION_SEND)
            i.setType("message/rfc822")
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            try {
                startActivity(Intent.createChooser(i, getString(R.string.SendingMessage)))
            } catch (ex: ActivityNotFoundException) {
                println(ex)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.mail_clients_not_installed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        with(binding) {
            if (vacancy.keySkills.isNullOrEmpty()) {
                keySkillsRecyclerView.visibility = View.GONE
                binding.keySkills.visibility = View.GONE
            } else {
                keySkillsRecyclerView.visibility = View.VISIBLE
                binding.keySkills.visibility = View.VISIBLE
                var skills = "• "
                skillsListLength = vacancy.keySkills.count()
                vacancy.keySkills.forEach { skill ->
                    skillLength = skill.length
                    while (skillLength != 0) {
                        skill.forEach {
                            skills += it
                            skillLength--
                        }
                        skillsListLength--
                    }
                    if (skillsListLength != 0) {
                        skills += "\n• "
                    }
                }
                keySkillsRecyclerView.text = skills
            }

            binding.contactPersonPhoneData.text = phones
            binding.contactPersonPhoneData.setOnClickListener {
                activityRequest.launch(Manifest.permission.CALL_PHONE)
            }
            binding.contactCommentData.text = comment

            vacancy.employer?.let {
                Glide.with(requireContext())
                    .load(vacancy.employer.logoUrls)
                    .placeholder(R.drawable.placeholder_company_icon)
                    .fitCenter()
                    .transform(RoundedCorners(RADIUS))
                    .into(binding.ivCompany)
            }

            vacancy.description?.let {
                val markwon = Markwon.builder(requireContext())
                    .usePlugin(HtmlPlugin.create())
                    .build()
                markwon.setMarkdown(binding.vacancyDescription, it)
            }
        }
    }
}
