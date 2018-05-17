package com.dev.tasker.commons.data.local

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Entity(indices = [(Index("taskId"))])
@Parcelize
data class Task(@SerializedName("id") @PrimaryKey val taskId: Long,
                @SerializedName("description") val description: String,
                @SerializedName("name") val name: String,
                @SerializedName("keywords") val keywords: String,
                @SerializedName("startedTime") val startedTime: Long,
                @SerializedName("finishedTime") val finishedTime: Long,
                @SerializedName("started") val started: Boolean,
                @SerializedName("finished") val finished: Boolean,
                @SerializedName("postponed") val postponed: Boolean,
                @SerializedName("spendingTime") val spendingTime: Long,
                @SerializedName("filePath") val filePath: String) : Parcelable {

    @Ignore
    constructor(taskId: Long,
                description: String,
                name: String,
                keywords: String,
                imagePath: String) : this(taskId, description, name, keywords,
            0L, 0L, false,false, false, 0L,  imagePath)
}

