package com.cms.inventariostareas.models;

public class Prestamo {
    private String id;
    private String equipoId;
    private String equipoNombre;
    private int cantidad;
    private String instructorId;
    private String instructorNombre;
    private String fechaPrestamo;
    private String fechaDevolucion;
    private String estado; // "pendiente", "aprobado", "rechazado", "devuelto"
    private String supervisorId;
    private String supervisorNombre;
    private String fechaAprobacion;
    private String condicionEquipo;

    public Prestamo() {}

    public Prestamo(String id, String equipoId, String equipoNombre, int cantidad,
                    String instructorId, String instructorNombre, String fechaPrestamo,
                    String fechaDevolucion, String estado, String supervisorId,
                    String supervisorNombre, String fechaAprobacion, String condicionEquipo) {
        this.id = id;
        this.equipoId = equipoId;
        this.equipoNombre = equipoNombre;
        this.cantidad = cantidad;
        this.instructorId = instructorId;
        this.instructorNombre = instructorNombre;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = estado;
        this.supervisorId = supervisorId;
        this.supervisorNombre = supervisorNombre;
        this.fechaAprobacion = fechaAprobacion;
        this.condicionEquipo = condicionEquipo;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEquipoId() { return equipoId; }
    public void setEquipoId(String equipoId) { this.equipoId = equipoId; }
    public String getEquipoNombre() { return equipoNombre; }
    public void setEquipoNombre(String equipoNombre) { this.equipoNombre = equipoNombre; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }
    public String getInstructorNombre() { return instructorNombre; }
    public void setInstructorNombre(String instructorNombre) { this.instructorNombre = instructorNombre; }
    public String getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(String fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }
    public String getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(String fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getSupervisorId() { return supervisorId; }
    public void setSupervisorId(String supervisorId) { this.supervisorId = supervisorId; }
    public String getSupervisorNombre() { return supervisorNombre; }
    public void setSupervisorNombre(String supervisorNombre) { this.supervisorNombre = supervisorNombre; }
    public String getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(String fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }
    public String getCondicionEquipo() { return condicionEquipo; }
    public void setCondicionEquipo(String condicionEquipo) { this.condicionEquipo = condicionEquipo; }
}