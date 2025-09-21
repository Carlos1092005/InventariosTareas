package com.cms.inventariostareas.models;

public class MovimientoInventario {
    private String id;
    private String equipoId;
    private String equipoNombre;
    private String tipo; // "entrada" o "salida"
    private int cantidad;
    private String proveedor;
    private String responsable;
    private String fecha;

    public MovimientoInventario() {}

    public MovimientoInventario(String id, String equipoId, String equipoNombre, String tipo,
                                int cantidad, String proveedor, String responsable, String fecha) {
        this.id = id;
        this.equipoId = equipoId;
        this.equipoNombre = equipoNombre;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.proveedor = proveedor;
        this.responsable = responsable;
        this.fecha = fecha;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEquipoId() { return equipoId; }
    public void setEquipoId(String equipoId) { this.equipoId = equipoId; }
    public String getEquipoNombre() { return equipoNombre; }
    public void setEquipoNombre(String equipoNombre) { this.equipoNombre = equipoNombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}