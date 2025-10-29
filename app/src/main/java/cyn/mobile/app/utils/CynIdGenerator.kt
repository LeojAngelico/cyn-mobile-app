import android.os.Build
import java.util.Calendar
import java.util.TimeZone


fun generateCynIdTimeOrdered(): String {
    val (y, m, d) = if (Build.VERSION.SDK_INT >= 26) {
        val now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
        Triple(
            (now.year % 100).toString().padStart(2, '0'),
            now.monthValue.toString().padStart(2, '0'),
            now.dayOfMonth.toString().padStart(2, '0')
        )
    } else {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val year = cal.get(Calendar.YEAR) % 100
        val month = cal.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        val day = cal.get(Calendar.DAY_OF_MONTH)
        Triple(
            year.toString().padStart(2, '0'),
            month.toString().padStart(2, '0'),
            day.toString().padStart(2, '0')
        )
    }

    val rand = kotlin.random.Random.nextInt(36 * 36 * 36 * 36) // 0..1,679,615
    val body = rand.toString(36).uppercase().padStart(4, '0')   // 4 base36 chars
    val core = "$y$m$d-$body"
    val cc = checksumBase36(core)
    return "CYN-$core-$cc"
}

fun generateSessionIdTimeOrdered(): String {
    val (y, m, d) = if (Build.VERSION.SDK_INT >= 26) {
        val now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
        Triple(
            (now.year % 100).toString().padStart(2, '0'),
            now.monthValue.toString().padStart(2, '0'),
            now.dayOfMonth.toString().padStart(2, '0')
        )
    } else {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val year = cal.get(Calendar.YEAR) % 100
        val month = cal.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        val day = cal.get(Calendar.DAY_OF_MONTH)
        Triple(
            year.toString().padStart(2, '0'),
            month.toString().padStart(2, '0'),
            day.toString().padStart(2, '0')
        )
    }

    val rand = kotlin.random.Random.nextInt(36 * 36 * 36 * 36) // 0..1,679,615
    val body = rand.toString(36).uppercase().padStart(4, '0')   // 4 base36 chars
    val core = "$y$m$d-$body"
    val cc = checksumBase36(core)
    return "CYN-Session-$core-$cc"
}

private fun checksumBase36(s: String): String {
    var sum = 0
    for (c in s) {
        sum = (sum * 31 + c.code) and 0x7FFFFFFF
    }
    val v = sum % (36 * 36) // 0..1295
    val hi = v / 36
    val lo = v % 36
    return "${digit36(hi)}${digit36(lo)}"
}

private fun digit36(v: Int): Char = if (v < 10) ('0' + v) else ('A' + (v - 10))



