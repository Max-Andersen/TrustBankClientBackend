package com.trb_client.backend.models.response

import com.trb_client.backend.models.Sex
import java.time.LocalDate
import java.util.*


data class ClientInfo(
    val id: UUID? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val patronymic: String? = null,
    val birthDate: LocalDate? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val isClient: Boolean? = null,
    val isOfficer: Boolean? = null,
    val address: String? = null,
    val passportNumber: String? = null,
    val passportSeries: String? = null,
    val isBlocked: Boolean = false,
    val whoBlocked: ClientInfo? = null,
    val whoCreated: ClientInfo? = null,

    private val sex: Sex? = null
)
