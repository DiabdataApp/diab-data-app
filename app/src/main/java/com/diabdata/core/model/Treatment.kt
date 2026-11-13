package com.diabdata.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.diabdata.shared.utils.dataTypes.TreatmentType
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import kotlin.time.Instant

/**
 * A treatment supply owned by the user (e.g. an insulin cartridge), tracked by its expiration date.
 *
 * Alternate JSON names in [SerializedName] are kept so that older backups can still be imported.
 *
 * @property id Local database id. Only meaningful on this device: do not use it to identify
 * an entry across devices or backups, use [entryUuid] instead.
 * @property createdAt When the entry was created. Never changes afterwards; used to match
 * entries from legacy backups that have no [entryUuid].
 * @property updatedAt When the entry was last modified. Must be updated on every change,
 * otherwise the import conflict resolver may keep an outdated version.
 * @property entryUuid Stable identity of the entry across devices and backups, used by the
 * import conflict resolver.
 */
@Entity(
    tableName = "treatments",
    indices = [Index(value = ["entryUuid"], unique = true)]
)
data class Treatment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @SerializedName("treatment_expiration_date", alternate = ["expirationDate"])
    val expirationDate: LocalDate,
    @SerializedName("treatment_name", alternate = ["name"])
    val name: String,
    @SerializedName("created_at", alternate = ["createdAt"])
    val createdAt: Instant,
    @SerializedName("is_archived", alternate = ["isArchived"])
    val isArchived: Boolean,
    @SerializedName("treatment_type", alternate = ["type"])
    val type: TreatmentType,
    @SerializedName("updated_at", alternate = ["updatedAt"])
    val updatedAt: Instant,
    @SerializedName("entry_uuid")
    val entryUuid: String
)