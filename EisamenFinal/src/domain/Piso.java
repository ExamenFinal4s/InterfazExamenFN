
package domain;

import java.util.ArrayList;

public class Piso {
    private int numero;
    private ArrayList<Celda> celdas;

    public Piso(int numero) {
        this.numero = numero;
        this.celdas = new ArrayList<>();
    }

    public int getNumero() {
        return numero;
    }

    public ArrayList<Celda> getCeldas() {
        return celdas;
    }

    public void agregarCelda(Celda celda) {
        celdas.add(celda);
    }

    public Celda buscarCelda(int num) {
        for (Celda c : celdas) {
            if (c.getNumero() == num) return c;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Piso " + numero + " con " + celdas.size() + " celdas.";
    }
}
