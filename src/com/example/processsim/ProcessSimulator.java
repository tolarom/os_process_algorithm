package com.example.processsim;

import com.example.processsim.algorithms.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ProcessSimulator extends JFrame {
    // Colors
    private static final Color PRIMARY = new Color(41, 128, 185);
    private static final Color ACCENT = new Color(46, 204, 113);
    private static final Color BACKGROUND = new Color(236, 240, 241);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(44, 62, 80);
    private static final Color TEXT_SECONDARY = new Color(127, 140, 141);
    private static final Color[] GANTT_COLORS = {
        new Color(231, 76, 60), new Color(52, 152, 219), new Color(46, 204, 113),
        new Color(155, 89, 182), new Color(241, 196, 15), new Color(230, 126, 34),
        new Color(26, 188, 156), new Color(52, 73, 94)
    };

    private DefaultTableModel tableModel;
    private JTextArea outputArea;
    private JTextField nameField, arrivalField, burstField;
    private JTextField quantumRRField, quantumQ0Field, quantumQ1Field;
    private JLabel quantumRRLabel, quantumQ0Label, quantumQ1Label;
    private JComboBox<String> algorithmCombo;
    private GanttPanel ganttPanel;
    private JLabel statusLabel;
    private int processCounter = 1;

    public ProcessSimulator() {
        super("Process Scheduling Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        loadAppIcon();
        getContentPane().setBackground(BACKGROUND);

        initComponents();
    }

    private void loadAppIcon() {
        try {
            // Try loading from classpath (for JAR/EXE)
            var iconUrl = getClass().getResource("/icon.png");
            if (iconUrl != null) {
                setIconImage(new ImageIcon(iconUrl).getImage());
                return;
            }
            // Fallback: load from resources folder (for development)
            java.io.File iconFile = new java.io.File("resources/icon.png");
            if (iconFile.exists()) {
                setIconImage(new ImageIcon(iconFile.getAbsolutePath()).getImage());
            }
        } catch (Exception ignored) {}
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setOpaque(false);

        // Left: Input + Table
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setOpaque(false);
        leftPanel.add(createInputCard(), BorderLayout.NORTH);
        leftPanel.add(createTableCard(), BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(400, 0));

        // Right: Gantt + Output
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setOpaque(false);
        rightPanel.add(createGanttCard(), BorderLayout.NORTH);
        rightPanel.add(createOutputCard(), BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Status bar
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Process Scheduling Simulator");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Visualize CPU scheduling algorithms");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(255, 255, 255, 200));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    private JPanel createInputCard() {
        JPanel card = createCard("Add Process");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = createStyledTextField(6);
        nameField.setText("P" + processCounter);  // Auto-fill with next process name
        arrivalField = createStyledTextField(5);
        burstField = createStyledTextField(5);
        
        // Quantum fields for different algorithms
        quantumRRField = createStyledTextField(5);
        quantumRRField.setText("2");
        quantumQ0Field = createStyledTextField(5);
        quantumQ0Field.setText("2");
        quantumQ1Field = createStyledTextField(5);
        quantumQ1Field.setText("4");
        
        // Labels for quantum fields
        quantumRRLabel = createLabel("Quantum:");
        quantumQ0Label = createLabel("Q0:");
        quantumQ1Label = createLabel("Q1:");

        algorithmCombo = new JComboBox<>(new String[]{"Round Robin", "FCFS", "SJF", "SRTF", "MLFQ"});
        algorithmCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        algorithmCombo.addActionListener(e -> updateQuantumFieldsVisibility());

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(createLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField.setEditable(false);
        form.add(nameField, gbc);
        gbc.gridx = 2;
        form.add(createLabel("Arrival:"), gbc);
        gbc.gridx = 3;
        form.add(arrivalField, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(createLabel("Burst:"), gbc);
        gbc.gridx = 1;
        form.add(burstField, gbc);

        // Row 2 - Algorithm
        gbc.gridx = 0; gbc.gridy = 2;
        form.add(createLabel("Algorithm:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        form.add(algorithmCombo, gbc);
        gbc.gridwidth = 1;
        
        // Row 3 - Quantum fields (visibility controlled by algorithm selection)
        gbc.gridy = 3;
        gbc.gridx = 0;
        form.add(quantumRRLabel, gbc);
        gbc.gridx = 1;
        form.add(quantumRRField, gbc);
        gbc.gridx = 0;
        form.add(quantumQ0Label, gbc);
        gbc.gridx = 1;
        form.add(quantumQ0Field, gbc);
        gbc.gridx = 2;
        form.add(quantumQ1Label, gbc);
        gbc.gridx = 3;
        form.add(quantumQ1Field, gbc);
        
        // Set initial visibility
        updateQuantumFieldsVisibility();

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setOpaque(false);

        JButton addBtn = createStyledButton("Add", ACCENT);
        addBtn.addActionListener(this::onAdd);
        JButton runBtn = createStyledButton("Run", PRIMARY);
        runBtn.addActionListener(this::onRun);
        JButton clearBtn = createStyledButton("Clear", new Color(231, 76, 60));
        clearBtn.addActionListener(e -> {
            tableModel.setRowCount(0);
            ganttPanel.setTimeline(null, null);
            outputArea.setText("");
            processCounter = 1;
            nameField.setText("P" + processCounter);  // Reset name field
            statusLabel.setText("Ready");
        });
        JButton sampleBtn = createStyledButton("Sample", TEXT_SECONDARY);
        sampleBtn.addActionListener(this::loadSampleData);

        btnPanel.add(addBtn);
        btnPanel.add(runBtn);
        btnPanel.add(clearBtn);
        btnPanel.add(sampleBtn);

        card.add(form, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }
    
    private void updateQuantumFieldsVisibility() {
        int idx = algorithmCombo.getSelectedIndex();
        boolean isRR = (idx == 0);      // Round Robin
        boolean isMLFQ = (idx == 4);     // MLFQ
        
        // RR: show single quantum field
        quantumRRLabel.setVisible(isRR);
        quantumRRField.setVisible(isRR);
        
        // MLFQ: show Q0 and Q1 fields
        quantumQ0Label.setVisible(isMLFQ);
        quantumQ0Field.setVisible(isMLFQ);
        quantumQ1Label.setVisible(isMLFQ);
        quantumQ1Field.setVisible(isMLFQ);
    }

    private JPanel createTableCard() {
        JPanel card = createCard("Process Queue");

        tableModel = new DefaultTableModel(new Object[]{"#", "Name", "Arrival", "Burst"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col > 0; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.BLACK);
        table.setSelectionBackground(new Color(52, 152, 219, 100));
        table.setGridColor(new Color(220, 220, 220));

        // Center alignment
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        table.getColumnModel().getColumn(0).setPreferredWidth(30);

        // Delete on right-click
        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) tableModel.removeRow(row);
        });
        popup.add(deleteItem);
        table.setComponentPopupMenu(popup);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createGanttCard() {
        JPanel card = createCard("Gantt Chart");
        ganttPanel = new GanttPanel();
        ganttPanel.setPreferredSize(new Dimension(0, 100));
        card.add(ganttPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createOutputCard() {
        JPanel card = createCard("Results");
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        outputArea.setBackground(new Color(250, 250, 250));
        outputArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(44, 62, 80));
        bar.setBorder(new EmptyBorder(8, 15, 8, 15));
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bar.add(statusLabel, BorderLayout.WEST);

        JLabel credit = new JLabel("Process Scheduling Simulator v1.0");
        credit.setForeground(new Color(255, 255, 255, 150));
        credit.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bar.add(credit, BorderLayout.EAST);
        return bar;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_PRIMARY);
        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    private JTextField createStyledTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(5, 8, 5, 8)
        ));
        return tf;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            private float scale = 1.0f;
            private boolean isPressed = false;
            private boolean isHovered = false;
            
            {
                setContentAreaFilled(false);
                setBorderPainted(false);
                setOpaque(false);
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Calculate scaled dimensions for press effect
                int scaledW = (int)(w * scale);
                int scaledH = (int)(h * scale);
                int offsetX = (w - scaledW) / 2;
                int offsetY = (h - scaledH) / 2;
                
                // Determine color based on state
                Color fillColor = bg;
                if (isPressed) {
                    fillColor = bg.darker().darker();
                } else if (isHovered) {
                    fillColor = bg.darker();
                }
                
                // Draw shadow when not pressed
                if (!isPressed && isEnabled()) {
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.fillRoundRect(offsetX + 2, offsetY + 3, scaledW, scaledH, 10, 10);
                }
                
                // Draw button background
                g2.setColor(fillColor);
                g2.fillRoundRect(offsetX, offsetY, scaledW, scaledH, 10, 10);
                
                // Draw subtle gradient overlay
                GradientPaint gradient = new GradientPaint(
                    0, offsetY, new Color(255, 255, 255, isPressed ? 0 : 40),
                    0, offsetY + scaledH, new Color(0, 0, 0, isPressed ? 20 : 0)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(offsetX, offsetY, scaledW, scaledH, 10, 10);
                
                // Draw border
                g2.setColor(new Color(0, 0, 0, 30));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(offsetX, offsetY, scaledW - 1, scaledH - 1, 10, 10);
                
                g2.dispose();
                
                // Let the UI paint the text
                super.paintComponent(g);
            }
        };
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                try {
                    var field = btn.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.set(btn, true);
                    btn.repaint();
                } catch (Exception ignored) {}
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                try {
                    var field = btn.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.set(btn, false);
                    btn.repaint();
                } catch (Exception ignored) {}
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    var field = btn.getClass().getDeclaredField("isPressed");
                    field.setAccessible(true);
                    field.set(btn, true);
                    var method = btn.getClass().getDeclaredMethod("animatePress", boolean.class);
                    method.setAccessible(true);
                    method.invoke(btn, true);
                } catch (Exception ignored) {}
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    var field = btn.getClass().getDeclaredField("isPressed");
                    field.setAccessible(true);
                    field.set(btn, false);
                    var method = btn.getClass().getDeclaredMethod("animatePress", boolean.class);
                    method.setAccessible(true);
                    method.invoke(btn, false);
                } catch (Exception ignored) {}
            }
        });
        
        return btn;
    }

    private void onAdd(ActionEvent e) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) name = "P" + processCounter;
        String at = arrivalField.getText().trim();
        String bt = burstField.getText().trim();
        // priority removed; ignore any priority UI
        if (at.isEmpty()) at = "0";
        if (bt.isEmpty()) { showError("Burst time is required"); return; }
        try {
            int arrival = Integer.parseInt(at);
            int burst = Integer.parseInt(bt);
            if (burst <= 0) { showError("Burst must be > 0"); return; }
            tableModel.addRow(new Object[]{processCounter++, name, arrival, burst});
            nameField.setText("P" + processCounter);
            arrivalField.setText(""); burstField.setText("");
            statusLabel.setText("Added process: " + name);
        } catch (NumberFormatException ex) {
            showError("Arrival and Burst must be integers.");
        }
    }

    private void loadSampleData(ActionEvent e) {
        tableModel.setRowCount(0);
        processCounter = 1;
        Object[][] samples = {{"P1", 0, 5}, {"P2", 1, 3}, {"P3", 2, 8}, {"P4", 3, 6},{"P5", 4, 2}};
        for (Object[] s : samples) {
            tableModel.addRow(new Object[]{processCounter++, s[0], s[1], s[2]});
        }
        nameField.setText("P" + processCounter);
        statusLabel.setText("Loaded sample data");
    }

    private void onRun(ActionEvent e) {
        int rows = tableModel.getRowCount();
        if (rows == 0) { showError("Add at least one process"); return; }
        List<Proc> list = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            String n = tableModel.getValueAt(i, 1).toString();
            int a = Integer.parseInt(tableModel.getValueAt(i, 2).toString());
            int b = Integer.parseInt(tableModel.getValueAt(i, 3).toString());
            list.add(new Proc(n, a, b));
        }
        String algo = (String) algorithmCombo.getSelectedItem();
        SchedulingAlgorithm algorithm = switch (algo) {
            case "FCFS" -> new FCFSAlgorithm(list);
            case "SJF" -> new SJFAlgorithm(list);
            case "SRTF" -> new SRTFAlgorithm(list);
            case "MLFQ" -> {
                int q0 = parseQuantum(quantumQ0Field.getText().trim(), 2);
                int q1 = parseQuantum(quantumQ1Field.getText().trim(), 4);
                yield new MLFQAlgorithm(list, q0, q1);
            }
            default -> {
                int quantum = parseQuantum(quantumRRField.getText().trim(), 2);
                yield new RoundRobinAlgorithm(list, quantum);
            }
        };
        
        SimResult result = algorithm.run();
        ganttPanel.setTimeline(result.timeline, result.colorMap);
        outputArea.setText(result.text);
        statusLabel.setText("Simulation complete: " + algo);
    }

    /**
     * Parse a single quantum value with fallback to default.
     */
    private int parseQuantum(String input, int defaultValue) {
        if (input == null || input.isEmpty()) {
            return defaultValue;
        }
        try {
            return Math.max(1, Integer.parseInt(input.trim()));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new ProcessSimulator().setVisible(true));
    }

    // ========== Gantt Panel ==========
    class GanttPanel extends JPanel {
        private List<GanttEntry> timeline;
        private Map<String, Color> colorMap;

        void setTimeline(List<GanttEntry> t, Map<String, Color> c) { timeline = t; colorMap = c; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(250, 250, 250));
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (timeline == null || timeline.isEmpty()) {
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                g2.drawString("Run a simulation to see the Gantt chart", 20, getHeight() / 2);
                return;
            }

            int maxTime = timeline.get(timeline.size() - 1).end;
            int padding = 40, barHeight = 40, y = 30;
            double scale = (getWidth() - 2.0 * padding) / maxTime;

            for (GanttEntry e : timeline) {
                int x1 = padding + (int)(e.start * scale);
                int w = (int)((e.end - e.start) * scale);
                Color c = colorMap.getOrDefault(e.name, GANTT_COLORS[0]);
                g2.setColor(c);
                g2.fillRoundRect(x1, y, w, barHeight, 8, 8);
                g2.setColor(c.darker());
                g2.drawRoundRect(x1, y, w, barHeight, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(e.name);
                if (tw < w - 4) g2.drawString(e.name, x1 + (w - tw) / 2, y + barHeight / 2 + 5);
            }

            // Time markers
            g2.setColor(TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            Set<Integer> drawn = new HashSet<>();
            for (GanttEntry e : timeline) {
                if (drawn.add(e.start)) g2.drawString(String.valueOf(e.start), padding + (int)(e.start * scale) - 3, y + barHeight + 15);
                if (drawn.add(e.end)) g2.drawString(String.valueOf(e.end), padding + (int)(e.end * scale) - 3, y + barHeight + 15);
            }
        }
    }
}
