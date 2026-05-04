package com.ossadkowski.crm.mobile

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import com.ossadkowski.crm.mobile.data.SessionManager

abstract class BaseDrawerActivity : BaseActivity() {



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
        drawerWindykacjaHeader: View,
        drawerWindykacjaArrow: ImageView,
        drawerWindykacjaSub: View,
        drawerWindykacjaWnioski: View,
        drawerWindykacjaNowy: View,
        drawerWindykacjaZadania: View,
        drawerHrHeader: View? = null,
        drawerHrArrow: ImageView? = null,
        drawerHrSub: View? = null,
        drawerHrAkceptacje: View? = null,
        drawerHrHistoria: View? = null,
        drawerHrKalendarz: View? = null,
        drawerHrNadgodziny: View? = null,
        drawerHrHomeOffice: View? = null,
        drawerHrPrawoPracy: View? = null,
        drawerHrSchemat: View? = null,
        drawerSalesHeader: View? = null,
        drawerSalesArrow: ImageView? = null,
        drawerSalesSub: View? = null,
        drawerSalesOrders: View? = null,
        drawerSalesTransport: View? = null,
        drawerSalesContracts: View? = null,
        drawerSalesReps: View? = null,
        drawerSalesClientPanel: View? = null,
        drawerSalesClients: View? = null,
        drawerSalesGrainTrade: View? = null,
        drawerLogout: View,
        drawerKontrahenci: View? = null,
        drawerTowary: View? = null,
        drawerZamowienia: View? = null,
        drawerTransakcje: View? = null,
        drawerWizyty: View? = null,
        drawerOferty: View? = null,
        drawerCrm: View? = null,
        drawerInfo: View? = null,
        drawerMessages: View? = null
    ) {
        drawerName.text = sessionManager.username
        drawerRole.text = sessionManager.role

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
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
        
        // Windykacja visibility (always collapsed by default)
        val hasWindykacja = sessionManager.hasClaim("nav.windykacja") || 
                            sessionManager.hasClaim("nav.windykacja.wnioski_o_limit")
        drawerWindykacjaHeader.visibility = if (hasWindykacja) View.VISIBLE else View.GONE
        drawerWindykacjaSub.visibility = View.GONE
        drawerWindykacjaArrow.rotation = 0f

        // HR visibility (if any HR claim, default visible for now)
        val hasHr = true // sessionManager.hasClaim("nav.hr")
        drawerHrHeader?.visibility = if (hasHr) View.VISIBLE else View.GONE
        drawerHrSub?.visibility = View.GONE // Default collapsed

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

        // Windykacja logic
        drawerWindykacjaHeader.setOnClickListener {
            val isVisible = drawerWindykacjaSub.visibility == View.VISIBLE
            drawerWindykacjaSub.visibility = if (isVisible) View.GONE else View.VISIBLE
            drawerWindykacjaArrow.rotation = if (isVisible) 0f else 90f
        }

        drawerWindykacjaWnioski.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(LimityKredytoweListActivity::class.java)
        }

        drawerWindykacjaNowy.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(LimitKredytowyActivity::class.java)
        }

        drawerWindykacjaZadania.setOnClickListener {
            drawerLayout.closeDrawers()
            // Placeholder/Target for task collection
            navigateTo(DebtTasksListActivity::class.java)
        }

        // HR logic
        drawerHrHeader?.setOnClickListener {
            if (drawerHrSub == null || drawerHrArrow == null) return@setOnClickListener
            val isVisible = drawerHrSub.visibility == View.VISIBLE
            drawerHrSub.visibility = if (isVisible) View.GONE else View.VISIBLE
            drawerHrArrow.rotation = if (isVisible) 0f else 90f
        }

        drawerHrAkceptacje?.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(ApprovalActivity::class.java) // Same as drawerApprovals
        }



        drawerHrHistoria?.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(HrHistoriaWnioskowActivity::class.java)
        }

        drawerHrKalendarz?.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(HrKalendarzZamrozenActivity::class.java)
        }

        drawerHrNadgodziny?.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(HrNadgodzinyActivity::class.java)
        }

        drawerHrHomeOffice?.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(HrHomeOfficeActivity::class.java)
        }

        drawerHrPrawoPracy?.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(HrPrawoPracyActivity::class.java)
        }

        drawerHrSchemat?.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(HrSchematOrgActivity::class.java)
        }

        // Sales logic
        drawerSalesHeader?.setOnClickListener {
            if (drawerSalesSub == null || drawerSalesArrow == null) return@setOnClickListener
            val isVisible = drawerSalesSub.visibility == View.VISIBLE
            drawerSalesSub.visibility = if (isVisible) View.GONE else View.VISIBLE
            drawerSalesArrow.rotation = if (isVisible) 0f else 90f
        }

        drawerSalesOrders?.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(SalesOrdersActivity::class.java)
        }

        val salesPlaceholder = View.OnClickListener {
            drawerLayout.closeDrawers()
            Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show()
        }

        drawerSalesTransport?.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(TransportListActivity::class.java)
        }
        drawerSalesContracts?.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(GrainContractsListActivity::class.java)
        }
        drawerSalesReps?.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(HandlowcyListActivity::class.java)
        }
        drawerSalesClientPanel?.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(CustomerPanelActivity::class.java)
        }
        drawerSalesClients?.setOnClickListener(salesPlaceholder)
        drawerSalesGrainTrade?.setOnClickListener(salesPlaceholder)

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

        drawerMessages?.setOnClickListener {
            drawerLayout.closeDrawers()
            navigateTo(ConversationsActivity::class.java)
        }
    }

    protected fun navigateTo(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

    protected abstract fun performLogout()
}
