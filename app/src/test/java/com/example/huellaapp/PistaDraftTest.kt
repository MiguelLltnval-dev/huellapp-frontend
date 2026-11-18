package com.example.huellaapp

import com.example.huellaapp.model.*
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class PistaDraftTest {
    @Test fun incompleteOrRemovedLocationCannotBePublished() {
        assertFalse(PistaBorrador("Collar rojo", " ").listaParaPublicar())
        assertFalse(PistaBorrador(null, "Parque").listaParaPublicar())
        assertTrue(PistaBorrador("Collar rojo", "Parque").listaParaPublicar())
    }
    @Test fun followUpSendsLatestCorrectedDraft() {
        val draft = PistaBorrador("Collar azul", "Barrio Bolívar")
        val request = ChatRequest("No, fue en el parque", modoPista = true, pistaActual = draft)
        val restored = Gson().fromJson(Gson().toJson(request), ChatRequest::class.java)
        assertEquals(draft, restored.pistaActual)
        assertTrue(restored.modoPista)
    }
}
