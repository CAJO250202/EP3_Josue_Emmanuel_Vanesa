package com.example.ep2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// CREAR CLASE DATABASEHELPER QUE HEREDA DE LA CLASE SQLITEOPENHELPER
class DataBaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    // DATOS DE LA BASE DE DATOS
    companion object {
        // INFORMACIÓN DE LA BD
        private const val DATABASE_NAME = "recetas.db"
        private const val DATABASE_VERSION = 3

        // TABLA USUARIOS
        private const val TABLE_USUARIOS = "usuarios"
        private const val COL_ID = "id"
        private const val COL_NOMBRE = "nombre"
        private const val COL_CORREO = "correo"
        private const val COL_PASSWORD = "password"

        // TABLA FAVORITOS
        private const val TABLE_FAVORITOS = "favoritos"
        private const val COL_FAV_ID = "id"
        private const val COL_RECETA_ID = "receta_id"
        private const val COL_TITULO = "titulo"
        private const val COL_TIEMPO = "tiempo"
        private const val COL_PORCIONES = "porciones"
        private const val COL_IMAGEN = "imagen"

        // CREAR AMBAS TABLAS DE LA BASE DE DATOS
        private const val CREATE_TABLE_USUARIOS = """
            CREATE TABLE $TABLE_USUARIOS(
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_NOMBRE TEXT NOT NULL,
            $COL_CORREO TEXT NOT NULL,
            $COL_PASSWORD TEXT NOT NULL
            )
        """

        private const val CREATE_TABLE_FAVORITOS = """
            CREATE TABLE $TABLE_FAVORITOS(
            $COL_FAV_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_RECETA_ID INTEGER UNIQUE,
            $COL_TITULO TEXT NOT NULL,
            $COL_TIEMPO INTEGER,
            $COL_PORCIONES INTEGER,
            $COL_IMAGEN TEXT
            )
        """
    }

    // METODO ONCREATE -- para crear la base de datos
    override fun onCreate(db: SQLiteDatabase?) {
        // CREAR TABLA DE USUARIO Y FAVORITOS
        db?.execSQL(CREATE_TABLE_USUARIOS)
        db?.execSQL(CREATE_TABLE_FAVORITOS)
    }

    // METODO ONUPGRADE
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db?.execSQL(CREATE_TABLE_FAVORITOS)
        }
    }

    // METODO PARA REGISTRAR UN USUARIO
    fun registrarUsuario(nombre: String, corre: String, password: String): Boolean {
        // CREAR VARIABLE PARA ESCRIBIR EN LA BD
        val db = writableDatabase

        // INSERTAR LOS DATOS
        val datos = ContentValues().apply {
            put(COL_NOMBRE, nombre)
            put(COL_CORREO, corre)
            put(COL_PASSWORD, password)
        }

        // EJECUTAR INSERCION DE DATOS
        val resultado = db.insert(TABLE_USUARIOS, null, datos)

        // CERRAR CONEXION DE LA DB
        db.close()

        // REGRESAR EL VALOR OBTENIDO DE LA INSERCION ( verdadero o falso )
        return resultado != -1L
    }

    fun insertarUsuario(nombre: String, correo: String, pass: String): Boolean {
        return registrarUsuario(nombre, correo, pass)
    }

    // METODO PARA INICIAR SESION (CONSULTA)
    fun iniciarSesion(corre: String, password: String): Boolean {
        // VARIABLE QUE LEE LA BASE DE DATOS
        val db = readableDatabase

        // CREAR CONSULTA
        val querySql = """
            SELECT * FROM $TABLE_USUARIOS
            WHERE $COL_CORREO = ? AND $COL_PASSWORD = ?
        """.trimIndent()

        // CREAR CURSOR QUE PERMITA RECORRER TODOS LOS REGISTROS
        val cursor = db.rawQuery(
            querySql, arrayOf(corre, password)
        )

        // VERIFICAR CUANTOS RESULTADOS ENCUENTRA EL CURSOR
        val usuarioEncontrado = cursor.count > 0
        cursor.close()
        db.close()

        return usuarioEncontrado
    }

    fun validarUsuario(correo: String, pass: String): Boolean {
        return iniciarSesion(correo, pass)
    }

    // METODO PARA GUARDAR RECETA EN FAVORITOS
    fun guardarRecetaFavorita(recetaId: Int, titulo: String, tiempo: Int, porciones: Int, imagen: String?): Boolean {
        val db = writableDatabase

        // crear variable para pasar los valores del API
        val valores = ContentValues().apply {
            put(COL_RECETA_ID, recetaId)
            put(COL_TITULO, titulo)
            put(COL_TIEMPO, tiempo)
            put(COL_PORCIONES, porciones)
            put(COL_IMAGEN, imagen ?: "")
        }

        val resultado = db.insertWithOnConflict(
            TABLE_FAVORITOS,
            null,
            valores,
            SQLiteDatabase.CONFLICT_IGNORE
        )

        db.close()
        return resultado != -1L
    }

    fun agregarFavorito(recetaId: Int, titulo: String, tiempo: Int, porciones: Int, imagen: String?): Boolean {
        return guardarRecetaFavorita(recetaId, titulo, tiempo, porciones, imagen)
    }

    // METODO PARA ELIMINAR RECETA DE FAVORITOS
    fun eliminarFavorito(recetaId: Int): Boolean {
        val db = writableDatabase
        val filasAfectadas = db.delete(
            TABLE_FAVORITOS,
            "$COL_RECETA_ID = ?",
            arrayOf(recetaId.toString())
        )
        db.close()
        return filasAfectadas > 0
    }

    fun eliminarRecetaFavorita(recetaId: Int): Boolean {
        return eliminarFavorito(recetaId)
    }

    // METODO PARA VERIFICAR SI UNA RECETA ESTA REGISTRADA COMO FAVORITA
    fun esFavorito(recetaId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_RECETA_ID FROM $TABLE_FAVORITOS WHERE $COL_RECETA_ID = ?",
            arrayOf(recetaId.toString())
        )

        // VERIFICAR SI EXISTE EL REGISTRO
        val existe = cursor.moveToFirst()
        cursor.close()
        db.close()

        return existe
    }

    fun existeRecetaFavorita(recetaId: Int): Boolean {
        return esFavorito(recetaId)
    }
}
