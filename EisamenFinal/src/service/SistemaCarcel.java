
package service;

import domain.*;
import java.util.*;
import java.io.*;

public class SistemaCarcel {

    private static final String DELITOS_FILE = "data/delitos.txt";
    private static final String INTERNOS_FILE = "data/internos.txt";

    private static ArrayList<Delito> delitos = new ArrayList<>();
    private static ArrayList<Piso> pisos = new ArrayList<>();
    private static ArrayList<Interno> internos = new ArrayList<>();

    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        cargarDelitosDesdeArchivo();
        cargarEstructuraCarcel();      // Definimos pisos y celdas
        cargarInternosDesdeArchivo();  // Los internos y su ubicación

        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n--- SISTEMA CÁRCEL MODELO OCAÑA ---");
            System.out.println("1. Registrar interno");
            System.out.println("2. Asignar interno a celda");
            System.out.println("3. Consultar pisos y celdas");
            System.out.println("4. Listar internos");
            System.out.println("5. Listar delitos");
            System.out.println("6. Agregar delito");
            System.out.println("7. Avanzar 1 año (reducir condenas)");
            System.out.println("0. Guardar y salir");
            System.out.print("Opción: ");

            try {
                opcion = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                opcion = -1;
            }

            switch (opcion) {
                case 1 -> registrarInterno();
                case 2 -> asignarCelda();
                case 3 -> consultarCeldas();
                case 4 -> listarInternos();
                case 5 -> listarDelitos();
                case 6 -> agregarDelito();
                case 7 -> avanzarUnAño();
                case 0 -> {
                    guardarDelitosEnArchivo();
                    guardarInternosEnArchivo();
                    System.out.println("Datos guardados. Saliendo...");
                }
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    // ===================== PERSISTENCIA DELITOS =====================

    private static void cargarDelitosDesdeArchivo() {
        File f = new File(DELITOS_FILE);
        if (!f.exists()) {
            System.out.println("No se encontró " + DELITOS_FILE + ". Creando delitos por defecto...");
            crearDelitosPorDefecto();
            guardarDelitosEnArchivo();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                Delito d = Delito.fromFileString(linea);
                if (d != null) {
                    delitos.add(d);
                }
            }
            System.out.println("Delitos cargados desde " + DELITOS_FILE);
        } catch (IOException e) {
            System.out.println("Error leyendo " + DELITOS_FILE + ": " + e.getMessage());
        }
    }

