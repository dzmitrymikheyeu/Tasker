package com.dev.tasker.create

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.design.chip.Chip
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.HorizontalScrollView
import com.dev.tasker.R
import com.dev.tasker.commons.TaskDH
import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.core.networking.Outcome
import com.dev.tasker.create.exceptions.TaskCreateException
import com.dev.tasker.create.viewmodel.CreateTaskViewModel
import com.dev.tasker.create.viewmodel.CreateViewModelFactory
import com.dev.tasker.extensions.showKeyboard
import com.dev.tasker.extensions.showToast
import com.dev.tasker.service.TaskerService
import com.dev.tasker.upload.FileUploadService
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_create_task.*
import javax.inject.Inject

class CreateTaskActivity : AppCompatActivity() {

    companion object {
        const val ARG_TASK = "ARG_TASK"
        const val ARG_SPEECH_DESCRIPTION = "ARG_SPEECH_DESCRIPTION"
        const val REQUEST_CODE_SPEECH = 1235
    }

    private val component by lazy { TaskDH.createComponent() }
    @Inject
    lateinit var viewModelFactory: CreateViewModelFactory

    private val viewModel: CreateTaskViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(CreateTaskViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)
        component.inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.add_task)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (intent.hasExtra(ARG_TASK)) {
            viewModel.editTask = intent.getParcelableExtra(ARG_TASK) as Task
        }

        if (viewModel.isEditMode()) {
            btn_create.setText(R.string.action_edit)
            supportActionBar?.title = getString(R.string.edit_task)
            with(viewModel.editTask) {
                if (this != null) {
                    edt_name.setText(name)
                    edt_desc.setText(description)
                    setFileView(filePath, !filePath.isBlank())
                    if (!keywords.isEmpty()) {
                        keywords.split(",")
                                .map { chip_group.addView(getChip(it), 0) }
                                .toList()
                    }
                }
            }
        }

        edt_keywords.setOnEditorActionListener { v, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> addChip(v.text)
            }
            return@setOnEditorActionListener true
        }

        btn_create.setOnClickListener {
            saveTask()
        }

        img_attach.setOnClickListener({
            RxPaparazzo.single(this)
                    .usingFiles()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (it.resultCode() != Activity.RESULT_OK) {
                            // User cancelled
                        } else {
                            it.targetUI().viewModel.uploadFilePath = it.data().file.absolutePath
                            setFileView(it.data().filename, true)
                        }
                    })
        })

        img_remove_file.setOnClickListener {
            setFileView(null, false)
        }

        chip_group.setOnTouchListener { _, _ ->
            edt_keywords.requestFocus()
            showKeyboard(edt_keywords)
            scroll_container.post({
                scroll_container.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
            })
            return@setOnTouchListener true
        }

        initiateDataListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SPEECH && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val speechText = results?.get(0)
            if (data != null && data.getBooleanExtra(ARG_SPEECH_DESCRIPTION, false)) {
                edt_desc.setText("")
                edt_desc.setText(speechText)
            } else {
                edt_name.setText("")
                edt_name.setText(speechText)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun addChip(text: CharSequence) {
        chip_group.addView(getChip(text), chip_group.childCount - 1)
        edt_keywords.setText("")
        scroll_container.post({
            scroll_container.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        })
    }

    private fun getChip(text: CharSequence): Chip {
        val chip = Chip(this)
        chip.chipText = text
        chip.isCloseIconEnabled = true
        chip.setOnCloseIconClickListener {
            chip_group.removeView(chip)
        }
        return chip
    }

    private fun setFileView(path: String?, visible: Boolean) {
        if (!visible) viewModel.uploadFilePath = null
        container_file.visibility = if (visible) View.VISIBLE else View.GONE
        txt_file.text = path
    }

    private fun showUploadDialog() {
        AlertDialog.Builder(this)
                .setTitle(R.string.upload_file_message)
                .setPositiveButton(android.R.string.ok, { _, _ ->
                    startUploadProcess()
                    finish()
                })
                .setNegativeButton(android.R.string.cancel, { _, _ ->
                    finish()
                })
                .show()
    }

    private fun saveTask() {
        viewModel.saveTask(edt_name.text.toString(), edt_desc.text.toString(),
                (0..chip_group.childCount)
                        .map { chip_group.getChildAt(it) }
                        .filter { it is Chip }
                        .map { (it as Chip).chipText }
                        .toList().joinToString())
    }

    private fun startUploadProcess() {
        val intent = Intent(this, FileUploadService::class.java)
        intent.putExtra(FileUploadService.ARG_FILE_PATH, viewModel.uploadFilePath)
        intent.putExtra(FileUploadService.ARG_UPLOAD_IMAGE, true)
        startService(intent)
    }

    private fun initiateDataListener() {
        viewModel.createTaskOutcome.observe(this, Observer<Outcome<Long>> { outcome ->
            when (outcome) {
                is Outcome.Success -> {
                    showToast(if (viewModel.isEditMode()) R.string.message_task_updated else R.string.message_task_created)
                    if(txt_reminder.calendar != null) {
                        val timeMillis = txt_reminder.calendar!!.timeInMillis
                        configureAlarm(outcome.data, timeMillis)
                    }
                    if (viewModel.uploadFilePath != null) {
                        showUploadDialog()
                    } else {
                        finish()
                    }
                }
                is Outcome.Failure -> {
                    if (outcome.e is TaskCreateException.EmptyFieldsException) {
                        showToast(R.string.error_name_desc)
                    } else {
                        showToast(outcome.e.localizedMessage)
                    }
                }

            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configureAlarm(taskId: Long, reminderTime: Long) {
        val intent = Intent(this, TaskerService::class.java)
        intent.putExtra(TaskerService.ARG_TASK_ID, taskId)
        intent.action = TaskerService.ACTION_REMINDER
        val alarm = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getService(this, taskId.toInt(),
                intent, PendingIntent.FLAG_CANCEL_CURRENT)
        alarm.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
    }

}
