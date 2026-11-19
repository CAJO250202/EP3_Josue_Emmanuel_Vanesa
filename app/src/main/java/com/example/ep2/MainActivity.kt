package com.example.ep2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DataBaseHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var btnGoogle: ImageButton

    // LAUNCHER PARA PROCESAR EL RESULTADO DE GOOGLE SIGN-IN
    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                AutenticaFirebase(idToken)
            } else {
                Toast.makeText(this, "No se pudo obtener el token de Google. Revisa Firebase Console", Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            Log.e("GoogleAuthError", "ApiException al iniciar sesión con Google. Código: ${e.statusCode}", e)
            when (e.statusCode) {
                12501 -> {
                    // El usuario canceló o cerró la ventana
                }
                12502 -> {
                    // Operación en progreso: limpiar estado
                    googleSignInClient.signOut()
                }
                10 -> {
                    Toast.makeText(this, "Error 10 (DEVELOPER_ERROR): Registrar huella SHA-1 o habilitar Google en Firebase Console", Toast.LENGTH_LONG).show()
                }
                12500 -> {
                    Toast.makeText(this, "Error 12500: Conflicto de paquete o SHA-1 en Firebase", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(this, "Error de Google (Código ${e.statusCode}): ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        dbHelper = DataBaseHelper(this)
        btnGoogle = findViewById(R.id.btnGoogle)
        auth = FirebaseAuth.getInstance()

        // CONFIGURAR OPCIONES DE GOOGLE SIGN-IN
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etPass = findViewById<EditText>(R.id.etPass)
        val btnInicio = findViewById<Button>(R.id.btninicio)
        val tvCrearCuenta = findViewById<Button>(R.id.tvCrearCuenta)

        btnInicio.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val pass = etPass.text.toString().trim()

            // Validaciones
            if (correo.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                // Validar credenciales en la base de datos
                val esValido = dbHelper.validarUsuario(correo, pass)
                if (esValido) {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnGoogle.setOnClickListener {
            iniciarSesionGoogle()
        }

        tvCrearCuenta.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        // VARIABLE QUE GUARDA EL ESTADO DEL USUARIO ACTUAL
        val usuarioActual = auth.currentUser

        // VALIDAR QUE LA INFORMACION DEL USUARIO ESTA ACTIVO
        if (usuarioActual != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // 3. MÉTODO PARA AUTENTICAR EN FIREBASE
    private fun AutenticaFirebase(idToken: String) {
        val credencialFirebase = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credencialFirebase).addOnCompleteListener(this) { inicio ->
            // VALIDAR SI EL INICIO DE SESIÓN ES CORRECTO
            if (inicio.isSuccessful) {
                // CREAR VARIABLE PARA GUARDAR AL USUARIO ACTUAL
                val usuario = auth.currentUser

                Toast.makeText(this, "Bienvenido ${usuario?.displayName ?: ""}", Toast.LENGTH_SHORT).show()

                // ENVIAR AL HOMEACTIVITY
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val errorDesc = inicio.exception?.localizedMessage ?: "ERROR AL INICIAR CON FIREBASE"
                Log.e("FirebaseError", "Error al autenticar en Firebase", inicio.exception)
                Toast.makeText(this, "Error Firebase: $errorDesc", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 5. MÉTODO PARA INICIAR SESIÓN CON GOOGLE
    private fun iniciarSesionGoogle() {
        btnGoogle.isEnabled = false
        googleSignInClient.signOut().addOnCompleteListener {
            btnGoogle.isEnabled = true
            val signInIntent = googleSignInClient.signInIntent
            googleLauncher.launch(signInIntent)
        }
    }
}
