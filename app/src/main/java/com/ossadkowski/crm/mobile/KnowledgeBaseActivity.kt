package com.ossadkowski.crm.mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ossadkowski.crm.mobile.databinding.ActivitySimpleDetailBinding

class KnowledgeBaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySimpleDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleText.text = getString(R.string.knowledge_base_title)
        binding.backButton.setOnClickListener { finish() }
        binding.detailContent.text = getString(R.string.knowledge_base_placeholder)
    }
}
