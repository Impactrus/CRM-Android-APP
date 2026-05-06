package com.ossadkowski.crm.mobile.example.serverstatus.ui

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ossadkowski.crm.mobile.domain.common.Result
import dagger.hilt.android.AndroidEntryPoint

/**
 * Throwaway example screen demonstrating the Clean Architecture stack
 * (UseCase -> repo interface -> repo impl -> DTO->domain mapper).
 *
 * Not wired into any user-facing flow. Launch only via:
 *   adb shell am start -n com.ossadkowski.crm.mobile/.example.serverstatus.ui.ServerStatusActivity
 *
 * Delete this entire `example/serverstatus/` package once a real new feature
 * has been added following the same pattern.
 */
@AndroidEntryPoint
class ServerStatusActivity : AppCompatActivity() {

    private val viewModel: ServerStatusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }

        val title = TextView(this).apply {
            text = "Server Status (debug)"
            textSize = 22f
            setTextColor(Color.parseColor("#111111"))
        }

        val output = TextView(this).apply {
            text = "Tap Check to call /auth/profile through the Clean Architecture stack."
            textSize = 16f
            setPadding(0, 48, 0, 48)
            gravity = Gravity.START
        }

        val button = Button(this).apply {
            text = "Check"
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            setOnClickListener { viewModel.check() }
        }

        root.addView(title)
        root.addView(button)
        root.addView(output)
        setContentView(root)

        viewModel.state.observe(this) { result ->
            output.text = when (result) {
                is Result.Loading -> "Loading..."
                is Result.Success -> "OK\nuserId=${result.data.userId}\nname=${result.data.displayName}\nworkpost=${result.data.workpost ?: "-"}"
                is Result.Error -> "ERROR: ${result.message}"
            }
        }
    }
}
