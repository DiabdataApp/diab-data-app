package com.diabdata.core.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun newEntryUuid(): String {
    return Uuid.generateV7().toString()
}