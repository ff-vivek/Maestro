package maestro.orchestra.yaml

import com.fasterxml.jackson.annotation.JsonCreator

data class YamlSwitchTab(
    val index: String,
    val label: String? = null,
    val optional: Boolean = false,
) {
    companion object {

        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun parse(value: Any): YamlSwitchTab {
            return when (value) {
                is Map<*, *> -> {
                    YamlSwitchTab(
                        index = value["index"]?.toString() ?: throw IllegalArgumentException("index is required for switchTab"),
                        label = value["label"]?.toString(),
                        optional = value["optional"]?.toString()?.toBoolean() ?: false,
                    )
                }
                else -> {
                    YamlSwitchTab(
                        index = value.toString(),
                    )
                }
            }
        }
    }
}
