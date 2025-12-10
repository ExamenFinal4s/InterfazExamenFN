
package domain;

public class Interno {
    private String id;
    private String nombre;
    private Delito delito;
    private int condenaTotal;
    private int condenaRestante;
    private int pisoNumero;   // -1 si no está asignado
    private int celdaNumero;  // -1 si no está asignado

    public Interno(String id, String nombre, Delito delito, int condenaTotal) {
        this.id = id;
        this.nombre = nombre;
        this.delito = delito;
        this.condenaTotal = condenaTotal;
        this.condenaRestante = condenaTotal;
        this.pisoNumero = -1;
        this.celdaNumero = -1;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Delito getDelito() {
        return delito;
    }

    public int getCondenaTotal() {
        return condenaTotal;
    }

    public int getCondenaRestante() {
        return condenaRestante;
    }

    public void setCondenaRestante(int condenaRestante) {
        this.condenaRestante = condenaRestante;
    }

    public int getPisoNumero() {
        return pisoNumero;
    }

    public int getCeldaNumero() {
        return celdaNumero;
    }

    public void setUbicacion(int pisoNumero, int celdaNumero) {
        this.pisoNumero = pisoNumero;
        this.celdaNumero = celdaNumero;
    }

    public String toFileString() {
        return id + ";" + nombre + ";" + delito.getNombre() + ";" +
               condenaTotal + ";" + condenaRestante + ";" +
               pisoNumero + ";" + celdaNumero;
    }

    @Override
    public String toString() {
        String ubic = (pisoNumero == -1 || celdaNumero == -1)
                ? "SIN CELDA"
                : "Piso " + pisoNumero + ", Celda " + celdaNumero;

        return "ID: " + id + " | " + nombre +
               " | Delito: " + delito.getNombre() +
               " | Condena total: " + condenaTotal + " años" +
               " | Restante: " + condenaRestante + " años" +
               " | Ubicación: " + ubic;
    }
}
