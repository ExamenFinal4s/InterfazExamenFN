package service;

import domain.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class SistemaCarcelGUIExtension extends JFrame {

	private static final String DELITOS_FILE = "data/delitos.txt";
	private static final String INTERNOS_FILE = "data/internos.txt";

	private java.util.List<Delito> delitos = new ArrayList<>();
	private java.util.List<Piso> pisos = new ArrayList<>();
	private java.util.List<Interno> internos = new ArrayList<>();

	private DefaultListModel<String> internosModel = new DefaultListModel<>();
	private DefaultListModel<String> delitosModel = new DefaultListModel<>();

	private JList<String> internosList;
	private JList<String> delitosList;
	private JTextArea estructuraArea;

	public SistemaCarcelGUIExtension() {
		super("Sistema Cárcel - GUI");
		initStructure();
		cargarDelitosDesdeArchivo();
		cargarInternosDesdeArchivo();
		initUI();
		refreshViews();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(null);
	}

	private void initUI() {
		JPanel main = new JPanel(new BorderLayout(8,8));

		// Left: internos
		JPanel left = new JPanel(new BorderLayout(4,4));
		left.setBorder(BorderFactory.createTitledBorder("Internos"));
		internosList = new JList<>(internosModel);
		left.add(new JScrollPane(internosList), BorderLayout.CENTER);

		JPanel leftButtons = new JPanel(new GridLayout(0,1,4,4));
		JButton btnRegistrar = new JButton("Registrar Interno");
		btnRegistrar.addActionListener(e -> registrarInternoDialog());
		JButton btnAsignar = new JButton("Asignar a Celda");
		btnAsignar.addActionListener(e -> asignarCeldaDialog());
		JButton btnAvanzar = new JButton("Avanzar 1 año");
		btnAvanzar.addActionListener(e -> { avanzarUnAno(); refreshViews(); });
		leftButtons.add(btnRegistrar);
		leftButtons.add(btnAsignar);
		leftButtons.add(btnAvanzar);
		left.add(leftButtons, BorderLayout.SOUTH);

		// Center: estructura
		JPanel center = new JPanel(new BorderLayout(4,4));
		center.setBorder(BorderFactory.createTitledBorder("Pisos y Celdas"));
		estructuraArea = new JTextArea();
		estructuraArea.setEditable(false);
		center.add(new JScrollPane(estructuraArea), BorderLayout.CENTER);

		// Right: delitos
		JPanel right = new JPanel(new BorderLayout(4,4));
		right.setBorder(BorderFactory.createTitledBorder("Delitos"));
		delitosList = new JList<>(delitosModel);
		right.add(new JScrollPane(delitosList), BorderLayout.CENTER);

		JPanel rightButtons = new JPanel(new GridLayout(0,1,4,4));
		JButton btnAgregarDelito = new JButton("Agregar Delito");
		btnAgregarDelito.addActionListener(e -> agregarDelitoDialog());
		JButton btnGuardar = new JButton("Guardar");
		btnGuardar.addActionListener(e -> { guardarDelitosEnArchivo(); guardarInternosEnArchivo(); JOptionPane.showMessageDialog(this, "Datos guardados."); });
		JButton btnRefresh = new JButton("Refrescar");
		btnRefresh.addActionListener(e -> refreshViews());
		rightButtons.add(btnAgregarDelito);
		rightButtons.add(btnGuardar);
		rightButtons.add(btnRefresh);
		right.add(rightButtons, BorderLayout.SOUTH);

		main.add(left, BorderLayout.WEST);
		main.add(center, BorderLayout.CENTER);
		main.add(right, BorderLayout.EAST);

		// Layout sizes
		left.setPreferredSize(new Dimension(300, 600));
		right.setPreferredSize(new Dimension(300, 600));

		add(main);
	}

	// ================= Persistence =================

	private void cargarDelitosDesdeArchivo() {
		File f = new File(DELITOS_FILE);
		if (!f.exists()) {
			crearDelitosPorDefecto();
			guardarDelitosEnArchivo();
			return;
		}

		delitos.clear();
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				Delito d = Delito.fromFileString(linea);
				if (d != null) delitos.add(d);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error leyendo delitos: " + e.getMessage());
		}
	}

	private void guardarDelitosEnArchivo() {
		try (PrintWriter pw = new PrintWriter(new FileWriter(DELITOS_FILE))) {
			for (Delito d : delitos) pw.println(d.toFileString());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error guardando delitos: " + e.getMessage());
		}
	}

	private void crearDelitosPorDefecto() {
		delitos.clear();
		delitos.add(new Delito("Homicidio", 10, 40));
		delitos.add(new Delito("Hurto", 1, 8));
		delitos.add(new Delito("Tráfico de estupefacientes", 4, 20));
		delitos.add(new Delito("Extorsión", 5, 20));
	}

	private void cargarInternosDesdeArchivo() {
		File f = new File(INTERNOS_FILE);
		if (!f.exists()) {
			return;
		}

		internos.clear();
		// limpiar celdas
		initStructure();

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
				if (d == null) continue;

				Interno interno = new Interno(id, nombre, d, condenaTotal);
				interno.setCondenaRestante(condenaRestante);
				interno.setUbicacion(pisoNum, celdaNum);
				internos.add(interno);

				if (pisoNum != -1 && celdaNum != -1) {
					Piso piso = buscarPiso(pisoNum);
					if (piso != null) {
						Celda celda = piso.buscarCelda(celdaNum);
						if (celda != null) celda.agregarInterno(interno);
					}
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error leyendo internos: " + e.getMessage());
		}
	}

	private void guardarInternosEnArchivo() {
		try (PrintWriter pw = new PrintWriter(new FileWriter(INTERNOS_FILE))) {
			for (Interno i : internos) pw.println(i.toFileString());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error guardando internos: " + e.getMessage());
		}
	}

	// ================ Estructura ================

	private void initStructure() {
		pisos.clear();
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
	}

	// ================ Operaciones ================

	private void registrarInternoDialog() {
		JPanel panel = new JPanel(new GridLayout(0,1,4,4));
		JTextField idField = new JTextField();
		JTextField nombreField = new JTextField();
		JComboBox<String> delitoCombo = new JComboBox<>();
		for (Delito d : delitos) delitoCombo.addItem(d.getNombre());
		JTextField condenaField = new JTextField();
		panel.add(new JLabel("ID:")); panel.add(idField);
		panel.add(new JLabel("Nombre:")); panel.add(nombreField);
		panel.add(new JLabel("Delito:")); panel.add(delitoCombo);
		panel.add(new JLabel("Condena total (años):")); panel.add(condenaField);

		int res = JOptionPane.showConfirmDialog(this, panel, "Registrar Interno", JOptionPane.OK_CANCEL_OPTION);
		if (res != JOptionPane.OK_OPTION) return;

		String id = idField.getText().trim();
		String nombre = nombreField.getText().trim();
		String delitoNombre = (String) delitoCombo.getSelectedItem();
		if (id.isEmpty() || nombre.isEmpty() || delitoNombre == null) {
			JOptionPane.showMessageDialog(this, "Complete todos los campos.");
			return;
		}
		if (buscarInternoPorId(id) != null) { JOptionPane.showMessageDialog(this, "Ya existe un interno con ese ID."); return; }
		Delito d = buscarDelitoPorNombre(delitoNombre);
		int condena;
		try { condena = Integer.parseInt(condenaField.getText().trim()); } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Condena inválida."); return; }
		if (condena < d.getPenaMinima() || condena > d.getPenaMaxima()) { JOptionPane.showMessageDialog(this, "La condena no está dentro del rango del delito."); return; }

		Interno interno = new Interno(id, nombre, d, condena);
		internos.add(interno);
		guardarInternosEnArchivo();
		refreshViews();
		JOptionPane.showMessageDialog(this, "Interno registrado (sin celda).");
	}

	private void asignarCeldaDialog() {
		JPanel panel = new JPanel(new GridLayout(0,1,4,4));
		JTextField idField = new JTextField();
		JTextField pisoField = new JTextField();
		JTextField celdaField = new JTextField();
		panel.add(new JLabel("ID del interno:")); panel.add(idField);
		panel.add(new JLabel("Número de piso:")); panel.add(pisoField);
		panel.add(new JLabel("Número de celda:")); panel.add(celdaField);

		int res = JOptionPane.showConfirmDialog(this, panel, "Asignar Interno a Celda", JOptionPane.OK_CANCEL_OPTION);
		if (res != JOptionPane.OK_OPTION) return;

		String id = idField.getText().trim();
		int pisoNum, celdaNum;
		try { pisoNum = Integer.parseInt(pisoField.getText().trim()); celdaNum = Integer.parseInt(celdaField.getText().trim()); }
		catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Valores de piso/celda inválidos."); return; }

		Interno interno = buscarInternoPorId(id);
		if (interno == null) { JOptionPane.showMessageDialog(this, "No existe un interno con ese ID."); return; }

		Piso piso = buscarPiso(pisoNum);
		if (piso == null) { JOptionPane.showMessageDialog(this, "Piso no existe."); return; }
		Celda celda = piso.buscarCelda(celdaNum);
		if (celda == null) { JOptionPane.showMessageDialog(this, "Celda no existe."); return; }

		if (celda.agregarInterno(interno)) {
			interno.setUbicacion(pisoNum, celdaNum);
			guardarInternosEnArchivo();
			refreshViews();
			JOptionPane.showMessageDialog(this, "Interno asignado a piso " + pisoNum + ", celda " + celdaNum);
		} else {
			JOptionPane.showMessageDialog(this, "La celda está llena.");
		}
	}

	private void agregarDelitoDialog() {
		JPanel panel = new JPanel(new GridLayout(0,1,4,4));
		JTextField nombreField = new JTextField();
		JTextField minField = new JTextField();
		JTextField maxField = new JTextField();
		panel.add(new JLabel("Nombre del delito:")); panel.add(nombreField);
		panel.add(new JLabel("Pena mínima (años):")); panel.add(minField);
		panel.add(new JLabel("Pena máxima (años):")); panel.add(maxField);

		int res = JOptionPane.showConfirmDialog(this, panel, "Agregar Delito", JOptionPane.OK_CANCEL_OPTION);
		if (res != JOptionPane.OK_OPTION) return;

		String nombre = nombreField.getText().trim();
		int min, max;
		try { min = Integer.parseInt(minField.getText().trim()); max = Integer.parseInt(maxField.getText().trim()); }
		catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Valores inválidos."); return; }
		if (max < min) { JOptionPane.showMessageDialog(this, "La pena máxima no puede ser menor que la mínima."); return; }
		delitos.add(new Delito(nombre, min, max));
		guardarDelitosEnArchivo();
		refreshViews();
		JOptionPane.showMessageDialog(this, "Delito agregado.");
	}

	private void avanzarUnAno() {
		java.util.List<Interno> aLiberar = new ArrayList<>();
		for (Interno i : new ArrayList<>(internos)) {
			int restante = i.getCondenaRestante() - 1;
			i.setCondenaRestante(Math.max(restante, 0));
			if (restante <= 0) aLiberar.add(i);
		}
		for (Interno i : aLiberar) {
			if (i.getPisoNumero() != -1 && i.getCeldaNumero() != -1) {
				Piso piso = buscarPiso(i.getPisoNumero());
				if (piso != null) {
					Celda celda = piso.buscarCelda(i.getCeldaNumero());
					if (celda != null) celda.removerInternoPorId(i.getId());
				}
			}
			internos.remove(i);
		}
		guardarInternosEnArchivo();
		JOptionPane.showMessageDialog(this, "Año avanzado. Condenas actualizadas.");
	}

	// ================ Helpers ================

	private void refreshViews() {
		internosModel.clear();
		for (Interno i : internos) internosModel.addElement(i.toString());

		delitosModel.clear();
		for (Delito d : delitos) delitosModel.addElement(d.toString());

		StringBuilder sb = new StringBuilder();
		for (Piso piso : pisos) {
			sb.append(piso).append('\n');
			for (Celda celda : piso.getCeldas()) {
				sb.append("   ").append(celda).append('\n');
				for (Interno i : celda.getInternos()) {
					sb.append("      - ").append(i.getId()).append(" | ").append(i.getNombre()).append('\n');
				}
			}
		}
		estructuraArea.setText(sb.toString());
	}

	private Delito buscarDelitoPorNombre(String nombre) {
		for (Delito d : delitos) if (d.getNombre().equalsIgnoreCase(nombre)) return d;
		return null;
	}

	private Piso buscarPiso(int numero) {
		for (Piso p : pisos) if (p.getNumero() == numero) return p;
		return null;
	}

	private Interno buscarInternoPorId(String id) {
		for (Interno i : internos) if (i.getId().equals(id)) return i;
		return null;
	}

	// ================ Main ================

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			SistemaCarcelGUIExtension gui = new SistemaCarcelGUIExtension();
			gui.setVisible(true);
		});
	}

}