    private static void guardarDelitosEnArchivo() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DELITOS_FILE))) {
            for (Delito d : delitos) {
                pw.println(d.toFileString());
            }
            System.out.println("Delitos guardados en " + DELITOS_FILE);
        } catch (IOException e) {
            System.out.println("Error guardando " + DELITOS_FILE + ": " + e.getMessage());
        }
    }

    private static void crearDelitosPorDefecto() {
        delitos.add(new Delito("Homicidio", 10, 40));
        delitos.add(new Delito("Hurto", 1, 8));
        delitos.add(new Delito("Tráfico de estupefacientes", 4, 20));
        delitos.add(new Delito("Extorsión", 5, 20));
    }

    // ===================== PERSISTENCIA INTERNOS =====================

    private static void cargarInternosDesdeArchivo() {
        File f = new File(INTERNOS_FILE);
        if (!f.exists()) {
            System.out.println("No se encontró " + INTERNOS_FILE + ". No hay internos registrados aún.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(";");
                if (partes.length < 7) continue;

                String id = partes[0];
                String nombre = partes[1];
                String nombreDelito = partes[2];
                int condenaTotal = Integer.parseInt(partes[3]);
                int condenaRestante = Integer.parseInt(partes[4]);
                int pisoNum = Integer.parseInt(partes[5]);
                int celdaNum = Integer.parseInt(partes[6]);

                Delito d = buscarDelitoPorNombre(nombreDelito);
                if (d == null) {
                    System.out.println("Delito no encontrado para interno " + id + ", se omitirá.");
                    continue;
                }

                Interno interno = new Interno(id, nombre, d, condenaTotal);
                interno.setCondenaRestante(condenaRestante);
                interno.setUbicacion(pisoNum, celdaNum);
                internos.add(interno);

                if (pisoNum != -1 && celdaNum != -1) {
                    Piso piso = buscarPiso(pisoNum);
                    if (piso != null) {
                        Celda celda = piso.buscarCelda(celdaNum);
                        if (celda != null) {
                            celda.agregarInterno(interno);
                        }
                    }
                }
            }
            System.out.println("Internos cargados desde " + INTERNOS_FILE);
        } catch (IOException e) {
            System.out.println("Error leyendo " + INTERNOS_FILE + ": " + e.getMessage());
        }
    }

    private static void guardarInternosEnArchivo() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(INTERNOS_FILE))) {
            for (Interno i : internos) {
                pw.println(i.toFileString());
            }
            System.out.println("Internos guardados en " + INTERNOS_FILE);
        } catch (IOException e) {
            System.out.println("Error guardando " + INTERNOS_FILE + ": " + e.getMessage());
        }
    }

    // ===================== ESTRUCTURA CÁRCEL (PISOS Y CELDAS) =====================

    private static void cargarEstructuraCarcel() {
        // Puedes adaptar esto a la cárcel real de Ocaña.
        // Ejemplo: 3 pisos, cada uno con 5 celdas de capacidad 4.
        int numPisos = 3;
        int celdasPorPiso = 5;
        int capacidadPorCelda = 4;

        for (int p = 1; p <= numPisos; p++) {
            Piso piso = new Piso(p);
            for (int c = 1; c <= celdasPorPiso; c++) {
                piso.agregarCelda(new Celda(c, capacidadPorCelda));
            }
            pisos.add(piso);
        }

        System.out.println("Estructura de la cárcel creada: " + numPisos + " pisos, " +
                celdasPorPiso + " celdas por piso.");
    }

    // ===================== OPERACIONES DEL MENÚ =====================

    private static void registrarInterno() {
        System.out.println("\n--- Registrar interno ---");
        System.out.print("ID del interno: ");
        String id = sc.nextLine();

        if (buscarInternoPorId(id) != null) {
            System.out.println("Ya existe un interno con ese ID.");
            return;
        }

        System.out.print("Nombre del interno: ");
        String nombre = sc.nextLine();

        System.out.println("Seleccione delito:");
        for (int i = 0; i < delitos.size(); i++) {
            System.out.println((i + 1) + ". " + delitos.get(i));
        }
        System.out.print("Opción: ");
        int opDelito;
        try {
            opDelito = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Opción inválida.");
            return;
        }

        if (opDelito < 1 || opDelito > delitos.size()) {
            System.out.println("Opción fuera de rango.");
            return;
        }

        Delito delito = delitos.get(opDelito - 1);

        System.out.print("Condena total en años (entre " + delito.getPenaMinima() +
                         " y " + delito.getPenaMaxima() + "): ");
        int condena;
        try {
            condena = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido.");
            return;
        }

        if (condena < delito.getPenaMinima() || condena > delito.getPenaMaxima()) {
            System.out.println("La condena no está dentro del rango permitido para este delito.");
            return;
        }

        Interno interno = new Interno(id, nombre, delito, condena);
        internos.add(interno);
        guardarInternosEnArchivo();
        System.out.println("Interno registrado (aún sin celda).");
    }

    private static void asignarCelda() {
        System.out.println("\n--- Asignar interno a celda ---");
        System.out.print("ID del interno: ");
        String id = sc.nextLine();

        Interno interno = buscarInternoPorId(id);
        if (interno == null) {
            System.out.println("No existe un interno con ese ID.");
            return;
        }

        System.out.print("Número de piso: ");
        int pisoNum;
        try {
            pisoNum = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Número de piso inválido.");
            return;
        }

        Piso piso = buscarPiso(pisoNum);
        if (piso == null) {
            System.out.println("Piso no existe.");
            return;
        }

        System.out.print("Número de celda: ");
        int celdaNum;
        try {
            celdaNum = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Número de celda inválido.");
            return;
        }

        Celda celda = piso.buscarCelda(celdaNum);
        if (celda == null) {
            System.out.println("Celda no existe.");
            return;
        }

        if (celda.agregarInterno(interno)) {
            interno.setUbicacion(pisoNum, celdaNum);
            guardarInternosEnArchivo();
            System.out.println("Interno asignado a piso " + pisoNum + ", celda " + celdaNum);
        } else {
            System.out.println("La celda está llena, no se puede asignar.");
        }
    }

    private static void consultarCeldas() {
        System.out.println("\n--- Consultar pisos y celdas ---");
        for (Piso piso : pisos) {
            System.out.println(piso);
            for (Celda celda : piso.getCeldas()) {
                System.out.println("   " + celda);
                for (Interno i : celda.getInternos()) {
                    System.out.println("      - " + i.getId() + " | " + i.getNombre());
                }
            }
        }
    }

    private static void listarInternos() {
        System.out.println("\n--- Lista de internos ---");
        if (internos.isEmpty()) {
            System.out.println("No hay internos registrados.");
            return;
        }
        for (Interno i : internos) {
            System.out.println(i);
        }
    }

    private static void listarDelitos() {
        System.out.println("\n--- Lista de delitos ---");
        for (Delito d : delitos) {
            System.out.println("- " + d);
        }
    }

    private static void agregarDelito() {
        System.out.println("\n--- Agregar delito ---");
        System.out.print("Nombre del delito: ");
        String nombre = sc.nextLine();

        System.out.print("Pena mínima (años): ");
        int min;
        try {
            min = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido.");
            return;
        }

        System.out.print("Pena máxima (años): ");
        int max;
        try {
            max = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido.");
            return;
        }

        if (max < min) {
            System.out.println("La pena máxima no puede ser menor que la mínima.");
            return;
        }

        delitos.add(new Delito(nombre, min, max));
        guardarDelitosEnArchivo();
        System.out.println("Delito agregado y guardado.");
    }

    private static void avanzarUnAño() {
        System.out.println("\n--- Avanzar 1 año (reducción de condenas) ---");
        ArrayList<Interno> aLiberar = new ArrayList<>();

        for (Interno i : internos) {
            int restante = i.getCondenaRestante() - 1;
            i.setCondenaRestante(Math.max(restante, 0));
            if (restante <= 0) {
                aLiberar.add(i);
            }
        }

        // Liberar internos cuya condena llegó a 0
        for (Interno i : aLiberar) {
            if (i.getPisoNumero() != -1 && i.getCeldaNumero() != -1) {
                Piso piso = buscarPiso(i.getPisoNumero());
                if (piso != null) {
                    Celda celda = piso.buscarCelda(i.getCeldaNumero());
                    if (celda != null) {
                        celda.removerInternoPorId(i.getId());
                    }
                }
            }
            internos.remove(i);
            System.out.println("Interno liberado: " + i.getId() + " | " + i.getNombre());
        }

        guardarInternosEnArchivo();
        System.out.println("Año avanzado. Condenas actualizadas.");
    }

    // ===================== FUNCIONES AUXILIARES =====================

    private static Delito buscarDelitoPorNombre(String nombre) {
        for (Delito d : delitos) {
            if (d.getNombre().equalsIgnoreCase(nombre)) {
                return d;
            }
        }
        return null;
    }

    private static Piso buscarPiso(int numero) {
        for (Piso p : pisos) {
            if (p.getNumero() == numero) return p;
        }
        return null;
    }

    private static Interno buscarInternoPorId(String id) {
        for (Interno i : internos) {
            if (i.getId().equals(id)) return i;
        }
        return null;
    }
}
