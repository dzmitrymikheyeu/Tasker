package com.dev.tasker.upload

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.support.annotation.DrawableRes
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import com.dev.tasker.BuildConfig
import com.dev.tasker.R
import com.dev.tasker.service.TaskerService
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.util.*

class FileUploadService : IntentService(SERVICE_NAME) {

    companion object {
        val SERVICE_NAME: String = FileUploadService::class.java.canonicalName
        const val ID_DETERMINATE_SERVICE = 9001
        const val ID_NOTIFICATION_PROCESS = 9002
        const val ARG_UPLOAD_IMAGE = "ARG_UPLOAD_IMAGE"
        const val ARG_FILE_PATH = "ARG_FILE_PATH"
        const val PROGRESS_MAX = 100
        const val FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".file_provider"
    }

    private val notificationManager: NotificationManager
            by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private var notification: NotificationCompat.Builder? = null

    override fun onHandleIntent(intent: Intent) {
        val filePath = intent.getStringExtra(ARG_FILE_PATH)
        val upload = intent.getBooleanExtra(ARG_UPLOAD_IMAGE, false)
        if (!filePath.isNullOrEmpty()) {
            val fileUri = Uri.fromFile(File(filePath))
            val fileReference = FirebaseStorage.getInstance().reference.child(fileUri.lastPathSegment)
            if (upload) {
                generateNotification()
                uploadFile(fileUri, fileReference)
            } else {
                val file = File(filePath)
                if (file.exists()) {
                    startActivity(getFileIntent(file))
                } else {
                    generateNotification()
                    downloadFile(fileUri, fileReference)
                }
            }
        }
    }

    private fun downloadFile(fileUri: Uri, fileReference: StorageReference) {
        val dir = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).toString() + getString(R.string.app_name))
        val file = File(dir, fileUri.lastPathSegment + Date().toString())
        try {
            if (!dir.exists()) dir.mkdir()
            file.createNewFile()
            fileReference.getFile(file)
                    .addOnSuccessListener {
                        updateNotification(getString(R.string.message_file_downloaded),
                                android.R.drawable.stat_sys_download_done, 0,
                                0, ID_NOTIFICATION_PROCESS, true, file)
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                        updateNotification(getString(R.string.message_download_error),
                                android.R.drawable.stat_notify_error, 0,
                                0, ID_NOTIFICATION_PROCESS, true, null)
                    }
                    .addOnProgressListener {
                        // progress percentage
                        val progress = PROGRESS_MAX * it.bytesTransferred / it.totalByteCount
                        updateNotification(getString(R.string.message_downloading_file),
                                android.R.drawable.stat_sys_download, PROGRESS_MAX,
                                progress.toInt(), ID_NOTIFICATION_PROCESS, false, null)
                    }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun uploadFile(fileUri: Uri, fileReference: StorageReference) {
        fileReference.putFile(fileUri)
                .addOnSuccessListener {
                    updateNotification(getString(R.string.message_file_uploaded),
                            android.R.drawable.stat_sys_upload_done, 0,
                            0, ID_NOTIFICATION_PROCESS, true, null)
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    updateNotification(getString(R.string.message_upload_error),
                            android.R.drawable.stat_notify_error, 0,
                            0, ID_NOTIFICATION_PROCESS, true, null)
                }
                .addOnProgressListener {
                    // progress percentage
                    val progress = PROGRESS_MAX * it.bytesTransferred / it.totalByteCount
                    updateNotification(getString(R.string.message_uploading_file),
                            android.R.drawable.stat_sys_upload, PROGRESS_MAX,
                            progress.toInt(), ID_NOTIFICATION_PROCESS, false, null)
                }
    }

    private fun updateNotification(message: String, @DrawableRes icon: Int,
                                   progressMax: Int, progress: Int,
                                   notificationId: Int, isStop: Boolean, file: File?) {
        notification
                ?.setSmallIcon(icon)
                ?.setContentTitle(message)
                ?.setProgress(progressMax, progress, false)
        if (file != null) {
            notification?.setContentIntent(PendingIntent.getActivity(this, 0, getFileIntent(file),
                    PendingIntent.FLAG_CANCEL_CURRENT));
        }
        notificationManager.notify(notificationId, notification?.build())
        if (isStop) stopSelf()
    }

    private fun getFileIntent(file: File): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file)
        intent.setDataAndType(uri, FileUtils.getMimeType(this, uri))
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return intent
    }

    private fun generateNotification() {
        notification = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this, TaskerService.CHANNEL_ID)
        } else NotificationCompat.Builder(this)

        notification
                ?.setCategory(NotificationCompat.CATEGORY_PROGRESS)
                ?.setOnlyAlertOnce(true)
                ?.setColor(ContextCompat.getColor(this, R.color.colorPrimary))

        notification?.setProgress(PROGRESS_MAX, 0, false)
        startForeground(ID_DETERMINATE_SERVICE, notification?.build())
    }

}