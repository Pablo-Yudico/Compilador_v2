/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package compiladorv2;

/**
 *
 * @author Omen
 */


import java.io.File;
import jflex.Main;

public class Generador {
    public static void main(String[] args) {
        String rutaFlex = "C:\\Users\\Omen\\Documents\\NetBeansProjects\\Compiladorv2\\src\\compiladorv2\\Lexer.flex"; // Ajusta la ruta según tu estructura de proyecto

        // Genera el archivo .java a partir del archivo .flex
        generarLexer(rutaFlex);
        
        // Aquí puedes compilar y ejecutar la clase generada si es necesario
    }

    public static void generarLexer(String rutaFlex) {
        File archivoFlex = new File(rutaFlex);
        
        if (archivoFlex.exists()) {
            try {
                // Ejecuta JFlex en el archivo .flex especificado
                String[] argumentos = { rutaFlex };
                Main.generate(argumentos);
                System.out.println("Lexer.java generado correctamente.");
            } catch (Exception e) {
                System.err.println("Error al generar Lexer.java: " + e.getMessage());
            }
        } else {
            System.err.println("El archivo .flex no existe en la ruta especificada.");
        }
    }
}