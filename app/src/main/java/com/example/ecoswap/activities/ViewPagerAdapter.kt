package com.example.ecoswap.activities

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.ecoswap.ProductosUsuario
import com.example.ecoswap.ValoracionUsuario

// Clase ViewPagerAdapter que extiende FragmentStateAdapter para manejar fragmentos en un ViewPager
class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    // Método que crea un fragmento basado en la posición del ViewPager
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProductosUsuario() // Si la posición es 0, devuelve una instancia del fragmento ProductosUsuario
            1 -> ValoracionUsuario() // Si la posición es 1, devuelve una instancia del fragmento ValoracionUsuario
            else -> ProductosUsuario() // Para cualquier otra posición, devuelve por defecto ProductosUsuario
        }
    }
    // Método que devuelve el número total de elementos (pestañas) en el ViewPager
    override fun getItemCount(): Int {
        return 2 // En este caso, hay 2 pestañas: Productos y Valoración
    }
}