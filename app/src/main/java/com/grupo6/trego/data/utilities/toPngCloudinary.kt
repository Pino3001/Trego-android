package com.grupo6.trego.data.utilities

/**
 * Estas funciones de extensión nos ayudan a manipular las URLs de las imágenes que vienen 
 * de Cloudinary, por ejemplo para forzar que se descarguen como PNG en lugar de otros formatos.
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