package com.cms.inventariostareas.models;

public class Usuario {
    private String uid;
    private String nombre;
    private String apellido;
    private String correo;
    private String rol;
    private boolean activo;

    // Constructor vacío necesario para Firebase
    public Usuario() {}

    public Usuario(String uid, String nombre, String apellido, String correo, String rol, boolean activo) {
        this.uid = uid;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.rol = rol;
        this.activo = activo;
    }

    // Getters y setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}