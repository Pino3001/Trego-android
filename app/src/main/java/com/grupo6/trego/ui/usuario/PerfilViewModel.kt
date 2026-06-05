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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class PerfilUiState {
    object Loading : PerfilUiState()
    data class Success(val user: DTOUsuario, val direcciones: List<DTODireccion>, val tags: List<String> = emptyList() ) : PerfilUiState()
    data class Error(val message: String) : PerfilUiState()
}

class PerfilViewModel(
    private val repository: UsuarioRepository,
    private val cludinaryRepo: CloudinaryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<PerfilUiState>(PerfilUiState.Loading)
    private var ultimoExito: PerfilUiState.Success? = null
    val state: StateFlow<PerfilUiState> = _state.asStateFlow()

    // Estado local para la dirección seleccionada (esto no afecta al perfil)
    var direccionSeleccionada by mutableStateOf<DTODireccion?>(null)

    init {
        cargarPerfil()
    }

    fun descartarError(){
        _state.value = ultimoExito ?: PerfilUiState.Loading
    }
    // ──── Carga completa (usado al iniciar y tras mutaciones) ────
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
                            tags = direccionesResult.getOrThrow().mapNotNull { it.tag?.takeIf { t -> t.isNotBlank() } }
                        )
                        ultimoExito = successState
                        successState
                    }
                    usuarioResult.isFailure -> {
                        PerfilUiState.Error(usuarioResult.exceptionOrNull()?.message ?: "Error al cargar perfil")
                    }
                    direccionesResult.isFailure -> {
                        PerfilUiState.Error(direccionesResult.exceptionOrNull()?.message ?: "Error al cargar direcciones")
                    }
                    else -> estadoAnterior
                }
            } catch (e: Exception) {
                _state.value = PerfilUiState.Error("Error de red o servidor: ${e.message}")
            }
        }
    }

    // ──── Actualizar perfil (dispara recarga completa) ────
    fun actualizarPerfil(cliente: DTOUsuario, imagenUri: Uri?) {
        viewModelScope.launch {
            _state.value = PerfilUiState.Loading
            var clienteParaActualizar = cliente
            if (imagenUri != null) {
                val resultado = subirImagenACloudinary(imagenUri)
                if (resultado.isSuccess) {
                    clienteParaActualizar = cliente.copy(urlImagen = resultado.getOrThrow())
                } else {
                    _state.value = PerfilUiState.Error(
                        resultado.exceptionOrNull()?.message ?: "Error al subir imagen"
                    )
                    return@launch
                }
            }
            repository.actualizarPerfil(clienteParaActualizar.toDTOCliente())
                .onSuccess {
                    cargarPerfil()
                }
                .onFailure { error ->
                    _state.value = PerfilUiState.Error(error.message ?: "Error al actualizar perfil")
                }
        }
    }

    // ──── Agregar dirección (optimista + recarga completa) ────
    fun agregarDireccion(direccion: DTODireccion) {
        viewModelScope.launch {
            try {
                repository.agregarDireccion(direccion)
                    .onSuccess { cargarPerfil() }
                    .onFailure { error ->
                        _state.value = PerfilUiState.Error(error.message ?: "Error al agregar dirección")
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
                    .onSuccess { cargarPerfil() }
                    .onFailure { error ->
                        _state.value = PerfilUiState.Error(error.message ?: "Error al actualizar dirección")
                    }
            } catch (e: Exception) {
                _state.value = PerfilUiState.Error("Excepción al actualizar: ${e.message}")
            }
        }
    }

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
