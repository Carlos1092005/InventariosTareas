package com.cms.inventariostareas.models;

public class Equipo {
    private String id;
    private String nombre;
    private String categoria;
    private String marca;
    private String modelo;
    private String estado;
    private String ubicacion;
    private int cantidad;
    private int disponibles;
    private String qrCode;

    public Equipo() {}

    public Equipo(String id, String nombre, String categoria, String marca, String modelo,
                  String estado, String ubicacion, int cantidad, int disponibles, String qrCode) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.marca = marca;
        this.modelo = modelo;
        this.estado = estado;
        this.ubicacion = ubicacion;
        this.cantidad = cantidad;
        this.disponibles = disponibles;
        this.qrCode = qrCode;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getDisponibles() { return disponibles; }
    public void setDisponibles(int disponibles) { this.disponibles = disponibles; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
}