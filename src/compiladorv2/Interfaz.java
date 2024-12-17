/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package compiladorv2;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author Omen
 */
public class Interfaz extends javax.swing.JFrame {

    /**
     * Creates new form Interfaz
     */
    public Interfaz() {
        initComponents();

        NumeroLinea lineNumber = new NumeroLinea(Area_Edicion);
        lineNumber.setMinimumDisplayDigits(4);
        lineNumber.setDigitAlignment(NumeroLinea.CENTER);
        lineNumber.setCurrentLineForeground(Color.white);
        lineNumber.setBackground(Color.lightGray);
        jScrollPane1.setRowHeaderView(lineNumber);
        CargarTabla();
    }

    //Variables y constantes 
    File ubicacion = new File("C:\\Users\\Omen\\Documents\\NetBeansProjects\\Compiladorv2\\src\\compiladorv2\\archivos");
    ImageIcon bien = new ImageIcon(getClass().getResource("/Compiladorv2/img/check.png"));
    ImageIcon mal = new ImageIcon(getClass().getResource("/Compiladorv2/img/error.png"));
    public static List<String> errores;
    List<String> columnas = new ArrayList<>();
    List<List<String>> matriz = new ArrayList<>();
    List<List<String>> matriz_semantica;
    String codigo_intermedio;
    List<Token> tokens;
    Stack<String> operadores;
    Stack<String> posfija;
    String[] tipos_guia = {"int", "float", "char"};

    //Métodos agregados
    public void Vaciar() {
        ubicacion = new File("C:\\Users\\Omen\\Documents\\NetBeansProjects\\Compiladorv2\\src\\compiladorv2\\archivos");
        errores = null;

        Area_Edicion.setText("");
        Area_Lexico.setText("");
        Area_Sintactico.setText("");
        Area_Errores.setText("");
        Selector.setCurrentDirectory(ubicacion);
        Selector.setSelectedFile(null);
    }
    
    public void Reiniciar()
    {
        //Arreglos
        operadores = new Stack<>();
        posfija = new Stack<>();
        codigo_intermedio = "";
        tokens = new ArrayList<>();
        errores = new ArrayList<>();
        matriz_semantica = new ArrayList<>();
        matriz_semantica.add(new ArrayList<>());
        matriz_semantica.add(new ArrayList<>());
        
        //Ventana
        Area_Lexico.setText("Tipo\tLexema\tLinea\n");
        Area_Sintactico.setText("");
    }

