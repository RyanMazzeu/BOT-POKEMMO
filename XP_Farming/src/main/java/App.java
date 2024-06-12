import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class App implements NativeKeyListener {
    private static class Movement {
        private int key;
        private float time;

        public Movement(int key, float time) {
            this.key = key;
            this.time = time;
        }

        public int getKey() {
            return key;
        }

        public float getTime() {
            return time;
        }

        @Override
        public String toString() {
            String KEY = KeyEvent.getKeyText(key);
            return "Key: " + key  + " ("+ KEY + ')'+ ", Time: " + time + "s";
        }
    }

    private Movement movements[];
    private static Thread botThread;
    private static JButton botao;
    private Tesseract tesseract;
    private static final String FILE_PATH = "config.properties";
    private int coordenadas_pixel_pretoCP[] = new int[2];
    private int coordenadas_RUN[] = new int[2];
    private Rectangle coordenadas_BrancoPartida;
    private Rectangle coordenadas_NAME;
    private int Up;
    private int Down;
    private int Left;
    private int Right;
    private int TeclaA;
    private int TeclaB;
    private int Bike;
    private int Teleport;
    private int SweetScent;
    private int quantidadeQtySweetScent;
    private boolean KILL;
    private boolean CP;
    private Properties properties;
    private JLabel labelTeclaBike;
    private JLabel labelTeclaA;
    private JLabel labelTeclaB;
    private JLabel labelTeclaUp;
    private JLabel labelDown;
    private JLabel labelLeft;
    private JLabel labelRight;
    private JLabel labelSweetScent;
    private JLabel labelTeleport;
    private JLabel Avisos;
    private static JScrollPane scrollPane;
    private static CardLayout cardLayout;
    private static JPanel cardPanel;

    public App() {
        tesseract = new Tesseract();
        setupTesseract();
        properties = new Properties();
        readProperties();
    }
    private void setupTesseract() {
        try {
            String trainedDataPath = "/tessdata/eng.traineddata";

            InputStream tessData = getClass().getResourceAsStream(trainedDataPath);
            if (tessData == null) {
                throw new RuntimeException("Resource not found: " + trainedDataPath);
            }
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "tessdata");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            File trainedDataFile = new File(tempDir, "eng.traineddata");
            try (FileOutputStream out = new FileOutputStream(trainedDataFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = tessData.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            tesseract.setDatapath(tempDir.getAbsolutePath());
            tesseract.setLanguage("eng");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Tesseract data files", e);
        }
    }

    int[] StringtoArrayInt(String s) {
        String[] parts = s.split(",");
        int[] array = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            array[i] = Integer.parseInt(parts[i]);
        }
        return array;
    }

    String RectangletoString(Rectangle coordenadas) {
        return coordenadas.x + "," + coordenadas.y + "," + coordenadas.width + "," + coordenadas.height;
    }

    Rectangle stringToRectangle(String s) {
        String[] parts = s.split(",");
        return new Rectangle(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]));
    }

    private void readProperties() {
        try (FileInputStream input = new FileInputStream(FILE_PATH)) {
            properties.load(input);
            coordenadas_pixel_pretoCP = parseIntArray(properties.getProperty("coordenadas_pixel_pretoCP"));
            coordenadas_RUN = parseIntArray(properties.getProperty("coordenadas_RUN"));
            coordenadas_BrancoPartida = stringToRectangle(properties.getProperty("coordenadas_BrancoPartida"));
            coordenadas_NAME = stringToRectangle(properties.getProperty("coordenadas_NAME"));
            Up = Integer.parseInt(properties.getProperty("Up"));
            Down = Integer.parseInt(properties.getProperty("Down"));
            Left = Integer.parseInt(properties.getProperty("Left"));
            Right = Integer.parseInt(properties.getProperty("Right"));
            TeclaA = Integer.parseInt(properties.getProperty("TeclaA"));
            TeclaB = Integer.parseInt(properties.getProperty("TeclaB"));
            Bike = Integer.parseInt(properties.getProperty("Bike")); 
            Teleport = Integer.parseInt(properties.getProperty("Teleport"));
            SweetScent = Integer.parseInt(properties.getProperty("SweetScent"));
            quantidadeQtySweetScent = Integer.parseInt(properties.getProperty("quantidadeQtySweetScent"));
            KILL = Boolean.parseBoolean(properties.getProperty("KILL"));
            CP = Boolean.parseBoolean(properties.getProperty("CP"));
            String movementsStr = properties.getProperty("movements");
            if (movementsStr != null && !movementsStr.isEmpty()) {
                String[] movementsArray = movementsStr.split(";");
                movements = new Movement[movementsArray.length];
                for (int i = 0; i < movementsArray.length; i++) {
                    String[] parts = movementsArray[i].split(",");
                    int key = Integer.parseInt(parts[0]);
                    float time = Float.parseFloat(parts[1]);
                    movements[i] = new Movement(key, time);
                }
            } else {
                movements = new Movement[0];
            }

        } catch (IOException e) {
            
        }
    }

    private void saveProperties() {
        properties.setProperty("coordenadas_pixel_pretoCP", intArrayToString(coordenadas_pixel_pretoCP));
        properties.setProperty("coordenadas_RUN", intArrayToString(coordenadas_RUN));
        properties.setProperty("coordenadas_BrancoPartida", RectangletoString(coordenadas_BrancoPartida));
        properties.setProperty("coordenadas_NAME", RectangletoString(coordenadas_NAME));
        properties.setProperty("Up", String.valueOf(Up));
        properties.setProperty("Down", String.valueOf(Down));
        properties.setProperty("Left", String.valueOf(Left));
        properties.setProperty("Right", String.valueOf(Right));
        properties.setProperty("TeclaA", String.valueOf(TeclaA));
        properties.setProperty("TeclaB", String.valueOf(TeclaB));
        properties.setProperty("Bike", String.valueOf(Bike)); // Save as int
        properties.setProperty("Teleport", String.valueOf(Teleport));
        properties.setProperty("SweetScent", String.valueOf(SweetScent));
        properties.setProperty("quantidadeQtySweetScent", String.valueOf(quantidadeQtySweetScent));
        properties.setProperty("KILL", String.valueOf(KILL));
        properties.setProperty("CP", String.valueOf(CP));
        StringBuilder movementsStr = new StringBuilder();
        for (Movement movement : movements) {
            if (movementsStr.length() > 0) {
                movementsStr.append(";");
            }
            movementsStr.append(movement.getKey()).append(",").append(movement.getTime());
        }
        properties.setProperty("movements", movementsStr.toString());

        try (FileOutputStream output = new FileOutputStream(FILE_PATH)) {
            properties.store(output, null);
        } catch (IOException e) {
            
        }
    }
    public void configurarMovimentos() {
        
        JFrame frame = new JFrame("Movements Settings");
        try {
            Image icon = ImageIO.read(new File("data/icon.png"));
            frame.setIconImage(icon);
        } catch (IOException e) {
        }

        frame.setSize(300, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        DefaultListModel<Movement> listModel = new DefaultListModel<>();
        for (Movement movimento : movements) {
            listModel.addElement(movimento);
        }

        JList<Movement> movimentoList = new JList<>(listModel);
        frame.add(new JScrollPane(movimentoList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Movement");
        JButton removeButton = new JButton("Remove Movement");

        addButton.addActionListener(e -> {
            JFrame addFrame = new JFrame("Add Movement");
            addFrame.setSize(300, 150);
            addFrame.setLayout(new FlowLayout());

            JLabel keyLabel = new JLabel("Key:");
            JTextField keyField = new JTextField(10);
            keyField.setEditable(false);
            JButton keyButton = new JButton("Set Key");
            keyButton.addActionListener(ae -> {
                JFrame keyFrame = new JFrame("Set Key");
                keyFrame.setSize(300, 100);
                keyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                keyFrame.setLayout(new FlowLayout());

                JLabel instructionLabel = new JLabel("Press a key to set it.");
                keyFrame.add(instructionLabel);

                JTextField inputField = new JTextField(10);
                inputField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        keyField.setText(KeyEvent.getKeyText(e.getKeyCode()));
                        keyField.putClientProperty("keyCode", e.getKeyCode());
                        keyFrame.dispose();
                    }
                });
                keyFrame.add(inputField);

                keyFrame.setVisible(true);
            });

            JLabel timeLabel = new JLabel("Time (s):");
            JTextField timeField = new JTextField(10);

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(ae -> {
                try {
                    int key = (int) keyField.getClientProperty("keyCode");
                    float time = Float.parseFloat(timeField.getText());
                    Movement movimento = new Movement(key, time);
                    listModel.addElement(movimento);
                    Movement[] newMovements = new Movement[movements.length + 1];
                    System.arraycopy(movements, 0, newMovements, 0, movements.length);
                    newMovements[movements.length] = movimento;
                    movements = newMovements;
                    saveProperties();
                    addFrame.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(addFrame, "Not a Number!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            addFrame.add(keyLabel);
            addFrame.add(keyField);
            addFrame.add(keyButton);
            addFrame.add(timeLabel);
            addFrame.add(timeField);
            addFrame.add(saveButton);

            addFrame.setVisible(true);
        });

        removeButton.addActionListener(e -> {
            int selectedIndex = movimentoList.getSelectedIndex();
            if (selectedIndex != -1) {
                listModel.remove(selectedIndex);
                Movement[] newMovements = new Movement[movements.length - 1];
                for (int i = 0, j = 0; i < movements.length; i++) {
                    if (i != selectedIndex) {
                        newMovements[j++] = movements[i];
                    }
                }
                movements = newMovements;
                saveProperties();
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }
    private void createAndShowGUI() {
        JFrame frame = new JFrame("PBOT");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(190, 230);//colocar 180 e 240 para gerar o app
        frame.setResizable(true);
        frame.setAlwaysOnTop(true);

        try {
            Image icon = ImageIO.read(new File("data/icon.png"));
            frame.setIconImage(icon);
        } catch (IOException e) {
        }

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topPanel.setPreferredSize(new Dimension(250, 85));
        topPanel.setLayout(new BorderLayout());

        JLabel labelTexto = new JLabel("POKEBOT", JLabel.CENTER);
        labelTexto.setFont(new Font("Fixedsys", Font.BOLD, 15));
        labelTexto.setForeground(Color.WHITE);

        try {
            Image imagem = ImageIO.read(new File("data/background.png"));
            JLabel labelImagem = new JLabel(new ImageIcon(imagem));
            labelImagem.setHorizontalAlignment(SwingConstants.RIGHT);
            topPanel.add(labelImagem , BorderLayout.EAST);
        } catch (IOException e) {
            
        }

        topPanel.add(labelTexto, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel scrollablePanel = new JPanel();
        scrollablePanel.setLayout(new BoxLayout(scrollablePanel, BoxLayout.Y_AXIS));

        for (int i = 1; i <= 17; i++) {
            JPanel itemPanel = new JPanel();
            itemPanel.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
            itemPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JLabel label;
            switch (i) {
                case 1:
                    label = new JLabel("Bike", JLabel.LEFT);
                    break;
                case 2:
                    label = new JLabel("Key A", JLabel.LEFT);
                    break;
                case 3:
                    label = new JLabel("Key B", JLabel.LEFT);
                    break;
                case 4:
                    label = new JLabel("Key Up", JLabel.LEFT);
                    break;
                case 5:
                    label = new JLabel("Key Right", JLabel.LEFT);
                    break;
                case 6:
                    label = new JLabel("Key Down", JLabel.LEFT);
                    break;
                case 7:
                    label = new JLabel("Key Left", JLabel.LEFT);
                    break;
                case 8:
                    label = new JLabel("Key Sweet Scent", JLabel.LEFT);
                    break;
                case 9:
                    label = new JLabel("Key Teleport", JLabel.LEFT);
                    break;
                case 10:
                    label = new JLabel("Movements", JLabel.LEFT);
                    break;
                case 11:
                    label = new JLabel("Qty SweetScent (≥ 20)", JLabel.LEFT);
                    break;
                case 12:
                    label = new JLabel("Back to CP", JLabel.LEFT);
                    break;
                case 13:
                    label = new JLabel("Kill All Monsters", JLabel.LEFT);
                    break;
                case 14:
                    label = new JLabel("CP Black Pixel", JLabel.LEFT);
                    break;
                case 15:
                    label = new JLabel("Run Pixel", JLabel.LEFT);
                    break;
                case 16:
                    label = new JLabel("Match White", JLabel.LEFT);
                    break;
                case 17:
                    label = new JLabel("NAMES Coordinates", JLabel.LEFT);
                    break;
                default:
                    label = new JLabel("ERROR!", JLabel.LEFT);
            }

            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            itemPanel.add(label);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

            if (i == 1) {
                labelTeclaBike = new JLabel("Bike: " + KeyEvent.getKeyText(Bike));
                buttonPanel.add(labelTeclaBike);
                JButton bikeButton = new JButton("Set Bike");
                bikeButton.addActionListener(e -> SetandoBike());
                buttonPanel.add(bikeButton);
            }
            if (i == 2) {
                labelTeclaA = new JLabel("Key A: " + KeyEvent.getKeyText(TeclaA));
                buttonPanel.add(labelTeclaA);
                JButton teclaAButton = new JButton("Set Key A");
                teclaAButton.addActionListener(e -> SetandoTeclaA());
                buttonPanel.add(teclaAButton);
            }
            if (i == 3) {
                labelTeclaB = new JLabel("Key B: " + KeyEvent.getKeyText(TeclaB));
                buttonPanel.add(labelTeclaB);
                JButton teclaBButton = new JButton("Set Key B");
                teclaBButton.addActionListener(e -> SetandoTeclaB());
                buttonPanel.add(teclaBButton);
            }
            if (i == 4) {
                labelTeclaUp = new JLabel("Up: " + KeyEvent.getKeyText(Up));
                buttonPanel.add(labelTeclaUp);
                JButton upButton = new JButton("Set Key Up");
                upButton.addActionListener(e -> SetandoUp());
                buttonPanel.add(upButton);
            }
            if (i == 5) {
                labelRight = new JLabel("Right: " + KeyEvent.getKeyText(Right));
                buttonPanel.add(labelRight);
                JButton rightButton = new JButton("Set Key Right");
                rightButton.addActionListener(e -> SetandoRight());
                buttonPanel.add(rightButton);
            }
            if (i == 6) {
                labelDown = new JLabel("Down: " + KeyEvent.getKeyText(Down));
                buttonPanel.add(labelDown);
                JButton downButton = new JButton("Set Key Down");
                downButton.addActionListener(e -> SetandoDown());
                buttonPanel.add(downButton);
            }
            if (i == 7) {
                labelLeft = new JLabel("Left: " + KeyEvent.getKeyText(Left));
                buttonPanel.add(labelLeft);
                JButton leftButton = new JButton("Set Key Left");
                leftButton.addActionListener(e -> SetandoLeft());
                buttonPanel.add(leftButton);
            }
            if (i == 8) {
                labelSweetScent = new JLabel("SS: " + KeyEvent.getKeyText(SweetScent));
                buttonPanel.add(labelSweetScent);
                JButton SweetScentButton = new JButton("Set Key SS");
                SweetScentButton.addActionListener(e -> SetandoSweetScent());
                buttonPanel.add(SweetScentButton);
            }
            if (i == 9) {
                labelTeleport = new JLabel("TP: " + KeyEvent.getKeyText(Teleport));
                buttonPanel.add(labelTeleport);
                JButton teleportButton = new JButton("Set Teleport");
                teleportButton.addActionListener(e -> SetandoTeleport());
                buttonPanel.add(teleportButton);
            }
            if (i == 10) {
                JButton configurarMovimentosButton = new JButton("Movements Settings");
                configurarMovimentosButton.addActionListener(e -> configurarMovimentos());
                buttonPanel.add(configurarMovimentosButton);
                JButton verMovimentosButton = new JButton("See Movements");
                verMovimentosButton.addActionListener(e -> {
                    StringBuilder sb = new StringBuilder();
                    for (Movement movimento : movements) {
                        sb.append(movimento).append("\n");
                    }
                    JOptionPane.showMessageDialog(null, sb.toString(), "Movements", JOptionPane.INFORMATION_MESSAGE);
                });
            }
            if (i == 11) {
                JLabel labelQuantidadeQtySweetScent = new JLabel(quantidadeQtySweetScent + "");
                buttonPanel.add(labelQuantidadeQtySweetScent);
                JButton quantidadeQtySweetScentButton = new JButton("Set Qty");
                quantidadeQtySweetScentButton.addActionListener(e -> {
                    String input = JOptionPane.showInputDialog("Enter the quantity:");
                    if (input == null || input.isEmpty() || !input.matches("\\d+") || Integer.parseInt(input) < 20) {
                        return;
                    }
                    quantidadeQtySweetScent = Integer.parseInt(input);
                    labelQuantidadeQtySweetScent.setText(quantidadeQtySweetScent + "");
                    saveProperties();
                });
                buttonPanel.add(quantidadeQtySweetScentButton);
            }
            if(i==12){
                JLabel labelCP = new JLabel("" + CP);
                buttonPanel.add(labelCP);
                JButton CPButton = new JButton("CP");
                CPButton.addActionListener(e -> {
                    CP = !CP;
                    labelCP.setText("" + CP);
                    saveProperties();
                });
                buttonPanel.add(CPButton);
            }

            if (i==13){
                JLabel labelKILL = new JLabel("" + KILL);
                buttonPanel.add(labelKILL);
                JButton KILLButton = new JButton("KILL");
                KILLButton.addActionListener(e -> {
                    KILL = !KILL;
                    labelKILL.setText("" + KILL);
                    saveProperties();
                });
                buttonPanel.add(KILLButton);
            }
            if (i == 14) {
                JButton coordenadasPixelPretoCPButton = new JButton("🎯");
                coordenadasPixelPretoCPButton.addActionListener(e -> {
                    coordenadas_pixel_pretoCP = SetandoCoordenadasPixel("Black Pixel CP");
                    saveProperties();
                });
                buttonPanel.add(coordenadasPixelPretoCPButton);
                JButton MovercoordenadasPixelPretoCPButton = new JButton("🔎");
                MovercoordenadasPixelPretoCPButton.addActionListener(e -> {
                    try {
                        Robot robot = new Robot();
                        Point alvo = new Point(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1]);
                        int n = 0;
                        while (++n < 5) {
                            robot.mouseMove(alvo.x, alvo.y);
                        }
                    } catch (AWTException e1) {
                    }
                });
                buttonPanel.add(MovercoordenadasPixelPretoCPButton);
            }
            if (i == 15) {
                JButton coordenadasRUNButton = new JButton("🎯");
                coordenadasRUNButton.addActionListener(e -> {
                    coordenadas_RUN = SetandoCoordenadasPixel("Run Pixel");
                    saveProperties();
                });
                buttonPanel.add(coordenadasRUNButton);
                JButton MovercoordenadasRUNButton = new JButton("🔎");
                MovercoordenadasRUNButton.addActionListener(e -> {
                    try {
                        Robot robot = new Robot();
                        Point alvo = new Point(coordenadas_RUN[0], coordenadas_RUN[1]);
                        int n = 0;
                        while (++n < 5) {
                            robot.mouseMove(alvo.x, alvo.y);
                        }
                    } catch (AWTException e1) {
                }
                });
                buttonPanel.add(MovercoordenadasRUNButton);
            }

            if (i == 16) {
                JButton coordenadasBrancoPartidaButton = new JButton("🎯");
                coordenadasBrancoPartidaButton.addActionListener(e -> {
                    Rectangle input = SetandoCoordenadasBranco();
                    if (input == null) {
                        return;
                    }
                    coordenadas_BrancoPartida = new Rectangle(input.x, input.y + 42, input.width, input.height);
                    saveProperties();
                });
                buttonPanel.add(coordenadasBrancoPartidaButton);
                JButton MovercoordenadasBrancoPartidaButton = new JButton("🔎");
                MovercoordenadasBrancoPartidaButton.addActionListener(e -> {
                    AreaSelection.showArea(coordenadas_BrancoPartida);
                });
                buttonPanel.add(MovercoordenadasBrancoPartidaButton);
            }
            if (i == 17) {
                JButton coordenadasNAMEButton = new JButton("🎯");
                coordenadasNAMEButton.addActionListener(e -> {
                    Rectangle input = SetandoCoordenadasNAME();
                    if (input == null) {
                        return;
                    }
                    coordenadas_NAME = new Rectangle(input.x, input.y + 42, input.width, input.height);
                    saveProperties();
                });
                buttonPanel.add(coordenadasNAMEButton);
                JButton MoverCoordenadasNAMEButton = new JButton("🔎");
                MoverCoordenadasNAMEButton.addActionListener(e -> {
                    AreaSelection.showArea(coordenadas_NAME);
                });
                buttonPanel.add(MoverCoordenadasNAMEButton);
            }
 

            itemPanel.add(buttonPanel);
            scrollablePanel.add(itemPanel);
        }

        scrollPane = new JScrollPane(scrollablePanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        Avisos = new JLabel("", JLabel.CENTER);
        Avisos.setVerticalAlignment(JLabel.NORTH); // Alinhamento vertical
        Avisos.setFont(new Font("Fixedsys", Font.BOLD, 15));
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(scrollPane, "ScrollPane");
        cardPanel.add(Avisos, "Label");
        cardLayout.show(cardPanel, "ScrollPane");
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        // mainPanel.remove(Avisos);

        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(new App());

        JPanel bottomPanel = new JPanel();
        botao = new JButton("Start");
        botao.setBackground(new Color(0, 0, 0));
        botao.setForeground(Color.WHITE);
        botao.setFont(new Font("Fixedsys", Font.PLAIN, 16));
        botao.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (botThread == null || !botThread.isAlive()) {
                    startBot(botao);
                } else {
                    // Parar o bot
                    botao.setText("Start");
                    while (botThread.isAlive()) {
                        botThread.interrupt();
                    }
                    stopBot(botao);
                }

            }
        });

        bottomPanel.add(botao);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
    public boolean hasWaitPixel(Rectangle area){
        try {
            Robot robot = new Robot();
            for (int i = area.x; i < area.x + area.width; i++) {
                for (int j = area.y; j < area.y + area.height; j++) {
                    if (robot.getPixelColor(i, j).getRed() > 200
                            && robot.getPixelColor(i, j).getGreen() > 200
                            && robot.getPixelColor(i, j).getBlue() > 200) {
                        return true;
                    }
                }
            }
        } catch (AWTException e) {
            
    }
    return false;
    }


    public void startBot(JButton button) {
        
        button.setText("Stop [ESC]");

        try {
            if (scrollPane.isVisible()) {
                cardLayout.show(cardPanel, "Label");
            } else {
                cardLayout.show(cardPanel, "ScrollPane");
            }
        } catch (Exception e) {
            
        }
        botThread = new Thread(() -> {
            int QtySweetScent = quantidadeQtySweetScent;
            botloop: while (!Thread.currentThread().isInterrupted()) {
                // ######################################################################################################################################

                try {
                    try {
                        scrollPane.setVisible(false);
                        Robot robot = new Robot();
                        // Esconder o frame e iniciar o Robot enquanto o bot está rodando
                        try{
                            Avisos.setText("BOT Getting Started.");
                            Thread.sleep(300);
                            Avisos.setText("BOT Getting Started..");
                            Thread.sleep(300);
                            Avisos.setText("BOT Getting Started...");
                            Thread.sleep(300);
                            if(!Thread.currentThread().isInterrupted()){
                                robot.mouseMove(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1]);
                                robot.mouseMove(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1]);
                                robot.mousePress(MouseEvent.BUTTON1_DOWN_MASK);
                            }
                        }catch(InterruptedException e){
                            Thread.currentThread().interrupt();
                            break botloop;
                        }
                        
                        if(CP){
                            QtySweetScent = quantidadeQtySweetScent;
                            robot.keyPress(TeclaA);
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break botloop;
                            }
                            robot.keyPress(Down);
                            // #################################Healing Pokemons#####################################################################################################
                            while (!Thread.currentThread().isInterrupted() &&
                                    robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                            .getRed() < 5
                                    &&
                                    robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                            .getGreen() < 5
                                    &&
                                    robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                            .getBlue() < 5) {

                                Avisos.setText(
                                        "<html><body style='text-align: center;'>Healing Pokemons...<br><br><br><br><br></body></html>");
                            }
                            robot.keyRelease(TeclaA);
                            robot.keyRelease(Down);

                            // ###############################Going to the SPOT#######################################################################################################
                            try{
                                Thread.sleep(500);
                                Avisos.setText(
                                    "<html><body style='text-align: center;'>Going to the SPOT.<br><br><br><br><br></body></html>");
                                robot.keyPress(Bike);
                                Thread.sleep(500);
                                robot.keyRelease(Bike);
                            }catch(InterruptedException e){
                                Thread.currentThread().interrupt();
                                break botloop;
                            }
                            //Seguir todos os comandos guardados na variavel Movimentos
                            for (Movement movement : movements) {
                                if (Thread.currentThread().isInterrupted()) {
                                    break botloop;
                                }
                                robot.keyPress(movement.getKey());
                                try {
                                    Thread.sleep((long) (movement.getTime() * 1000));
                                } catch (InterruptedException e) {
                                    robot.keyRelease(movement.getKey());
                                    // Handle the InterruptedException appropriately
                                    Thread.currentThread().interrupt();
                                    break botloop;
                                }
                                robot.keyRelease(movement.getKey());
                            }

                            // ######################################################################################################################################
                        }
                            int error = 0;
                            externloop: while (QtySweetScent >= 5 && !Thread.currentThread().isInterrupted()) {
                                while (!Thread.currentThread().isInterrupted() && (robot
                                        .getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                        .getRed() > 5
                                        ||
                                        robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                                .getGreen() > 5
                                        ||
                                        robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                                .getBlue() > 5)) {
                                    robot.keyPress(TeclaA);
                                    try{
                                        Thread.sleep(500);
                                        robot.keyRelease(TeclaA);
                                        Thread.sleep(500);
                                        Avisos.setText(
                                                "<html><body style='text-align: center;'>Using Sweet Scent..<br><br><br><br><br></body></html>");
                                        robot.keyPress(SweetScent);
                                        Thread.sleep(500);
                                        robot.keyRelease(SweetScent);
                                        Thread.sleep(500);
                                        Avisos.setText(
                                                "<html><body style='text-align: center;'>Using Sweet Scent...<br><br><br><br><br></body></html>");
                                    }catch(InterruptedException e){
                                        Thread.currentThread().interrupt();
                                        break botloop;
                                    }
                                    error++;
                                    if(error > 15 && (robot
                                    .getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                    .getRed() > 5
                                    ||
                                    robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                            .getGreen() > 5
                                    ||
                                    robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                            .getBlue() > 5)){
                                        break externloop;
                                    }
                                }
    // ######################################################################################################################################
                                if(error<15 && !Thread.currentThread().isInterrupted()){
                                Avisos.setText(
                                        "<html><body style='text-align: center;'>Battling<br><br><br><br><br></body></html>");
                                try{
                                    Thread.sleep(500);

                                    Avisos.setText(
                                                "<html><body style='text-align1: center;'>Battling<br>Waiting for Match to Normalize<br><br><br><br></body></html>");
                                    Thread.sleep(500);
                                }catch(InterruptedException e){
                                    Thread.currentThread().interrupt();
                                    break botloop;
                                }
                                outerloop: while(!Thread.currentThread().isInterrupted()){
                                    for (int x = coordenadas_BrancoPartida.x; x < coordenadas_BrancoPartida.x + coordenadas_BrancoPartida.width; x++) {
                                        for (int y = coordenadas_BrancoPartida.y; y < coordenadas_BrancoPartida.y + coordenadas_BrancoPartida.height; y++) {
                                            if (!Thread.currentThread().isInterrupted() &&
                                                    robot.getPixelColor(x, y).getRed() > 240 &&
                                                    robot.getPixelColor(x, y).getGreen() > 240 &&
                                                    robot.getPixelColor(x, y).getBlue() > 240) {
                                                        break outerloop;
                                            }
                                        }
                                    }
                                }
                                String nomePokemon = "Unidentified";
                                while(nomePokemon.equals("Unidentified") && !Thread.currentThread().isInterrupted()){
                                    try{
                                        Thread.sleep(10);
                                        Avisos.setText(
                                            "<html><body style='text-align: center;'>Battling<br>reading Pokemon Name<br><br><br><br></body></html>");
                                        try {
                                            String resulttesseract = "";
                                            if(!Thread.currentThread().isInterrupted()){
                                                BufferedImage screenShot = robot.createScreenCapture(coordenadas_NAME);
                                                resulttesseract = tesseract.doOCR(screenShot);
                                            }
                                            // Passar tudo para minusculo e remover numeros e caracteres especiais
                                            if (!Thread.currentThread().isInterrupted() && !resulttesseract.isEmpty()) {
                                                resulttesseract = resulttesseract.toLowerCase().replaceAll("[^a-z]", "");
                                                if (resulttesseract.length() > 4) {
                                                    nomePokemon = resulttesseract;
                                                }
                                            }
                                        } catch (TesseractException e) {
                                            Thread.currentThread().interrupt();
                                            break botloop;
                                        }
                                    }catch(InterruptedException e){
                                        Thread.currentThread().interrupt();
                                        break botloop;
                                    }
                                }
                                if (!Thread.currentThread().isInterrupted()){
                                    try{
                                        robot.keyPress(Up);
                                        Thread.sleep(500);
                                        robot.keyRelease(Up);
                                        Thread.sleep(500);
                                        if(nomePokemon.length() > 15){
                                            String nome1 = nomePokemon.substring(0, nomePokemon.length()/3);
                                            String nome2 = nomePokemon.substring(nomePokemon.length()/3, 2*nomePokemon.length()/3);
                                            String nome3 = nomePokemon.replace(nome1, "").replace(nome2, "");
                                            nome3 = nome3.substring(0, 15);
                                            Avisos.setText("<html><body style='text-align: center;'>"+nome1+"<br>"+nome2+"<br>"+nome3+"<br><br></body></html>");
                                        }else{
                                            Avisos.setText("<html><body style='text-align: center;'>"+nomePokemon+"<br><br><br><br></body></html>");
                                        }
                                        
                                        Thread.sleep(500);
                                    }catch(InterruptedException e){
                                        Thread.currentThread().interrupt();
                                        break botloop;
                                    }
                                }

                                if (nomePokemon.contains("shin") || nomePokemon.contains("hiny")) {
                                    Avisos.setText(
                                            "<html><body style='text-align: center;'>Shiny Found!<br>Stopping the Bot.<br><br><br></body></html>");
                                    robot.keyRelease(TeclaA);
                                    robot.keyRelease(Down);
                                    robot.keyRelease(Bike);
                                    robot.keyRelease(SweetScent);
                                    robot.keyRelease(TeclaB);
                                    robot.keyRelease(Up);
                                    robot.keyRelease(Left);
                                    robot.keyRelease(Right);
                                    robot.keyRelease(Teleport);
                            
                                    stopBot(button);
                                    while(!Thread.currentThread().isInterrupted()){
                                        Thread.sleep(10000);
                                    }
                                    while (botThread.isAlive()) {
                                        botThread.interrupt();
                                    }
                                    break botloop;
                                }else{
                                    if(!Thread.currentThread().isInterrupted()){
                                        QtySweetScent -= 5;
                                        if(KILL){
                                            while(!Thread.currentThread().isInterrupted() &&
                                            robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                                    .getRed() < 5
                                            &&
                                            robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                                    .getGreen() < 5
                                            &&
                                            robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                                    .getBlue() < 5){
                                                        try{
                                                            robot.keyPress(TeclaA);
                                                            Thread.sleep(500);
                                                            robot.keyRelease(TeclaA);
                                                            Thread.sleep(500);
                                                        }catch(InterruptedException e){
                                                            Thread.currentThread().interrupt();
                                                            break botloop;
                                                        }
                                            }
                                        }else{
                                            while(!Thread.currentThread().isInterrupted() &&
                                            robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                                    .getRed() < 5
                                            &&
                                            robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                                    .getGreen() < 5
                                            &&
                                            robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                                    .getBlue() < 5){
                                                    robot.mouseMove(coordenadas_RUN[0], coordenadas_RUN[1]);
                                                    robot.mouseMove(coordenadas_RUN[0], coordenadas_RUN[1]);
                                                    robot.mouseMove(coordenadas_RUN[0], coordenadas_RUN[1]);
                                                    try{
                                                        Thread.sleep(500);
                                                        robot.mousePress(MouseEvent.BUTTON1_DOWN_MASK);
                                                        Thread.sleep(500);
                                                        robot.mouseRelease(MouseEvent.BUTTON1_DOWN_MASK);
                                                        robot.mousePress(MouseEvent.BUTTON1_DOWN_MASK);
                                                        Thread.sleep(500);
                                                        robot.mouseRelease(MouseEvent.BUTTON1_DOWN_MASK);
                                                    }catch(InterruptedException e){
                                                        Thread.currentThread().interrupt();
                                                        break botloop;
                                                    }
                                            }
                                        }
                                    }
                                }
                            }
                        }
    // ######################################################################################################################################
    // ######################################################################################################################################
    // ######################################################################################################################################
                        if (CP){
                            if(!Thread.currentThread().isInterrupted()){
                                Avisos.setText(
                                    "<html><body style='text-align: center;'>No more PP.<br>Going back to the CP.<br><br><br><br></body></html>");
                            }
                            while (!Thread.currentThread().isInterrupted() &&
                                    robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                            .getRed() > 5
                                    ||
                                    robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                            .getGreen() > 5
                                    ||
                                    robot.getPixelColor(coordenadas_pixel_pretoCP[0], coordenadas_pixel_pretoCP[1])
                                            .getBlue() > 5) {
                                try{
                                    robot.keyPress(Teleport);
                                    Thread.sleep(500);
                                    robot.keyRelease(Teleport);
                                    Thread.sleep(500);
                                }catch(InterruptedException e){
                                    Thread.currentThread().interrupt();
                                    break botloop;
                                }
                            }
                        }
                    } catch (AWTException e) {

                    }
                } catch (InterruptedException e) {


                    Thread.currentThread().interrupt();
                    stopBot(button);
                } // try
                // ######################################################################################################################################
            } // while (!Thread.currentThread().isInterrupted())
        scrollPane.setVisible(true);
        });
        botThread.start();
    }

    public static void stopBot(JButton button) {
        
        if (botThread != null && botThread.isAlive()) {

            botThread.interrupt();
            try {
                botThread.join(); // Wait for the thread to finish
            } catch (InterruptedException e) {
                
            }
            try {
                if (scrollPane.isVisible()) {
                    cardLayout.show(cardPanel, "Label");
                } else {
                    cardLayout.show(cardPanel, "ScrollPane");
                }
            } catch (Exception e) {
                
            }
            button.setText("Start");
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
            botao.setText("Start");
            while (botThread.isAlive()) {
                botThread.interrupt();
            }
            stopBot(botao);
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // Implementação do método nativeKeyReleased
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Implementação do método nativeKeyTyped
    }

    public void SetandoBike() {
        JFrame frame = new JFrame("Set Bike");
        frame.setSize(300, 100);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel instructionLabel = new JLabel("Press any key to set to 'Bike':");
        frame.add(instructionLabel);

        JTextField inputField = new JTextField(10);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Bike = e.getKeyCode();
                labelTeclaBike.setText("Bike: " + KeyEvent.getKeyText(Bike));
                frame.dispose();
                saveProperties();
            }
        });
        frame.add(inputField);

        frame.setVisible(true);
    }

    public void SetandoTeclaA() {
        JFrame frame = new JFrame("Set Key B");
        frame.setSize(300, 100);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel instructionLabel = new JLabel("Press any key to set to 'Key B':");
        frame.add(instructionLabel);

        JTextField inputField = new JTextField(10);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                TeclaA = e.getKeyCode();
                labelTeclaA.setText("Key B: " + KeyEvent.getKeyText(TeclaA));
                frame.dispose();
                saveProperties();
            }
        });
        frame.add(inputField);

        frame.setVisible(true);
    }

    public void SetandoTeclaB() {
        JFrame frame = new JFrame("Set Key B");
        frame.setSize(300, 100);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel instructionLabel = new JLabel("Press any key to set to 'Key B':");
        frame.add(instructionLabel);

        JTextField inputField = new JTextField(10);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                TeclaB = e.getKeyCode();
                labelTeclaB.setText("Key B: " + KeyEvent.getKeyText(TeclaB));
                frame.dispose();
                saveProperties();
            }
        });
        frame.add(inputField);

        frame.setVisible(true);
    }

    public void SetandoUp() {
        JFrame frame = new JFrame("Set Key Up");
        frame.setSize(300, 100);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel instructionLabel = new JLabel("Press any key to set to 'Key Up':");
        frame.add(instructionLabel);

        JTextField inputField = new JTextField(10);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Up = e.getKeyCode();
                labelTeclaUp.setText("Up: " + KeyEvent.getKeyText(Up));
                frame.dispose();
                saveProperties();
            }
        });
        frame.add(inputField);

        frame.setVisible(true);
    }

    public void SetandoRight() {
        JFrame frame = new JFrame("Set Key Right");
        frame.setSize(300, 100);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel instructionLabel = new JLabel("Press any key to set to 'Key Right':");
        frame.add(instructionLabel);

        JTextField inputField = new JTextField(10);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Right = e.getKeyCode();
                labelRight.setText("Right: " + KeyEvent.getKeyText(Right));
                frame.dispose();
                saveProperties();
            }
        });
        frame.add(inputField);

        frame.setVisible(true);
    }

    public void SetandoDown() {
        JFrame frame = new JFrame("Set Key Down");
        frame.setSize(300, 100);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel instructionLabel = new JLabel("Press any key to set to 'Key Down':");
        frame.add(instructionLabel);

        JTextField inputField = new JTextField(10);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Down = e.getKeyCode();
                labelDown.setText("Down: " + KeyEvent.getKeyText(Down));
                frame.dispose();
                saveProperties();
            }
        });
        frame.add(inputField);

        frame.setVisible(true);
    }

    public void SetandoLeft() {
        JFrame frame = new JFrame("Set Key Left");
        frame.setSize(300, 100);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel instructionLabel = new JLabel("Press any key to set to 'Key Left':");
        frame.add(instructionLabel);

        JTextField inputField = new JTextField(10);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Left = e.getKeyCode();
                labelLeft.setText("Left: " + KeyEvent.getKeyText(Left));
                frame.dispose();
                saveProperties();
            }
        });
        frame.add(inputField);

        frame.setVisible(true);
    }

    public void SetandoSweetScent() {
        JFrame frame = new JFrame("Set Sweet Scent");
        frame.setSize(300, 100);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel instructionLabel = new JLabel("Press any key to set to 'Sweet Scent':");
        frame.add(instructionLabel);

        JTextField inputField = new JTextField(10);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                SweetScent = e.getKeyCode();
                labelSweetScent.setText("SweetScent: " + KeyEvent.getKeyText(SweetScent));
                frame.dispose();
                saveProperties();
            }
        });
        frame.add(inputField);

        frame.setVisible(true);
    }

    public void SetandoTeleport() {
        JFrame frame = new JFrame("Set Teleport");
        frame.setSize(300, 100);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel instructionLabel = new JLabel("Press any key to set to 'Teleport':");
        frame.add(instructionLabel);

        JTextField inputField = new JTextField(10);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Teleport = e.getKeyCode();
                labelTeleport.setText("Teleport: " + KeyEvent.getKeyText(Teleport));
                frame.dispose();
                saveProperties();
            }
        });
        frame.add(inputField);

        frame.setVisible(true);
    }

    public int[] SetandoCoordenadasPixel(String Name) {
        int aux[] = new int[2];
        Point singleClick = AreaSelection.selectSingleClick("Select"+ Name + "Coordinates");
        aux[0] = singleClick.x;
        aux[1] = singleClick.y + 42;
        return aux;
    }


    public Rectangle SetandoCoordenadasNAME() {
        Rectangle selectedArea = AreaSelection.selectArea("Select NAMES Coordinates");
        return selectedArea;
    }

    public Rectangle SetandoCoordenadasBranco(){
        Rectangle selectedArea = AreaSelection.selectArea("Select White Pixel Coordinates");
        return selectedArea;
    }


    private int[] parseIntArray(String s) {
        String[] parts = s.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i]);
        }
        return result;
    }

    private String intArrayToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        App gui = new App();
        SwingUtilities.invokeLater(() -> gui.createAndShowGUI());
        gui.saveProperties();
    }
}

