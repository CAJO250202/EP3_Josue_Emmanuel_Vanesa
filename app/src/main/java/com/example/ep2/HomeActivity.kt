package com.example.ep2

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.ep2.model.Recipe
import com.example.ep2.model.RecipeResponse
import com.example.ep2.network.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private val apiKey = "d4db6cce1be14b728fcb6e66867e6d9b"

    private lateinit var tvResultado: TextView
    private lateinit var contenedorPasta: LinearLayout
    private lateinit var contenedorTacos: LinearLayout
    private lateinit var contenedorAleatorias: LinearLayout

    private lateinit var toolbar: Toolbar
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        tvResultado = findViewById(R.id.tvResultado)
        contenedorPasta = findViewById(R.id.contenedorPasta)
        contenedorTacos = findViewById(R.id.contenedorTacos)
        contenedorAleatorias = findViewById(R.id.contenedorAleatorias)

        // 3 CONSULTAS A LA API
        obtenerRecetasPorBusqueda("pasta", contenedorPasta)
        obtenerRecetasPorBusqueda("tacos", contenedorTacos)
        obtenerRecetasAleatorias(contenedorAleatorias)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_boton, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.opc_inicio -> {
                Toast.makeText(this, "Inicio", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.opc_favoritos -> {
                val intent = Intent(this, FavoritesActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.opc_salir -> {
                cerrarSesionGoogle()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // MÉTODO PARA CERRAR SESIÓN DE GOOGLE Y FIREBASE
    private fun cerrarSesionGoogle() {
        // 1. CERRAR SESIÓN EN FIREBASE
        auth.signOut()

        // 2. CERRAR SESIÓN EN GOOGLE SIGN IN CLIENT
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()

        // 3. REDIRIGIR A LA PANTALLA DE INICIO DE SESIÓN (MAINACTIVITY)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    // METODO PARA OBTENER INFORMACION DE LAS RECETAS POR BUSQUEDA
    private fun obtenerRecetasPorBusqueda(query: String, contenedor: LinearLayout) {
        // HACER EL LLAMADO A LA CONSTRUCCION
        RetrofitClient.api.obtenerRecetasBusqueda(
            query = query,
            apiKey = apiKey,
            limite = 10
        ).enqueue(object : Callback<RecipeResponse?> {
            override fun onResponse(
                call: Call<RecipeResponse?>,
                response: Response<RecipeResponse?>
            ) {
                val recetas = response.body()?.results ?: emptyList()

                // Validar si la variable recetas tiene informacion
                if (!recetas.isNullOrEmpty()) {
                    tvResultado.visibility = View.GONE
                    mostrarRecetas(recetas, contenedor)
                } else {
                    Toast.makeText(this@HomeActivity, "No hay respuesta del Api", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecipeResponse?>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error al conectar con la API: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // METODO PARA OBTENER RECETAS ALEATORIAS
    private fun obtenerRecetasAleatorias(contenedor: LinearLayout) {
        RetrofitClient.api.obtenerRecetasAleatorias(
            apiKey = apiKey,
            limite = 10
        ).enqueue(object : Callback<RecipeResponse?> {
            override fun onResponse(
                call: Call<RecipeResponse?>,
                response: Response<RecipeResponse?>
            ) {
                val recetas = response.body()?.recipes ?: emptyList()

                // Validar si la variable recetas tiene informacion
                if (!recetas.isNullOrEmpty()) {
                    tvResultado.visibility = View.GONE
                    mostrarRecetas(recetas, contenedor)
                } else {
                    Toast.makeText(this@HomeActivity, "No hay respuesta del Api", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecipeResponse?>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error al conectar con la API: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarRecetas(recetas: List<Recipe>, contenedor: LinearLayout) {
        // OBLIGATORIO EVITAR ELEMENTOS DUPLICADOS
        contenedor.removeAllViews()

        // CICLO PARA RECORRER LAS MULTIPLES PETICIONES A UN API
        for (receta in recetas) {
            val vistaReceta = layoutInflater.inflate(
                R.layout.item_recipe,
                contenedor,
                false
            )

            val imgPortada = vistaReceta.findViewById<ImageView>(R.id.imgPortadaReceta)
            val tvNombreReceta = vistaReceta.findViewById<TextView>(R.id.tvNombreReceta)
            val tvDetalleReceta = vistaReceta.findViewById<TextView>(R.id.tvDetalleReceta)

            // ASIGNANDO LOS NOMBRES OBTENIDOS DEL API
            tvNombreReceta.text = receta.title ?: "Receta sin nombre"
            tvDetalleReceta.text = if (receta.readyInMinutes != null) "${receta.readyInMinutes} min" else "ID: ${receta.id}"

            // MOSTRAR LA IMAGEN DE LA RECETA CON GLIDE
            Glide.with(this@HomeActivity)
                .load(receta.image)
                .into(imgPortada)

            vistaReceta.setOnClickListener {
                val intent = Intent(this@HomeActivity, DetailsActivity::class.java).apply {
                    putExtra("ID_RECETA", receta.id)
                    putExtra("NOMBRE_RECETA", receta.title ?: "Receta sin nombre")
                    putExtra("TIEMPO_RECETA", receta.readyInMinutes ?: 0)
                    putExtra("PORCIONES_RECETA", receta.servings ?: 0)
                    putExtra("IMAGEN_RECETA", receta.image)
                }
                startActivity(intent)
            }

            contenedor.addView(vistaReceta)
        }
    }
}
