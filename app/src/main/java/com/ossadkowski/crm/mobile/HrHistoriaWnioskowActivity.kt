package com.ossadkowski.crm.mobile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.databinding.ActivityHrHistoriaBinding
import com.ossadkowski.crm.mobile.ui.dashboard.WnioskiAdapter
import com.ossadkowski.crm.mobile.util.addDebouncedTextListener

class HrHistoriaWnioskowActivity : BaseActivity() {

    private lateinit var binding: ActivityHrHistoriaBinding
    private val viewModel: HrHistoriaViewModel by viewModels()
    private lateinit var usersAdapter: HrUsersAdapter
    private lateinit var wnioskiAdapter: WnioskiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHrHistoriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        // Menedżery list
        binding.usersRecycler.layoutManager = LinearLayoutManager(this)
        binding.historyRecycler.layoutManager = LinearLayoutManager(this)

        // Adapter wyników wyszukiwania (Użytkownicy)
        usersAdapter = HrUsersAdapter { selectedUser ->
            // Akcja po wybraniu pracownika
            binding.searchInput.setText(selectedUser.nazwa)
            binding.searchInput.clearFocus()
            binding.usersRecycler.visibility = View.GONE
            binding.emptyState.visibility = View.GONE
            binding.historyContainer.visibility = View.VISIBLE
            binding.selectedUserName.text = "Wnioski pracownika: ${selectedUser.nazwa}"
            
            viewModel.loadHistoryForUser(selectedUser.id)
        }
        binding.usersRecycler.adapter = usersAdapter

        // Adapter wniosków Historycznych
        wnioskiAdapter = WnioskiAdapter { wniosek ->
            // Historia click action e.g. details
            Toast.makeText(this, "Szczegóły wniosku: ${wniosek.typ}", Toast.LENGTH_SHORT).show()
        }
        binding.historyRecycler.adapter = wnioskiAdapter

        // Nasłuchiwanie paska wyszukiwania
        binding.searchInput.addDebouncedTextListener(lifecycleScope) { query ->
            if (query.isNotBlank()) {
                binding.emptyState.visibility = View.GONE
                binding.historyContainer.visibility = View.GONE
                binding.usersRecycler.visibility = View.VISIBLE
                viewModel.filterUsers(query)
            } else {
                binding.usersRecycler.visibility = View.GONE
                binding.historyContainer.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            }
        }

        // Obserwowanie użytkowników z ViewModelu
        viewModel.users.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    usersAdapter.submitList(result.data ?: emptyList())
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Obserwowanie historii wniosków wybranego pracownika
        viewModel.history.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val data = result.data?.items ?: emptyList()
                    wnioskiAdapter.submitList(data)
                    if (data.isEmpty()) {
                        Toast.makeText(this, "Ten pracownik nie ma jeszcze żadnych wniosków.", Toast.LENGTH_SHORT).show()
                    }
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Zainicjuj pobieranie słownika pracowników do filtra
        viewModel.loadUsers()
    }
}
