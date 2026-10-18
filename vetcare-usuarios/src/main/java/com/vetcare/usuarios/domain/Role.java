package com.vetcare.usuarios.domain;

/**
 * Perfiles del sistema. Son seis, y cada uno responde por una parte distinta del
 * proceso: quien agenda, quien atiende, quien procesa la muestra, quien entrega
 * el medicamento, quien cobra y quien administra.
 *
 * Estos nombres viajan en el claim "roles" del token y son los que autorizan
 * las operaciones en los cinco servicios.
 *
 * <ul>
 *   <li>ADMIN: cuentas, tarifas, sedes y parametros; consulta el tablero.</li>
 *   <li>RECEPCION: duenos, mascotas, agenda, check-in, cobro y factura.</li>
 *   <li>VETERINARIO: historia clinica, diagnostico, formula, examenes y vacunas.</li>
 *   <li>LABORATORIO: recibe ordenes de examen, registra resultados y los asocia
 *       al paciente. No interpreta, no diagnostica y no cobra.</li>
 *   <li>FARMACIA: despacho de formulas, lotes, vencimientos y reposicion.</li>
 *   <li>DUENO: portal con sus mascotas, proximas dosis y facturas.</li>
 * </ul>
 */
public enum Role {

    ADMIN("Administrador"),
    RECEPCION("Recepcionista"),
    VETERINARIO("Veterinario"),
    LABORATORIO("Auxiliar de laboratorio"),
    FARMACIA("Encargado de farmacia"),
    DUENO("Dueno de mascota");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
