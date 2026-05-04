package com.ossadkowski.crm.mobile.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ossadkowski.crm.mobile.BaseActivity
import com.ossadkowski.crm.mobile.R
import com.ossadkowski.crm.mobile.data.NetworkResult

class ChatActivity : BaseActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter
    
    private var instanceId: Int = -1

    private lateinit var backButton: ImageView
    private lateinit var chatTitle: TextView
    private lateinit var chatType: TextView
    private lateinit var chatStatus: TextView
    private lateinit var recycler: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var chatInput: EditText
    private lateinit var btnSend: Button
    private lateinit var inputContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        initSession()
        instanceId = intent.getIntExtra("ID", -1)
        val initialTitle = intent.getStringExtra("TITLE") ?: "Konwersacja"

        if (instanceId == -1) {
            Toast.makeText(this, "Nie znaleziono konwersacji", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        setupUI(initialTitle)
        observeData()

        viewModel.loadTaskDetail(instanceId)
        viewModel.loadComments(instanceId)
    }

    private fun bindViews() {
        backButton = findViewById(R.id.back_button)
        chatTitle = findViewById(R.id.chat_title)
        chatType = findViewById(R.id.chat_type)
        chatStatus = findViewById(R.id.chat_status)
        recycler = findViewById(R.id.chat_recycler)
        progressBar = findViewById(R.id.progress_bar)
        emptyText = findViewById(R.id.empty_text)
        chatInput = findViewById(R.id.chat_input)
        btnSend = findViewById(R.id.btn_send)
        inputContainer = findViewById(R.id.input_container)
    }

    private fun setupUI(initialTitle: String) {
        chatTitle.text = initialTitle
        
        backButton.setOnClickListener { finish() }

        adapter = ChatAdapter(sessionManager.userId)
        
        val layoutManager = LinearLayoutManager(this)
        // Wiadomości rosną od góry do dołu, chcemy przewijać do końca
        layoutManager.stackFromEnd = true
        recycler.layoutManager = layoutManager
        recycler.adapter = adapter

        btnSend.setOnClickListener {
            val text = chatInput.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendComment(instanceId, text)
                chatInput.text.clear()
            }
        }
    }

    private fun observeData() {
        viewModel.taskDetail.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val detail = result.data ?: return@observe
                chatTitle.text = detail.tytul ?: "Konwersacja"
                chatType.text = detail.typ ?: "brak"
                chatStatus.text = detail.status ?: "brak"
                
                // Zablokowanie wysyłania jeśli Zamknięte
                if (detail.status.equals("Zamknięte", ignoreCase = true) || detail.status.equals("Zakończone", ignoreCase = true)) {
                    chatInput.isEnabled = false
                    btnSend.isEnabled = false
                    chatInput.hint = "Konwersacja zamknięta"
                    inputContainer.alpha = 0.5f
                } else {
                    chatInput.isEnabled = true
                    btnSend.isEnabled = true
                    chatInput.hint = "Napisz wiadomość..."
                    inputContainer.alpha = 1.0f
                }
            }
        }

        viewModel.comments.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    emptyText.visibility = View.GONE
                }
                is NetworkResult.Success -> {
                    progressBar.visibility = View.GONE
                    val items = result.data ?: emptyList()
                    adapter.submitList(items) {
                        // Po zaktualizowaniu listy przewiń na sam dół
                        if (items.isNotEmpty()) {
                            recycler.scrollToPosition(items.size - 1)
                        }
                    }
                    if (items.isEmpty()) {
                        emptyText.visibility = View.VISIBLE
                    } else {
                        emptyText.visibility = View.GONE
                    }
                }
                is NetworkResult.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Błąd pobierania wiadomości", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.sendCommentStatus.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    btnSend.isEnabled = false
                }
                is NetworkResult.Success -> {
                    btnSend.isEnabled = true
                }
                is NetworkResult.Error -> {
                    btnSend.isEnabled = true
                    Toast.makeText(this, "Błąd wysyłania wiadomości", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
