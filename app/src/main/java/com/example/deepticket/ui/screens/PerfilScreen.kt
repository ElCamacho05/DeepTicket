package com.example.deepticket.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepticket.ui.components.ProfilePillButton
import com.example.deepticket.ui.theme.AppColors
import io.github.jan.supabase.gotrue.auth
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(userName: String, totalRastreados: Int) {
    // 1. Instanciamos el scope para poder ejecutar tareas de Supabase
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Box(modifier = Modifier.size(110.dp).background(AppColors.elementBackground, CircleShape).border(2.dp, AppColors.orangeAccent, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, contentDescription = null, tint = AppColors.greyText, modifier = Modifier.size(60.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(userName, color = AppColors.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(32.dp))
        Card(modifier = Modifier.fillMaxWidth().height(100.dp), colors = CardDefaults.cardColors(containerColor = AppColors.orangeAccent.copy(alpha = 0.1f)), shape = RoundedCornerShape(30.dp), border = BorderStroke(1.dp, AppColors.orangeAccent.copy(alpha = 0.3f))) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(totalRastreados.toString(), color = AppColors.orangeAccent, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                Text("Artículos Rastreados en Total", color = AppColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Configuración", color = AppColors.greyText, fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp, start = 8.dp))
            ProfilePillButton(icon = Icons.Outlined.ManageAccounts, title = "Editar Perfil")
            ProfilePillButton(icon = Icons.Outlined.Notifications, title = "Notificaciones")
            ProfilePillButton(icon = Icons.Outlined.Lock, title = "Privacidad y Seguridad")
            ProfilePillButton(icon = Icons.Outlined.HelpOutline, title = "Ayuda y Soporte")
            Spacer(modifier = Modifier.height(8.dp))

            // 2. Aquí está la lógica de cierre de sesión
            ProfilePillButton(
                icon = Icons.Outlined.Logout,
                title = "Cerrar Sesión",
                isDestructive = true,
                onClick = {
                    coroutineScope.launch {
                        try {
                            com.example.deepticket.supabase.auth.signOut()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(120.dp))
    }
}