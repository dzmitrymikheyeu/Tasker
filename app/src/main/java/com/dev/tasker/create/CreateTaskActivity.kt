package com.dev.tasker.create

import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.EditText
import com.dev.tasker.R
import com.dev.tasker.commons.TaskDH
import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.core.networking.Outcome
import com.dev.tasker.create.exceptions.TaskCreateException
import com.dev.tasker.create.viewmodel.CreateTaskViewModel
import com.dev.tasker.create.viewmodel.CreateViewModelFactory
import com.dev.tasker.extensions.showToast
import com.dev.tasker.upload.FileUploadService
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_create_task.*
import javax.inject.Inject

class CreateTaskActivity : AppCompatActivity() {

    companion object {
        const val ARG_TASK = "ARG_TASK"
    }

    private val component by lazy { TaskDH.createComponent() }
    @Inject lateinit var viewModelFactory: CreateViewModelFactory

    private val viewModel: CreateTaskViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(CreateTaskViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)
        component.inject(this)

        if (intent.hasExtra(ARG_TASK)) {
            viewModel.editTask = intent.getParcelableExtra(ARG_TASK) as Task
        }

        setSupportActionBar(toolbarCreate)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (viewModel.isEditMode()) {
            btnCreate.setText(R.string.action_edit)
            with(viewModel.editTask) {
                if(this != null) {
                    edtName.setText(name)
                    edtDesc.setText(description)
                    edtFile.setText(filePath)
                    if (keywords.isNullOrEmpty()) {
                        tagsContainer.setTags(keywords.split(","))
                    }
                }
            }
            supportActionBar?.setTitle(R.string.edit_task)
        } else {
            supportActionBar?.setTitle(R.string.add_task)
        }

        btnKeywords.setOnClickListener({
            val edt = EditText(this)
            AlertDialog.Builder(this)
                    .setTitle(R.string.add_keywords)
                    .setView(edt)
                    .setPositiveButton(R.string.add_keywords, { _, _ ->
                        if (!edt.text.isNullOrEmpty()) {
                            var tags = edt.text.toString()
                                    .plus(if (tagsContainer.tags.isNotEmpty()) {
                                        "," + tagsContainer.tags.joinToString()
                                    } else "")
                            tagsContainer.setTags(tags.trim().split(","))
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
        })

        btnCreate.setOnClickListener {
            saveTask()
        }

        edtFile.setOnClickListener({
            RxPaparazzo.single(this)
                    .usingFiles()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (it.resultCode() != Activity.RESULT_OK) {
                            // User cancelled
                        } else {
                            it.targetUI().viewModel.uploadFilePath = it.data().file.absolutePath
                            edtFile.setText(it.data().filename)
                        }
                    })
        })

        initiateDataListener()
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
        viewModel.saveTask(edtName.text.toString(), edtDesc.text.toString(),
                tagsContainer.tags.joinToString())
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

}
