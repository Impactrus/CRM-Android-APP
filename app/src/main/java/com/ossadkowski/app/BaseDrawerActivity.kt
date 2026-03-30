package com.ossadkowski.app

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import com.ossadkowski.app.data.SessionManager

abstract class BaseDrawerActivity : BaseActivity() {

    protected lateinit var sessionManager: SessionManager

    protected fun initSession() {
        sessionManager = SessionManager(this)
    }

    protected fun setupDrawer(
        drawerLayout: DrawerLayout,
        menuButton: View,
        drawerClose: View,
        drawerName: TextView,
        drawerRole: TextView,
        drawerPanel: View,
        drawerApprovals: View,
        drawerTasks: View,
        drawerCalendar: View,
        drawerLimityKredytowe: View,
        drawerLogout: View,
        drawerKontrahenci: View? = null,
        drawerTowary: View? = null,
        drawerZamowienia: View? = null,
        drawerTransakcje: View? = null,
        drawerWizyty: View? = null,
        drawerOferty: View? = null,
        drawerCrm: View? = null,
        drawerInfo: View? = null
    ) {
        drawerName.text = sessionManager.username
        drawerRole.text = sessionManager.role

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(findViewById<View>(R.id.nav_drawer))
        }

        drawerClose.setOnClickListener {
            drawerLayout.closeDrawers()
        }

        drawerPanel.setOnClickListener {
            drawerLayout.closeDrawers()
        }

        // Claims-based visibility
        drawerApprovals.visibility = if (sessionManager.hasClaim("nav.pracownicy.akceptacja")) View.VISIBLE else View.GONE
        drawerTasks.visibility = if (sessionManager.hasClaim("nav.planer.zadania")) View.VISIBLE else View.GONE
        drawerCalendar.visibility = if (sessionManager.hasClaim("nav.planer.kalendarz")) View.VISIBLE else View.GONE
        drawerLimityKredytowe.visibility = if (sessionManager.hasClaim("nav.windykacja.wnioski_o_limit")) View.VISIBLE else View.GONE

        drawerApprovals.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(ApprovalActivity::class.java)
        }

        drawerTasks.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(TasksListActivity::class.java)
        }

        drawerCalendar.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(CalendarActivity::class.java)
        }

        drawerLimityKredytowe.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(LimityKredytoweListActivity::class.java)
        }

        drawerLogout.setOnClickListener {
            drawerLayout.closeDrawers()
            performLogout()
        }

        // New menu items — placeholder toasts for unimplemented screens
        val placeholderClick = View.OnClickListener {
            drawerLayout.closeDrawers()
            Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show()
        }

        drawerKontrahenci?.setOnClickListener(placeholderClick)
        drawerTowary?.setOnClickListener(placeholderClick)
        drawerZamowienia?.setOnClickListener(placeholderClick)
        drawerTransakcje?.setOnClickListener(placeholderClick)
        drawerWizyty?.setOnClickListener(placeholderClick)
        drawerOferty?.setOnClickListener(placeholderClick)
        drawerCrm?.setOnClickListener(placeholderClick)
        drawerInfo?.setOnClickListener(placeholderClick)
    }

    protected fun navigateTo(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

    protected abstract fun performLogout()
}
