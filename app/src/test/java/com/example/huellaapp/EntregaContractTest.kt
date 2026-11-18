package com.example.huellaapp

import com.example.huellaapp.model.PerfilRequest
import com.example.huellaapp.model.ChatResponse
import com.example.huellaapp.model.esEstadoEnCasa
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class EntregaContractTest {
    @Test fun telefonoDelPerfilUsaElContratoDelServidor() {
        val json = Gson().toJsonTree(PerfilRequest("Miguel", "3001234567", "Cauca", "Popayán", "foto")).asJsonObject
        assertEquals("3001234567", json.get("celular").asString)
        assertFalse(json.has("telefono"))
    }
    @Test fun reconoceEstadosNuevosYAnteriores() {
        assertTrue("EN CASA".esEstadoEnCasa())
        assertTrue("EN_CASA".esEstadoEnCasa())
        assertFalse("PERDIDO".esEstadoEnCasa())
    }
    @Test fun conservaElBorradorDeUnaPista() {
        val data = Gson().fromJson("""{"respuesta":"Revisa la pista","esBorrador":true,"esPista":true,"pistaGenerada":{"descripcion":"Lo vi junto al parque","ubicacion":"Popayán"}}""", ChatResponse::class.java)
        assertTrue(data.esBorrador)
        assertTrue(data.esPista)
        assertEquals("Popayán", data.pistaGenerada?.ubicacion)
    }
}
