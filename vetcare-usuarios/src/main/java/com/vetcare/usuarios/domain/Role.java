package com.vetcare.usuarios.domain;

/**
 * Perfiles del sistema. Se redujeron de seis a cinco: las funciones del
 * auxiliar de laboratorio se integraron al veterinario, que es quien ordena el
 * examen y responde por el resultado.
 *
 * Estos nombres viajan en el claim "roles" del token y son los que autorizan
 * las operaciones en los cinco servicios.
 *
 * <ul>
 *   <li>ADMIN: cuentas, tarifas, sedes y parametros; consulta el tablero.</li>
 *   <li>RECEPCION: duenos, mascotas, agenda, check-in, cobro y factura.</li>
 *   <li>VETERINARIO: historia clinica, diagnostico, formula, examenes y vacunas.</li>
 *   <li>FARMACIA: despacho de formulas, lotes, vencimientos y reposicion.</li>
 *   <li>DUENO: portal con sus mascotas, proximas dosis y facturas.</li>
 * </ul>
 */
public enum Role {

    ADMIN("Administrador"),
    RECEPCION("Recepcionista"),
    VETERINARIO("Veterinario"),
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
