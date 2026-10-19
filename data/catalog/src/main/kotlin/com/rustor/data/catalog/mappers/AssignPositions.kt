package com.rustor.data.catalog.mappers

import com.rustor.core.database.entity.AppEntity
import kotlin.random.Random

internal fun assignPositions(apps: List<AppEntity>, existing: Map<String, Int>, random: Random): List<AppEntity> {
    val (kept, added) = apps.partition { it.id in existing }

    val keptWithPositions = kept.map { it.copy(feedPosition = existing.getValue(it.id)) }

    val start = keptWithPositions.maxOfOrNull { it.feedPosition }?.plus(1) ?: 0

    val addedWithPositions = added
        .shuffled(random)
        .mapIndexed { index, app -> app.copy(feedPosition = start + index) }

    return (keptWithPositions + addedWithPositions).sortedBy { it.feedPosition }
}
