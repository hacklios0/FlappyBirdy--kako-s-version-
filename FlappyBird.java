
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    private int birdY = 250; // Posición inicial del pájaro
    private int birdVelocity = 0;
    private int gravity = 1;
    private float score = 0;
    private boolean gameOver = false;
    private boolean newRecord = false;
    private ArrayList<Pipe> pipes = new ArrayList<>();
    private javax.swing.Timer timer;
    private final int PIPE_WIDTH = 50;
    private final int PIPE_GAP = 100;
    private final int PIPE_SPEED = 5;
    private Image birdImage1, birdImage2, birdImage3; // Imagen del pájaro
    private Image pipeImage1, pipeImage2, pipeImage3; // Imagen de las tuberías
    private Image backgroundImage1, backgroundImage2, backgroundImage3; // Imagen del fondo
    private int textureSet = 0; // Bandera para controlar cuál conjunto de texturas se usa

    private final ArrayList<String> highScores = new ArrayList<>();
    private final String SCORE_FILE = "scores.txt"; // Archivo de texto de los scores

    class Pipe {
        Rectangle rect;
        boolean isTop; // Indica si la tubería es superior o inferior

        public Pipe(int x, int y, int width, int height, boolean isTop) {
            this.rect = new Rectangle(x, y, width, height);
            this.isTop = isTop;
        }
    }

    public FlappyBird() {
        loadHighScores();
        loadTextures();

        JFrame frame = new JFrame("Flappy Bird");
        frame.setSize(800, 600); // Establece el tamaño de la ventana
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.addKeyListener(this);
        frame.setLocationRelativeTo(null); // Centra la ventana en la pantalla

        // Prevenir que la ventana sea redimensionada
        frame.setResizable(false);

        // Prevenir que la ventana se ponga en pantalla completa
        frame.setExtendedState(JFrame.NORMAL); // Evita que la ventana esté maximizada al inicio

        frame.setVisible(true);

        timer = new javax.swing.Timer(20, this);
        timer.start();

        // Generar las primeras tuberías
        for (int i = 0; i < 5; i++) {
            addPipe(800 + i * 200);
        }
    }

    private String getValidPlayerName() {
        String playerName = JOptionPane.showInputDialog("¡Nuevo récord! Ingresa tu nombre:");

        if (playerName != null) {
            // Eliminar los espacios al inicio y al final
            playerName = playerName.trim();

            // Validar que el nombre no esté vacío
            if (playerName.isEmpty()) {
                JOptionPane.showMessageDialog(null, "El nombre no puede estar vacío.");
                return null; // Regresar null para que el nombre no sea válido
            }

            // Reemplazar caracteres no alfanuméricos y acentos
            playerName = playerName.replaceAll("[^\\w\\s]", ""); // Eliminar caracteres no alfanuméricos
            playerName = playerName.replaceAll("[áÁéÉíÍóÓúÚ]", ""); // Eliminar caracteres acentuados

            return playerName;
        }

        return null; // Si el usuario presiona Cancelar
    }

    private void loadTextures() {
        try {
            // Paquete de imagenes 1
            birdImage1 = new ImageIcon(getClass().getResource("/recursos/chinosyedi.png")).getImage();
            pipeImage1 = new ImageIcon(getClass().getResource("/recursos/pipe1.png")).getImage();
            backgroundImage1 = new ImageIcon(getClass().getResource("/recursos/fondo1.png")).getImage();

            // Paquete de imagenes 2
            birdImage2 = new ImageIcon(getClass().getResource("/recursos/bird2.png")).getImage();
            pipeImage2 = new ImageIcon(getClass().getResource("/recursos/pipe2.png")).getImage();
            backgroundImage2 = new ImageIcon(getClass().getResource("/recursos/fondo2.png")).getImage();

            // Paquete de imagenes 3
            birdImage3 = new ImageIcon(getClass().getResource("/recursos/bird3.png")).getImage();
            pipeImage3 = new ImageIcon(getClass().getResource("/recursos/pipe3.png")).getImage();
            backgroundImage3 = new ImageIcon(getClass().getResource("/recursos/fondo3.png")).getImage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addPipe(int x) {
        // Genera la altura aleatoria de la tubería superior
        int pipeHeight = (int) (Math.random() * 300) + 100;

        // Aseguramos que la nueva tubería esté separada de las anteriores
        // Verificar si la tubería nueva se superpone con alguna tubería existente
        boolean overlap = false;
        for (Pipe existingPipe : pipes) {
            // Verificar la distancia entre tuberias
            if (Math.abs(existingPipe.rect.x - x) < 200) {
                overlap = true;
                break;
            }
        }

        // Si la tubería se superpone, volvemos a intentar generarla
        if (overlap) {
            addPipe(x + 200); // Generamos una nueva tubería desplazada
        } else {
            // Añadir la tubería superior
            pipes.add(new Pipe(x, 0, PIPE_WIDTH, pipeHeight, true)); // Tubería superior
            // Añadir la tubería inferior
            pipes.add(new Pipe(x, pipeHeight + PIPE_GAP, PIPE_WIDTH, 600 - pipeHeight - PIPE_GAP, false));              
        }
    }

    private void resetGame() { //Resetear el juego a los valores de inicio
        birdY = 250;
        birdVelocity = 0;
        score = 0;
        gameOver = false;
        newRecord = false;
        pipes.clear();
        for (int i = 0; i < 5; i++) {
            addPipe(800 + i * 200);
        }
    }

    private void loadHighScores() { //Leemos el archivo de puntajes
        try (BufferedReader reader = new BufferedReader(new FileReader(SCORE_FILE))) {
            String line;
            highScores.clear();
            while ((line = reader.readLine()) != null) {
                highScores.add(line);
            }
        } catch (IOException e) {
            highScores.clear();
        }
    }

    private void saveHighScores() { //Guardamos los nuevos puntajes
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SCORE_FILE))) {
            for (String highScore : highScores) {
                writer.write(highScore);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateHighScores(String playerName, float newScore) { //Sustitución de puntajes
        highScores.add(playerName + " " + newScore);
        highScores.sort((a, b) -> {
            float scoreA = Float.parseFloat(a.split(" ")[1]);
            float scoreB = Float.parseFloat(b.split(" ")[1]);
            return Float.compare(scoreB, scoreA);
        });

        while (highScores.size() > 5) {
            highScores.remove(highScores.size() - 1);
        }

        saveHighScores();
    }

    private void checkAndUpdateHighScore() { //Validación de que un puntaje es mayor a otro
        boolean isNewRecord = false;

        // Verificar si el puntaje actual es mayor que alguno de los 5 mejores
        if (highScores.size() < 5) {
            // Si hay menos de 5 puntuaciones, agregar el nuevo récord directamente
            String playerName = getValidPlayerName();
            if (playerName != null) {
                updateHighScores(playerName, score); // Actualiza la lista de récords
                newRecord = true;
                isNewRecord = true;
            }
        } else {
            // Verificar si el puntaje supera alguno de los 5 mejores
            for (int i = 0; i < 5; i++) {
                float highScore = Float.parseFloat(highScores.get(i).split(" ")[1]);

                if (score > highScore) {
                    // Si el puntaje es mayor, lo guardamos como un nuevo récord
                    String playerName = getValidPlayerName();
                    if (playerName != null && !playerName.isEmpty()) {
                        updateHighScores(playerName, score); // Actualiza la lista de récords
                        newRecord = true;
                        isNewRecord = true;
                    }
                    break; // Salir del ciclo después de agregar el récord
                }
            }
        }

        if (!isNewRecord) {
            repaint();
        }

        // Si el puntaje actual no supera ningún récord, simplemente continúa sin hacer
        // cambios
        if (!isNewRecord) {
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) { //Cambio de escenas y dibujos
        super.paintComponent(g);

        // Fondo
        Image currentBackground = (textureSet == 0) ? backgroundImage1
                : (textureSet == 1) ? backgroundImage2 : backgroundImage3;
        g.drawImage(currentBackground, 0, 0, getWidth(), getHeight(), this);

        // Pájaro
        Image currentBird = (textureSet == 0) ? birdImage1 : (textureSet == 1) ? birdImage2 : birdImage3;
        g.drawImage(currentBird, 100, birdY, 30, 30, this);

        // Tuberías
        for (Pipe pipe : pipes) {
            Image currentPipe = (textureSet == 0) ? pipeImage1 : (textureSet == 1) ? pipeImage2 : pipeImage3;

            if (pipe.isTop) {
                // Rotar la tubería superior 180° y dibujarla
                Graphics2D g2d = (Graphics2D) g;
                g2d.rotate(Math.toRadians(180), pipe.rect.x + pipe.rect.width / 2, pipe.rect.y + pipe.rect.height / 2);
                g2d.drawImage(currentPipe, pipe.rect.x, pipe.rect.y, pipe.rect.width, pipe.rect.height, this);
                g2d.rotate(Math.toRadians(-180), pipe.rect.x + pipe.rect.width / 2, pipe.rect.y + pipe.rect.height / 2);
            } else {
                // Dibujar la tubería inferior normalmente
                g.drawImage(currentPipe, pipe.rect.x, pipe.rect.y, pipe.rect.width, pipe.rect.height, this);
            }
        }

        // Puntuación
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Puntuación: " + score, 10, 20);

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.setColor(Color.RED);
            String gameOverText = "¡Game Over!";
            int gameOverWidth = g.getFontMetrics().stringWidth(gameOverText);
            g.drawString(gameOverText, (getWidth() - gameOverWidth) / 2, getHeight() / 2 - 40);

            g.setFont(new Font("Arial", Font.BOLD, 20));
            String restartText = "Presiona ENTER para reiniciar";
            int restartWidth = g.getFontMetrics().stringWidth(restartText);
            g.drawString(restartText, (getWidth() - restartWidth) / 2, getHeight() / 2 + 20);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            String menuText = "Presiona ESC para ir al menú";
            int menuWidth = g.getFontMetrics().stringWidth(menuText);
            g.drawString(menuText, (getWidth() - menuWidth) / 2, getHeight() / 2 + 40);
        }

        if (newRecord) {
            g.setColor(Color.BLUE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            String newRecordText = "¡¡¡Nuevo récord!!!";
            int newRecordWidth = g.getFontMetrics().stringWidth(newRecordText);
            g.drawString(newRecordText, (getWidth() - newRecordWidth) / 2, getHeight() / 2 - 80);
        }

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Récords:", 600, 20);
        for (int i = 0; i < highScores.size(); i++) {
            g.drawString((i + 1) + ". " + highScores.get(i), 600, 50 + i * 20);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            birdVelocity += gravity;
            birdY += birdVelocity;

            for (int i = 0; i < pipes.size(); i++) {
                Pipe pipe = pipes.get(i); // Aquí obtenemos el objeto Pipe completo
                pipe.rect.x -= PIPE_SPEED; // Accedemos al rectángulo de la tubería y modificamos su posición

                if (pipe.rect.intersects(new Rectangle(100, birdY, 30, 30))) {
                    gameOver = true;
                    checkAndUpdateHighScore(); // Verifica si el puntaje es un nuevo récord
                    repaint();
                }

                if (pipe.rect.x + PIPE_WIDTH < 0) {
                    pipes.remove(i);
                    i--;
                }
            }

            if (pipes.size() < 10) {
                addPipe(800);
            }

            for (Pipe pipe : pipes) {
                if (pipe.rect.x == 100) {
                    score += 0.5;
                }
            }

            if (birdY < 0 || birdY > getHeight()) {
                gameOver = true;
                checkAndUpdateHighScore(); // Verifica si el puntaje es un nuevo récord
                repaint();
            }

            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_C) { // Usar C para cambiar entre los 3 conjuntos de texturas
            textureSet = (textureSet + 1) % 3; // Incrementar y usar módulo 3 para alternar entre 0, 1, 2
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !gameOver) {
            birdVelocity = -10;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER && gameOver) {
            resetGame();
        }

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && gameOver) {
            int choice = JOptionPane.showConfirmDialog(null, "¿Deseas regresar al menú principal?", "Confirmar",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                ((Window) SwingUtilities.getWindowAncestor(this)).dispose();
                showMainMenu();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void showMainMenu() {
        SwingUtilities.invokeLater(() -> {
            JFrame menuFrame = new JFrame("Flappy Bird - Menú");
            menuFrame.setSize(400, 300);
            menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            menuFrame.setLayout(new GridLayout(3, 1));
            // Prevenir que la ventana sea redimensionada
            menuFrame.setResizable(false);

            // Prevenir que la ventana se ponga en pantalla completa
            menuFrame.setExtendedState(JFrame.NORMAL); // Evita que la ventana esté maximizada al inicio

            JButton playButton = new JButton("Jugar");
            JButton highscoresButton = new JButton("Highscores");
            JButton exitButton = new JButton("Salir");

            playButton.addActionListener(e -> {
                new FlappyBird();
                menuFrame.dispose(); // Cerrar el menú después de iniciar el juego
            });

            highscoresButton.addActionListener(e -> {
                StringBuilder sb = new StringBuilder("Highscores:\n");
                try (BufferedReader reader = new BufferedReader(new FileReader("scores.txt"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                } catch (IOException ex) {
                    sb.append("No hay puntuaciones guardadas.");
                }
                JOptionPane.showMessageDialog(menuFrame, sb.toString());
            });

            exitButton.addActionListener(e -> System.exit(0));

            menuFrame.add(playButton);
            menuFrame.add(highscoresButton);
            menuFrame.add(exitButton);
            menuFrame.setLocationRelativeTo(null); // Centra la ventana del menú en la pantalla
            menuFrame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        showMainMenu(); // Llamamos al menú inicial
    }
}
