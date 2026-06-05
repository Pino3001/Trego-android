package com.grupo6.trego.data.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.Cloudinary
import com.grupo6.trego.data.model.DTOFirma
import com.grupo6.trego.data.remote.UsuarioApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CloudinaryRepository(
    private val context: Context,
    private val apiService: UsuarioApiService
) {
    // Obtiene la firma desde tu backend
    suspend fun obtenerFirma(nombreArchivo: String, tipo: String): Result<DTOFirma> {
        return try {
            val response = apiService.firmarImagen(nombreArchivo, tipo)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener firma"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sube la imagen directamente a Cloudinary
    suspend fun subirImagenACloudinary(
        imageUri: Uri,
        firmaResponse: DTOFirma
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: throw Exception("No se pudo abrir la imagen")

            // Si usas el SDK de Cloudinary, puedes configurarlo así:
            val cloudinary = Cloudinary(
                mapOf(
                    "cloud_name" to firmaResponse.cloudName,
                    "api_key" to firmaResponse.apiKey,
                    "api_secret" to "" // No necesitas el secret si ya viene la firma
                )
            )

            val options = mapOf(
                "timestamp" to firmaResponse.timestamp.toString(),
                "signature" to firmaResponse.firma,  // <-- "firma" equivale a "signature"
                "api_key" to firmaResponse.apiKey,
                "public_id" to firmaResponse.publicId,
                "resource_type" to "image"
            )
            val uploadResult = cloudinary.uploader().upload(bytes, options)
            Result.success(uploadResult["secure_url"] as String)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}