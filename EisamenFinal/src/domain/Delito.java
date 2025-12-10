
package domain;

public class Delito {
    private String nombre;
    private int penaMinima;
    private int penaMaxima;

    public Delito(String nombre, int penaMinima, int penaMaxima) {
        this.nombre = nombre;
        this.penaMinima = penaMinima;
        this.penaMaxima = penaMaxima;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPenaMinima() {
        return penaMinima;
    }

    public int getPenaMaxima() {
        return penaMaxima;
    }

    public String toFileString() {
        return nombre + ";" + penaMinima + ";" + penaMaxima;
    }

    public static Delito fromFileString(String linea) {
        String[] partes = linea.split(";");
        if (partes.length < 3) return null;
        String nombre = partes[0];
        int min = Integer.parseInt(partes[1]);
        int max = Integer.parseInt(partes[2]);
        return new Delito(nombre, min, max);
    }

    @Override
    public String toString() {
        return nombre + " (Pena: " + penaMinima + " - " + penaMaxima + " años)";
    }
}
