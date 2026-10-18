# vetcare-usuarios

Microservicio de **cuentas, perfiles y emision de tokens** de VetCare. Es el unico
servicio que firma los JWT; los otros cinco solo validan la firma con el secreto
compartido.

- Java 21 · Spring Boot 4.1.1 · Spring Data MongoDB · Spring Security (JWT HS256)
- Base de datos propia: `vetcare_usuarios` (patron database-per-service)
- Documentacion de la API: `/swagger-ui.html`
- Puerto local: `8081`

## Perfiles del sistema (5)

| Rol | Que hace | Limite |
|---|---|---|
| `ADMIN` | Gestiona usuarios, tarifas, sedes y parametros; consulta el tablero | No edita una historia cerrada ni una factura pagada |
| `RECEPCION` | Duenos, mascotas, agenda, check-in, cobro y factura | No registra diagnosticos ni formulas |
| `VETERINARIO` | Historia clinica, diagnostico, formula, examenes y vacunas | No despacha medicamentos ni anula facturas |
| `FARMACIA` | Despacha formulas, controla lotes y vencimientos | No formula ni modifica precios |
| `DUENO` | Portal con sus mascotas, proximas dosis y facturas | No ve historias de otros pacientes |

El perfil de auxiliar de laboratorio se integro al veterinario: es quien ordena el
examen y responde por el resultado, de modo que el sistema queda en cinco roles.

## Variables de entorno

| Variable | Para que sirve | Valor por defecto |
|---|---|---|
| `PORT` | Puerto HTTP | `8081` |
| `VETCARE_MONGO_URI` | Cadena de MongoDB de este servicio | `mongodb://localhost:27017/vetcare_usuarios` |
| `VETCARE_JWT_SECRET` | Secreto compartido para firmar y validar (**minimo 32 caracteres**) | valor de desarrollo |
| `VETCARE_JWT_EXPIRATION_MINUTES` | Vigencia del token | `480` |
| `VETCARE_SEED_ENABLED` | Crea las cuentas de demostracion | `true` |
| `VETCARE_ADMIN_EMAIL` / `VETCARE_ADMIN_PASSWORD` | Cuenta administradora inicial | `admin@vetcare.co` / `Admin1234*` |
| `VETCARE_DEMO_PASSWORD` | Contrasena de las cuentas de demostracion | `Demo1234*` |

## Cuentas de demostracion

| Correo | Rol |
|---|---|
| `admin@vetcare.co` | ADMIN |
| `recepcion@vetcare.co` | RECEPCION |
| `veterinario@vetcare.co` | VETERINARIO |
| `farmacia@vetcare.co` | FARMACIA |
| `dueno@vetcare.co` | DUENO |

Todas salvo la administradora comparten la contrasena de demostracion.

## Endpoints

| Metodo | Ruta | Acceso | Descripcion |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Publico | Devuelve el token JWT con los roles |
| POST | `/api/v1/auth/register` | Publico | Registra un dueno (siempre rol DUENO) |
| GET | `/api/v1/users/me` | Autenticado | Cuenta propia |
| PATCH | `/api/v1/users/me` | Autenticado | Nombre y telefono propios |
| PATCH | `/api/v1/users/me/password` | Autenticado | Cambio de contrasena con la actual |
| GET | `/api/v1/users/staff?role=` | ADMIN, RECEPCION, VETERINARIO | Personal activo por rol (agenda) |
| GET | `/api/v1/users` | ADMIN | Lista de cuentas |
| GET | `/api/v1/users/{id}` | ADMIN | Detalle de una cuenta |
| POST | `/api/v1/users` | ADMIN | Crea una cuenta con perfiles |
| PATCH | `/api/v1/users/{id}` | ADMIN | Datos de presentacion |
| PATCH | `/api/v1/users/{id}/roles` | ADMIN | Asigna roles |
| PATCH | `/api/v1/users/{id}/status?active=` | ADMIN | Activa o desactiva |
| POST | `/api/v1/users/{id}/reset-password` | ADMIN | Restablece la contrasena |

## Reglas de negocio implementadas

1. La contrasena se guarda unicamente como hash BCrypt.
2. El registro publico nunca permite elegir perfil: siempre asigna `DUENO`.
3. Correo inexistente y contrasena incorrecta devuelven el mismo 401.
4. Una cuenta inactiva no inicia sesion aunque la clave sea correcta.
5. El correo es unico a nivel de base mediante indice unico.
6. Un administrador no puede retirarse a si mismo el rol `ADMIN`.
7. La plataforma no puede quedarse sin administradores activos.
8. Nadie puede desactivar su propia cuenta.
9. Todo cambio de rol, estado o contrasena guarda autor y fecha en `audit`.

## Pruebas

```bash
mvn clean test
```

Trece pruebas unitarias: once sobre cuentas y roles, dos sobre emision y
validacion del token. No requieren MongoDB.

## Despliegue

```bash
docker build -t vetcare-usuarios .
docker run -p 8081:8081 -e VETCARE_MONGO_URI="mongodb+srv://..." \
  -e VETCARE_JWT_SECRET="secreto-de-32-caracteres-o-mas" vetcare-usuarios
```
