# vetcare-web

Capa de vista en **Thymeleaf + Bootstrap 5**. No tiene base de datos: inicia
sesion contra `vetcare-usuarios`, guarda el token en la sesion y lo reenvia en
cada llamada a los microservicios.

- Puerto local: `8080` · Entry point: `http://localhost:8080`

## Pantallas por perfil

| Ruta | Perfiles | Contenido |
|---|---|---|
| `/tablero` | ADMIN | Citas del dia, cartera, proximas dosis, alertas de farmacia |
| `/agenda` | ADMIN, RECEPCION | Agenda por fecha, cambio de estado y agendamiento |
| `/atencion` | ADMIN, VETERINARIO | Citas del dia y acceso a la historia |
| `/atencion/registro/{id}` | ADMIN, VETERINARIO | Signos, diagnostico, formula, examen, vacuna y cierre |
| `/examenes` | ADMIN, LABORATORIO | Ordenes pendientes y registro de resultados |
| `/farmacia` | ADMIN, FARMACIA | Formulas por despachar, stock, alertas y recepcion de mercancia |
| `/facturacion` | ADMIN, RECEPCION | Emision, pagos y anulacion |
| `/admin` | ADMIN | Cuentas, roles y estado |
| `/portal` | DUENO | Mascotas, proximas dosis, citas y facturas |

## Decisiones

- El token vive en la sesion; no se guardan contrasenas ni se consultan bases ajenas.
- Un servicio que responde 403 o 409 muestra su propio mensaje: la vista no inventa el motivo.
- Los errores de la API se presentan en una pantalla con el codigo y el texto del servicio.

## Pruebas manuales sugeridas

1. Entrar como `recepcion@vetcare.co` y agendar una cita para Luna manana a las 09:00.
2. Entrar como `veterinario@vetcare.co`, abrir la historia y registrar signos, diagnostico y una formula.
3. Entrar como `farmacia@vetcare.co` y despachar; verificar que la existencia baja.
4. Entrar como `laboratorio@vetcare.co` y registrar el resultado de un examen.
5. Entrar como `recepcion@vetcare.co`, emitir la factura y registrar un pago parcial.
6. Entrar como `dueno@vetcare.co` y ver mascotas, proximas dosis y facturas.
