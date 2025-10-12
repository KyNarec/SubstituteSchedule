package org.substitute.schedule.update

data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: String? = null
) : Comparable<Version> {

    override fun compareTo(other: Version): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        if (patch != other.patch) return patch.compareTo(other.patch)

        // Handle pre-release versions (alpha, beta, rc)
        return when {
            preRelease == null && other.preRelease == null -> 0
            preRelease == null -> 1 // stable > pre-release
            other.preRelease == null -> -1
            else -> preRelease.compareTo(other.preRelease)
        }
    }

    override fun toString(): String = buildString {
        append("$major.$minor.$patch")
        preRelease?.let { append("-$it") }
    }

    companion object {
        fun parse(versionString: String): Version? {
            val regex = """(\d+)\.(\d+)\.(\d+)(?:-(.+))?""".toRegex()
            val match = regex.matchEntire(versionString) ?: return null

            return Version(
                major = match.groupValues[1].toInt(),
                minor = match.groupValues[2].toInt(),
                patch = match.groupValues[3].toInt(),
                preRelease = match.groupValues.getOrNull(4)?.takeIf { it.isNotEmpty() }
            )
        }
    }
}
