package com.ossadkowski.app.data.repository

import com.google.gson.reflect.TypeToken
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.api.ApiService
import com.ossadkowski.app.data.api.RetrofitClient
import com.ossadkowski.app.data.cache.ActionQueue
import com.ossadkowski.app.data.cache.AppDatabase
import com.ossadkowski.app.data.cache.CacheTtl
import com.ossadkowski.app.data.cache.CommentPayload
import com.ossadkowski.app.data.cache.TaskStatusPayload
import com.ossadkowski.app.data.model.*
import com.google.gson.Gson

class TasksRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val db: AppDatabase = RetrofitClient.cacheDb,
    private val actionQueue: ActionQueue = ActionQueue(RetrofitClient.cacheDb)
) : BaseRepository() {

    private val gson = Gson()

    suspend fun getList(page: Int, pageSize: Int, search: String?, status: String?, typ: String?): NetworkResult<PaginatedResponse<TaskListItemDto>> {
        val key = "tasks_p${page}_s${status ?: ""}_q${search ?: ""}_t${typ ?: ""}"
        return cachedApiCall(db, key, CacheTtl.SHORT,
            object : TypeToken<PaginatedResponse<TaskListItemDto>>() {}.type
        ) { apiService.getTasksV2(page, pageSize, search, status, typ) }
    }

    suspend fun getDetail(id: Int): NetworkResult<TaskDetailDto> {
        return cachedApiCall(db, "task_$id", CacheTtl.MODERATE,
            object : TypeToken<TaskDetailDto>() {}.type
        ) { apiService.getTaskDetail(id) }
    }

    suspend fun getComments(id: Int): NetworkResult<List<TaskCommentDto>> {
        return cachedApiCall(db, "task_${id}_comments", CacheTtl.SHORT,
            object : TypeToken<List<TaskCommentDto>>() {}.type
        ) { apiService.getTaskComments(id) }
    }

    suspend fun addComment(id: Int, tresc: String): NetworkResult<Any> {
        val result = safeApiCall { apiService.addTaskComment(id, AddTaskCommentRequest(tresc)) }
        if (result is NetworkResult.Success) {
            db.invalidate("task_${id}_comments")
        } else if (result is NetworkResult.Error) {
            actionQueue.enqueue("add_comment", gson.toJson(CommentPayload(id, tresc)))
            return NetworkResult.Success("queued_offline")
        }
        return result
    }

    suspend fun getFiles(id: Int): NetworkResult<List<TaskFileDto>> {
        return cachedApiCall(db, "task_${id}_files", CacheTtl.MODERATE,
            object : TypeToken<List<TaskFileDto>>() {}.type
        ) { apiService.getTaskFiles(id) }
    }

    suspend fun getHistoria(id: Int): NetworkResult<List<TaskHistoriaDto>> {
        return cachedApiCall(db, "task_${id}_historia", CacheTtl.MODERATE,
            object : TypeToken<List<TaskHistoriaDto>>() {}.type
        ) { apiService.getTaskHistoria(id) }
    }

    suspend fun getObservers(id: Int): NetworkResult<List<TaskObserverDto>> {
        return cachedApiCall(db, "task_${id}_observers", CacheTtl.MODERATE,
            object : TypeToken<List<TaskObserverDto>>() {}.type
        ) { apiService.getTaskObservers(id) }
    }

    suspend fun changeStatus(id: Int, status: String): NetworkResult<Any> {
        val result = safeApiCall { apiService.changeTaskStatus(id, ChangeTaskStatusRequest(status)) }
        if (result is NetworkResult.Success) {
            db.invalidate("task_$id")
            db.invalidateByPrefix("tasks_")
            db.invalidateByPrefix("dash_tasks_")
        } else if (result is NetworkResult.Error) {
            actionQueue.enqueue("change_task_status", gson.toJson(TaskStatusPayload(id, status)))
            return NetworkResult.Success("queued_offline")
        }
        return result
    }
}
