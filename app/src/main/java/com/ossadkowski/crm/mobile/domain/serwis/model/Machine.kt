package com.ossadkowski.crm.mobile.domain.serwis.model

import java.time.LocalDate

data class Machine(
    val id: Long,
    val accountNum: String?,
    val marka: String?,
    val model: String?,
    val numerSeryjny: String?,
    val typMaszyny: String?,
    val rokProdukcji: Int?,
    val gwarancjaOd: LocalDate?,
    val gwarancjaDo: LocalDate?,
    val dataSprzedazy: LocalDate?,
    val nrRejestracyjny: String?,
    val itemId: String?,
    val itemName: String?,
    val zrodlo: String?,
    val uwagi: String?,
    val warrantyStatus: WarrantyStatus,
    val totalOrders: Int?,
    val openOrders: Int?,
    val history: List<MachineHistoryEntry>
)

data class MachineHistoryEntry(
    val orderRegNum: String,
    val orderDate: LocalDate?,
    val orderType: Int?,
    val status: OrderStatus,
    val reportedSymptoms: String?,
    val serviceType: String?,
    val isWarranty: Boolean?
)

data class WarrantyCheck(
    val machineId: Long,
    val gwarancjaOd: LocalDate?,
    val gwarancjaDo: LocalDate?,
    val warrantyStatus: WarrantyStatus,
    val isActive: Boolean?
)
