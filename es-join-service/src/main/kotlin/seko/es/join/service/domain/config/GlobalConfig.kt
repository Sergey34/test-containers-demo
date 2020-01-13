package seko.es.join.service.domain.config


import com.fasterxml.jackson.annotation.JsonProperty
import seko.es.join.service.domain.config.GlobalConfig.RotationIndexType.NO_ROTATION

data class GlobalConfig(
    @JsonProperty("rotation_target_index_date_format")
    val rotationTargetIndexDateFormat: String? = null, // YYYY-MM
    @JsonProperty("rotation_target_index_type")
    val rotationTargetIndexType: RotationIndexType = NO_ROTATION,
    @JsonProperty("target_index")
    val targetIndex: String
) {
    enum class RotationIndexType {
        DAILY, MONTHLY, WEEKLY, NUMBER, CUSTOM_DATE_FORMAT, NO_ROTATION
    }
}