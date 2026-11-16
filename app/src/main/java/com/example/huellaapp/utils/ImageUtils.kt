package com.example.huellaapp.utils

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView

object ImageUtils {


    fun cargarBase64EnImageView(base64String: String?, imageView: ImageView) {
        if (base64String.isNullOrEmpty()) {
            return
        }

        try {
            // Limpiar encabezados si los tiene (ej. "data:image/png;base64,")
            val cleanBase64 = if (base64String.contains(",")) {
                base64String.substringAfter(",")
            } else {
                base64String
            }

            // Decodificar Base64 a ByteArray
            val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)

            // Convertir ByteArray a Bitmap
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            // Asignar al ImageView
            if (decodedImage != null) {
                imageView.setImageBitmap(decodedImage)
            }

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            // El Base64 es inválido, puedes asignar un placeholder aquí si lo deseas
        } catch (e: Exception) {
            e.printStackTrace()
            // Error genérico de memoria o decodificación
        }
    }
}
