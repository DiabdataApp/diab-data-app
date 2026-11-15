package com.diabdata.core.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun newEntryUuid(): String = Uuid.generateV7().toString()
