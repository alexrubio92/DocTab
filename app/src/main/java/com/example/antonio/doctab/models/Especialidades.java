package com.example.antonio.doctab.models;

/**
 * Created by Ricardo on 13/02/2018.
 */

public class Especialidades {

    private String especialidad;
    private String subespecialidad;
    private String univerisdad;
    private String anoEgreso;
    private String firabaseId;

    public Especialidades(String especialidad, String subespecialidad, String univerisdad,
                          String anoEgreso, String firabaseId) {
        this.especialidad = especialidad;
        this.subespecialidad = subespecialidad;
        this.univerisdad = univerisdad;
        this.anoEgreso = anoEgreso;
        this.firabaseId = firabaseId;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getSubespecialidad() {
        return subespecialidad;
    }

    public void setSubespecialidad(String subespecialidad) {
        this.subespecialidad = subespecialidad;
    }

    public String getUniverisdad() {
        return univerisdad;
    }

    public void setUniverisdad(String univerisdad) {
        this.univerisdad = univerisdad;
    }

    public String getAnoEgreso() {
        return anoEgreso;
    }

    public void setAnoEgreso(String anoEgreso) {
        this.anoEgreso = anoEgreso;
    }

    public String getFirabaseId() {
        return firabaseId;
    }

    public void setFirabaseId(String firabaseId) {
        this.firabaseId = firabaseId;
    }
}
