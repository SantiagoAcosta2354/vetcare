# VetCare

Plataforma distribuida para la operacion clinica y comercial de una clinica
veterinaria: pacientes, agenda, historia clinica, formula de medicamentos,
inventario de farmacia y facturacion.

Proyecto Integrador de Aula — Formulacion y Desarrollo de Plataforma Empresarial
Distribuida. Arquitectura Java / Spring Framework · Microservicios · MongoDB ·
Despliegue en la nube.

## Estructura

Monorepo: una carpeta raiz con un directorio por servicio y un unico repositorio
de Git. Cada servicio tiene su propio `pom.xml`, su `Dockerfile` y su base de
datos; se puede compilar y desplegar por separado.

| Servicio | Puerto | Base de datos | Responsabilidad |
|---|---|---|---|
| `vetcare-usuarios` | 8081 | `vetcare_usuarios` | Cuentas, perfiles, autenticacion y emision del token |
| `vetcare-pacientes` | 8082 | `vetcare_pacientes` | Duenos, mascotas, sedes, disponibilidad y citas |
| `vetcare-clinica` | 8083 | `vetcare_clinica` | Historia, diagnostico, tratamiento, formula, examenes y vacunas |
| `vetcare-farmacia` | 8084 | `vetcare_farmacia` | Medicamentos, lotes, vencimientos, despachos y reposicion |
| `vetcare-facturacion` | 8085 | `vetcare_facturacion` | Tarifas, facturas, pagos y cartera |
| `vetcare-web` | 8080 | — | Capa de vista con Thymeleaf y Bootstrap |

## Perfiles (6)

| Rol | Que hace | Limite |
|---|---|---|
| `ADMIN` | Usuarios, tarifas, sedes, parametros y tablero | No edita una historia cerrada ni una factura pagada |
| `RECEPCION` | Duenos, mascotas, agenda, check-in, cobro y factura | No registra diagnosticos ni formulas |
| `VETERINARIO` | Historia, diagnostico, formula, examenes y vacunas | No despacha medicamentos ni anula facturas |
| `LABORATORIO` | Ordenes de examen, registro de resultados y asociacion al paciente | No interpreta, no diagnostica y no cobra |
| `FARMACIA` | Despacho de formulas, lotes y vencimientos | No formula ni modifica precios |
| `DUENO` | Portal con sus mascotas, proximas dosis y facturas | No ve historias de otros pacientes |

Los seis perfiles son usuarios finales de la plataforma y no deben confundirse con
los tres roles de Scrum del equipo (Dueno de Producto, Scrum Master y Equipo de
Desarrollo).

## Seguridad entre servicios

El servicio de usuarios firma los tokens con HS256. Los demas solo validan la
firma con el mismo secreto (`VETCARE_JWT_SECRET`, minimo 32 caracteres), de modo
que no consultan al servicio de usuarios en cada peticion y una caida suya no
bloquea la operacion clinica.

El token lleva:

```
sub   -> identificador del usuario
email -> correo
name  -> nombre completo
roles -> ["ADMIN"] | ["RECEPCION"] | ["VETERINARIO"] | ["LABORATORIO"] | ["FARMACIA"] | ["DUENO"]
iss   -> vetcare-usuarios
exp   -> expiracion
```

Cada servicio aplica el permiso en el servidor: ocultar una opcion en la pantalla
no protege la operacion. La ruta directa con un rol sin autorizacion responde 403.

## Puesta en marcha local

Requisitos: Java 21, Maven 3.9, MongoDB (local o Atlas) y, para el despliegue,
Docker.

1. Definir el secreto compartido en la sesion antes de arrancar cualquier
   servicio:

```powershell
$env:VETCARE_JWT_SECRET = "vetcare-clave-de-desarrollo-cambiar-en-produccion-2026"
```

2. Arrancar cada servicio en su carpeta y comprobar su salud:

```powershell
cd vetcare-usuarios
mvn spring-boot:run
# http://localhost:8081/actuator/health
```

3. Repetir con `vetcare-pacientes` (8082). El orden importa: sin cuentas no hay
   permisos que probar y sin pacientes no hay a quien atender.

4. Entrar con la cuenta de administrador (`admin@vetcare.co` / `Admin1234*`) y
   verificar que cada perfil ve unicamente sus areas.

## Estado del desarrollo

| Modulo | Estado | Que existe |
|---|---|---|
| `vetcare-usuarios` | Terminado | Cuentas, seis roles, JWT, auditoria de cambios, 13 pruebas |
| `vetcare-pacientes` | Terminado | Duenos, mascotas, sedes, agenda con disponibilidad, 12 pruebas |
| `vetcare-clinica` | Pendiente | — |
| `vetcare-farmacia` | Pendiente | — |
| `vetcare-facturacion` | Pendiente | — |
| `vetcare-web` | Pendiente | — |

El avance se mide por servicio desplegado y demostrable, no por lineas de codigo.

## Despliegue en Render

`render.yaml` declara los servicios del monorepo. Al sincronizar el Blueprint,
Render pide la cadena de MongoDB de cada servicio y genera el secreto compartido
en `vetcare-usuarios`; los demas lo copian con `fromService`.

En el plan gratuito el servicio se suspende por inactividad y el primer acceso
tarda alrededor de un minuto: conviene despertar los servicios antes de la
sustentacion, no durante.
