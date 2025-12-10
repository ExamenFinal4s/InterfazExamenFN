
package domain;

import java.util.ArrayList;

public class Celda {
    private int numero;
    private int capacidad;
    private ArrayList<Interno> internos;

    public Celda(int numero, int capacidad) {
        this.numero = numero;
        this.capacidad = capacidad;
        this.internos = new ArrayList<>();
    }

    public int getNumero() {
        return numero;
    }

    public ArrayList<Interno> getInternos() {
        return internos;
    }

    public boolean agregarInterno(Interno interno) {
        if (internos.size() < capacidad) {
            internos.add(interno);
            return true;
        }
        return false;
    }

    public boolean removerInternoPorId(String id) {
        return internos.removeIf(interno -> interno.getId().equals(id));
    }

    public int espaciosDisponibles() {
        return capacidad - internos.size();
    }

    @Override
    public String toString() {
        return "Celda " + numero +
               " | Capacidad: " + capacidad +
               " | Ocupados: " + internos.size() +
               " | Disponibles: " + espaciosDisponibles();
    }
}
