# 🍔 TREGO - Aplicación Android

> Aplicación móvil de **TREGO**, una plataforma de pedidos de comida en línea desarrollada como proyecto final de la carrera **Tecnólogo en Informática**.

TREGO permite a los clientes descubrir restaurantes cercanos, explorar sus productos, realizar pedidos y gestionar su información personal desde una única aplicación móvil. La plataforma busca simplificar la interacción entre clientes y restaurantes, manteniendo la gestión de la preparación y entrega de los pedidos bajo responsabilidad del restaurante.

---

## 📱 Características Principales

- 🔐 Autenticación mediante Google o SMS utilizando Firebase Authentication.
- 🍔 Exploración de restaurantes por ubicación, nombre y categoría.
- 📋 Visualización de platos, artículos, combos y ofertas.
- 🛒 Gestión completa del carrito de compras.
- 💳 Pago integrado mediante Mercado Pago.
- 📍 Administración de múltiples direcciones de entrega con autocompletado.
- 🔔 Notificaciones push mediante Firebase Cloud Messaging (FCM).
- ⭐ Sistema de calificaciones y comentarios sobre restaurantes.
- 👤 Gestión del perfil del usuario e imagen de perfil.
- 📦 Historial y seguimiento de pedidos.
- 🎯 Filtrado de restaurantes según zonas de entrega.

---

## 🏗️ Arquitectura

La aplicación fue desarrollada siguiendo una arquitectura **MVVM (Model - View - ViewModel)** con separación por capas, buscando facilitar el mantenimiento, la escalabilidad y la reutilización del código.

```text
UI (Jetpack Compose)
        │
ViewModel
        │
Repository
        │
Remote API / Firebase
```

La comunicación entre componentes se realiza mediante **StateFlow** y **Kotlin Coroutines**, mientras que la inyección de dependencias se implementa utilizando **Koin**.

---

## 🛠️ Stack Tecnológico

| Tecnología | Uso |
|------------|-----|
| Kotlin | Lenguaje principal |
| Jetpack Compose | Interfaz de usuario |
| Material Design 3 | Componentes visuales |
| MVVM | Arquitectura de la aplicación |
| Koin | Inyección de dependencias |
| Retrofit + Gson | Comunicación con APIs REST |
| Kotlin Coroutines + Flow | Programación asíncrona |
| Firebase Authentication | Autenticación |
| Firebase Cloud Messaging | Notificaciones Push |
| Cloudinary | Almacenamiento de imágenes |
| Coil | Carga y caché de imágenes |
| Geoapify API | Geocodificación y autocompletado |
| Google Play Services Location | Geolocalización |
| Mercado Pago | Procesamiento de pagos |

---

## 📂 Estructura del Proyecto

```text
app/
└── src/main/java/com/grupo6/trego
    ├── ui/
    │   ├── auth/
    │   ├── carrito/
    │   ├── home/
    │   ├── menu/
    │   ├── pedidos/
    │   └── usuario/
    │
    ├── data/
    │   ├── model/
    │   ├── remote/
    │   ├── repository/
    │   └── utilities/
    │
    ├── domain/
    ├── di/
    └── MainActivity.kt
```

---

## 🚀 Funcionalidades Implementadas

### Usuarios

- Registro mediante Google Authentication.
- Registro mediante verificación SMS.
- Inicio y cierre de sesión.
- Gestión del perfil de usuario.
- Carga y actualización de foto de perfil.
- Administración de direcciones de entrega.

### Restaurantes

- Visualización de restaurantes disponibles.
- Búsqueda por nombre.
- Filtrado por categorías.
- Validación de zonas de reparto.

### Productos

- Consulta de platos, artículos y combos.
- Visualización de ingredientes.
- Personalización de productos.
- Gestión de ofertas y descuentos.

### Pedidos

- Creación de pedidos.
- Gestión del carrito de compras.
- Confirmación de pedidos.
- Historial de pedidos.
- Seguimiento del estado del pedido.

### Pagos

