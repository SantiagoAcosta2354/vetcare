package com.vetcare.web.client;

/** Error devuelto por un microservicio, con el mensaje que ese servicio explico. */
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public boolean isNotFound() {
        return status == 404;
    }
}
