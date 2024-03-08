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
    val password: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val passportNumber: String? = null,
    val passportSeries: String? = null,
    val isBlocked: Boolean = false,
    val whoBlocked: OfficerInfo? = null,
    val whoCreated: OfficerInfo? = null,

    private val sex: Sex? = null
)
