# vetcare-clinica

Microservicio de **historia clinica, formulas, examenes y vacunacion**.
- Base propia: `vetcare_clinica` · Puerto local: `8083` · Swagger: `/swagger-ui.html`

## Reglas de negocio

1. **Una historia por cita** (indice unico sobre `appointmentId`).
2. Una historia **cerrada no admite cambios**: ni signos vitales, ni diagnostico, ni formulas nuevas.
3. La formula solo se emite dentro de una historia abierta y con medicamentos que **existen en el catalogo de farmacia** (consulta HTTP).
4. Formular **no descuenta inventario**: el descuento ocurre cuando farmacia despacha.
5. La orden de examen nace `PENDIENTE`; el laboratorio la cierra al registrar el resultado, y solo entonces puede cobrarse como entregada.
6. La proxima dosis se calcula con el esquema de la vacuna (`nextDoseDays`) y la **especie** debe coincidir.
7. Cerrar con formulas pendientes de despacho se permite, pero deja **constancia escrita** en la historia.

## Estados

```
Historia:   ABIERTA -> CERRADA
Formula:    EMITIDA -> DESPACHADA | PENDIENTE_POR_STOCK
Examen:     PENDIENTE -> ENTREGADO
```

## Endpoints principales

| Metodo | Ruta | Acceso |
|---|---|---|
| POST | `/api/v1/records` | VETERINARIO |
| GET | `/api/v1/records/{id}` · `/by-appointment/{id}` · `?petId=` | VETERINARIO |
| PATCH | `/api/v1/records/{id}/vitals` · `/diagnosis` | VETERINARIO |
| POST | `/api/v1/records/{id}/close` | VETERINARIO |
| POST | `/api/v1/records/{id}/prescriptions` · `/lab-orders` · `/vaccinations` | VETERINARIO |
| GET | `/api/v1/prescriptions?recordId=&petId=&status=` | VETERINARIO, FARMACIA |
| PATCH | `/api/v1/prescriptions/{id}/status` | FARMACIA |
| GET | `/api/v1/lab-orders?status=` | VETERINARIO, LABORATORIO |
| POST | `/api/v1/lab-orders/{id}/result` | LABORATORIO |
| GET | `/api/v1/vaccines` · `/vaccinations?petId=` · `/vaccinations/upcoming` | VETERINARIO |
| GET | `/api/v1/vaccinations/portal/{petId}` | DUENO (verifica pertenencia) |

## Pruebas

```bash
mvn test
```

Catorce pruebas: historia (apertura unica, cierre, inmutabilidad), formulas (catalogo y estados) y vacunacion (calculo de dosis y especie).
