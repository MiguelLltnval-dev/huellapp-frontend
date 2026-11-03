package com.example.huellaapp.utils

object LocationData {
    // Mapa con los Departamentos principales y sus ciudades para el prototipo
    val departamentosYCiudades = mapOf(
        "Antioquia" to listOf("Medellín", "Bello", "Itagüí", "Envigado", "Apartadó", "Rionegro", "Turbo", "Caucasia"),
        "Atlántico" to listOf("Barranquilla", "Soledad", "Malambo", "Sabanalarga"),
        "Bogotá D.C." to listOf("Bogotá"),
        "Bolívar" to listOf("Cartagena", "Magangué", "Turbaco"),
        "Boyacá" to listOf("Tunja", "Sogamoso", "Duitama", "Chiquinquirá"),
        "Caldas" to listOf("Manizales", "La Dorada", "Chinchiná"),
        "Cauca" to listOf("Popayán", "Santander de Quilichao", "Puerto Tejada"),
        "Cesar" to listOf("Valledupar", "Aguachica", "Agustín Codazzi"),
        "Córdoba" to listOf("Montería", "Lorica", "Cereté", "Sahagún"),
        "Cundinamarca" to listOf("Soacha", "Chía", "Zipaquirá", "Facatativá", "Fusagasugá", "Girardot"),
        "Huila" to listOf("Neiva", "Pitalito", "Garzón"),
        "Magdalena" to listOf("Santa Marta", "Ciénaga", "Fundación"),
        "Meta" to listOf("Villavicencio", "Acacías"),
        "Nariño" to listOf("Pasto", "Tumaco", "Ipiales"),
        "Norte de Santander" to listOf("Cúcuta", "Ocaña", "Pamplona", "Los Patios"),
        "Quindío" to listOf("Armenia", "Calarcá", "Quimbaya"),
        "Risaralda" to listOf("Pereira", "Dosquebradas", "Santa Rosa de Cabal"),
        "Santander" to listOf("Bucaramanga", "Floridablanca", "Girón", "Piedecuesta", "Barrancabermeja"),
        "Sucre" to listOf("Sincelejo", "Corozal"),
        "Tolima" to listOf("Ibagué", "Espinal", "Melgar"),
        "Valle del Cauca" to listOf("Cali", "Buenaventura", "Palmira", "Tuluá", "Yumbo", "Cartago", "Jamundí", "Buga")
    )

    // Lista de Departamentos ordenada alfabéticamente
    val departamentos = departamentosYCiudades.keys.toList().sorted()
}
