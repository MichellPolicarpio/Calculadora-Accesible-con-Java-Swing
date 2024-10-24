package com.mycompany.calculadora;

/**
 * Calculadora accesible con interfaz gráfica y capacidades de voz
 * @author Michell Alexis Policarpio Moran - zs21002379 - Ingenieria en informatica
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.sun.speech.freetts.*;

public class Calculadora extends javax.swing.JFrame {
    // Variables de control para las operaciones
    private float primerNumero;      // Almacena el primer número de la operación
    private float segundoNumero;     // Almacena el segundo número de la operación
    private String operador = "";    // Almacena el operador actual (+, -, *, /)
    private int ban = 0;            // Bandera para controlar la entrada de nuevos números
    private float mem;              // Memoria de la calculadora
    private int banmem = 1;         // Bandera para controlar el estado de la memoria
    
    // Componentes de la interfaz
    private JTextField pantalla;     // Campo de texto para mostrar números y resultados
    private Voice voz;              // Motor de voz para accesibilidad
    private String ultimoValor = ""; // Almacena el último valor ingresado
    
    /**
     * Constructor de la calculadora
     * Inicializa la interfaz y el motor de voz
     */
    public Calculadora() {
        initComponents();
        this.setLocationRelativeTo(null);  // Centra la ventana en la pantalla
        inicializarVoz();                  // Configura el sistema de voz
    }
    
    /**
     * Inicializa el motor de voz FreeTTS
     * Configura la voz "kevin16" para la salida de audio
     */
    private void inicializarVoz() {
        try {
            System.setProperty("freetts.voices", 
                "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            VoiceManager vm = VoiceManager.getInstance();
            voz = vm.getVoice("kevin16");
            if (voz != null) {
                voz.allocate();
            }
        } catch (Exception e) {
            System.err.println("Error al inicializar la voz: " + e.getMessage());
        }
    }
    
    /**
     * Inicializa los componentes de la interfaz gráfica
     * Crea y configura la ventana, pantalla y botones
     */
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Calculadora Accesible");
        setResizable(false);
        
        // Configuración del panel principal
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Configuración de la pantalla
        pantalla = new JTextField("0");
        pantalla.setHorizontalAlignment(JTextField.RIGHT);
        pantalla.setFont(new Font("Arial", Font.BOLD, 24));
        pantalla.setEditable(false);
        panel.add(pantalla, BorderLayout.NORTH);
        
        // Panel para los botones
        JPanel botonesPanel = new JPanel(new GridLayout(6, 4, 5, 5));
        
        // Array con las etiquetas de los botones
        String[] botones = {
            "C", "CE", "DEL", "%",
            "MC", "MR", "M+", "M-",
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            "+/-", "0", ".", "+"
        };
        
        // Creación de los botones con sus listeners
        for (String boton : botones) {
            JButton btn = new JButton(boton);
            btn.setFont(new Font("Arial", Font.BOLD, 18));
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    botonPresionado(boton);
                }
            });
            botonesPanel.add(btn);
        }
        
        // Botón igual con tamaño especial
        JButton btnIgual = new JButton("=");
        btnIgual.setFont(new Font("Arial", Font.BOLD, 18));
        btnIgual.addActionListener(e -> botonPresionado("="));
        
        panel.add(botonesPanel, BorderLayout.CENTER);
        panel.add(btnIgual, BorderLayout.SOUTH);
        
        add(panel);
        pack();
    }
    
    /**
     * Maneja los eventos de los botones presionados
     * @param valor El valor o comando del botón presionado
     */
    private void botonPresionado(String valor) {
        String valorAnterior = pantalla.getText();
        
        switch (valor) {
            case "C":  // Limpia toda la calculadora
                pantalla.setText("0");
                primerNumero = 0;
                segundoNumero = 0;
                operador = "";
                ban = 0;
                hablar("Clear");
                break;
                
            case "CE":  // Limpia solo la entrada actual
                pantalla.setText("0");
                hablar("Clear entry");
                break;
                
            case "DEL":  // Borra el último dígito
                if (valorAnterior.length() > 1) {
                    pantalla.setText(valorAnterior.substring(0, valorAnterior.length() - 1));
                } else {
                    pantalla.setText("0");
                }
                hablar("Borrar");
                break;
                
            case "%":  // Calcula el porcentaje
                float valor_porcentaje = Float.parseFloat(pantalla.getText());
                float resultado_porcentaje = valor_porcentaje / 100;
                pantalla.setText(sincero(resultado_porcentaje));
                hablar("Percent");
                break;
                
            case "+/-":  // Cambia el signo del número
                float valor_actual = Float.parseFloat(pantalla.getText());
                pantalla.setText(sincero(-valor_actual));
                hablar("Change sign");
                break;
                
            // Manejo de números y punto decimal
            case "0": case "1": case "2": case "3": case "4":
            case "5": case "6": case "7": case "8": case "9":
            case ".":
                if (ban == 0) {  // Si no hay operación pendiente
                    if (pantalla.getText().equals("0") && !valor.equals(".")) {
                        pantalla.setText(valor);
                    } else {
                        if (!(valor.equals(".") && pantalla.getText().contains("."))) {
                            pantalla.setText(pantalla.getText() + valor);
                        }
                    }
                } else {  // Si hay operación pendiente, inicia nuevo número
                    pantalla.setText(valor);
                    ban = 0;
                }
                hablar(valor);
                break;
                
            // Operadores matemáticos
            case "+": case "-": case "*": case "/":
                primerNumero = Float.parseFloat(pantalla.getText());
                operador = valor;
                ban = 1;
                String operadorTexto = valor.equals("+") ? "plus" : 
                                     valor.equals("-") ? "minus" :
                                     valor.equals("*") ? "multiply" : "divide";
                hablar(operadorTexto);
                break;
                
            case "=":  // Realiza el cálculo
                if (!operador.isEmpty()) {
                    segundoNumero = Float.parseFloat(pantalla.getText());
                    float resultado = calcular();
                    pantalla.setText(sincero(resultado));
                    hablar("equals " + sincero(resultado));
                    ban = 1;
                }
                break;
                
            // Operaciones de memoria
            case "MC":  // Limpia la memoria
                mem = 0;
                banmem = 0;
                hablar("Memory clear");
                break;
                
            case "MR":  // Recupera el valor de memoria
                if (banmem == 1) {
                    pantalla.setText(sincero(mem));
                    hablar("Memory recall " + sincero(mem));
                }
                break;
                
            case "M+":  // Suma a la memoria
                mem += Float.parseFloat(pantalla.getText());
                banmem = 1;
                hablar("Memory plus");
                break;
                
            case "M-":  // Resta de la memoria
                mem -= Float.parseFloat(pantalla.getText());
                banmem = 1;
                hablar("Memory minus");
                break;
        }
    }
    
    /**
     * Realiza el cálculo según el operador seleccionado
     * @return El resultado de la operación
     */
    private float calcular() {
        float resultado = 0;
        switch (operador) {
            case "+":
                resultado = primerNumero + segundoNumero;
                break;
            case "-":
                resultado = primerNumero - segundoNumero;
                break;
            case "*":
                resultado = primerNumero * segundoNumero;
                break;
            case "/":
                if (segundoNumero != 0) {
                    resultado = primerNumero / segundoNumero;
                } else {
                    JOptionPane.showMessageDialog(this, "No se puede dividir por cero");
                    hablar("Error, división por cero");
                    resultado = 0;
                }
                break;
        }
        return resultado;
    }
    
    /**
     * Formatea un número float para eliminar decimales innecesarios
     * @param resultado El número a formatear
     * @return String formateado del número
     */
    private String sincero(float resultado) {
        String retorno = Float.toString(resultado);
        if (resultado % 1 == 0) {
            retorno = retorno.substring(0, retorno.length() - 2);
        }
        return retorno;
    }
    
    /**
     * Pronuncia el texto proporcionado usando el motor de voz
     */
    private void hablar(String texto) {
        if (voz != null) {
            voz.speak(texto);
        }
    }
    
    /**
     * Libera los recursos del motor de voz al cerrar la aplicación
     */
    @Override
    public void dispose() {
        if (voz != null) {
            voz.deallocate();
        }
        super.dispose();
    }
}