    public void Seleccionar() {
        Selector.setDialogTitle("Abrir archivo");
        Selector.setFileFilter(new FileNameExtensionFilter("Archivos de texto", "txt"));

        int result = Selector.showOpenDialog(this);
        if (result == Selector.APPROVE_OPTION) {
            File selectedFile = Selector.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                Area_Edicion.read(reader, null);
            } catch (IOException ex) {
                Emergente.showMessageDialog(this, "Error al leer el archivo: " + ex.getMessage(), "Error", Emergente.ERROR_MESSAGE);
            }
        }
    }

    public void Guardar() {
        if (Selector.getSelectedFile() != null) {
            File selectedFile = Selector.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                Area_Edicion.write(writer);
                Emergente.showMessageDialog(null, "Se guardó correctamente", "Archivo guardado", Emergente.INFORMATION_MESSAGE, bien);
            } catch (IOException ex) {
                Emergente.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage(), "Error", Emergente.ERROR_MESSAGE);
            }
        } else
            GuardarComo();
    }

    public void GuardarComo() {
        Selector.setCurrentDirectory(ubicacion);
        Selector.setDialogTitle("Guardar archivo de texto como...");
        Selector.setFileFilter(new FileNameExtensionFilter("Archivos de texto", "txt"));

        int result = Selector.showSaveDialog(this);
        if (result == Selector.APPROVE_OPTION) {
            File selectedFile = Selector.getSelectedFile();
            if (!selectedFile.getPath().endsWith(".txt")) {
                selectedFile = new File(selectedFile.getPath() + ".txt");
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                Area_Edicion.write(writer);
                Emergente.showMessageDialog(null, "Se guardó correctamente", "Archivo guardado", Emergente.INFORMATION_MESSAGE, bien);
            } catch (IOException ex) {
                Emergente.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage(), "Error", Emergente.ERROR_MESSAGE);
            }
        }
    }

    public void Lexico() {
        
        Reiniciar();

        // Crear una instancia del lexer
        Lexer lexer = new Lexer(new StringReader(Area_Edicion.getText()));

        // Llamar a un método para procesar los tokens
        procesarTokens(lexer);

        if (!tokens.isEmpty())
            tokens.add(new Token(14, "$", tokens.getLast().Linea()));
        Area_Lexico.setText(Area_Lexico.getText() + 14 +"\t" + "$"+"\t" + tokens.getLast().Linea());
        if (!errores.isEmpty()) {
            System.out.println("Hay errores");
            Area_Errores.setText("Errores léxicos encontrados");
            for (String error : errores) {
                Area_Errores.setText(Area_Errores.getText() + "\n" + error);
            }
        }
        else
            Sintactico();
    }
    
    private void procesarTokens(Lexer lexer) {
        Token token;
        try {
            // Extraer tokens hasta que no haya más
            while ((token = lexer.yylex()) != null) {
                // Mostrar el tipo y el texto del token
                Area_Lexico.setText(Area_Lexico.getText() + token.Tipo() + "\t" + token.Lexema()+"\t" + token.Linea()+"\n");
                tokens.add(token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CargarTabla() {

        System.out.println("Cargando tabla...");
        String linea, sep = ("\t");
        /*Notas
        La primera fila es la de los tokens
        La primera columna columna de cada fila es el estado
        La cadena vacia se representa por medio de un ?
         */
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Omen\\Documents\\Escuela\\7mo\\Autómatas II\\Tabla de análisis sintáctico.csv"))) {

            //Esta  es la parte en la que iniciamos los tokens a trabajar
            if ((linea = br.readLine()) != null) {
                String[] colums = linea.split(sep);
                for (String valor : colums) {
                    columnas.add(valor);
                }
                columnas.remove(0);
            }

            //Aquí leemos los estados y la matriz
            while ((linea = br.readLine()) != null) {
                String[] valores = linea.split(sep);
                ArrayList<String> fila = new ArrayList<>();
                for (int i = 1; i < valores.length; i++) {
                    fila.add(valores[i]);
                }
                matriz.add(fila);

            }
            
            //Mostramos los datos
            System.out.println("Tabla cargada\n");
            for(String elemento:columnas)
                System.out.print(elemento+"\t");
             for (List<String> fila : matriz){ 
                 System.out.println("");
                for (String elemento : fila) 
                    System.out.print(elemento + "\t"); 
             }
        } catch (IOException e) {
            System.err.println("No se encontró el archivo: " + e.getMessage());
        }


    }

    public void Sintactico() {
        
        
        String[][] producciones = 
        {
            {"P" , "Tipo" , "id" , "V"},       //P1
            {"P" , "A"},                       //P2
            {"Tipo" , "int"},                  //P3
            {"Tipo" , "float"},                //P4
            {"Tipo" , "char"},                 //P5
            {"V" , "," , "id" , "V"},          //P6
            {"V" , ";", "P"},                  //P7
            {"A" , "id" , "=" , "Exp" , ";"},  //P8
            {"Exp" , "+" , "Term" , "E"},      //P9
            {"Exp" , "-" , "Term" , "E"},      //P10
            {"Exp" , "Term" , "E"},            //P11
            {"E" , "+" , "Term" , "E"},        //P12
            {"E" , "-" , "Term" , "E"},        //P13
            {"E" , ""},                        //P14
            {"Term" , "F" , "T"},              //P15
            {"T" , "*" , "F" , "T"},           //P16
            {"T" , "/" , "F" , "T"},           //P17
            {"T" , ""},                        //P18
            {"F" , "id"},                      //P19
            {"F" , "num"},                     //P20
            {"F" , "(", "Exp", ")"}            //P21
        };
        
        
        
        
        //Inicializar
        Stack<String> pila = new Stack<>();
        int estado = 0;
        String accion;
        
        
        boolean asignacion = false; //Variable para identifiar si estamos en las asignaciones
        boolean valido = true;
        List<Token> expresion = new ArrayList<>();
        
        pila.push("$"); // Estado inicial
        pila.push("0"); // Estado inicial
        
        MostrarPila(pila);
        
        Token tok;
        String produccion;
        int ppro;

        int numtoken = 0;
        // Recorrer toda la cadena de simbolos
        while (!pila.empty()) {

            tok = tokens.get(numtoken);
            

            System.out.println("Nueva acción\nPosicion: I" + estado + ", Simbolo(" + columnas.get(tok.Tipo()) + ") en columna: " + tok.Tipo());
            if (tok.Tipo() != -1) {

                if(tok.Tipo() >= matriz.get(estado).size())
                {    System.out.println("Error sintáctico - Linea "+tok.Linea()+" - Se esperaba: "+ Validos(estado));
                    Area_Errores.setText("Error sintáctico - Linea "+tok.Linea()+" - Se esperaba: "+ Validos(estado));
                    break;
                }
                accion = matriz.get(estado).get(tok.Tipo());
                
                if (accion.equals("")) {
                    System.out.println("Error sintáctico - Linea "+tok.Linea()+" - Se esperaba: "+ Validos(estado));
                    Area_Errores.setText("Error sintáctico - Linea "+tok.Linea()+" - Se esperaba: "+ Validos(estado));
                    break;
                }
                System.out.println("Accion: " + accion);
                
                
                //Checar si es desplazamiento(I) o reducción(P)
                if (accion.startsWith("P")) {
                    if(accion.equals("P0"))
                    {
                        
                        System.out.println("Datos de la matriz semántica:\nVar\tTipo\tCodigo\n"+matriz_semantica.get(0).size()+"\t"+matriz_semantica.get(1).size());
                        for(int x = matriz_semantica.get(0).size()-1; x>=0;x--)
                            System.out.println(matriz_semantica.get(0).get(x)+"\t"+tipos_guia[Integer.parseInt(matriz_semantica.get(1).get(x))]+"\t"+matriz_semantica.get(1).get(x));
                        
                        
                        valido = Evaluar(expresion);
                        
                        if(!valido)
                            break;
                        
                        System.out.println("Cadena aceptada.");
                        Area_Errores.setText("Cadena aceptada.");
                        break;
                    }
                    
                    ppro = Integer.parseInt(accion.substring(1)) -1; //Le restamos 1 porque no ponemos la producción 0, así que se recorren los números
                    produccion = producciones[ppro][0];
                    

                    if(!producciones[ppro][1].isBlank())
                    {
                        String pop;
                        for(int x = (producciones[ppro].length - 1) * 2; x > 0; x--) 
                             pop = pila.pop();
                        MostrarPila(pila);
                    
                    }
                    estado = Integer.parseInt(pila.peek());
                    System.out.println("Terminó la reducción de "+((producciones[ppro].length - 1) * 2)+"\nEl estado en la cima es :"+estado);
                    pila.push(produccion);
                    //
                    //Buscar la ACCION en la tabla 
                    if (columnas.indexOf(produccion) != -1) {
                        accion = matriz.get(estado).get(columnas.indexOf(produccion));
                        System.out.println("La acción después de la reducción es: "+accion);
                        
                        if (accion.equals("")) {
                            System.out.println("Error sintáctico - Linea "+tok.Linea()+" - Se esperaba: "+ Validos(estado));
                            Area_Errores.setText("Error sintáctico - Linea "+tok.Linea()+" - Se esperaba: "+ Validos(estado));
                            break;
                        }
                        pila.push(accion.substring(1));
                        estado = Integer.parseInt(accion.substring(1));
                    }
                    
                } else if (accion.startsWith("I")) { 
                    
                        estado = Integer.parseInt(accion.substring(1));
                        pila.add(columnas.get(tok.Tipo()));
                        pila.add(estado+"");
                        System.out.println("Terminó el desplazamiento de "+columnas.get(tok.Tipo())+" al estado I"+estado);
                        numtoken ++;
                        
                        //INTERVENCIÓN DEL SEMÁNTICO
                
                
                
                switch(tok.Tipo())
                {
                    case 6: //;
                        asignacion = true;
                        break;
                    case 2: //int
                    case 3: //float
                    case 4: //char
                        asignacion = false;
                        break;
                }
                
                if(asignacion){
                    valido = Semantico(tok, 1);
                    expresion.add(tok);
                }
                else
                    valido = Semantico(tok, 0);
                
                if(!valido)
                    break;
                
                
                }
            } else {
                System.out.println("Error sintáctico - Linea "+tok.Linea()+"Se esperaba: "+ Validos(estado));
                Area_Errores.setText("Error sintáctico - Linea "+tok.Linea()+"Se esperaba: "+ Validos(estado));
            }
             MostrarPila(pila);
            
        }
    
    }
    
    /*
        Semántico
            Vamos a checar tres cosas:
            
            1.- Ir asignando a cada variable su tipo de dato
                Si el token es int, float o char asignará el valor en una nueva posición
                Si el token es una variable, se guardará en la ultima posición si no tiene asginado el id, de otro modo asignará el id en una nueva posición 
                con el último tipo de dato (Esto requiere distinguir entre asignaciones y declaraciones siendo I7)
    
            2.- Verificar que las variables que se usen en las expresiones ya estén declaradas
                A partir de que dejamos las declaraciones, cada vez que se encuentre un id se tendrá que verificar
    
            3.- Verificar que cuando se haga una operación los tipos de datos coincidan
                Para ésto tendré que hacer la tabla de análisis semantico donde se vean qué saldrá a cada operaciones
    
          
        
        Generación de código intermedio
            Generaremos lo siguiente:
            
            Las asignaciones de un tipo de dato con una variable:
                Cuando se haga una asignación se irá poniendo el código dentro de una variable String global
    
            Las actualizaciones de los valores de las variables:
                Aquí no me queda claro el cómo se plantea una operaciones cuando hay una constante, o cuando es más compleja.
            
    */
    
    public boolean Semantico(Token ob, int op) //ob Tiene el lexema que se está analizando, op indica si estamos en una declaración o un operación
    {
        
        
        switch(op)
        {
            case 0: //Declaración
                  //En éste caso solo se irán agregando las variable para ir definiendo (también se corrobora que no esté declarada ya)
                  switch(ob.Tipo())
                  {
                      case 2:
                          matriz_semantica.get(1).add("0");
                          break;
                          
                      case 3:
                          matriz_semantica.get(1).add("1");
                          break;
                          
                      case 4:
                          matriz_semantica.get(1).add("2");
                          break;
                      case 0:
                          
                          if(Buscar(ob.Lexema()) != -1)
                          {
                               System.out.println("Error semántico - Linea "+ob.Linea()+" - Variable ya declarada: "+ ob.Lexema());
                               Area_Errores.setText("Error semántico - Linea "+ob.Linea()+" - Variable ya declarada: "+ ob.Lexema());
                              return false;
                          }
                              
                          
                          if(matriz_semantica.get(0).size() < matriz_semantica.get(1).size()) //Es el caso en el que ya está el tipo pero no la variable
                              matriz_semantica.get(0).add(ob.Lexema());
                          else
                          {
                              matriz_semantica.get(1).add(matriz_semantica.get(1).getLast());
                              matriz_semantica.get(0).add(ob.Lexema());
                          }   
                          break;                          
                  }
                break;
            case 1: //Una operación
                //En este caso se va evaluando la valides de las operaciones pero no se declara nada
                
                if(ob.Tipo() == 0)
                    if(Buscar(ob.Lexema()) == -1){
                        System.out.println("Error semántico - Linea "+ob.Linea()+" - Variable no declarada: "+ ob.Lexema());
                        Area_Errores.setText("Error semántico - Linea "+ob.Linea()+" - Variable no declarada: "+ ob.Lexema());
                        return false;
                    }     
                break;  
                     
        }
        return true;
    }  
    
    public boolean Evaluar(List<Token> expresion)
    {
        String cadena = "", op = "", intermedio="Codigo intermedio\n";
        Token principal;
        boolean aux;
        int var1, var2, vart=-1, con=0;
        
        List<String> c = new ArrayList();
        c.add("(");
        c.add(")");
        c.add("+");
        c.add("-");
        c.add("/");
        c.add("*");
        
        
        //Apuntador donde la primera posición será fila (lo que llega a la expresión) y la segunda será la columna (lo que sale del peek)
        int [] ap = {-1, -1}; //Apuntador para el autómata de la pila
        
        Stack<Token> pilao = new Stack<Token>(); //Pila de operadores
        Stack<Integer> pilae = new Stack<Integer>(); //Pila de operadores
        List<Token> evaluar = new ArrayList(); //Es el arrreglo en el que vamos a recorrer las cosas con orden a evaluar
        
        //Autómata para manejar la pila
        /*
            0 - Meter a la pila
            1 - Sacar de la pila y luego meter
            2 - Sacar de la pila y no meter
        */
        int [][] autop =    { { 0, 0, 0, 0, 0, 0}, 
                              { 2, 0, 1, 1, 1, 1},
                              { 0, 0, 1, 1, 1, 1},
                              { 0, 0, 1, 1, 1, 1},
                              { 0, 0, 0, 0, 1, 1},
                              { 0, 0, 0, 0, 1, 1} };
        
        //Autómata para manejar las operaciones
        int [][] autot =    { {  0,  1, -1}, 
                              {  1,  1, -1},
                              { -1, -1, -1} };
        
        //Limpiar la expresión
        
        while(expresion.getFirst().Tipo() != 0) //Quitamos todos los simbolos que no sean utiles hasta llegar a la primera variable, la que tiene la asignación
            expresion.removeFirst().Lexema(); 
        
        principal = expresion.getFirst();
        expresion.removeFirst().Lexema(); //Quitamos la variable a evaluar
        expresion.removeFirst().Lexema(); //Quitamos el =
        expresion.removeLast().Lexema(); //Quitamos el ;
        
        //Obtener información de la expresión que se va a evaluar
        for(Token t : expresion)
          cadena+= t.Lexema();
        
        System.out.println("Variable a evaluar: "+principal.Lexema());
        System.out.println("Expresion a evaluar: "+cadena);
        System.out.println("Elementos a evaluar: "+expresion.size());
        
        cadena = "";
        
        
        //EMPEZAMOS A TRABAJAR SOBRE LA EXPRESIÓN
        
        System.out.println("Operadores\t Cadena");
        
        for(Token t : expresion){
          switch(t.Tipo())
          {
              case 0: //Si es variable
              case 1: //Si es un número constante
                evaluar.add(t);
                cadena+= t.Lexema()+" ";
                break;
              case 7:
              case 8:
              case 9:
              case 10:
              case 11:
              case 12:
                  if(pilao.empty())
                      pilao.push(t);
                  else
                  {
                      
                      aux = true;
                      while(aux)
                      {
                          if(pilao.empty()){
                            pilao.push(t);
                            break;
                          }
                          ap[1] = c.indexOf(pilao.peek().Lexema());
                          ap[0] = c.indexOf(t.Lexema());
                          switch(autop[ap[0]][ap[1]])
                          {
                              case 0: //Metemos a la pila
                                  pilao.push(t);
                                  aux= false;
                                  break;
                              case 1: //Sacamos
                                  evaluar.add(pilao.pop());
                                  cadena += evaluar.getLast().Lexema()+" ";
                                  break;
                              case 2: //Sacamos y luego terminamos
                                  pilao.pop();
                                  aux= false;
                          }
                      }
                  }
          }
        }
        
        while(!pilao.empty())
        {
            evaluar.add(pilao.pop());
            cadena += evaluar.getLast().Lexema()+" ";
        }
        
        System.out.println("Notacion postfija de la operacion:\n"+cadena);
        
        //Declaración de las variables
        for(int x = 0 ;x < matriz_semantica.get(0).size();x++)
            intermedio+= Tipo(Integer.parseInt(matriz_semantica.get(1).get(x)))+" "+matriz_semantica.get(0).get(x)+";\n";
        
        for(Token t : evaluar)
        {
            switch(t.Tipo())
            {
                case 0: //Para variables
                    pilae.push(Integer.parseInt(matriz_semantica.get(1).get(Buscar(t.Lexema())))); //Obtenemos el tipo de dato de la variable
                    con ++;
                    intermedio += "V"+con+" = "+t.Lexema()+";\n";
                    break;
                case 1: //Para números los manejare a manera estándar como enteros
                    pilae.push(0);
                    con ++;
                    intermedio += "V"+con+" = "+t.Lexema()+";\n";
                    break;
                default: //Osea que debe ser un operador
                    con --;
                    var2 = pilae.pop();
                    var1 = pilae.pop();
                    vart = autot[var2][var1];
                    intermedio += "V"+con+" = V"+con+" "+t.Lexema()+" V"+(con+1)+";\n";
                    
                    if(vart == -1)
                    {
                        System.out.println("Error semántico - Linea "+t.Linea()+" - Los tipos de datos no son compatibles para la operación: "+ Tipo(var1)+" "+t.Lexema()+" "+Tipo(var2));
                        Area_Errores.setText("Error semántico - Linea "+t.Linea()+" - Los tipos de datos no son compatibles para la operación: "+ Tipo(var1)+" "+t.Lexema()+" "+Tipo(var2));
                        return false;
                    }
                    pilae.push(vart);
            }
        }
        
        if(vart != (Integer.parseInt(matriz_semantica.get(1).get(Buscar(principal.Lexema()))))){
            System.out.println("Error semántico - Linea "+principal.Linea()+" - El resultado de tipo "+Tipo(vart)+" de la expresión no concuerda con la variable "+principal.Lexema()+" de tipo: "+ Tipo((Integer.parseInt(matriz_semantica.get(1).get(Buscar(principal.Lexema()))))));
            Area_Errores.setText("Error semántico - Linea "+principal.Linea()+" - El resultado de tipo "+Tipo(vart)+" de la expresión no concuerda con la variable "+principal.Lexema()+" de tipo: "+ Tipo((Integer.parseInt(matriz_semantica.get(1).get(Buscar(principal.Lexema()))))));
            return false;
        }
        
        intermedio += principal.Lexema()+" = V1;";
        
        System.out.println(intermedio);
        return true;
    }
    
    
    public int Buscar(String id)
    {
        int x = -1;
        for(x = matriz_semantica.get(0).size()-1;x>-1;x--)
            if(matriz_semantica.get(0).get(x).equals(id))
                return x;
        return x;
    }
    
    
    public String Validos(int est)
    {
        String val="";
        List<String> estado = matriz.get(est);
        for(String elemento : estado)
        {
            if(estado.indexOf(elemento) >= columnas.indexOf("$"))
                return val;
            if(!elemento.isBlank())
                val+= columnas.get(estado.indexOf(elemento))+" ";
            
        }
        return val;
    }

    public void MostrarPila(Stack<String> pp) {
        Stack<String> pa = new Stack<>();
        String texto = Area_Sintactico.getText();
        while (!pp.isEmpty()) {
            pa.push(pp.pop());
        }
        while (!pa.isEmpty()) {
            texto += pp.push(pa.pop());
        }
        texto += "\n";
        Area_Sintactico.setText(texto);
    }
    
    public String Tipo (int t)
    {
        switch(t)
        {
            case 0 -> {
                return "Int";
            }
            case 1 -> {
                return "Float";
            }
            case 2 -> {
                return "Char";
            }
        }
        return "Tipo no identificado";
    }
    
    

    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textLine2 = new org.jfree.text.TextLine();
        Selector = new javax.swing.JFileChooser();
        Emergente = new javax.swing.JOptionPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        Area_Edicion = new javax.swing.JTextArea();
        jToolBar1 = new javax.swing.JToolBar();
        btn_nuevo = new javax.swing.JButton();
        btn_abrir = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        btn_guardar = new javax.swing.JButton();
        btn_guardarcomo = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        btn_correr = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        btn_cerrar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        Area_Errores = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        Area_Sintactico = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        Area_Lexico = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        menu_archivo = new javax.swing.JMenu();
        op_nuevo = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        op_abrir = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        op_guardar = new javax.swing.JMenuItem();
        op_guardarcomo = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        op_cerrar = new javax.swing.JMenuItem();
        menu_opciones = new javax.swing.JMenu();
        op_correr = new javax.swing.JMenuItem();

        Selector.setCurrentDirectory(new java.io.File("C:\\Users\\Omen\\Documents\\NetBeansProjects\\Compiladorv2\\src\\compiladorv2\\archivos"));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(31, 31, 31));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMinimumSize(new java.awt.Dimension(1250, 800));
        setResizable(false);

        Area_Edicion.setBackground(new java.awt.Color(51, 51, 51));
        Area_Edicion.setColumns(20);
        Area_Edicion.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        Area_Edicion.setForeground(new java.awt.Color(204, 204, 204));
        Area_Edicion.setRows(5);
        Area_Edicion.setMaximumSize(new java.awt.Dimension(192, 89));
        Area_Edicion.setMinimumSize(new java.awt.Dimension(10, 10));
        jScrollPane1.setViewportView(Area_Edicion);

        jToolBar1.setRollover(true);

        btn_nuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/new.png"))); // NOI18N
        btn_nuevo.setText("Nuevo");
        btn_nuevo.setFocusable(false);
        btn_nuevo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_nuevo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btn_nuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_nuevoActionPerformed(evt);
            }
        });
        jToolBar1.add(btn_nuevo);

        btn_abrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/open.png"))); // NOI18N
        btn_abrir.setText("Abrir");
        btn_abrir.setFocusable(false);
        btn_abrir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_abrir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btn_abrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_abrirActionPerformed(evt);
            }
        });
        jToolBar1.add(btn_abrir);
        jToolBar1.add(jSeparator4);

        btn_guardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/save.png"))); // NOI18N
        btn_guardar.setText("Guardar");
        btn_guardar.setFocusable(false);
        btn_guardar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_guardar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btn_guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_guardarActionPerformed(evt);
            }
        });
        jToolBar1.add(btn_guardar);

        btn_guardarcomo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/save as.png"))); // NOI18N
        btn_guardarcomo.setText("Guardar como");
        btn_guardarcomo.setFocusable(false);
        btn_guardarcomo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_guardarcomo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btn_guardarcomo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_guardarcomoActionPerformed(evt);
            }
        });
        jToolBar1.add(btn_guardarcomo);
        jToolBar1.add(jSeparator5);

        btn_correr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/play.png"))); // NOI18N
        btn_correr.setText("Correr");
        btn_correr.setFocusable(false);
        btn_correr.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_correr.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btn_correr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_correrActionPerformed(evt);
            }
        });
        jToolBar1.add(btn_correr);
        jToolBar1.add(jSeparator6);
        jToolBar1.add(jSeparator7);

        btn_cerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/close.png"))); // NOI18N
        btn_cerrar.setText("Cerrar");
        btn_cerrar.setFocusable(false);
        btn_cerrar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btn_cerrar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btn_cerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cerrarActionPerformed(evt);
            }
        });
        jToolBar1.add(btn_cerrar);

        jLabel1.setFont(new java.awt.Font("Roboto Light", 1, 18)); // NOI18N
        jLabel1.setText("Análisis sintáctico");

        jLabel2.setFont(new java.awt.Font("Roboto Light", 1, 18)); // NOI18N
        jLabel2.setText("Análisis léxico");

        Area_Errores.setBackground(new java.awt.Color(51, 51, 51));
        Area_Errores.setColumns(20);
        Area_Errores.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        Area_Errores.setForeground(new java.awt.Color(204, 204, 204));
        Area_Errores.setRows(5);
        jScrollPane2.setViewportView(Area_Errores);

        Area_Sintactico.setBackground(new java.awt.Color(51, 51, 51));
        Area_Sintactico.setColumns(20);
        Area_Sintactico.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        Area_Sintactico.setForeground(new java.awt.Color(204, 204, 204));
        Area_Sintactico.setRows(5);
        jScrollPane4.setViewportView(Area_Sintactico);

        Area_Lexico.setEditable(false);
        Area_Lexico.setBackground(new java.awt.Color(51, 51, 51));
        Area_Lexico.setColumns(20);
        Area_Lexico.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        Area_Lexico.setForeground(new java.awt.Color(204, 204, 204));
        Area_Lexico.setRows(5);
        jScrollPane3.setViewportView(Area_Lexico);

        jMenuBar1.setBackground(new java.awt.Color(38, 38, 38));
        jMenuBar1.setForeground(new java.awt.Color(51, 51, 51));

        menu_archivo.setText("Archivo");
        menu_archivo.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        op_nuevo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        op_nuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/new.png"))); // NOI18N
        op_nuevo.setText("Nuevo");
        op_nuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                op_nuevoActionPerformed(evt);
            }
        });
        menu_archivo.add(op_nuevo);
        menu_archivo.add(jSeparator1);

        op_abrir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        op_abrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/open.png"))); // NOI18N
        op_abrir.setText("Abrir");
        op_abrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                op_abrirActionPerformed(evt);
            }
        });
        menu_archivo.add(op_abrir);
        menu_archivo.add(jSeparator2);

        op_guardar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        op_guardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/save.png"))); // NOI18N
        op_guardar.setText("Guardar");
        op_guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                op_guardarActionPerformed(evt);
            }
        });
        menu_archivo.add(op_guardar);

        op_guardarcomo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        op_guardarcomo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/save as.png"))); // NOI18N
        op_guardarcomo.setText("Guardar como");
        op_guardarcomo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                op_guardarcomoActionPerformed(evt);
            }
        });
        menu_archivo.add(op_guardarcomo);
        menu_archivo.add(jSeparator3);

        op_cerrar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0));
        op_cerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/close.png"))); // NOI18N
        op_cerrar.setText("Cerrar");
        op_cerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                op_cerrarActionPerformed(evt);
            }
        });
        menu_archivo.add(op_cerrar);

        jMenuBar1.add(menu_archivo);

        menu_opciones.setText("Opciones");
        menu_opciones.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        op_correr.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        op_correr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compiladorv2/img/play.png"))); // NOI18N
        op_correr.setText("Correr");
        op_correr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                op_correrActionPerformed(evt);
            }
        });
        menu_opciones.add(op_correr);

        jMenuBar1.add(menu_opciones);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 1250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 644, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 576, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(668, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 576, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 469, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(8, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(324, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(239, 239, 239)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void op_abrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_op_abrirActionPerformed
        Seleccionar();
    }//GEN-LAST:event_op_abrirActionPerformed

    private void op_guardarcomoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_op_guardarcomoActionPerformed
        GuardarComo();
    }//GEN-LAST:event_op_guardarcomoActionPerformed

    private void op_correrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_op_correrActionPerformed
        Area_Errores.setText("");
        Lexico();
        //Sintactico();
    }//GEN-LAST:event_op_correrActionPerformed

    private void op_nuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_op_nuevoActionPerformed
        Vaciar();
    }//GEN-LAST:event_op_nuevoActionPerformed

    private void op_cerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_op_cerrarActionPerformed
        Vaciar();
    }//GEN-LAST:event_op_cerrarActionPerformed

    private void btn_abrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_abrirActionPerformed
        Seleccionar();
    }//GEN-LAST:event_btn_abrirActionPerformed

    private void btn_cerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cerrarActionPerformed
        Vaciar();
    }//GEN-LAST:event_btn_cerrarActionPerformed

    private void btn_guardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_guardarActionPerformed
        Guardar();
    }//GEN-LAST:event_btn_guardarActionPerformed

    private void btn_guardarcomoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_guardarcomoActionPerformed
        GuardarComo();
    }//GEN-LAST:event_btn_guardarcomoActionPerformed

    private void op_guardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_op_guardarActionPerformed
        Guardar();
    }//GEN-LAST:event_op_guardarActionPerformed

    private void btn_nuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_nuevoActionPerformed
        Vaciar();
    }//GEN-LAST:event_btn_nuevoActionPerformed

    private void btn_correrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_correrActionPerformed
        Area_Errores.setText("");
        Lexico();
        //Sintactico();

    }//GEN-LAST:event_btn_correrActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        /* Create and display the form */

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Interfaz().setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea Area_Edicion;
    private javax.swing.JTextArea Area_Errores;
    private javax.swing.JTextArea Area_Lexico;
    private javax.swing.JTextArea Area_Sintactico;
    private javax.swing.JOptionPane Emergente;
    private javax.swing.JFileChooser Selector;
    private javax.swing.JButton btn_abrir;
    private javax.swing.JButton btn_cerrar;
    private javax.swing.JButton btn_correr;
    private javax.swing.JButton btn_guardar;
    private javax.swing.JButton btn_guardarcomo;
    private javax.swing.JButton btn_nuevo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JMenu menu_archivo;
    private javax.swing.JMenu menu_opciones;
    private javax.swing.JMenuItem op_abrir;
    private javax.swing.JMenuItem op_cerrar;
    private javax.swing.JMenuItem op_correr;
    private javax.swing.JMenuItem op_guardar;
    private javax.swing.JMenuItem op_guardarcomo;
    private javax.swing.JMenuItem op_nuevo;
    private org.jfree.text.TextLine textLine2;
    // End of variables declaration//GEN-END:variables
}
