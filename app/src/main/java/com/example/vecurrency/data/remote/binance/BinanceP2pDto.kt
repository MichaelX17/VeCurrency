package com.example.vecurrency.data.remote.binance

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class DolarApiResponse(
    val moneda: String? = null,
    val fuente: String? = null,
    val nombre: String? = null,
    val promedio: JsonPrimitive? = null,
    val fechaActualizacion: String? = null
)

@Serializable
data class YadioResponse(
    val USD: YadioUsdData? = null
)

@Serializable
data class YadioUsdData(
    val rate: JsonPrimitive? = null
)
