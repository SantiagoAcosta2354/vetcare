# vetcare-pacientes

Microservicio de **duenos, mascotas, sedes y agenda** de VetCare.
Administra la identificacion del cliente, la ficha del paciente y la
disponibilidad real de los profesionales.

- Java 21 · Spring Boot 4.1.1 · Spring Data MongoDB · Spring Security (JWT HS256)
- Base de datos propia: `vetcare_pacientes` (patron database-per-service)
- Documentacion de la API: `/swagger-ui.html`
- Puerto local: `8082`

## Colecciones

| Coleccion | Contenido |
|---|---|
| `owners` | Documento unico, nombre, telefono, correo, cuenta del portal (`userId`) |
| `pets` | Dueno, nombre, especie, raza, sexo, nacimiento, peso, microchip unico, estado |
| `clinics` | Sede, direccion, horario y duracion de la franja |
| `appointments` | Cita con paciente, dueno, profesional, fecha, franja y estado |

## Reglas de negocio

1. El documento del dueno es unico: dos registros con la misma cedula duplicarian
   la historia y la cartera del mismo cliente.
2. El microchip es opcional, pero cuando existe es unico en toda la clinica.
3. Solo se agenda sobre pacientes `ACTIVO`; un paciente `SUSPENDIDO` conserva su
   historia pero no admite citas nuevas.
4. La cita debe caer dentro del horario de la sede y empezar en un multiplo de la
   franja configurada (30 minutos por defecto).
5. Un profesional no puede tener dos citas cuyos intervalos se crucen. Una cita
   cancelada no ocupa franja: al cancelarla, la franja vuelve a estar disponible.
6. Las transiciones de estado son las del dominio:

```
AGENDADA   -> CONFIRMADA | EN_ATENCION | CANCELADA | NO_ASISTIO
CONFIRMADA -> EN_ATENCION | CANCELADA | NO_ASISTIO
EN_ATENCION-> ATENDIDA | CANCELADA
ATENDIDA, CANCELADA, NO_ASISTIO -> (final)
```

7. Cancelar exige motivo; la inasistencia conserva el registro.

## Endpoints

| Metodo | Ruta | Acceso | Descripcion |
|---|---|---|---|
| GET | `/api/v1/owners?q=` | ADMIN, RECEPCION, VETERINARIO | Lista o busca duenos |
| GET | `/api/v1/owners/{id}` | ADMIN, RECEPCION, VETERINARIO | Detalle |
| GET | `/api/v1/owners/by-document/{documento}` | ADMIN, RECEPCION, VETERINARIO | Busqueda por documento |
| GET | `/api/v1/owners/by-user/{userId}` | ADMIN, RECEPCION, VETERINARIO | Ficha del dueno de una cuenta |
| POST | `/api/v1/owners` | ADMIN, RECEPCION | Registra dueno |
| PUT | `/api/v1/owners/{id}` | ADMIN, RECEPCION | Actualiza |
| PATCH | `/api/v1/owners/{id}/status?active=` | ADMIN, RECEPCION | Activa o desactiva |
| GET | `/api/v1/pets?ownerId=` | ADMIN, RECEPCION, VETERINARIO | Pacientes del dueno |
| GET | `/api/v1/pets/{id}` | ADMIN, RECEPCION, VETERINARIO | Detalle |
| GET | `/api/v1/pets/by-microchip/{chip}` | ADMIN, RECEPCION, VETERINARIO | Busqueda por microchip |
| POST | `/api/v1/pets` | ADMIN, RECEPCION | Registra paciente |
| PUT | `/api/v1/pets/{id}` | ADMIN, RECEPCION | Actualiza ficha |
| PATCH | `/api/v1/pets/{id}/status?status=` | ADMIN, RECEPCION | Suspende o reactiva |
| GET | `/api/v1/clinics` | Autenticado | Sedes activas |
| POST | `/api/v1/clinics` | ADMIN | Crea sede |
| POST | `/api/v1/appointments` | ADMIN, RECEPCION, VETERINARIO | Agenda con validacion de franja |
| GET | `/api/v1/appointments?date=&clinicId=` | ADMIN, RECEPCION, VETERINARIO | Agenda del dia |
| GET | `/api/v1/appointments/availability?professionalId=&clinicId=&date=` | ADMIN, RECEPCION, VETERINARIO | Franjas libres |
| GET | `/api/v1/appointments/summary?date=` | ADMIN, RECEPCION, VETERINARIO | Cifras del dia |
| PATCH | `/api/v1/appointments/{id}/status` | ADMIN, RECEPCION, VETERINARIO | Cambio de estado |
| GET | `/api/v1/portal/pets` | DUENO | Sus mascotas (filtro por token) |
| GET | `/api/v1/portal/appointments` | DUENO | Sus citas (filtro por token) |

## Pruebas

```bash
mvn clean test
```

Doce pruebas unitarias: diez de agenda (solapamiento, franja, horario, paciente
suspendido, transiciones, disponibilidad) y dos del registro de pacientes.
