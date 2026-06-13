package com.ossadkowski.crm.mobile.ui.vacation

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.ossadkowski.crm.mobile.BaseDrawerActivity
import com.ossadkowski.crm.mobile.databinding.ActivityUrlopyRoczneBinding
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme

class UrlopyRoczneActivity : BaseDrawerActivity() {

    private lateinit var binding: ActivityUrlopyRoczneBinding
    private val viewModel: VacationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUrlopyRoczneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSession()
        setupDrawer(
            drawerLayout = binding.drawerLayout,
            menuButton = binding.menuButton,
            drawerClose = binding.includeDrawer.drawerClose,
            drawerName = binding.includeDrawer.drawerName,
            drawerRole = binding.includeDrawer.drawerRole,
            drawerPanel = binding.includeDrawer.drawerPanel,
            drawerApprovals = binding.includeDrawer.drawerApprovals,
            drawerTasks = binding.includeDrawer.drawerTasks,
            drawerCalendar = binding.includeDrawer.drawerCalendar,
            drawerWindykacjaHeader = binding.includeDrawer.drawerWindykacjaHeader,
            drawerWindykacjaArrow = binding.includeDrawer.drawerWindykacjaArrow,
            drawerWindykacjaSub = binding.includeDrawer.drawerWindykacjaSub,
            drawerWindykacjaWnioski = binding.includeDrawer.drawerWindykacjaWnioski,
            drawerWindykacjaNowy = binding.includeDrawer.drawerWindykacjaNowy,
            drawerWindykacjaZadania = binding.includeDrawer.drawerWindykacjaZadania,
            drawerHrHeader = binding.includeDrawer.drawerHrHeader,
            drawerHrArrow = binding.includeDrawer.drawerHrArrow,
            drawerHrSub = binding.includeDrawer.drawerHrSub,
            drawerHrAkceptacje = binding.includeDrawer.drawerHrAkceptacje,
            drawerHrHistoria = binding.includeDrawer.drawerHrHistoria,
            drawerHrKalendarz = binding.includeDrawer.drawerHrKalendarz,
            drawerHrNadgodziny = binding.includeDrawer.drawerHrNadgodziny,
            drawerHrHomeOffice = binding.includeDrawer.drawerHrHomeOffice,
            drawerHrPrawoPracy = binding.includeDrawer.drawerHrPrawoPracy,
            drawerHrSchemat = binding.includeDrawer.drawerHrSchemat,
            drawerHrUrlopy = binding.includeDrawer.drawerHrUrlopy,
            drawerSalesHeader = binding.includeDrawer.drawerSalesHeader,
            drawerSalesArrow = binding.includeDrawer.drawerSalesArrow,
            drawerSalesSub = binding.includeDrawer.drawerSalesSub,
            drawerSalesOrders = binding.includeDrawer.drawerSalesOrders,
            drawerSalesTransport = binding.includeDrawer.drawerSalesTransport,
            drawerSalesContracts = binding.includeDrawer.drawerSalesContracts,
            drawerSalesReps = binding.includeDrawer.drawerSalesReps,
            drawerSalesClientPanel = binding.includeDrawer.drawerSalesClientPanel,
            drawerSalesClients = binding.includeDrawer.drawerSalesClients,
            drawerSalesGrainTrade = binding.includeDrawer.drawerSalesGrainTrade,
            drawerLogout = binding.includeDrawer.drawerLogout,
            drawerKontrahenci = binding.includeDrawer.drawerKontrahenci,
            drawerTowary = binding.includeDrawer.drawerTowary,
            drawerZamowienia = binding.includeDrawer.drawerZamowienia,
            drawerTransakcje = binding.includeDrawer.drawerTransakcje,
            drawerWizyty = binding.includeDrawer.drawerWizyty,
            drawerOferty = binding.includeDrawer.drawerOferty,
            drawerCrm = binding.includeDrawer.drawerCrm,
            drawerInfo = binding.includeDrawer.drawerInfo,
            drawerMessages = binding.includeDrawer.drawerMessages,
            drawerWindykacjaProfil = binding.includeDrawer.drawerWindykacjaProfil
        )
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { CrmTheme { VacationScreen(viewModel) } }
        }
    }

    override fun performLogout() {
        sessionManager.clear()
        finishAffinity()
        startActivity(android.content.Intent(this, com.ossadkowski.crm.mobile.MainActivity::class.java))
    }
}
