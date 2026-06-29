package com.ossadkowski.crm.callhistory

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ossadkowski.crm.callhistory.databinding.ActivityVisitPanelBinding
import java.text.SimpleDateFormat
import java.util.*

class VisitPanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVisitPanelBinding
    private var clientId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisitPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clientId = intent.getStringExtra("CLIENT_ID")
        val clientName = intent.getStringExtra("CLIENT_NAME") ?: "Nieznany"
        val clientAddress = intent.getStringExtra("CLIENT_ADDRESS") ?: ""

        binding.tvClientName.text = clientName
        binding.tvClientAddress.text = clientAddress
        binding.tvVisitTime.text = "Wizyta: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())}"

        // Load existing note if any
        val manager = OrganizerManager(this)
        val item = manager.getItems().find { it.id == clientId }
        if (item != null && item.lastVisitNote.isNotBlank()) {
            binding.etVisitDescription.setText(item.lastVisitNote)
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }

        binding.btnSaveVisit.setOnClickListener {
            val description = binding.etVisitDescription.text.toString().trim()
            val result = binding.etVisitResult.text.toString().trim()

            val fullNote = buildString {
                if (description.isNotEmpty()) append(description)
                if (result.isNotEmpty()) {
                    if (isNotEmpty()) append("\n---\nUstalenia: ")
                    append(result)
                }
            }

            if (fullNote.isEmpty()) {
                Toast.makeText(this, "Wprowadź opis wizyty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cid = clientId
            if (cid != null) {
                manager.updateNote(cid, fullNote)
                Toast.makeText(this, "Wizyta zapisana!", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
