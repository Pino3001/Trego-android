package com.grupo6.trego.data.utilities

/**
 * Transforma una URL de Cloudinary para forzar la salida en PNG.
 * Inserta "f_png/" después de "/upload/" si no existe ya otro formato.
 */
fun String.toCloudinaryPng(): String {
    return if (this.contains("/upload/") && !this.contains("/f_")) {
        this.replace("/upload/", "/upload/f_png/")
    } else {
        this.replace(Regex("\\.svg(\\?.*)?$"), ".png")
    }
}

fun String.ensureCloudinaryTransformation(transformation: String): String {
    if (!this.contains("/upload/") || this.contains("/$transformation/")) {
        return this
    }

    val urlLimpia = this.replace(Regex("/upload/(?!v\\d+)[^/]+/"), "/upload/")

    return urlLimpia.replace("/upload/", "/upload/$transformation/")
}