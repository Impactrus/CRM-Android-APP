package com.ossadkowski.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.LimitKredytowyDetailDto
import com.ossadkowski.app.databinding.ActivityLimitKredytowyDetailBinding
import com.ossadkowski.app.ui.limitykredytowe.LimitKredytowyDetailViewModel
import java.text.NumberFormat
import java.util.Locale

class LimitKredytowyDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityLimitKredytowyDetailBinding
    private val viewModel: LimitKredytowyDetailViewModel by viewModels()

    private val currencyFormat = NumberFormat.getNumberInstance(Locale("pl", "PL")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLimitKredytowyDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        val id = intent.getIntExtra("id", 0)

        viewModel.detail.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            when (result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> result.data?.let { bindDetail(it) }
                is NetworkResult.Error -> {
                    Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.loadDetail(id)
    }

    private fun bindDetail(d: LimitKredytowyDetailDto) {
        binding.detailKontrahentNazwa.text = d.kontrahentNazwa ?: getString(R.string.lk_no_data)
        binding.detailAccountNum.text = d.kontrahentAccountNum ?: ""
        binding.detailCreatedAt.text = d.createdAt?.take(10) ?: ""

        applyStatusBadge(binding.detailStatus, d.status)

        // Limits
        binding.detailWnioskowanyLimit.text = formatCurrency(d.wnioskowanyLimit)
        binding.detailObecnyLimit.text = formatCurrency(d.obecnyLimit)
        binding.detailPozostalyKredyt.text = formatCurrency(d.pozostalyKredyt)

        // Finanse
        binding.detailSaldo.text = formatCurrency(d.saldo)
        binding.detailZamowione.text = formatCurrency(d.zamowione)
        binding.detailWartoscZabezpieczen.text = formatCurrency(d.wartoscZabezpieczen)
        binding.detailZadluzenie.text = formatCurrency(d.zadluzeniePrzeterminowane)
        binding.detailNakladyPoprzedni.text = formatCurrency(d.nakladyPoprzedni)
        binding.detailNakladyBiezacy.text = formatCurrency(d.nakladyBiezacy)
        binding.detailPrzychodyPoprzedni.text = formatCurrency(d.przychodyPoprzedni)
        binding.detailPrzychodyBiezacy.text = formatCurrency(d.przychodyBiezacy)

        // Details / uwagi
        bindTextOrHide(binding.detailOpisZabezpieczen, binding.labelOpisZabezpieczen, d.opisZabezpieczen)
        bindTextOrHide(binding.detailNoweZabezpieczenia, binding.labelNoweZabezpieczenia, d.noweZabezpieczenia)
        bindTextOrHide(binding.detailDodatkoweDochody, binding.labelDodatkoweDochody, d.dodatkoweDochody)
        bindTextOrHide(binding.detailZobowiazania, binding.labelZobowiazania, d.zobowiazania)
        bindTextOrHide(binding.detailUwagi, binding.labelUwagi, d.uwagi)

        binding.detailPotwierdzone.text = if (d.potwierdzonePrzeterminowane == true) getString(R.string.lk_yes) else getString(R.string.lk_no)
        binding.detailRozliczenie.text = if (d.rozliczeniePlonami == true) getString(R.string.lk_yes) else getString(R.string.lk_no)

        // Decision card
        if (!d.komentarzDecyzja.isNullOrBlank() || d.approvedAt != null) {
            binding.cardDecyzja.visibility = View.VISIBLE
            binding.detailKomentarzDecyzja.text = d.komentarzDecyzja ?: getString(R.string.lk_no_data)
            binding.detailApprovedAt.text = d.approvedAt?.take(10) ?: ""
        }
    }

    private fun formatCurrency(value: Double?): String {
        if (value == null) return getString(R.string.lk_no_data)
        return "${currencyFormat.format(value)} PLN"
    }

    private fun bindTextOrHide(textView: TextView, label: TextView, value: String?) {
        if (value.isNullOrBlank()) {
            textView.visibility = View.GONE
            label.visibility = View.GONE
        } else {
            textView.text = value
            textView.visibility = View.VISIBLE
            label.visibility = View.VISIBLE
        }
    }

    private fun applyStatusBadge(tv: TextView, status: String?) {
        tv.text = status ?: getString(R.string.lk_no_data)
        val s = status?.lowercase() ?: ""
        val (bgColor, textColor) = when {
            s.contains("szkic") || s.contains("draft") -> R.color.status_draft_bg to R.color.status_draft_text
            s.contains("zatwierdzony") || s.contains("approved") -> R.color.status_approved_bg to R.color.status_approved_text
            s.contains("odrzucony") || s.contains("rejected") -> R.color.status_rejected_bg to R.color.status_rejected_text
            s.contains("oczekuj") || s.contains("pending") || s.contains("review") -> R.color.status_pending_bg to R.color.status_pending_text
            else -> R.color.status_info_bg to R.color.status_info_text
        }
        tv.setBackgroundColor(getColor(bgColor))
        tv.setTextColor(getColor(textColor))
    }
}
