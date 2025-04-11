package hnau.plugin.fixdependencies.path

data class Path(
    val parts: List<String>,
) {

    operator fun plus(
        part: String,
    ): Path = Path(
        parts = parts + part,
    )

    fun isStartsWith(
        other: Path,
    ): Boolean {
        val thisParts = parts.iterator()
        val otherParts = other.parts.iterator()
        while (thisParts.hasNext() && otherParts.hasNext()) {
            if (thisParts.next() != otherParts.next()) {
                return false
            }
        }
        return !otherParts.hasNext()
    }

    companion object {

        val empty: Path = Path(emptyList())
    }
}

