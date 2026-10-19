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

El `pom.xml` de la raiz es un **agregador** (`packaging: pom`): no hereda de
Spring Boot, solo declara los modulos. Sirve para que el IDE muestre un solo
proyecto VetCare con sus modulos dentro y para compilar todo con un comando:

```powershell
mvn test                                  # compila y prueba los modulos
mvn -pl vetcare-usuarios spring-boot:run  # arranca uno solo
```

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

Requisitos: Java 21, Maven 3.9, Docker Desktop (para MongoDB local) y, para el
despliegue, la cuenta de Render.

1. Levantar MongoDB. `docker-compose.yml` crea un contenedor con las cinco bases
   del proyecto:

```powershell
cd C:\workspace-vetcare
docker compose up -d mongo
```

Si no usa Docker, defina `VETCARE_MONGO_URI` con su cadena de MongoDB Atlas
antes de arrancar; el valor por defecto apunta a `localhost:27017`.

2. Arrancar un servicio con el script, que define el secreto compartido y
   verifica MongoDB antes de ejecutar:

```powershell
.\arrancar-vetcare.ps1 usuarios     # 8081
.\arrancar-vetcare.ps1 pacientes    # 8082
.\arrancar-vetcare.ps1 clinica      # 8083
.\arrancar-vetcare.ps1 farmacia     # 8084
.\arrancar-vetcare.ps1 facturacion  # 8085
.\arrancar-vetcare.ps1 web          # 8080  <- abrir en el navegador
```

Tambien se puede arrancar a mano desde la carpeta del modulo con
`mvn spring-boot:run`, definiendo antes `VETCARE_JWT_SECRET`.

3. El orden importa: sin cuentas no hay permisos que probar y sin pacientes no
   hay a quien atender.

4. Entrar con la cuenta de administrador (`admin@vetcare.co` / `Admin1234*`) y
   verificar que cada perfil ve unicamente sus areas.

## Estado del desarrollo

| Modulo | Estado | Que existe |
|---|---|---|
| `vetcare-usuarios` | Terminado | Cuentas, seis roles, JWT, auditoria de cambios, 13 pruebas |
| `vetcare-pacientes` | Terminado | Duenos, mascotas, sedes, agenda con disponibilidad, 12 pruebas |
| `vetcare-clinica` | Terminado | Historia, formulas, examenes, vacunas y cierre inmutable, 14 pruebas |
| `vetcare-farmacia` | Terminado | Catalogo, lotes FEFO, despachos y alertas, 10 pruebas |
| `vetcare-facturacion` | Terminado | Tarifas, facturas, pagos, anulaciones y cartera, 8 pruebas |
| `vetcare-web` | Terminado | Thymeleaf + Bootstrap con las nueve pantallas por perfil |

Cincuenta y siete pruebas unitarias en total. El avance se mide por servicio
demostrable, no por lineas de codigo.

## Datos de demostracion

Al arrancar por primera vez, los servicios crean lo minimo para mostrar el flujo
completo: la sede **Sede Centro**, la duena **Ana Perez** (CC 1020304050) con la
paciente **Luna**, cuatro medicamentos con lote vigente, cinco vacunas con su
esquema y seis cuentas, una por perfil.

El flujo de la sustentacion es: agendar (recepcion) -> atender y formular
(veterinario) -> despachar (farmacia) -> registrar resultado (laboratorio) ->
facturar y cobrar (recepcion) -> consultar (dueno).

## En vivo (Render)

| Servicio | URL |
|---|---|
| **Portal web** | https://vetcare-web-yefk.onrender.com |
| usuarios | https://vetcare-usuarios.onrender.com |
| pacientes | https://vetcare-pacientes.onrender.com |
| clinica | https://vetcare-clinica.onrender.com |
| farmacia | https://vetcare-farmacia.onrender.com |
| facturacion | https://vetcare-facturacion.onrender.com |

La base es un cluster **M0 gratuito** de MongoDB Atlas exclusivo del proyecto
(`vetcare`), con una base por servicio. En el plan gratuito los servicios se
suspenden por inactividad: el primer acceso del dia tarda alrededor de un minuto
y conviene despertarlos antes de la sustentacion.

## Despliegue en Render

`render.yaml` declara los servicios del monorepo. Al sincronizar el Blueprint,
Render pide la cadena de MongoDB de cada servicio y genera el secreto compartido
en `vetcare-usuarios`; los demas lo copian con `fromService`.

En el plan gratuito el servicio se suspende por inactividad y el primer acceso
tarda alrededor de un minuto: conviene despertar los servicios antes de la
sustentacion, no durante.
