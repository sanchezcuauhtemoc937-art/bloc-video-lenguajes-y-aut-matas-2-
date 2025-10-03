// Importaciones necesarias para la interfaz gráfica y manejo de eventos
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

// -----------------------------
// Clase para representar un nodo en el árbol de expresiones
// Cada nodo puede ser un número, una letra (operando) o un operador (+, -, *, /, ^)
// -----------------------------
class Node {
    String value;   // valor del nodo (operador u operando)
    Node left, right; // hijos izquierdo y derecho

    Node(String value) {
        this.value = value;
        left = right = null; // al inicio no tiene hijos
    }
}

// -----------------------------
// Clase que maneja toda la lógica: validación, detección de tipo de expresión,
// conversiones a posfijo, y construcción del árbol de expresiones
// -----------------------------
class ExpressionParser {

    private String originalExpression; // expresión original escrita por el usuario
    private String expressionType;     // tipo de expresión: infija, prefija o posfija

    // Constructor: limpia espacios y valida la expresión
    public ExpressionParser(String expression) {
        this.originalExpression = expression.replaceAll("\\s+", ""); // quitar espacios
        this.validateExpression(); // validación básica
        this.expressionType = detectType(); // detectar el tipo (infija, prefija o posfija)
    }
    
    // Construye el árbol a partir de la expresión (convierte a posfija primero)
    public Node getRootNode() {
        String postfix = toPostfix();       // convertir según el tipo
        return buildFromPostfix(postfix);   // construir árbol desde posfija
    }
    
    // Convierte la expresión a notación posfija dependiendo del tipo
    private String toPostfix() {
        switch (expressionType) {
            case "Posfija": return originalExpression;
            case "Infija":  return infixToPostfix(originalExpression);
            case "Prefija": return prefixToPostfix(originalExpression);
            default:
                throw new IllegalArgumentException("No se pudo determinar el tipo de expresión.");
        }
    }

    public String getExpressionType() {
        return expressionType;
    }

    // -----------------------------
    // VALIDACIÓN DE LA EXPRESIÓN
    // -----------------------------
    private void validateExpression() {
        if (originalExpression.isEmpty()) {
            throw new IllegalArgumentException("La expresión no puede estar vacía.");
        }

        // Revisar que solo haya números, letras, operadores o paréntesis
        for (char c : originalExpression.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && !isOperator(c) && c != '(' && c != ')') {
                throw new IllegalArgumentException("Expresión inválida: carácter no reconocido '" + c + "'.");
            }
        }
    }

    // Detectar el tipo de expresión: infija, prefija o posfija
    private String detectType() {
        char firstChar = originalExpression.charAt(0);
        char lastChar = originalExpression.charAt(originalExpression.length() - 1);

        if (isOperator(lastChar) && !isOperator(firstChar)) return "Posfija";
        if (isOperator(firstChar) && !isOperator(lastChar)) return "Prefija";
        return "Infija";
    }

    // Revisa si un carácter es operador
    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    // Precedencia de los operadores (para infija a posfija)
    private int precedence(char c) {
        switch (c) {
            case '+':
            case '-': return 1;
            case '*':
            case '/': return 2;
            case '^': return 3;
        }
        return -1;
    }

    // -----------------------------
    // CONVERSIÓN DE INFIJA A POSFIJA (algoritmo Shunting-yard)
    // -----------------------------
    private String infixToPostfix(String infix) {
        StringBuilder postfix = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (int i = 0; i < infix.length(); i++) {
            char c = infix.charAt(i);

            // Validaciones de errores comunes en infija
            if (c == '(' && i < infix.length() - 1 && infix.charAt(i + 1) == ')') {
                throw new IllegalArgumentException("Error: paréntesis vacíos '()' en la posición " + i + ".");
            }
            if (isOperator(c) && i < infix.length() - 1 && infix.charAt(i + 1) == ')') {
                throw new IllegalArgumentException("Error: falta un operando después del operador '" + c + "'.");
            }
            if (c == '(' && i > 0 && Character.isLetterOrDigit(infix.charAt(i - 1))) {
                throw new IllegalArgumentException("Error: falta un operador antes del paréntesis en la posición " + i + ".");
            }

            // Si es operando → lo agrega directamente
            if (Character.isLetterOrDigit(c)) {
                if (i > 0 && Character.isLetterOrDigit(infix.charAt(i-1))) {
                    throw new IllegalArgumentException("Error: Falta operador entre '" + infix.charAt(i-1) + "' y '" + c + "' en la posición " + i + ".");
                }
                postfix.append(c);
            }
            // Si es paréntesis de apertura → lo apila
            else if (c == '(') {
                stack.push(c);
            }
            // Si es paréntesis de cierre → desapila hasta encontrar el (
            else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    postfix.append(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException("Error: falta paréntesis de apertura en la posición " + i + ".");
                }
                stack.pop(); // eliminar '('
            }
            // Si es operador
            else if (isOperator(c)) {
                if (i > 0 && isOperator(infix.charAt(i-1)) && infix.charAt(i-1) != '(' && infix.charAt(i) != '-') {
                    throw new IllegalArgumentException("Error: Falta operando entre '" + infix.charAt(i-1) + "' y '" + c + "'.");
                }
                while (!stack.isEmpty() && stack.peek() != '(' && precedence(c) <= precedence(stack.peek())) {
                    postfix.append(stack.pop());
                }
                stack.push(c);
            }
        }

        // Vaciar la pila
        while (!stack.isEmpty()) {
            if (stack.peek() == '(') {
                throw new IllegalArgumentException("Error: falta paréntesis de cierre.");
            }
            postfix.append(stack.pop());
        }
        return postfix.toString();
    }
    
    // -----------------------------
    // CONVERSIÓN DE PREFIJA A POSFIJA
    // -----------------------------
    private String prefixToPostfix(String prefix) {
        Stack<String> stack = new Stack<>();
        for (int i = prefix.length() - 1; i >= 0; i--) {
            char c = prefix.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                stack.push(String.valueOf(c));
            } else if (isOperator(c)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Error en Prefija: Faltan operandos para '" + c + "' en la posición " + i + ".");
                }
                String op1 = stack.pop();
                String op2 = stack.pop();
                stack.push(op1 + op2 + c);
            }
        }
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Expresión prefija inválida: operandos/operadores desbalanceados.");
        }
        return stack.pop();
    }

    // -----------------------------
    // CONSTRUCCIÓN DEL ÁRBOL DESDE POSFIJA
    // -----------------------------
    private Node buildFromPostfix(String postfix) {
        Stack<Node> stack = new Stack<>();
        for (int i = 0; i < postfix.length(); i++) {
            char c = postfix.charAt(i);
            String s = String.valueOf(c);

            if (Character.isLetterOrDigit(c)) {
                stack.push(new Node(s));
            } else if (isOperator(c)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Error en Posfija: Faltan operandos para '" + c + "'.");
                }
                Node t = new Node(s);
                t.right = stack.pop(); // hijo derecho
                t.left = stack.pop();  // hijo izquierdo
                stack.push(t);
            }
        }
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Expresión posfija inválida.");
        }
        return stack.pop();
    }

    // Recorridos del árbol → generan las notaciones
    public String inOrder(Node node) {
        if (node == null) return "";
        StringBuilder result = new StringBuilder();
        boolean needsParentheses = isOperator(node.value.charAt(0)) && (node.left != null || node.right != null);
        if (needsParentheses) result.append("(");
        result.append(inOrder(node.left));
        result.append(node.value);
        result.append(inOrder(node.right));
        if (needsParentheses) result.append(")");
        return result.toString();
    }
    public String preOrder(Node node) {
        if (node == null) return "";
        return node.value + preOrder(node.left) + preOrder(node.right);
    }
    public String postOrder(Node node) {
        if (node == null) return "";
        return postOrder(node.left) + postOrder(node.right) + node.value;
    }
}

