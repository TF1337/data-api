package nl.freek.vgwm.domain.value

/**
 * Structured incident context represented as Who/What/Where/When/Why/How.
 */
data class FiveW1H(
    val who: String,
    val what: String,
    val where: String,
    val whenText: String,
    val why: String,
    val how: String,
) {
    init {
        require(who.isNotBlank()) { "FiveW1H.who must not be blank." }
        require(what.isNotBlank()) { "FiveW1H.what must not be blank." }
        require(where.isNotBlank()) { "FiveW1H.where must not be blank." }
        require(whenText.isNotBlank()) { "FiveW1H.whenText must not be blank." }
        require(why.isNotBlank()) { "FiveW1H.why must not be blank." }
        require(how.isNotBlank()) { "FiveW1H.how must not be blank." }
    }
}
