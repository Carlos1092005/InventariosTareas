package com.cms.inventariostareas.models;

public class Reporte {
    private String tipo;
    private String descripcion;
    private String detalles;
    private String fecha;

    public Reporte() {}

    public Reporte(String tipo, String descripcion, String detalles, String fecha) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.detalles = detalles;
        this.fecha = fecha;
    }

    // Getters y setters
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}