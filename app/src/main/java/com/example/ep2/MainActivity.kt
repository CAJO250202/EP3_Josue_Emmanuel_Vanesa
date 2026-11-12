package com.example.ep2

import android.content.Intent
import android.credentials.GetCredentialException
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DataBaseHelper


    private lateinit var auth: FirebaseAuth
    private lateinit var  credentialManager: CredentialManager
    private lateinit var btnGoogle : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        dbHelper = DataBaseHelper(this)
        btnGoogle = findViewById(R.id.btnGoogle)
        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

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

    // 3. MÉTODO PARA AUTENTICAR EN FIREBSAE
    private fun AutenticaFirebase(idToken: String){

        val credencialFirebase = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credencialFirebase).addOnCompleteListener(this) {

                inicio ->

            //VALIDAR SI EL INICIO DE SESION ES CORRECTO
            if(inicio.isSuccessful){
                //CREAR VARIABLE PARA GUARDAR AL USUARIO ACTUAL
                val usuario =auth.currentUser

                Toast.makeText(this, "Bienvenido ${usuario?.displayName}", Toast.LENGTH_SHORT).show()

                //ENVIAR AL HOMEACTIVITY
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)

                finish()


            }else{
                Toast.makeText(this, "ERROR AL INICIAR CON FIREBASE", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun procesarCredenciales(credential: Credential){

        if(credential is CustomCredential && credential.type ==
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){

            try {


                //VALIDAR LA CREDENCIAL DEL PROVEEDOR
                val credencialGoogle = GoogleIdTokenCredential.createFrom(
                    credential.data
                )
                AutenticaFirebase(credencialGoogle.idToken)


            }catch (e : GoogleIdTokenParsingException){
                Toast.makeText(this, "Credencial Invalida", Toast.LENGTH_SHORT).show()


            }
        }
    }


    // 5. MÉTODO PARA INICIAR SESIÓN
    private fun iniciarSesionGoogle(){

        val inicioGoogle = GetSignInWithGoogleOption.Builder(
            getString(R.string.default_web_client_id)).build()

        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(inicioGoogle)
            .build()

        //PROCESO PARA INICIAR CON FIREBASE
        lifecycleScope.launch {

            try{
                val resultado = credentialManager.getCredential(
                    context = this@MainActivity,
                    request = solicitud
                )

            //LLAMAR AL METODO QUE PERMITE PROCESAR LA CREDENCIAL
            procesarCredenciales(resultado.credential)

            }catch (e: androidx.credentials.exceptions.GetCredentialException){
                Toast.makeText(this@MainActivity, "ERROR AL INICIAR", Toast.LENGTH_SHORT).show()

            }

        }


    }

}