// -----------------------------
// Clase de la interfaz gráfica principal (Swing)
// -----------------------------
public class ExpressionAnalyzerGUI extends JFrame {

    private JTextField expressionField;      // campo para escribir la expresión
    private JTextArea resultArea;            // muestra resultados de conversiones
    private JTextArea treeVisualizationArea; // muestra el árbol en texto

    // Constructor: crea la ventana
    public ExpressionAnalyzerGUI() {
        super("Analizador de Expresiones");
        createGUI(); // inicializa la interfaz
    }

    // Construcción de la interfaz gráfica
    private void createGUI() {
        setLayout(new BorderLayout(10, 10));

        // Panel de entrada
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        inputPanel.add(new JLabel("Introduce una expresión:"));
        expressionField = new JTextField(20);
        inputPanel.add(expressionField);

        JButton calculateButton = new JButton("Calcular");
        inputPanel.add(calculateButton);
        add(inputPanel, BorderLayout.NORTH);

        // Panel central con dos secciones: resultados y árbol
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Panel de resultados
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Resultados"));
        resultArea = new JTextArea(10, 30);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultPanel.add(resultScrollPane, BorderLayout.CENTER);
        centerPanel.add(resultPanel);

        // Panel del árbol
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.setBorder(BorderFactory.createTitledBorder("Visualización del Árbol (Texto)"));
        treeVisualizationArea = new JTextArea(10, 30);
        treeVisualizationArea.setEditable(false);
        treeVisualizationArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane treeScrollPane = new JScrollPane(treeVisualizationArea);
        treePanel.add(treeScrollPane, BorderLayout.CENTER);
        centerPanel.add(treePanel);

        add(centerPanel, BorderLayout.CENTER);

        // Acción del botón
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateExpression();
            }
        });

        // Configuración de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null); // centrar ventana
        setVisible(true);
    }

    // Método que procesa la expresión escrita por el usuario
    private void calculateExpression() {
        String expression = expressionField.getText().trim();
        if (expression.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, introduce una expresión.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            ExpressionParser parser = new ExpressionParser(expression);
            Node root = parser.getRootNode();

            // Mostrar resultados
            resultArea.setText("Tipo de Expresión: " + parser.getExpressionType() + "\n\n");
            resultArea.append("Posfija: " + parser.postOrder(root) + "\n");
            resultArea.append("Prefija: " + parser.preOrder(root) + "\n");
            resultArea.append("Infija: " + parser.inOrder(root) + "\n");

            // Mostrar árbol
            treeVisualizationArea.setText("Árbol de Expresiones:\n");
            printTree(root, "", true);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Error de Expresión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            resultArea.setText("");
            treeVisualizationArea.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // para depuración
            resultArea.setText("");
            treeVisualizationArea.setText("");
        }
    }
    
    // Imprimir el árbol de forma textual (con ramas └── y ┌──)
    private void printTree(Node node, String prefix, boolean isTail) {
        if (node == null) return;
        if (node.right != null) {
            printTree(node.right, prefix + (isTail ? "│   " : "    "), false);
        }
        treeVisualizationArea.append(prefix + (isTail ? "└── " : "┌── ") + node.value + "\n");
        if (node.left != null) {
            printTree(node.left, prefix + (isTail ? "    " : "│   "), true);
        }
    }

    // Método principal para ejecutar la aplicación
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExpressionAnalyzerGUI());
    }
}

