package com.ossadkowski.crm.mobile.data.repository

import com.google.gson.reflect.TypeToken
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.cache.AppDatabase
import com.ossadkowski.crm.mobile.data.cache.CacheTtl
import com.ossadkowski.crm.mobile.data.model.*
import okhttp3.MultipartBody

class DelegacjaRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val db: AppDatabase = RetrofitClient.cacheDb
) : BaseRepository() {

    suspend fun getKraje(): NetworkResult<List<DelegacjaKrajDto>> {
        return cachedApiCall(
            db = db,
            cacheKey = "ref_delegacja_kraje",
            ttlMs = CacheTtl.REFERENCE,
            type = object : TypeToken<List<DelegacjaKrajDto>>() {}.type
        ) {
            apiService.getDelegacjaKraje()
        }
    }

    suspend fun getWnioskiDelegacje(userId: Int): NetworkResult<PaginatedResponse<WniosekItem>> {
        return safeApiCall {
            // Get user's approved delegations to pre-fill wizard fields
            apiService.getWnioski(
                WnioskiListRequest(
                    userId = userId,
                    page = 1,
                    pageSize = 50,
                    typ = "Delegacja",
                    status = null
                )
            )
        }
    }

    suspend fun kalkulatorKrajowy(request: KalkulatorKrajowyRequest): NetworkResult<KalkulatorResponse> {
        return safeApiCall {
            apiService.kalkulatorKrajowy(request)
        }
    }

    suspend fun kalkulatorZagraniczny(request: KalkulatorZagranicznyRequest): NetworkResult<KalkulatorResponse> {
        return safeApiCall {
            apiService.kalkulatorZagraniczny(request)
        }
    }

    suspend fun kalkulatorMieszany(request: KalkulatorMieszanyRequest): NetworkResult<KalkulatorResponse> {
        return safeApiCall {
            apiService.kalkulatorMieszany(request)
        }
    }

    suspend fun createDelegacja(request: CreateDelegacjaRequest): NetworkResult<CreateDelegacjaResponse> {
        return safeApiCall {
            apiService.createDelegacja(request)
        }
    }

    suspend fun uploadZalacznik(id: Int, part: MultipartBody.Part): NetworkResult<Any> {
        return safeApiCall {
            apiService.uploadDelegacjaFile(id, part)
        }
    }

    suspend fun submitRozliczenie(id: Int, request: SubmitRozliczenieRequest): NetworkResult<Any> {
        return safeApiCall {
            apiService.submitRozliczenie(id, request)
        }
    }

    // ── Moje delegacje ──

    suspend fun getMojeDelegacje(): NetworkResult<List<DelegacjaListItem>> {
        return safeApiCall {
            apiService.getMojeDelegacje()
        }
    }

    suspend fun getDelegacjaDetail(id: Int): NetworkResult<DelegacjaDetail> {
        return safeApiCall {
            apiService.getDelegacjaDetail(id)
        }
    }

    // ── Zespół — delegacje (Manager) ──

    suspend fun getManagerTeamDelegacje(): NetworkResult<List<DelegacjaListItem>> {
        return safeApiCall {
            apiService.getManagerTeamDelegacje()
        }
    }

    // ── Finanse ──

    suspend fun getFinansePool(status: String? = null): NetworkResult<List<DelegacjaFinanseItem>> {
        return safeApiCall {
            apiService.getFinansePool(status)
        }
    }

    suspend fun getFinanseDetail(id: Int): NetworkResult<DelegacjaFinanseDetail> {
        return safeApiCall {
            apiService.getFinanseDetail(id)
        }
    }

    suspend fun decyzjaFinanse(id: Int, zatwierdzono: Boolean, powod: String? = null): NetworkResult<Any> {
        return safeApiCall {
            apiService.decyzjaFinanse(id, DecyzjaFinanseRequest(zatwierdzono, powod))
        }
    }

    suspend fun finanseKorekta(id: Int, request: FinanseKorektaRequest): NetworkResult<Any> {
        return safeApiCall {
            apiService.finanseKorekta(id, request)
        }
    }

    suspend fun doWyjasnienia(id: Int, pytanie: String): NetworkResult<Any> {
        return safeApiCall {
            apiService.doWyjasnienia(id, DoWyjasnieniaRequest(pytanie))
        }
    }

    // ── Zaliczki ──

    suspend fun zaliczkaWyplacona(id: Int): NetworkResult<Any> {
        return safeApiCall {
            apiService.zaliczkaWyplacona(id)
        }
    }

    // ── HR Audit ──

    suspend fun getHrDelegacjeList(): NetworkResult<List<DelegacjaAuditItem>> {
        return safeApiCall {
            apiService.getHrDelegacjeList()
        }
    }

    suspend fun getHrAudit(id: Int): NetworkResult<DelegacjaAuditDetail> {
        return safeApiCall {
            apiService.getHrAudit(id)
        }
    }

    // ── PDF ──

    suspend fun getDelegacjaPdf(id: Int, wariant: String? = null): NetworkResult<okhttp3.ResponseBody> {
        return safeApiCall {
            apiService.getDelegacjaPdf(id, wariant)
        }
    }
}

