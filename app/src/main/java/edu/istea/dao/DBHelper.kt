package edu.istea.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import edu.istea.model.Alimentacion
import edu.istea.model.Entorno
import edu.istea.model.Etapa
import edu.istea.model.HistorialEvento
import edu.istea.model.Planta
import edu.istea.model.User
import edu.istea.views.HistorialFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MisRegistros.db"
        private const val DATABASE_VERSION = 18 // Incremented version to force clean upgrade

        // Define all table and column names as constants
        private const val TABLE_USER = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_SURNAME = "surname"
        private const val COLUMN_NPILA = "npila"
        private const val COLUMN_PASS = "pass"

        private const val TABLE_PLANTAS = "plantas"
        private const val COLUMN_PLANTA_ID = "id"
        private const val COLUMN_PLANTA_NOMBRE = "nombre"
        private const val COLUMN_PLANTA_GENETICA = "genetica"
        private const val COLUMN_PLANTA_FECHA_ORIGEN = "fecha_origen"

        private const val TABLE_ETAPAS = "etapas"
        private const val COLUMN_ETAPA_ID = "id"
        private const val COLUMN_ETAPA_PLANTA_ID = "planta_id"
        private const val COLUMN_ETAPA_PLANTA_NOMBRE = "planta_nombre"
        private const val COLUMN_ETAPA_ESTADO = "estado"
        private const val COLUMN_ETAPA_FECHA = "fecha"

        private const val TABLE_ENTORNO = "entorno"
        private const val COLUMN_ENTORNO_ID = "id"
        private const val COLUMN_ENTORNO_PLANTA_ID = "planta_id"
        private const val COLUMN_ENTORNO_PLANTA_NOMBRE = "planta_nombre"
        private const val COLUMN_ENTORNO_FECHA = "fecha"
        private const val COLUMN_ENTORNO_TIPO = "tipo"
        private const val COLUMN_ENTORNO_VALOR = "valor"
        private const val COLUMN_ENTORNO_UNIDAD = "unidad"

        private const val TABLE_ALIMENTACION = "alimentacion"
        private const val COLUMN_ALIMENTACION_ID = "id"
        private const val COLUMN_ALIMENTACION_PLANTA_ID = "planta_id"
        private const val COLUMN_ALIMENTACION_PLANTA_NOMBRE = "planta_nombre"
        private const val COLUMN_ALIMENTACION_FECHA = "fecha"
        private const val COLUMN_ALIMENTACION_INSUMO = "insumo"
        private const val COLUMN_ALIMENTACION_CANTIDAD = "cantidad"
        private const val COLUMN_ALIMENTACION_UNIDAD = "unidad"

        private const val TABLE_HISTORIAL = "historial"
        private const val COLUMN_HISTORIAL_ID = "id"
        private const val COLUMN_HISTORIAL_FECHA = "fecha"
        private const val COLUMN_HISTORIAL_TIPO_EVENTO = "tipo_evento"
        private const val COLUMN_HISTORIAL_DESCRIPCION = "descripcion"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableUser = ("CREATE TABLE " + TABLE_USER +
                "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME + " TEXT," +
                COLUMN_SURNAME + " TEXT," +
                COLUMN_NPILA + " TEXT," +
                COLUMN_PASS + " TEXT )"
                )
        db.execSQL(createTableUser)

        val createTablePlantas = ("CREATE TABLE " + TABLE_PLANTAS +
                "(" + COLUMN_PLANTA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_PLANTA_NOMBRE + " TEXT," +
                COLUMN_PLANTA_GENETICA + " TEXT," +
                COLUMN_PLANTA_FECHA_ORIGEN + " TEXT )"
                )
        db.execSQL(createTablePlantas)

        val createTableEtapas = ("CREATE TABLE " + TABLE_ETAPAS +
                "(" + COLUMN_ETAPA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_ETAPA_PLANTA_ID + " INTEGER," +
                COLUMN_ETAPA_PLANTA_NOMBRE + " TEXT," +
                COLUMN_ETAPA_ESTADO + " TEXT," +
                COLUMN_ETAPA_FECHA + " TEXT )"
                )
        db.execSQL(createTableEtapas)

        val createTableEntorno = ("CREATE TABLE " + TABLE_ENTORNO +
                "(" + COLUMN_ENTORNO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_ENTORNO_PLANTA_ID + " INTEGER," +
                COLUMN_ENTORNO_PLANTA_NOMBRE + " TEXT," +
                COLUMN_ENTORNO_FECHA + " TEXT," +
                COLUMN_ENTORNO_TIPO + " TEXT," +
                COLUMN_ENTORNO_VALOR + " TEXT," +
                COLUMN_ENTORNO_UNIDAD + " TEXT )"
                )
        db.execSQL(createTableEntorno)

        val createTableAlimentacion = ("CREATE TABLE " + TABLE_ALIMENTACION +
                "(" + COLUMN_ALIMENTACION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_ALIMENTACION_PLANTA_ID + " INTEGER," +
                COLUMN_ALIMENTACION_PLANTA_NOMBRE + " TEXT," +
                COLUMN_ALIMENTACION_FECHA + " TEXT," +
                COLUMN_ALIMENTACION_INSUMO + " TEXT," +
                COLUMN_ALIMENTACION_CANTIDAD + " REAL," +
                COLUMN_ALIMENTACION_UNIDAD + " TEXT )"
                )
        db.execSQL(createTableAlimentacion)

        val createTableHistorial = ("CREATE TABLE " + TABLE_HISTORIAL +
                "(" + COLUMN_HISTORIAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_HISTORIAL_FECHA + " TEXT," +
                COLUMN_HISTORIAL_TIPO_EVENTO + " TEXT," +
                COLUMN_HISTORIAL_DESCRIPCION + " TEXT )"
                )
        db.execSQL(createTableHistorial)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w("DBHelper", "Upgrading database from version $oldVersion to $newVersion, which will destroy all old data")
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANTAS)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ETAPAS)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTORNO)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALIMENTACION)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORIAL)
        onCreate(db)
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun saveHistorialEvento(db: SQLiteDatabase, tipo: String, descripcion: String) {
        val values = ContentValues()
        values.put(COLUMN_HISTORIAL_FECHA, getCurrentDate())
        values.put(COLUMN_HISTORIAL_TIPO_EVENTO, tipo)
        values.put(COLUMN_HISTORIAL_DESCRIPCION, descripcion)
        db.insert(TABLE_HISTORIAL, null, values)
    }

    private fun <T> performDbOperation(operation: (SQLiteDatabase) -> T): T? {
        val db = this.writableDatabase
        var result: T? = null
        db.beginTransaction()
        try {
            result = operation(db)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("DBHelper", "Database operation failed", e)
        } finally {
            db.endTransaction()
        }
        return result
    }

    fun plantaNombreExiste(nombre: String, plantaId: Int? = null): Boolean {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            val query: String
            val args: Array<String>

            if (plantaId != null && plantaId != 0) {
                query = "SELECT $COLUMN_PLANTA_ID FROM $TABLE_PLANTAS WHERE $COLUMN_PLANTA_NOMBRE = ? AND $COLUMN_PLANTA_ID != ?"
                args = arrayOf(nombre, plantaId.toString())
            } else {
                query = "SELECT $COLUMN_PLANTA_ID FROM $TABLE_PLANTAS WHERE $COLUMN_PLANTA_NOMBRE = ?"
                args = arrayOf(nombre)
            }
            cursor = db.rawQuery(query, args)
            return cursor.count > 0
        } finally {
            cursor?.close()
        }
    }

    fun savePlanta(planta: Planta) {
        performDbOperation { db ->
            val values = ContentValues()
            values.put(COLUMN_PLANTA_NOMBRE, planta.nombre)
            values.put(COLUMN_PLANTA_GENETICA, planta.genetica)
            values.put(COLUMN_PLANTA_FECHA_ORIGEN, planta.fechaOrigen)
            val id = db.insert(TABLE_PLANTAS, null, values)
            if (id != -1L) {
                 saveHistorialEvento(db, "Nueva Planta", "Se creó la planta '${planta.nombre}'.")
            }
        }
    }

    fun updatePlanta(planta: Planta) {
        performDbOperation { db ->
            val values = ContentValues()
            values.put(COLUMN_PLANTA_NOMBRE, planta.nombre)
            values.put(COLUMN_PLANTA_GENETICA, planta.genetica)
            values.put(COLUMN_PLANTA_FECHA_ORIGEN, planta.fechaOrigen)
            val updatedRows = db.update(TABLE_PLANTAS, values, "$COLUMN_PLANTA_ID = ?", arrayOf(planta.id.toString()))
            if (updatedRows > 0) {
                saveHistorialEvento(db, "Planta Actualizada", "Se actualizó la planta '${planta.nombre}'.")
            }
        }
    }

    fun deletePlanta(plantaId: Int, plantaNombre: String) {
        performDbOperation { db ->
            db.delete(TABLE_ETAPAS, "$COLUMN_ETAPA_PLANTA_ID = ?", arrayOf(plantaId.toString()))
            db.delete(TABLE_ENTORNO, "$COLUMN_ENTORNO_PLANTA_ID = ?", arrayOf(plantaId.toString()))
            db.delete(TABLE_ALIMENTACION, "$COLUMN_ALIMENTACION_PLANTA_ID = ?", arrayOf(plantaId.toString()))

            val deletedRows = db.delete(TABLE_PLANTAS, "$COLUMN_PLANTA_ID = ?", arrayOf(plantaId.toString()))
            if (deletedRows > 0) {
                saveHistorialEvento(db, "Planta Eliminada", "Se eliminó la planta '${plantaNombre}' y todos sus eventos.")
            }
        }
    }

    fun saveEtapa(etapa: Etapa) {
         performDbOperation { db ->
            val values = ContentValues()
            values.put(COLUMN_ETAPA_PLANTA_ID, etapa.plantaId)
            values.put(COLUMN_ETAPA_PLANTA_NOMBRE, etapa.plantaNombre)
            values.put(COLUMN_ETAPA_ESTADO, etapa.estado)
            values.put(COLUMN_ETAPA_FECHA, etapa.fecha)
            val id = db.insert(TABLE_ETAPAS, null, values)
             if (id != -1L) {
                saveHistorialEvento(db, "Etapa", "'${etapa.plantaNombre}' cambió a la etapa '${etapa.estado}'.")
             }
        }
    }

    fun deleteEtapa(etapaId: Int) {
        performDbOperation { db ->
            val deletedRows = db.delete(TABLE_ETAPAS, "$COLUMN_ETAPA_ID = ?", arrayOf(etapaId.toString()))
            if (deletedRows > 0) {
                saveHistorialEvento(db, "Etapa Eliminada", "Se eliminó un registro de etapa.")
            }
        }
    }

    fun saveEntorno(entorno: Entorno) {
        performDbOperation { db ->
            val values = ContentValues()
            values.put(COLUMN_ENTORNO_PLANTA_ID, entorno.plantaId)
            values.put(COLUMN_ENTORNO_PLANTA_NOMBRE, entorno.plantaNombre)
            values.put(COLUMN_ENTORNO_FECHA, entorno.fecha)
            values.put(COLUMN_ENTORNO_TIPO, entorno.tipo)
            values.put(COLUMN_ENTORNO_VALOR, entorno.valor)
            values.put(COLUMN_ENTORNO_UNIDAD, entorno.unidad)
            val id = db.insert(TABLE_ENTORNO, null, values)
            if (id != -1L) {
                saveHistorialEvento(db, "Entorno", "Medición para '${entorno.plantaNombre}': ${entorno.tipo} de ${entorno.valor} ${entorno.unidad}.")
            }
        }
    }

    fun updateEntorno(entorno: Entorno) {
        performDbOperation { db ->
            val values = ContentValues()
            values.put(COLUMN_ENTORNO_PLANTA_ID, entorno.plantaId)
            values.put(COLUMN_ENTORNO_PLANTA_NOMBRE, entorno.plantaNombre)
            values.put(COLUMN_ENTORNO_FECHA, entorno.fecha)
            values.put(COLUMN_ENTORNO_TIPO, entorno.tipo)
            values.put(COLUMN_ENTORNO_VALOR, entorno.valor)
            values.put(COLUMN_ENTORNO_UNIDAD, entorno.unidad)
            val updatedRows = db.update(TABLE_ENTORNO, values, "$COLUMN_ENTORNO_ID = ?", arrayOf(entorno.id.toString()))
            if (updatedRows > 0) {
                saveHistorialEvento(db, "Entorno Actualizado", "Se actualizó una medición de entorno para '${entorno.plantaNombre}'.")
            }
        }
    }

    fun deleteEntorno(entornoId: Int) {
        performDbOperation { db ->
            val deletedRows = db.delete(TABLE_ENTORNO, "$COLUMN_ENTORNO_ID = ?", arrayOf(entornoId.toString()))
            if (deletedRows > 0) {
                saveHistorialEvento(db, "Entorno Eliminado", "Se eliminó una medición de entorno.")
            }
        }
    }

    fun saveAlimentacion(alimentacion: Alimentacion) {
        performDbOperation { db ->
            val values = ContentValues()
            values.put(COLUMN_ALIMENTACION_PLANTA_ID, alimentacion.plantaId)
            values.put(COLUMN_ALIMENTACION_PLANTA_NOMBRE, alimentacion.plantaNombre)
            values.put(COLUMN_ALIMENTACION_FECHA, alimentacion.fecha)
            values.put(COLUMN_ALIMENTACION_INSUMO, alimentacion.insumo)
            values.put(COLUMN_ALIMENTACION_CANTIDAD, alimentacion.cantidad)
            values.put(COLUMN_ALIMENTACION_UNIDAD, alimentacion.unidad)
            val id = db.insert(TABLE_ALIMENTACION, null, values)
            if (id != -1L) {
                 saveHistorialEvento(db, "Alimentación", "Se añadió ${alimentacion.cantidad} ${alimentacion.unidad} de '${alimentacion.insumo}' a '${alimentacion.plantaNombre}'.")
            }
        }
    }

    fun deleteAlimentacion(alimentacionId: Int) {
        performDbOperation { db ->
            val deletedRows = db.delete(TABLE_ALIMENTACION, "$COLUMN_ALIMENTACION_ID = ?", arrayOf(alimentacionId.toString()))
            if (deletedRows > 0) {
                saveHistorialEvento(db, "Alimentación Eliminada", "Se eliminó un registro de alimentación.")
            }
        }
    }

    fun clearHistorial() {
        performDbOperation { db ->
            db.delete(TABLE_HISTORIAL, null, null)
        }
    }

    fun getPlanta(id: Int): Planta? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_PLANTAS, null, "$COLUMN_PLANTA_ID = ?", arrayOf(id.toString()), null, null, null)
        if (cursor.moveToFirst()) {
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLANTA_NOMBRE))
            val genetica = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLANTA_GENETICA))
            val fechaOrigen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLANTA_FECHA_ORIGEN))
            cursor.close()
            return Planta(id, nombre, genetica, fechaOrigen)
        }
        cursor.close()
        return null
    }

    fun getFilteredHistorialEventos(filter: HistorialFilter?): List<HistorialEvento> {
        if (filter == null) {
            return getAllHistorialEventos()
        }

        val eventos = mutableListOf<HistorialEvento>()
        val db = this.readableDatabase
        
        val selectionClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        filter.plantaId?.let {
            getPlanta(it)?.let {
                selectionClauses.add("$COLUMN_HISTORIAL_DESCRIPCION LIKE ?")
                selectionArgs.add("%${it.nombre}%")
            }
        }

        filter.tipoRegistro?.let {
            selectionClauses.add("$COLUMN_HISTORIAL_TIPO_EVENTO = ?")
            selectionArgs.add(it)
        }

        try {
            val inputFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            filter.fechaDesde?.let {
                val date = inputFormat.parse(it)
                if (date != null) {
                    selectionClauses.add("date($COLUMN_HISTORIAL_FECHA) >= date(?)")
                    selectionArgs.add(dbFormat.format(date))
                }
            }
            filter.fechaHasta?.let {
                val date = inputFormat.parse(it)
                if (date != null) {
                    selectionClauses.add("date($COLUMN_HISTORIAL_FECHA) <= date(?)")
                    selectionArgs.add(dbFormat.format(date))
                }
            }
        } catch (e: Exception) {
            Log.e("DBHelper", "Error parsing dates for filtering", e)
        }

        val selection = selectionClauses.joinToString(separator = " AND ")
        val cursor = db.query(TABLE_HISTORIAL, null, selection, selectionArgs.toTypedArray(), null, null, "$COLUMN_HISTORIAL_ID DESC")

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HISTORIAL_ID))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORIAL_FECHA))
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORIAL_TIPO_EVENTO))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORIAL_DESCRIPCION))
                eventos.add(HistorialEvento(id, fecha, tipo, desc))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return eventos
    }
    
    fun getAllHistorialEventos(): List<HistorialEvento> {
        val eventos = mutableListOf<HistorialEvento>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_HISTORIAL ORDER BY $COLUMN_HISTORIAL_ID DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HISTORIAL_ID))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORIAL_FECHA))
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORIAL_TIPO_EVENTO))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORIAL_DESCRIPCION))
                eventos.add(HistorialEvento(id, fecha, tipo, desc))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return eventos
    }
    
    fun getAllPlantas(): List<Planta> {
        val plantas = mutableListOf<Planta>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PLANTAS ORDER BY $COLUMN_PLANTA_ID DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLANTA_ID))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLANTA_NOMBRE))
                val genetica = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLANTA_GENETICA))
                val fechaOrigen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLANTA_FECHA_ORIGEN))
                plantas.add(Planta(id, nombre, genetica, fechaOrigen))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return plantas
    }

    fun getAllEtapas(): List<Etapa> {
        val etapas = mutableListOf<Etapa>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ETAPAS ORDER BY $COLUMN_ETAPA_ID DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ETAPA_ID))
                val plantaId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ETAPA_PLANTA_ID))
                val plantaNombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ETAPA_PLANTA_NOMBRE))
                val estado = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ETAPA_ESTADO))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ETAPA_FECHA))
                etapas.add(Etapa(id, plantaId, plantaNombre, estado, fecha))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return etapas
    }

    fun getAllEntornos(): List<Entorno> {
        val entornos = mutableListOf<Entorno>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ENTORNO ORDER BY $COLUMN_ENTORNO_ID DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENTORNO_ID))
                val plantaId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENTORNO_PLANTA_ID))
                val plantaNombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTORNO_PLANTA_NOMBRE))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTORNO_FECHA))
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTORNO_TIPO))
                val valor = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTORNO_VALOR))
                val unidad = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENTORNO_UNIDAD))
                entornos.add(Entorno(id, plantaId, plantaNombre, fecha, tipo, valor, unidad))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return entornos
    }

    fun getAllAlimentacion(): List<Alimentacion> {
        val alimentaciones = mutableListOf<Alimentacion>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ALIMENTACION ORDER BY $COLUMN_ALIMENTACION_ID DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ALIMENTACION_ID))
                val plantaId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ALIMENTACION_PLANTA_ID))
                val plantaNombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALIMENTACION_PLANTA_NOMBRE))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALIMENTACION_FECHA))
                val insumo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALIMENTACION_INSUMO))
                val cantidad = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ALIMENTACION_CANTIDAD))
                val unidad = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALIMENTACION_UNIDAD))
                alimentaciones.add(Alimentacion(id, plantaId, plantaNombre, fecha, insumo, cantidad, unidad))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return alimentaciones
    }

    fun saveUser(user: User) {
        performDbOperation { db ->
            val values = ContentValues()
            values.put(COLUMN_NAME, user.name)
            values.put(COLUMN_SURNAME, user.surname)
            values.put(COLUMN_NPILA, user.npila)
            values.put(COLUMN_PASS, user.pass)
            db.insert(TABLE_USER, null, values)
        }
    }

    fun validateUser(user: User): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USER WHERE $COLUMN_NAME = ? AND $COLUMN_PASS = ?"
        val cursor = db.rawQuery(query, arrayOf(user.name, user.pass))
        val userExists = cursor.count > 0
        cursor.close()
        return userExists
    }

    fun userActual(name: String, password: String):Int {
        val db = this.readableDatabase
        var idUser = 0
        val query= "SELECT $COLUMN_ID FROM $TABLE_USER WHERE $COLUMN_NAME = ? AND $COLUMN_PASS = ?"
        val cursor = db.rawQuery(query, arrayOf(name, password))
        if(cursor.moveToFirst()){
            idUser =  cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
        }
        cursor.close()
        return idUser
    }

    fun getUserInfo(userId: Number): User? {
        val db = this.readableDatabase
        var user: User? = null
        val cursor = db.query(TABLE_USER, arrayOf(COLUMN_NAME, COLUMN_SURNAME, COLUMN_NPILA, COLUMN_PASS),
            "$COLUMN_ID = ?", arrayOf(userId.toString()), null, null, null)
        if(cursor.moveToFirst()){
            val name =  cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val surname =  cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SURNAME))
            val npila =  cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NPILA))
            val password =  cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASS))
            user = User(name,surname,npila,password)
        }
        cursor.close()
        return user
    }
}