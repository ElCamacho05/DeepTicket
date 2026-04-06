package com.example.deepticket

import com.example.deepticket.data.Ticket
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.Auth // Nuevo
import io.github.jan.supabase.gotrue.auth // Nuevo
import io.github.jan.supabase.gotrue.providers.builtin.Email // Nuevo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// 1. Cliente actualizado con Auth
val supabase = createSupabaseClient(
    supabaseUrl = "https://kqwuxnejznikpcqvagul.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imtxd3V4bmVqem5pa3BjcXZhZ3VsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU0Mzc4MTYsImV4cCI6MjA5MTAxMzgxNn0.2aK62lF0vquIMLvOa8R4Tc-PHBZFvNKwpK64R1DVv7E"
) {
    install(Postgrest)
    install(Auth) // <--- Encendemos el módulo de autenticación
}

// 3. Función para Iniciar Sesión
suspend fun loginUsuario(correo: String, contrasena: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            supabase.auth.signInWith(Email) {
                email = correo
                password = contrasena
            }
            true // Login exitoso
        } catch (e: Exception) {
            e.printStackTrace()
            false // Error (ej. contraseña incorrecta)
        }
    }
}

suspend fun registrarUsuarioCompleto(
    correo: String,
    contrasena: String,
    nombre: String,
    apellidos: String,
    fechaNacimiento: String, // Cambiado
    genero: String
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            supabase.auth.signUpWith(Email) {
                email = correo
                password = contrasena
                data = buildJsonObject {
                    put("nombre", nombre)
                    put("apellidos", apellidos)
                    put("fecha_nacimiento", fechaNacimiento)
                    put("genero", genero)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

// 4. Tu función de buscar tickets (intacta)
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
suspend fun validarTicket(codigoEscaneado: String): Ticket? {
    return withContext(Dispatchers.IO) {
        try {
            val resultados = supabase.postgrest["tickets"]
                .select { filter { eq("order_id", codigoEscaneado) } }
                .decodeList<Ticket>()
            resultados.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}