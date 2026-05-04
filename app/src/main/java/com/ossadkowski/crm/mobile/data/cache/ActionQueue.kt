package com.ossadkowski.crm.mobile.data.cache

import android.util.Log
import com.google.gson.Gson
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.*

class ActionQueue(private val db: AppDatabase) {

    private val gson = Gson()
    private val api = RetrofitClient.apiService

    suspend fun enqueue(actionType: String, payload: String) {
        db.enqueueAction(actionType, payload)
    }

    suspend fun processAll(): Int {
        var processed = 0
        val pending = db.getPendingActions()
        for (action in pending) {
            try {
                executeAction(action)
                db.markActionDone(action.id)
                processed++
            } catch (e: Exception) {
                Log.w("ActionQueue", "Failed action ${action.id}: ${e.message}")
                db.markActionFailed(action.id, e.message ?: "Unknown")
            }
        }
        db.removeFailedActions(5)
        return processed
    }

    private suspend fun executeAction(action: PendingAction) {
        when (action.actionType) {
            "create_wniosek" -> {
                val req = gson.fromJson(action.payload, CreateWniosekRequest::class.java)
                api.createWniosek(req)
                db.invalidateByPrefix("wnioski_")
                db.invalidateByPrefix("dash_wnioski_")
            }
            "send_wniosek" -> {
                val p = gson.fromJson(action.payload, IdUserPayload::class.java)
                api.sendWniosek(p.id, UserIdRequest(p.userId))
                db.invalidateByPrefix("wnioski_")
                db.invalidateByPrefix("dash_wnioski_")
            }
            "resubmit_wniosek" -> {
                val p = gson.fromJson(action.payload, IdUserPayload::class.java)
                api.resubmitWniosek(p.id, UserIdRequest(p.userId))
                db.invalidateByPrefix("wnioski_")
                db.invalidateByPrefix("dash_wnioski_")
            }
            "delete_wniosek" -> {
                val p = gson.fromJson(action.payload, IdUserPayload::class.java)
                api.deleteWniosek(p.id, p.userId)
                db.invalidateByPrefix("wnioski_")
                db.invalidateByPrefix("dash_wnioski_")
            }
            "add_comment" -> {
                val p = gson.fromJson(action.payload, CommentPayload::class.java)
                api.addTaskComment(p.taskId, AddTaskCommentRequest(p.tresc))
                db.invalidateByPrefix("task_${p.taskId}_comments")
            }
            "change_task_status" -> {
                val p = gson.fromJson(action.payload, TaskStatusPayload::class.java)
                api.changeTaskStatus(p.taskId, ChangeTaskStatusRequest(p.status))
                db.invalidate("task_${p.taskId}")
                db.invalidateByPrefix("tasks_")
                db.invalidateByPrefix("dash_tasks_")
            }
            "approve_manager" -> {
                val p = gson.fromJson(action.payload, ApprovalPayload::class.java)
                api.approveManager(p.wniosekId, ManagerApprovalRequest(p.actorId, p.approved))
                db.invalidateByPrefix("approvals_")
                db.invalidateByPrefix("wnioski_")
                db.invalidateByPrefix("dash_wnioski_")
            }
            "approve_hr" -> {
                val p = gson.fromJson(action.payload, ApprovalPayload::class.java)
                api.approveHr(p.wniosekId, HrApprovalRequest(p.actorId, p.approved))
                db.invalidateByPrefix("approvals_")
                db.invalidateByPrefix("wnioski_")
                db.invalidateByPrefix("dash_wnioski_")
            }
            "create_limit" -> {
                val req = gson.fromJson(action.payload, CreateLimitKredytowyRequest::class.java)
                api.createLimitKredytowy(req)
                db.invalidateByPrefix("limity_")
            }
            "update_wniosek" -> {
                val p = gson.fromJson(action.payload, UpdateWniosekPayload::class.java)
                val req = gson.fromJson(p.requestJson, CreateWniosekRequest::class.java)
                api.updateWniosek(p.id, req)
                db.invalidate("wniosek_${p.id}")
                db.invalidateByPrefix("wnioski_")
                db.invalidateByPrefix("dash_wnioski_")
            }
            else -> Log.w("ActionQueue", "Unknown action type: ${action.actionType}")
        }
    }

    suspend fun getPendingCount(): Int = db.getPendingCount()
}

// Payload DTOs for action queue serialization
data class IdUserPayload(val id: Int, val userId: Int)
data class CommentPayload(val taskId: Int, val tresc: String)
data class TaskStatusPayload(val taskId: Int, val status: String)
data class ApprovalPayload(val wniosekId: Int, val actorId: Int, val approved: Boolean)
data class UpdateWniosekPayload(val id: Int, val requestJson: String)