// * * * * * * * * * * * * * * * * * *
// *OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO

class AreaSelection extends JDialog {
    private int startX, startY, endX, endY;
    private Rectangle selectionRectangle;
    private JPanel drawingPanel;

    public AreaSelection(Frame owner, String title) {
        super(owner, title, true);
        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (selectionRectangle != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(Color.RED);
                    g2d.draw(selectionRectangle);
                }
            }
        };
        drawingPanel.setBackground(Color.BLACK);
        drawingPanel.setOpaque(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        drawingPanel.setPreferredSize(screenSize);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setBackground(new Color(0, 0, 0, 150)); // Semi-transparent black background
        titleLabel.setOpaque(true);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(titleLabel, BorderLayout.NORTH);
        getContentPane().add(drawingPanel, BorderLayout.CENTER);

        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
                selectionRectangle = new Rectangle(startX, startY, 0, 0);
                drawingPanel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                endX = e.getX();
                endY = e.getY();
                setVisible(false);
            }
        });

        drawingPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                endX = e.getX();
                endY = e.getY();
                selectionRectangle = new Rectangle(
                        Math.min(startX, endX),
                        Math.min(startY, endY),
                        Math.abs(endX - startX),
                        Math.abs(endY - startY));
                drawingPanel.repaint();
            }
        });

        setUndecorated(true);
        setAlwaysOnTop(true);
        setOpacity(0.85f); // Slightly less transparent for better visibility
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public Rectangle getSelectedArea() {
        return selectionRectangle;
    }

    public static Rectangle selectArea(String title) {
        AreaSelection areaSelection = new AreaSelection(null, title);
        areaSelection.setVisible(true);
        return areaSelection.getSelectedArea();
    }

    public static Point selectSingleClick(String title) {
        final Point[] clickPoint = { null };

        JDialog clickDialog = new JDialog((Frame) null, title, true);
        clickDialog.setUndecorated(true);
        clickDialog.setAlwaysOnTop(true);
        clickDialog.setOpacity(0.85f); // Slightly less transparent for better visibility

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JPanel clickPanel = new JPanel();
        clickPanel.setPreferredSize(screenSize);
        clickPanel.setBackground(Color.BLACK);
        clickPanel.setOpaque(true);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setBackground(new Color(0, 0, 0, 150)); // Semi-transparent black background
        titleLabel.setOpaque(true);

        clickDialog.getContentPane().setLayout(new BorderLayout());
        clickDialog.getContentPane().add(titleLabel, BorderLayout.NORTH);
        clickDialog.getContentPane().add(clickPanel, BorderLayout.CENTER);

        clickPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clickPoint[0] = e.getPoint();
                clickDialog.setVisible(false);
            }
        });

        clickDialog.pack();
        clickDialog.setLocationRelativeTo(null);
        clickDialog.setVisible(true);

        return clickPoint[0];
    }

    public static void showArea(Rectangle area) {
        JDialog areaDialog = new JDialog((Frame) null, "Selected Area", true);
        areaDialog.setUndecorated(true);
        areaDialog.setAlwaysOnTop(true);
        areaDialog.setOpacity(0.85f); // Slightly less transparent for better visibility

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JPanel areaPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black background
                g2d.fillRect(0, 0, screenSize.width, screenSize.height);
                g2d.setColor(Color.WHITE);
                g2d.drawRect(area.x, area.y, area.width, area.height);
            }
        };
        areaPanel.setPreferredSize(screenSize);

        areaDialog.getContentPane().setLayout(new BorderLayout());
        areaDialog.getContentPane().add(areaPanel, BorderLayout.CENTER);

        areaPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                areaDialog.setVisible(false);
            }
        });

        areaDialog.pack();
        areaDialog.setLocationRelativeTo(null);
        areaDialog.setVisible(true);
    }
}