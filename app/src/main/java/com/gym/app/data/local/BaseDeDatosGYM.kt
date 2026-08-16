/**
 * @file BaseDeDatosGYM.kt
 * @brief Base de datos principal de Room para la aplicación GYM.
 */
package com.gym.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gym.app.data.local.dao.DaoComida
import com.gym.app.data.local.dao.DaoEntrenamiento
import com.gym.app.data.local.dao.DaoRegistroPeso
import com.gym.app.data.local.dao.DaoUsuarioPerfil
import com.gym.app.data.local.entidad.Converters
import com.gym.app.data.local.entidad.EntidadComida
import com.gym.app.data.local.entidad.EntidadEntrenamiento
import com.gym.app.data.local.entidad.EntidadRegistroPeso
import com.gym.app.data.local.entidad.EntidadUsuarioPerfil

/**
 * @class BaseDeDatosGYM
 * @brief Clase abstracta que define la configuración y DAOs de la base de datos Room.
 */
@Database(
    entities = [
        EntidadUsuarioPerfil::class,
        EntidadRegistroPeso::class,
        EntidadEntrenamiento::class,
        EntidadComida::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BaseDeDatosGYM : RoomDatabase() {

    abstract fun daoUsuarioPerfil(): DaoUsuarioPerfil
    abstract fun daoRegistroPeso(): DaoRegistroPeso
    abstract fun daoEntrenamiento(): DaoEntrenamiento
    abstract fun daoComida(): DaoComida

    companion object {
        @Volatile
        private var INSTANCE: BaseDeDatosGYM? = null

        /**
         * @brief Obtiene la instancia singleton de la base de datos Room.
         * @param context Contexto de la aplicación.
         * @return Instancia de [BaseDeDatosGYM].
         */
        fun obtenerInstancia(context: Context): BaseDeDatosGYM {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BaseDeDatosGYM::class.java,
                    "gym_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
