# vetcare-facturacion

Microservicio de **tarifas, facturas, pagos y cartera**.
- Base propia: `vetcare_facturacion` · Puerto local: `8085` · Swagger: `/swagger-ui.html`

## Reglas de negocio

1. La **consulta del dia no se factura dos veces** al mismo paciente.
2. El **IVA solo se aplica a productos** (19 % por defecto); los servicios van sin IVA.
3. Se admiten **pagos parciales**; la factura pasa a `PAGADA` solo cuando el saldo llega a **cero**.
4. Un pago **no se edita**: se anula con motivo y el saldo se recalcula.
5. Anular una factura exige motivo y **no admite pagos aplicados sin revertir** (solo ADMIN).
6. El numero de factura es consecutivo (`FV-000001`) y unico.

## Endpoints principales

| Metodo | Ruta | Acceso |
|---|---|---|
| GET | `/api/v1/rates` | ADMIN, RECEPCION |
| POST/PUT/PATCH | `/api/v1/rates` | ADMIN |
| POST | `/api/v1/invoices` | ADMIN, RECEPCION |
| GET | `/api/v1/invoices` · `/{id}` · `/by-number/{n}` | ADMIN, RECEPCION |
| POST | `/api/v1/invoices/{id}/payments` | ADMIN, RECEPCION |
| POST | `/api/v1/payments/{id}/void` | ADMIN, RECEPCION |
| POST | `/api/v1/invoices/{id}/cancel` | ADMIN |
| GET | `/api/v1/portfolio` | ADMIN, RECEPCION |
| GET | `/api/v1/portal/invoices` | DUENO (filtra por token) |

## Pruebas

```bash
mvn test
```

Ocho pruebas: IVA por tipo, consulta duplicada, pago parcial y total, sobrepago, anulacion de pago y de factura.
