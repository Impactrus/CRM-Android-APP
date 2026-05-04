package com.ossadkowski.crm.mobile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.HrOrgItemDto
import com.ossadkowski.crm.mobile.databinding.ActivityHrSchematBinding

class HrSchematOrgActivity : BaseActivity() {

    private lateinit var binding: ActivityHrSchematBinding
    private val viewModel: HrOrgViewModel by viewModels()
    private lateinit var adapter: HrOrgAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHrSchematBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { 
            if (!viewModel.navigateBack()) {
                finish()
            }
        }

        // Obsługa fizycznego przycisku wstecz
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!viewModel.navigateBack()) {
                    finish()
                }
            }
        })

        adapter = HrOrgAdapter { child ->
            if (child.children.isNotEmpty()) {
                viewModel.navigateTo(child)
            } else {
                Toast.makeText(this, "${child.displayName} nie ma podwładnych", Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerSubordinates.adapter = adapter

        viewModel.state.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    result.data?.let { displayManager(it) }
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.loadStructure()
    }

    private fun displayManager(manager: HrOrgItemDto) {
        binding.tvManagerName.text = manager.displayName
        binding.tvManagerPosition.text = manager.workpost
        binding.tvBadgeDept.text = manager.dzial
        binding.tvManagerInitials.text = getInitials(manager.displayName ?: "")
        
        binding.tvSubLabel.text = "Bezpośredni podwładni (${manager.children.size})"
        adapter.submitList(manager.children)
    }

    private fun getInitials(name: String): String {
        val parts = name.split(" ")
        if (parts.size >= 2) {
            return (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
        return name.take(2).uppercase()
    }
}
