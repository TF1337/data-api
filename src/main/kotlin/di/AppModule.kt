package nl.freek.vgwm.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import nl.freek.vgwm.config.AppConfig
import nl.freek.vgwm.data.external.HttpChatbotClient
import nl.freek.vgwm.data.external.LocalFilePhotoStorage
import nl.freek.vgwm.data.repository.ExposedCategoryRepository
import nl.freek.vgwm.data.repository.ExposedCheckpointRepository
import nl.freek.vgwm.data.repository.ExposedLocationRepository
import nl.freek.vgwm.data.repository.ExposedObservationRepository
import nl.freek.vgwm.data.repository.ExposedTourRepository
import nl.freek.vgwm.data.repository.ExposedUserRepository
import nl.freek.vgwm.data.security.BcryptPasswordHasher
import nl.freek.vgwm.data.security.JwtTokenProvider
import nl.freek.vgwm.domain.repository.CategoryRepository
import nl.freek.vgwm.domain.repository.CheckpointRepository
import nl.freek.vgwm.domain.repository.LocationRepository
import nl.freek.vgwm.domain.repository.ObservationRepository
import nl.freek.vgwm.domain.repository.TourRepository
import nl.freek.vgwm.domain.repository.UserRepository
import nl.freek.vgwm.domain.service.AuthService
import nl.freek.vgwm.domain.service.ChatbotClient
import nl.freek.vgwm.domain.service.ObservationCreationService
import nl.freek.vgwm.domain.service.PasswordHasher
import nl.freek.vgwm.domain.service.PhotoStorage
import nl.freek.vgwm.domain.service.TokenProvider
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Creates the application dependency injection module.
 */
fun appModule(appConfig: AppConfig): Module = module {
    single { appConfig }

    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                        encodeDefaults = true
                    }
                )
            }
        }
    }

    single<LocationRepository> { ExposedLocationRepository() }
    single<CategoryRepository> { ExposedCategoryRepository() }
    single<CheckpointRepository> { ExposedCheckpointRepository() }
    single<UserRepository> { ExposedUserRepository() }
    single<ObservationRepository> { ExposedObservationRepository() }
    single<TourRepository> { ExposedTourRepository() }

    single<PasswordHasher> { BcryptPasswordHasher() }
    single<TokenProvider> {
        JwtTokenProvider(
            secret = get<AppConfig>().jwt.secret,
            issuer = get<AppConfig>().jwt.issuer,
            audience = get<AppConfig>().jwt.audience,
            expirationHours = get<AppConfig>().jwt.expirationHours,
        )
    }
    single<PhotoStorage> { LocalFilePhotoStorage(uploadDir = get<AppConfig>().uploadDir) }
    single<ChatbotClient> {
        HttpChatbotClient(
            baseUrl = get<AppConfig>().chatbotApiUrl,
            httpClient = get(),
        )
    }

    single {
        ObservationCreationService(
            observations = get(),
            chatbot = get(),
            photoStorage = get(),
        )
    }
    single {
        AuthService(
            users = get(),
            passwordHasher = get(),
            tokenProvider = get(),
        )
    }
}