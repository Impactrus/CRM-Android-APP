package com.ossadkowski.crm.mobile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.databinding.ActivityHrPrawoPracyBinding

class HrPrawoPracyActivity : BaseActivity() {

    private lateinit var binding: ActivityHrPrawoPracyBinding
    private val viewModel: HrPrawoPracyViewModel by viewModels()
    private val adapter = HrPrawoPracyAdapter { item, action ->
        handleAction(item, action)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHrPrawoPracyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.data.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    result.data?.let { adapter.submitList(it) }
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.loadAll()
    }

    private fun handleAction(item: com.ossadkowski.crm.mobile.data.model.HrPrawoPracyTypDto, action: String) {
        when (action) {
            "EDIT" -> showEditDialog(item)
            "HISTORY" -> Toast.makeText(this, "Wyświetlam historię dla: ${item.nazwa}", Toast.LENGTH_SHORT).show()
            "DELETE" -> Toast.makeText(this, "Dezaktywacja: ${item.nazwa}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditDialog(item: com.ossadkowski.crm.mobile.data.model.HrPrawoPracyTypDto) {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val dialogBinding = com.ossadkowski.crm.mobile.databinding.DialogHrPrawoPracyEditBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvModalTitle.text = "Nowa wersja — ${item.nazwa}"
        dialogBinding.etLimit.setText(item.limitRoczny?.toString() ?: "")
        dialogBinding.etBasis.setText(item.podstawaPrawna ?: "")

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            Toast.makeText(this, "Zapisywanie wersji... (Funkcja niedostępna w API)", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
