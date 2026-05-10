package nl.freek.vgwm.api.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import nl.freek.vgwm.api.dto.CompanyInfoDto
import nl.freek.vgwm.api.dto.ErrorResponseDto
import nl.freek.vgwm.domain.repository.CompanyInfo
import nl.freek.vgwm.domain.repository.CompanyInfoRepository

/**
 * Registers read-only company-info HTTP routes.
 */
fun Route.companyInfoRoutes(companyInfoRepository: CompanyInfoRepository) {
    route("/api/company-info") {
        get {
            val companyInfo = companyInfoRepository.findCurrent()
            if (companyInfo == null) {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = ErrorResponseDto(
                        message = "Company info was not found.",
                        code = "COMPANY_INFO_NOT_FOUND",
                    )
                )
                return@get
            }

            call.respond(companyInfo.toDto())
        }
    }
}

private fun CompanyInfo.toDto(): CompanyInfoDto = CompanyInfoDto(
    companyName = companyName,
    displayName = displayName,
    primaryColorHex = primaryColorHex,
    secondaryColorHex = secondaryColorHex,
    defaultLanguage = defaultLanguage,
)