- Integración con Mercado Pago.
- Confirmación automática de transacciones.
- Registro de pagos realizados.

### Notificaciones

- Notificaciones push en tiempo real.
- Actualización de estados de pedidos.
- Avisos de promociones y novedades.

### Comentarios y Calificaciones

- Valoración de restaurantes.
- Publicación de comentarios.
- Consulta de opiniones de otros usuarios.

---

## 🔧 Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/trego-android.git
```

### 2. Configurar Firebase

Agregar el archivo:

```text
app/google-services.json
```

### 3. Configurar variables y claves

Configurar las credenciales necesarias para:

- Firebase Authentication
- Firebase Cloud Messaging
- Cloudinary
- Geoapify
- Mercado Pago
- API Backend TREGO

Se recomienda almacenarlas mediante `local.properties` o variables de entorno.

### 4. Ejecutar la aplicación

Sincronizar el proyecto con Gradle y ejecutar la aplicación desde Android Studio.

---

## 📋 Requisitos

- Android Studio Hedgehog o superior.
- Android SDK 26 (Android 8.0 Oreo) o superior.
- JDK 17 o superior.
- Conexión a Internet para consumir los servicios de backend.

---

## 📸 Capturas de Pantalla

| Inicio | Restaurantes | Menú|
|---------|------------|----------|
|<img width="246" height="533" alt="image" src="https://github.com/user-attachments/assets/956d78dc-d0cc-41e2-8315-53b1b2d0e0bf" /> | <img width="246" height="533" alt="image" src="https://github.com/user-attachments/assets/168afd1f-19ac-4d96-a800-65d165d767b6" /> | <img width="246" height="533" alt="image" src="https://github.com/user-attachments/assets/03d7f7c1-bbc3-457e-a19a-c50f6a01f995" /> |

| Pedido | Perfil | Carrito |
|---------|---------|------|
| <img width="246" height="533" alt="image" src="https://github.com/user-attachments/assets/4b068ed8-61d2-4cd0-a99e-57fe592b68e3" /> | <img width="246" height="533" alt="image" src="https://github.com/user-attachments/assets/1b21d71b-cf54-4865-8ad2-13b2bb95ad93" /> | <img width="246" height="533" alt="image" src="https://github.com/user-attachments/assets/c919cb3e-b8f5-47d8-8a71-18e01b9a8ac3" /> |

---

## 🔗 Servicios Integrados

La aplicación utiliza distintos servicios externos para brindar sus funcionalidades:

- Firebase Authentication
- Firebase Cloud Messaging
- Mercado Pago
- Cloudinary
- Geoapify API
- Google Play Services Location

---

## 🧪 Calidad y Buenas Prácticas

Durante el desarrollo se aplicaron distintas prácticas orientadas a mejorar la calidad del software:

- Arquitectura MVVM.
- Separación por capas.
- Inyección de dependencias.
- Programación reactiva mediante Flow.
- Uso de componentes reutilizables con Jetpack Compose.
- Manejo centralizado de errores.
- Consumo desacoplado de APIs mediante Retrofit.
- Principios de Clean Architecture.

---

## 🎓 Contexto Académico

TREGO fue desarrollado como proyecto final de la carrera **Tecnólogo en Informática**, impartida conjuntamente por la **Facultad de Ingeniería de la Universidad de la República (UdelaR)** y la **Dirección General de Educación Técnico Profesional (DGETP - UTU)**.

El proyecto tuvo como objetivo diseñar e implementar una plataforma de pedidos de comida en línea que facilite la interacción entre clientes y restaurantes mediante aplicaciones web y móviles.

---

## 👨‍💻 Autores

**Grupo 6 - Proyecto Final**

- Alexis La Cruz
- Ezequiel Medina
- Maikol Brion
- Dámaso Tor
- Horacio Duarte
- Nicolas Fernandez
- Cristian Gonzalez
- Mateo Sparano
---

## 📄 Licencia

Este proyecto fue desarrollado con fines académicos como trabajo final de carrera.

No se autoriza su utilización comercial sin el consentimiento de sus autores.
