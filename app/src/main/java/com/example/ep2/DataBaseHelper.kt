package com.example.ep2

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DataBaseHelper (context: Context): SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION){
    //BASE DE DATOS
    companion object{
        private const val DATABASE_NAME = "recetas.db"
        private const val  DATABASE_VERSION = 1

        //Tablas en la BD
        private const val TABLE_USUARIOS = "usuarios"
        private const val COL_ID = "id"
        private const val COL_NOMBRE = "nombre"
        private const val COL_CORREO = "correo"
        private const val COL_PASSWORD = "password"
    }

    //METODO PARA CREAR LA BASE DE DATOS
    override fun onCreate(db: SQLiteDatabase?) {
        //crear tabla
        val crearTablaUsuario = """
            CREATE TABLE $TABLE_USUARIOS(
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_NOMBRE TEXT NOT NULL,
            $COL_CORREO TEXT NOT NULL,
            $COL_PASSWORD TEXT NOT NULL
            )
        """.trimIndent()
        db?.execSQL(crearTablaUsuario)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        onCreate(db)
    }

    //METODO PARA INICIO DE SESION
    fun validarUsuario(correo: String, pass: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USUARIOS WHERE $COL_CORREO = ? AND $COL_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(correo, pass))
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    //METODO PARA REGISTRAR USUARIO
    fun insertarUsuario(nombre: String, correo: String, pass: String): Boolean {
        val db = this.writableDatabase
        val values = android.content.ContentValues().apply {
            put(COL_NOMBRE, nombre)
            put(COL_CORREO, correo)
            put(COL_PASSWORD, pass)
        }
        val resultado = db.insert(TABLE_USUARIOS, null, values)
        return resultado != -1L
    }
}