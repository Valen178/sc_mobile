# Integración con Backend Node.js

## Configuración

### 1. URL del Backend
La URL base del backend está configurada en:
```
app/src/main/java/com/example/sportconnection/network/ApiClient.java
```

**Importante:** Cambia la constante `BASE_URL` según tu entorno:

- **Emulador Android:** `http://10.0.2.2:3000/api/`
- **Dispositivo físico:** `http://TU_IP_LOCAL:3000/api/` (ejemplo: `http://192.168.1.100:3000/api/`)
- **Producción:** `https://tu-dominio.com/api/`

### 2. Estructura del Proyecto

Se han creado las siguientes carpetas y archivos:

#### Network Layer
- `network/ApiClient.java` - Cliente Retrofit configurado
- `network/ApiService.java` - Definición de endpoints API

#### Modelos (Request/Response)
- `model/LoginRequest.java`
- `model/LoginResponse.java`
- `model/RegisterRequest.java`
- `model/RegisterResponse.java`
- `model/ProfileRequest.java`
- `model/ProfileResponse.java`

#### Repositorio
- `repository/AuthRepository.java` - Maneja todas las llamadas al backend

#### Utilidades
- `utils/SessionManager.java` - Gestión de sesión y tokens

### 3. Endpoints Implementados

#### Autenticación
- **POST** `/auth/register` - Registro de usuario
  - Body: `{ "email": "string", "password": "string" }`
  - Response: `{ "success": boolean, "message": string, "token": string, "userId": number }`

- **POST** `/auth/login` - Inicio de sesión
  - Body: `{ "email": "string", "password": "string" }`
  - Response: `{ "success": boolean, "message": string, "token": string, "user": {...} }`

#### Perfil
- **POST** `/profile` - Crear perfil
  - Header: `Authorization: Bearer {token}`
  - Body: ProfileRequest (ver estructura abajo)
  - Response: `{ "success": boolean, "message": string, "profile": {...} }`

- **GET** `/profile/{id}` - Obtener perfil
  - Header: `Authorization: Bearer {token}`

- **PUT** `/profile/{id}` - Actualizar perfil
  - Header: `Authorization: Bearer {token}`
  - Body: ProfileRequest

### 4. Estructura de ProfileRequest

```json
{
  "email": "string",
  "profile_type": "ATHLETE|AGENT|TEAM",
  "name": "string",
  "last_name": "string",
  "description": "string",
  "location_id": number,
  "sport_id": number,
  "phone_number": "string",
  "ig_user": "string",
  "x_user": "string",
  
  // Solo para ATHLETE
  "birthdate": "string",
  "height": "string",
  "weight": "string",
  
  // Solo para AGENT
  "agency": "string",
  
  // Solo para TEAM
  "job": "string"
}
```

### 5. Flujo de Registro

1. Usuario ingresa email y contraseña en `RegisterActivity`
2. Se llama a `POST /auth/register`
3. Si es exitoso, se recibe un token
4. Usuario selecciona tipo de perfil en `SelectProfileActivity`
5. Usuario completa datos del perfil en `ProfileFormActivity`
6. Se llama a `POST /profile` con el token
7. Sesión se guarda con `SessionManager`

### 6. Flujo de Login

1. Usuario ingresa email y contraseña en `LoginActivity`
2. Se llama a `POST /auth/login`
3. Si es exitoso, se recibe token y datos de usuario
4. Sesión se guarda con `SessionManager`
5. Usuario es redirigido a la pantalla principal

### 7. Gestión de Sesión

La clase `SessionManager` guarda:
- Token de autenticación
- ID de usuario
- Email
- Tipo de perfil
- Estado de sesión activa

Uso:
```java
SessionManager sessionManager = new SessionManager(context);

// Guardar sesión
sessionManager.saveSession(token, userId, email, profileType);

// Obtener datos
String token = sessionManager.getToken();
boolean isLoggedIn = sessionManager.isLoggedIn();

// Cerrar sesión
sessionManager.logout();
```

### 8. Dependencias Agregadas

En `app/build.gradle.kts`:
```gradle
// Retrofit para HTTP
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
implementation("com.google.code.gson:gson:2.10.1")
```

### 9. Permisos en AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Y en `<application>`:
```xml
android:usesCleartextTraffic="true"
```

### 10. Pasos para Probar

1. **Sincronizar Gradle:** En Android Studio, haz clic en "Sync Now" o ejecuta:
   ```
   ./gradlew build
   ```

2. **Configurar Backend:** Asegúrate de que tu backend Node.js esté corriendo en el puerto 3000

3. **Ajustar URL:** Modifica `BASE_URL` en `ApiClient.java` según tu configuración

4. **Ejecutar App:** Instala la app en el emulador o dispositivo

5. **Probar Registro:** 
   - Abre la app
   - Click en "Registrarse"
   - Ingresa email y contraseña
   - Selecciona tipo de perfil
   - Completa los datos
   - Verifica en el backend que se creó el usuario y perfil

6. **Probar Login:**
   - Click en "Iniciar Sesión"
   - Ingresa credenciales
   - Verifica que la sesión se guarde correctamente

### 11. Debug

Para ver los logs de las peticiones HTTP en Logcat, filtra por:
- Tag: `AuthRepository`
- Tag: `OkHttp` (para ver requests/responses completos)

### 12. Notas Importantes

- Los campos `location_id` y `sport_id` deben ser IDs válidos que existan en tu base de datos
- El formato de `birthdate` debe coincidir con el esperado por tu backend
- Los campos de redes sociales (`ig_user`, `x_user`) son opcionales
- El token JWT se envía automáticamente en el header `Authorization: Bearer {token}`

### 13. Próximos Pasos

- Crear pantalla principal después del login exitoso
- Implementar pantalla para ver/editar perfil
- Agregar búsqueda de atletas/agentes/equipos
- Implementar funcionalidades de conexión entre perfiles

