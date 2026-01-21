package edu.istea.logic

import edu.istea.model.Entorno

enum class AlertState {
    NORMAL,
    ALERTA
}

data class AlertStatus(val state: AlertState, val message: String)

object AlertLogic {

    // Define constants for the rules based on our discussion
    private const val MIN_PH = 6.0
    private const val MAX_PH = 7.0
    private const val MIN_TEMP_AMBIENTE = 20.0
    private const val MAX_TEMP_AMBIENTE = 28.0
    private const val MIN_HUMEDAD_AMBIENTE = 40.0
    private const val MAX_HUMEDAD_AMBIENTE = 60.0
    private const val MIN_TEMP_TIERRA = 18.0
    private const val MAX_TEMP_TIERRA = 24.0

    private val HUMEDAD_TIERRA_ALERT_LEVELS = setOf("DRY", "DRY+")
    private val LUZ_ALERT_LEVELS = setOf("LOW-", "LOW", "LOW+", "NOR-", "NOR")

    /**
     * Analyzes the latest measurements and returns the most critical alert status.
     */
    fun getAlertStatus(latestMeasurements: List<Entorno>): AlertStatus {
        if (latestMeasurements.isEmpty()) {
            return AlertStatus(AlertState.NORMAL, "Sin mediciones recientes")
        }

        for (medicion in latestMeasurements) {
            val alertMessage = getAlertMessageFor(medicion)
            if (alertMessage != null) {
                return AlertStatus(AlertState.ALERTA, alertMessage) // Return the first alert found
            }
        }

        return AlertStatus(AlertState.NORMAL, "Todo en orden")
    }

    private fun getAlertMessageFor(medicion: Entorno): String? {
        val valorNumerico = medicion.valor.toDoubleOrNull()

        return when (medicion.tipo) {
            "Acidez de Tierra" -> when {
                valorNumerico == null -> null
                valorNumerico < MIN_PH -> "Alerta: pH de tierra muy bajo"
                valorNumerico > MAX_PH -> "Alerta: pH de tierra muy alto"
                else -> null
            }
            "Temperatura ambiente" -> when {
                valorNumerico == null -> null
                valorNumerico < MIN_TEMP_AMBIENTE -> "Alerta: Temperatura ambiente muy baja"
                valorNumerico > MAX_TEMP_AMBIENTE -> "Alerta: Temperatura ambiente muy alta"
                else -> null
            }
            "Humedad Ambiente" -> when {
                valorNumerico == null -> null
                valorNumerico < MIN_HUMEDAD_AMBIENTE -> "Alerta: Humedad ambiente muy baja"
                valorNumerico > MAX_HUMEDAD_AMBIENTE -> "Alerta: Humedad ambiente muy alta"
                else -> null
            }
            "Temperatura de Tierra" -> when {
                valorNumerico == null -> null
                valorNumerico < MIN_TEMP_TIERRA -> "Alerta: Temperatura de tierra muy baja"
                valorNumerico > MAX_TEMP_TIERRA -> "Alerta: Temperatura de tierra muy alta"
                else -> null
            }
            "Humedad de Tierra" -> {
                if (HUMEDAD_TIERRA_ALERT_LEVELS.contains(medicion.valor.uppercase())) "Alerta: Tierra muy seca"
                else null
            }
            "Luz a Hoja" -> {
                if (LUZ_ALERT_LEVELS.contains(medicion.valor.uppercase())) "Alerta: Niveles de luz bajos"
                else null
            }
            else -> null
        }
    }
}