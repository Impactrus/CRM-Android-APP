package com.ossadkowski.crm.mobile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.SessionManager
import com.ossadkowski.crm.mobile.databinding.ActivityApprovalDetailBinding
import com.ossadkowski.crm.mobile.ui.approval.ApprovalDetailViewModel
import com.ossadkowski.crm.mobile.util.StatusHelper

class ApprovalDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityApprovalDetailBinding
    private val viewModel: ApprovalDetailViewModel by viewModels()
    private lateinit var session: SessionManager
    private var wniosekId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApprovalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        wniosekId = intent.getIntExtra("wniosek_id", -1)
        val wniosekNum = intent.getStringExtra("wniosek_num") ?: wniosekId.toString()

        if (wniosekId == -1) {
            finish()
            return
        }

        binding.toolbarTitle.text = "Szczegóły wniosku #$wniosekNum"
        binding.btnBack.setOnClickListener { finish() }

        viewModel.detail.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            when (result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    val d = result.data ?: return@observe
                    binding.detailUsername.text = d.username ?: "-"
                    binding.detailCreatedAt.text = d.createdAt?.take(10) ?: "-"
                    binding.detailOddzial.text = d.oddzial ?: "-"
                    binding.detailStanowisko.text = d.stanowisko ?: "-"
                    binding.detailManagerName.text = d.managerName ?: d.managerId?.toString() ?: "-"

                    binding.detailTyp.text = d.typ ?: "-"
                    binding.detailOdDo.text = d.odDo ?: "-"
                    binding.detailIloscDni.text = "${d.iloscDni ?: "-"}"
                    binding.detailGodziny.text = "${d.godziny ?: "-"}"
                    binding.detailPowod.text = d.powod ?: "-"

                    // Zastępstwo
                    binding.detailZastepca.text = d.zastepstwoUsername ?: "Brak zastępcy"
                    val zStatus = d.zastepstwoStatus
                    if (zStatus != null) {
                        binding.detailZastepstwoStatus.text = zStatus
                        StatusHelper.applyStatusStyle(binding.detailZastepstwoStatus, zStatus)
                        binding.detailZastepstwoWarning.visibility =
                            if (zStatus.contains("Brak", true) || zStatus.contains("Oczekuje", true))
                                View.VISIBLE else View.GONE
                    } else {
                        binding.detailZastepstwoStatus.text = "Brak decyzji"
                        binding.detailZastepstwoWarning.visibility = View.VISIBLE
                    }

                    // Akceptacja managera
                    binding.detailManagerApprovalName.text = d.managerName ?: "-"
                    binding.detailManagerApprovedAt.text = d.managerApprovedAt?.take(16)?.replace("T", " ") ?: "-"

                    // Poprzedni komentarz
                    val prevKomentarz = d.komentarzManager ?: d.komentarzHr ?: ""
                    if (prevKomentarz.isNotBlank()) {
                        binding.inputKomentarz.setText(prevKomentarz)
                    }
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, "Błąd wczytywania: ${result.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        binding.btnApprove.setOnClickListener {
            val komentarz = binding.inputKomentarz.text.toString().trim()
            setButtonsEnabled(false)
            viewModel.approve(wniosekId, session.userId, session.role, komentarz) { success ->
                runOnUiThread {
                    setButtonsEnabled(true)
                    if (success) {
                        Toast.makeText(this, "Wniosek zaakceptowany", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this, "Błąd akceptacji", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnReject.setOnClickListener {
            val komentarz = binding.inputKomentarz.text.toString().trim()
            setButtonsEnabled(false)
            viewModel.reject(wniosekId, session.userId, session.role, komentarz) { success ->
                runOnUiThread {
                    setButtonsEnabled(true)
                    if (success) {
                        Toast.makeText(this, "Wniosek odrzucony", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this, "Błąd odrzucenia", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.loadDetail(wniosekId)
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnApprove.isEnabled = enabled
        binding.btnReject.isEnabled = enabled
    }
}
