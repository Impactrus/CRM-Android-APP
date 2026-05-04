package com.ossadkowski.crm.mobile

import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

import com.ossadkowski.crm.mobile.data.SessionManager

open class BaseActivity : AppCompatActivity() {

    protected lateinit var sessionManager: SessionManager

    protected fun initSession() {
        sessionManager = SessionManager(this)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val loc = IntArray(2)
                v.getLocationOnScreen(loc)
                if (ev.rawX < loc[0] || ev.rawX > loc[0] + v.width ||
                    ev.rawY < loc[1] || ev.rawY > loc[1] + v.height) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}
