package com.example.ep2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataBaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_NAME = "recetas.db"
        private const val DATABASE_VERSION = 2

        // Tabla Usuarios
        private const val TABLE_USUARIOS = "usuarios"
        private const val COL_ID = "id"
        private const val COL_NOMBRE = "nombre"
        private const val COL_CORREO = "correo"
        private const val COL_PASSWORD = "password"

        // Tabla Favoritos
        private const val TABLE_FAVORITOS = "favoritos"
        private const val COL_FAV_ID = "id"
        private const val COL_RECETA_ID = "receta_id"
        private const val COL_TITULO = "titulo"
        private const val COL_TIEMPO = "tiempo"
        private const val COL_PORCIONES = "porciones"
        private const val COL_IMAGEN = "imagen"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val crearTablaUsuario = """
            CREATE TABLE $TABLE_USUARIOS(
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_NOMBRE TEXT NOT NULL,
            $COL_CORREO TEXT NOT NULL,
            $COL_PASSWORD TEXT NOT NULL
            )
        """.trimIndent()
        db?.execSQL(crearTablaUsuario)

        val crearTablaFavoritos = """
            CREATE TABLE $TABLE_FAVORITOS(
            $COL_FAV_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_RECETA_ID INTEGER NOT NULL,
            $COL_TITULO TEXT NOT NULL,
            $COL_TIEMPO INTEGER,
            $COL_PORCIONES INTEGER,
            $COL_IMAGEN TEXT
            )
        """.trimIndent()
        db?.execSQL(crearTablaFavoritos)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITOS")
        onCreate(db)
    }

    // Métodos para Usuarios
    fun validarUsuario(correo: String, pass: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USUARIOS WHERE $COL_CORREO = ? AND $COL_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(correo, pass))
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    fun insertarUsuario(nombre: String, correo: String, pass: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_NOMBRE, nombre)
            put(COL_CORREO, correo)
            put(COL_PASSWORD, pass)
        }
        val resultado = db.insert(TABLE_USUARIOS, null, values)
        return resultado != -1L
    }

    // Métodos para Favoritos
    fun esFavorito(recetaId: Int): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FAVORITOS WHERE $COL_RECETA_ID = ?", arrayOf(recetaId.toString()))
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    fun agregarFavorito(recetaId: Int, titulo: String, tiempo: Int, porciones: Int, imagen: String?): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_RECETA_ID, recetaId)
            put(COL_TITULO, titulo)
            put(COL_TIEMPO, tiempo)
            put(COL_PORCIONES, porciones)
            put(COL_IMAGEN, imagen ?: "")
        }
        val id = db.insert(TABLE_FAVORITOS, null, values)
        return id != -1L
    }

    fun eliminarFavorito(recetaId: Int): Boolean {
        val db = this.writableDatabase
        val filas = db.delete(TABLE_FAVORITOS, "$COL_RECETA_ID = ?", arrayOf(recetaId.toString()))
        return filas > 0
    }
}
