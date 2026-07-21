package com.screen.autolocker.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class LockDeviceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        fun isAdminEnabled(context: Context): Boolean {
            val dm = context.getSystemService(Context.DEVICE_POLICY_SERVICE)
                    as android.app.admin.DevicePolicyManager
            val admin = android.content.ComponentName(
                context,
                LockDeviceAdminReceiver::class.java
            )
            return dm.isAdminActive(admin)
        }

        fun lockNow(context: Context): Boolean {
            return try {
                val dm = context.getSystemService(Context.DEVICE_POLICY_SERVICE)
                        as android.app.admin.DevicePolicyManager
                val admin = android.content.ComponentName(
                    context,
                    LockDeviceAdminReceiver::class.java
                )
                if (dm.isAdminActive(admin)) {
                    dm.lockNow()
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}