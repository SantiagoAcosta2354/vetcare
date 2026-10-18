# vetcare-farmacia

Microservicio de **medicamentos, lotes, vencimientos y despachos**.
- Base propia: `vetcare_farmacia` · Puerto local: `8084` · Swagger: `/swagger-ui.html`

## Reglas de negocio

1. La existencia **no se guarda**: se calcula sumando los lotes vigentes. Un campo de stock se desincroniza en cuanto alguien despacha o recibe mercancia.
2. El descuento ocurre **al despachar**, nunca al formular.
3. Un lote vencido **no se entrega**, aunque haya existencia.
4. Se despacha primero el lote que **vence antes** (FEFO); si un lote no alcanza, se completa con el siguiente.
5. Si falta existencia de cualquier item, el despacho queda `PENDIENTE_POR_STOCK` y **no se descuenta nada**: un despacho parcial haria que la factura cobrara producto no entregado.
6. Recibir el mismo lote **suma** existencia; recibirlo con otra fecha de vencimiento se rechaza.
7. La entrada de mercancia exige lote y vencimiento futuro.

## Endpoints principales

| Metodo | Ruta | Acceso |
|---|---|---|
| GET | `/api/v1/medications?q=` · `/{id}` · `/{id}/batches` | ADMIN, FARMACIA, VETERINARIO |
| POST/PUT/PATCH | `/api/v1/medications` | ADMIN, FARMACIA |
| POST | `/api/v1/purchases` | ADMIN, FARMACIA |
| GET | `/api/v1/alerts` | ADMIN, FARMACIA |
| POST | `/api/v1/dispensings` | ADMIN, FARMACIA |
| GET | `/api/v1/dispensings?status=&petId=` | ADMIN, FARMACIA, VETERINARIO |
| POST | `/api/v1/dispensings/{id}/retry` | ADMIN, FARMACIA |

## Pruebas

```bash
mvn test
```

Diez pruebas: descuento, FEFO, lote vencido, pendiente por stock sin descuento, doble despacho, catalogo y alertas.
