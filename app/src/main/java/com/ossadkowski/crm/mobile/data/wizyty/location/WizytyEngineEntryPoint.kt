package com.ossadkowski.crm.mobile.data.wizyty.location

import com.ossadkowski.crm.mobile.domain.wizyty.repository.ContractorLocationSource
import com.ossadkowski.crm.mobile.domain.wizyty.repository.VisitRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt access for manifest [android.content.BroadcastReceiver]s, which can't use
 * constructor injection. Receivers obtain dependencies via
 * `EntryPointAccessors.fromApplication(context, WizytyEngineEntryPoint::class.java)`.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WizytyEngineEntryPoint {
    fun visitRepository(): VisitRepository
    fun contractorLocationSource(): ContractorLocationSource
    fun activityRecognitionManager(): ActivityRecognitionManager
    fun geofenceManager(): GeofenceManager
    fun wizytyPrefs(): WizytyPrefs
    fun wizytyNotifier(): WizytyNotifier
}
