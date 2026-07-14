import com.grupo6.trego.data.model.DTOAgregarAlCarritoRequest
import com.grupo6.trego.data.model.DTOCarrito
import com.grupo6.trego.data.model.DTOEliminarProductoRequest
import com.grupo6.trego.data.model.DTOProductoPedido
import com.grupo6.trego.data.remote.CarritoApiService

/**
 * Acá gestionamos toda la lógica del carrito de compras, permitiendo agregar, 
 * modificar o quitar productos antes de finalizar el pedido.
 */
class CarritoRepository(
    private val api: CarritoApiService
) {

    suspend fun obtenerCarrito(): Result<DTOCarrito?> {
        return try {
            val response = api.obtenerCarrito()
            if (response.isSuccessful) {
                val body = response.body()
                // body nulo, idCarrito 0, o lista de productos vacía → sin carrito
                if (body == null || body.idCarrito == 0L || body.productos.isNullOrEmpty()) {
                    Result.success(null)
                } else {
                    Result.success(body)
                }
            } else {
                // 404 o 204 también son “sin carrito”
                if (response.code() == 404 || response.code() == 204) {
                    Result.success(null)
                } else {
                    Result.failure(Exception("Error al obtener carrito: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) {
                throw e // Deja que la corrutina se cancele silenciosamente
            }
            Result.failure(e) // Cualquier otro error sí va al onFailure
        }
    }

    suspend fun agregarProducto(request: DTOProductoPedido): Result<DTOCarrito> {
        return try {
            val response = api.agregarProducto(request)
            if (response.isSuccessful) {
                Result.success(response.body() ?: throw Exception("Respuesta vacía"))
            } else {
                Result.failure(Exception("Error al agregar producto: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun modificarProducto(request: DTOProductoPedido): Result<DTOProductoPedido?> {
        return try {
            val response = api.modificarProductoCarrito(request)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error al modificar producto: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarProducto(request: DTOProductoPedido): Result<DTOCarrito?> {
        return try {
            val response = api.eliminarProducto(request)
            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Error al eliminar producto: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun limpiarCarrito(): Result<Unit> {
        return try {
            val response = api.limpiarCarrito()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al limpiar carrito: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}