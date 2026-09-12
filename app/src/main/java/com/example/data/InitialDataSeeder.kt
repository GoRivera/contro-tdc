package com.example.data

object InitialDataSeeder {
    /**
     * En la versión de producción, la base de datos se inicializa limpia,
     * sin datos personales ni registros heredados del proceso de construcción.
     */
    @Suppress("UNUSED_PARAMETER")
    suspend fun seedDatabase(database: AppDatabase) {
        // Inicialización limpia en producción.
    }
}
