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
        fun parse(index: Any): YamlSwitchTab {
            return YamlSwitchTab(
                index = index.toString(),
            )
        }
    }
}
