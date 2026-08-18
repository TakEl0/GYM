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
import com.gym.app.data.local.dao.DaoEjercicio
import com.gym.app.data.local.dao.DaoEntrenamiento
import com.gym.app.data.local.dao.DaoGimnasio
import com.gym.app.data.local.dao.DaoIngesta
import com.gym.app.data.local.dao.DaoListaCompra
import com.gym.app.data.local.dao.DaoMapeoAprendido
import com.gym.app.data.local.dao.DaoPerfilUsuario
import com.gym.app.data.local.dao.DaoPlanComida
import com.gym.app.data.local.dao.DaoRegistroPeso
import com.gym.app.data.local.dao.DaoRutina
import com.gym.app.data.local.dao.DaoSerieRealizada
import com.gym.app.data.local.dao.DaoSesionEntrenamiento
import com.gym.app.data.local.dao.DaoUsuarioPerfil
import com.gym.app.data.local.entidad.Converters
import com.gym.app.data.local.entidad.EntidadBloqueRutina
import com.gym.app.data.local.entidad.EntidadComida
import com.gym.app.data.local.entidad.EntidadEjercicio
import com.gym.app.data.local.entidad.EntidadEntrenamiento
import com.gym.app.data.local.entidad.EntidadGimnasio
import com.gym.app.data.local.entidad.EntidadIngestaRegistrada
import com.gym.app.data.local.entidad.EntidadIngredienteToma
import com.gym.app.data.local.entidad.EntidadItemListaCompra
import com.gym.app.data.local.entidad.EntidadListaCompra
import com.gym.app.data.local.entidad.EntidadMaquina
import com.gym.app.data.local.entidad.EntidadMapeoAprendido
import com.gym.app.data.local.entidad.EntidadPerfilUsuario
import com.gym.app.data.local.entidad.EntidadPlanComida
import com.gym.app.data.local.entidad.EntidadRegistroPeso
import com.gym.app.data.local.entidad.EntidadRutina
import com.gym.app.data.local.entidad.EntidadSerieRealizada
import com.gym.app.data.local.entidad.EntidadSesionEntrenamiento
import com.gym.app.data.local.entidad.EntidadToma
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
        EntidadComida::class,
        EntidadPerfilUsuario::class,
        EntidadPlanComida::class,
        EntidadToma::class,
        EntidadIngredienteToma::class,
        EntidadIngestaRegistrada::class,
        EntidadListaCompra::class,
        EntidadItemListaCompra::class,
        EntidadGimnasio::class,
        EntidadMaquina::class,
        EntidadEjercicio::class,
        EntidadRutina::class,
        EntidadBloqueRutina::class,
        EntidadSesionEntrenamiento::class,
        EntidadSerieRealizada::class,
        EntidadMapeoAprendido::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BaseDeDatosGYM : RoomDatabase() {

    abstract fun daoUsuarioPerfil(): DaoUsuarioPerfil
    abstract fun daoRegistroPeso(): DaoRegistroPeso
    abstract fun daoEntrenamiento(): DaoEntrenamiento
    abstract fun daoComida(): DaoComida
    abstract fun daoPerfilUsuario(): DaoPerfilUsuario
    abstract fun daoPlanComida(): DaoPlanComida
    abstract fun daoIngesta(): DaoIngesta
    abstract fun daoListaCompra(): DaoListaCompra
    abstract fun daoGimnasio(): DaoGimnasio
    abstract fun daoEjercicio(): DaoEjercicio
    abstract fun daoRutina(): DaoRutina
    abstract fun daoSesionEntrenamiento(): DaoSesionEntrenamiento
    abstract fun daoSerieRealizada(): DaoSerieRealizada
    abstract fun daoMapeoAprendido(): DaoMapeoAprendido

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