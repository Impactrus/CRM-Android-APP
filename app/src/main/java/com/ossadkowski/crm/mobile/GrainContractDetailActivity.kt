package com.ossadkowski.crm.mobile

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.GrainContractDetail
import com.ossadkowski.crm.mobile.databinding.ActivityGrainContractDetailBinding
import com.ossadkowski.crm.mobile.ui.sales.GrainContractsViewModel
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import android.content.Intent
import androidx.core.content.FileProvider
import android.net.Uri

class GrainContractDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityGrainContractDetailBinding
    private val viewModel: GrainContractsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrainContractDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val contractId = intent.getIntExtra("CONTRACT_ID", -1)
        if (contractId == -1) {
            Toast.makeText(this, "Błąd: Nieprawidłowe ID umowy", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        observeViewModel()
        
        binding.btnBack.setOnClickListener { finish() }
        binding.btnPrint.setOnClickListener { startPrintJob() }
        
        viewModel.loadContractDetail(contractId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun observeViewModel() {
        viewModel.contractDetail.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    result.data?.let { displayDetail(it) }
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Błąd: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun displayDetail(detail: GrainContractDetail) {
        binding.textTitle.text = "Umowa ${detail.nr ?: ""}"
        binding.textCreatedInfo.text = "Utworzona ${detail.createdAt?.split("T")?.firstOrNull() ?: ""} przez ${detail.createdByUsername ?: ""}"
        
        // Status mapping
        when (detail.status?.uppercase()) {
            "DRAFT" -> {
                binding.textStatus.text = "Szkic"
                binding.textStatus.setBackgroundResource(R.drawable.bg_status_draft)
            }
            "PENDING" -> {
                binding.textStatus.text = "Oczekujący"
                binding.textStatus.setBackgroundResource(R.drawable.bg_status_pending)
            }
            "APPROVED" -> {
                binding.textStatus.text = "Zatwierdzony"
                binding.textStatus.setBackgroundResource(R.drawable.bg_status_approved)
            }
            "REJECTED" -> {
                binding.textStatus.text = "Odrzucony"
                binding.textStatus.setBackgroundResource(R.drawable.bg_status_rejected)
            }
            else -> {
                binding.textStatus.text = detail.status ?: "Nieznany"
                binding.textStatus.setBackgroundResource(R.drawable.bg_status_draft)
            }
        }

        binding.valNrKontraktu.text = detail.nr ?: "-"
        binding.valDataZawarcia.text = detail.dataZawarcia?.split("T")?.firstOrNull() ?: "-"
        binding.valDataZobowiazania.text = detail.dataZobowiazania?.split("T")?.firstOrNull() ?: "-"
        
        binding.valKontrahent.text = detail.kontrahent ?: "Nieznany"
        binding.valKontoDostawcy.text = detail.kontoDostawcy ?: "-"
        binding.valAdresDostawy.text = detail.deliveryAddress ?: "Brak adresu"
        
        binding.valTowar.text = detail.towar ?: "-"
        binding.valIlosc.text = "${detail.ilosc ?: 0.0}"
        binding.valCena.text = "${detail.cena ?: 0.0} PLN"
        
        binding.valWarunekPlatnosci.text = detail.paymentTermId ?: "-"
    }

    private fun startPrintJob() {
        binding.webViewContainer.removeAllViews()
        val webView = WebView(this)
        binding.webViewContainer.addView(webView)
        
        val width = 595 // A4 width
        val height = 842 // A4 min height
        
        webView.layout(0, 0, width, height)
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                view.postDelayed({
                    view.measure(
                        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    view.layout(0, 0, view.measuredWidth, view.measuredHeight)
                    createWebPrintJob(view)
                }, 1000) // Zwiększone opóźnienie dla pewności
            }
        }

        // Generujemy prosty HTML z danymi umowy, aby wydruk był idealnie sformatowany pod A4
        val htmlContent = """
            <html>
            <head>
                <meta name="viewport" content="width=595, initial-scale=1.0">
                <style>
                    @page { size: A4; margin: 0; }
                    body { font-family: sans-serif; padding: 40px; color: #2D3748; width: 515px; margin: 0 auto; }
                    .header { border-bottom: 2px solid #E2E8F0; padding-bottom: 10px; margin-bottom: 30px; }
                    h1 { margin: 0; color: #2B6CB0; font-size: 24px; }
                    .section { margin-bottom: 25px; }
                    .label { font-size: 10px; color: #718096; font-weight: bold; margin-bottom: 5px; text-transform: uppercase; }
                    .value { font-size: 14px; margin-bottom: 10px; color: #1A202C; }
                    .row { display: flex; flex-direction: row; }
                    .col { flex: 1; }
                    .price { font-size: 18px; color: #2B6CB0; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Umowa ${binding.valNrKontraktu.text}</h1>
                    <p style="color: #718096; font-size: 12px;">${binding.textCreatedInfo.text}</p>
                </div>
                <div class="section">
                    <div class="row">
                        <div class="col">
                            <div class="label">NR KONTRAKTU</div>
                            <div class="value">${binding.valNrKontraktu.text}</div>
                        </div>
                        <div class="col">
                            <div class="label">DATA ZAWARCIA</div>
                            <div class="value">${binding.valDataZawarcia.text}</div>
                        </div>
                    </div>
                    <div class="row" style="margin-top: 15px;">
                        <div class="col">
                            <div class="label">DATA ZOBOWIĄZANIA</div>
                            <div class="value">${binding.valDataZobowiazania.text}</div>
                        </div>
                    </div>
                </div>
                <div class="section">
                    <div class="label">KONTRAHENT</div>
                    <div class="value" style="font-size: 18px;"><strong>${binding.valKontrahent.text}</strong></div>
                    <div class="label" style="margin-top: 10px;">KONTO DOSTAWCY</div>
                    <div class="value">${binding.valKontoDostawcy.text}</div>
                </div>
                <div class="section">
                    <div class="label">ADRES DOSTAWY (FCA)</div>
                    <div class="value">${binding.valAdresDostawy.text}</div>
                </div>
                <div class="section">
                    <div class="label">TOWAR I CENA</div>
                    <div class="row">
                        <div class="col">
                            <div class="label">TOWAR</div>
                            <div class="value">${binding.valTowar.text}</div>
                        </div>
                        <div class="col">
                            <div class="label">ILOŚĆ TON</div>
                            <div class="value">${binding.valIlosc.text}</div>
                        </div>
                    </div>
                    <div class="label" style="margin-top: 10px;">CENA ZAKUPU NETTO</div>
                    <div class="value price">${binding.valCena.text}</div>
                </div>
                <div class="section">
                    <div class="label">PŁATNOŚCI</div>
                    <div class="label" style="margin-top: 10px;">WARUNEK PŁATNOŚCI</div>
                    <div class="value">${binding.valWarunekPlatnosci.text}</div>
                </div>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }

    private fun createWebPrintJob(webView: WebView) {
        val jobName = "Umowa_${binding.valNrKontraktu.text}"
        val fileName = "${jobName.replace("/", "_")}.pdf"
        
        val width = 595 // A4 width in points
        val height = (webView.measuredHeight * (width.toFloat() / webView.measuredWidth)).toInt().coerceAtLeast(842)
        
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        
        val canvas = page.canvas
        val scale = width.toFloat() / webView.measuredWidth.toFloat()
        canvas.scale(scale, scale)
        
        webView.draw(canvas)
        pdfDocument.finishPage(page)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    uri?.let {
                        contentResolver.openOutputStream(it)?.use { outputStream ->
                            pdfDocument.writeTo(outputStream)
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@GrainContractDetailActivity, "Pobrano: $fileName", Toast.LENGTH_LONG).show()
                            openPdf(uri, fileName)
                        }
                    }
                } else {
                    val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadsFolder, fileName)
                    FileOutputStream(file).use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@GrainContractDetailActivity, "Pobrano: $fileName", Toast.LENGTH_LONG).show()
                        val uri = FileProvider.getUriForFile(this@GrainContractDetailActivity, "${packageName}.provider", file)
                        openPdf(uri, fileName)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@GrainContractDetailActivity, "Błąd zapisu: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                pdfDocument.close()
            }
        }
    }

    private fun openPdf(uri: Uri, fileName: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        
        try {
            startActivity(Intent.createChooser(intent, "Otwórz PDF"))
        } catch (e: Exception) {
            Toast.makeText(this, "Nie znaleziono aplikacji do otwierania PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
