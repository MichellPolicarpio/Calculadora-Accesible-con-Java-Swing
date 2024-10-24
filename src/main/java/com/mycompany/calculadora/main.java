package com.mycompany.calculadora;
import java.awt.EventQueue;
import javax.swing.UIManager;

/**
 *
 * @author Michell Alexis Policarpio Moran - zs21002379 - Ingenieria en informatica
 */
public class main{
    public static void main(String args[]){
        try {
            // Itera sobre todos los temas visuales instalados
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                // Busca específicamente el tema Nimbus
                if ("Nimbus".equals(info.getName())) {
                    // Cuando lo encuentra, lo establece como tema de la aplicación
                    UIManager.setLookAndFeel(info.getClassName());
                    break;  // Sale del bucle una vez encontrado
                }
            }
        } catch (Exception e) {
            // Manejo de errores si no se puede establecer el tema
            System.err.println("Error al establecer el look and feel: " + e.getMessage());
        }
        
        // Lanza la interfaz gráfica en el hilo de eventos de Swing
        EventQueue.invokeLater(() -> {
            // Crea una nueva instancia de la calculadora y la hace visible
            new Calculadora().setVisible(true);
        });
    }
}

