package nl.freek.vgwm.domain.value

/**
 * Plan-Do-Check-Act cycle details for improvement execution.
 */
data class PdcaCycle(
    val plan: String,
    val doStep: String,
    val check: String,
    val act: String,
) {
    init {
        require(plan.isNotBlank()) { "PdcaCycle.plan must not be blank." }
        require(doStep.isNotBlank()) { "PdcaCycle.doStep must not be blank." }
        require(check.isNotBlank()) { "PdcaCycle.check must not be blank." }
        require(act.isNotBlank()) { "PdcaCycle.act must not be blank." }
    }
}
