package com.grupo6.trego.ui.usuario

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupo6.trego.data.model.DTODireccion
import com.grupo6.trego.data.model.DTOUsuario
import com.grupo6.trego.data.model.toDTOCliente
import com.grupo6.trego.data.repository.CloudinaryRepository
import com.grupo6.trego.data.repository.UsuarioRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class PerfilUiState {
    object Loading : PerfilUiState()
    data class Success(
        val user: DTOUsuario,
        val direcciones: List<DTODireccion>,
        val tags: List<String> = emptyList()
    ) : PerfilUiState()

    data class Error(val message: String) : PerfilUiState()
}

/**
 * Este ViewModel se encarga de todo lo relacionado con el usuario. Gestiona la carga 
 * de sus datos personales, la subida de fotos de perfil a la nube y el mantenimiento 
 * de sus direcciones guardadas, asegurando que la información esté siempre al día.
 */
class PerfilViewModel(
    private val repository: UsuarioRepository,
    private val cludinaryRepo: CloudinaryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<PerfilUiState>(PerfilUiState.Loading)
    private var ultimoExito: PerfilUiState.Success? = null
    val state: StateFlow<PerfilUiState> = _state.asStateFlow()

    private val _eventChannel = Channel<String>()
    val eventFlow = _eventChannel.receiveAsFlow()

    // Estado local para la dirección seleccionada (esto no afecta al perfil)
    var direccionSeleccionada by mutableStateOf<DTODireccion?>(null)

    init {
        cargarPerfil()
    }

    fun descartarError() {
        _state.value = ultimoExito ?: PerfilUiState.Loading
    }

    /* Trae toda la información del usuario y sus direcciones en paralelo para que la carga sea más rápida. */
    fun cargarPerfil() {
        viewModelScope.launch {
            try {
                _state.value = PerfilUiState.Loading
                val estadoAnterior = _state.value

                val usuarioDeferred = async { repository.verPerfil() }
                val direccionesDeferred = async { repository.obtenerDirecciones() }

                val usuarioResult = usuarioDeferred.await()
                val direccionesResult = direccionesDeferred.await()

                _state.value = when {
                    usuarioResult.isSuccess && direccionesResult.isSuccess -> {
                        val successState = PerfilUiState.Success(
                            user = usuarioResult.getOrThrow(),
                            direcciones = direccionesResult.getOrThrow(),
                            tags = direccionesResult.getOrThrow()
                                .mapNotNull { it.tag?.takeIf { t -> t.isNotBlank() } }
                        )
                        ultimoExito = successState
                        successState
                    }

                    usuarioResult.isFailure -> {
                        PerfilUiState.Error(
                            usuarioResult.exceptionOrNull()?.message ?: "Error al cargar perfil"
                        )
                    }

                    direccionesResult.isFailure -> {
                        PerfilUiState.Error(
                            direccionesResult.exceptionOrNull()?.message
                                ?: "Error al cargar direcciones"
                        )
                    }

                    else -> estadoAnterior
                }
            } catch (e: Exception) {
                _state.value = PerfilUiState.Error("Error de red o servidor: ${e.message}")
            }
        }
    }

    /* Guarda los cambios en el perfil del usuario. Si cambió su foto, primero la subimos a Cloudinary y después mandamos la nueva URL al servidor. */
    fun actualizarPerfil(cliente: DTOUsuario, imagenUri: Uri?) {
        viewModelScope.launch {
            _state.value = PerfilUiState.Loading
            var clienteParaActualizar = cliente
            if (imagenUri != null) {
                val resultado = subirImagenACloudinary(imagenUri)
                if (resultado.isSuccess) {
                    clienteParaActualizar = cliente.copy(urlImagen = resultado.getOrThrow())
                } else {
                    _eventChannel.send(
                        resultado.exceptionOrNull()?.message ?: "Error al subir imagen"
                    )
                    cargarPerfil()
                    return@launch
                }
            }
            repository.actualizarPerfil(clienteParaActualizar.toDTOCliente())
                .onSuccess {
                    _eventChannel.send("Perfil actualizado con éxito")
                    cargarPerfil()
                }
                .onFailure { error ->
                    _eventChannel.send(error.message ?: "Error al actualizar perfil")
                    cargarPerfil() // Recargamos para limpiar el loading y volver al éxito anterior
                }
        }
    }

    /* Envía una nueva dirección al servidor para que el usuario la tenga disponible en sus próximos pedidos. */
    fun agregarDireccion(direccion: DTODireccion) {
        viewModelScope.launch {
            try {
                repository.agregarDireccion(direccion)
                    .onSuccess {
                        _eventChannel.send("Dirección agregada correctamente")
                        cargarPerfil()
                    }
                    .onFailure { error ->
                        _eventChannel.send(error.message ?: "Error al agregar dirección")
                        cargarPerfil()
                    }
            } catch (e: Exception) {
                _state.value = PerfilUiState.Error("Excepción al agregar: ${e.message}")
            }
        }
    }

    // ──── Actualizar dirección (recarga completa) ────
    fun actualizarDireccion(tagModificar: String, direccion: DTODireccion) {
        viewModelScope.launch {
            try {
                repository.actualizarDireccion(tagModificar, direccion)
                    .onSuccess {
                        _eventChannel.send("Dirección actualizada correctamente")
                        cargarPerfil()
                    }
                    .onFailure { error ->
                        _eventChannel.send(error.message ?: "Error al actualizar dirección")
                        cargarPerfil()
                    }
            } catch (e: Exception) {
                _state.value = PerfilUiState.Error("Excepción al actualizar: ${e.message}")
            }
        }
    }

    /* Proceso interno para subir una imagen de forma segura: pide permiso al backend y después sube el archivo directamente a la nube. */
    private suspend fun subirImagenACloudinary(imagenUri: Uri): Result<String> {
        val nombreArchivo = "perfil_${UUID.randomUUID()}"
        val tipo = "image"

        // 1. Obtener firma del backend
        val firmaResult = cludinaryRepo.obtenerFirma(nombreArchivo, tipo)
        if (firmaResult.isFailure) return Result.failure(firmaResult.exceptionOrNull()!!)

        // 2. Subir a Cloudinary usando la firma
        return cludinaryRepo.subirImagenACloudinary(imagenUri, firmaResult.getOrThrow())
    }

}
