
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author michel
 */
public class Interface extends javax.swing.JFrame {

    private static final String Buzzer_OFF = "c";
    private static String Buzzer_ON = "b";
    private static final String marche = "0";   // ordre de lancement du test
    private static final String arret = "1";    // ordre d'arrêt du test
    private static final String pause = "2";    // ordre de mettre le test en pause
    private static final String RAZ1 = "raz1";
    private static final String RAZ2 = "raz2";
    private static final String RAZ3 = "raz3";

    private boolean buzzer = false;
    private boolean test_off = false;
    private boolean test_on = false;
    private boolean test_pause = false;
    private boolean arret_valide = false;

    //private OutputStream output = null;
    //SerialPort serialPort;
    //Variables de connexion
    SerialPort portComm;
    OutputStream outputStream;

    private BufferedReader input;

    private String PORT = "COM1";
    private String inputLine;

    private static final int TIMEOUT = 2000; //Milisegundos

    private static final int DATA_RATE = 9600;

    // Variables pour sauvegarde fichier des résultats
    private String nomFichier;
    private boolean nomFichierInit = false;
    private FileWriter fluxSortie;
    private BufferedWriter Sortie;
    private File Repertoire;
    private String ligneEnCours;

    // Variables de séquence de test
    private boolean[] echantiilons = {false, false, false};
    private long[] totaux = {0, 0, 0};
    private boolean[] erreurs = {false, false, false};
    private boolean[] actifs = {false, false, false};
    private String config = null;
    private SerialPortEvent SerialPortEvent;
    private boolean acquittement;
    private long compteur1 = 0;
    private long compteur2 = 0;
    private long compteur3 = 0;
    private int interval = 1;
    private int nbr_seqs = 0;

    private URL imageVertOff = getClass().getClassLoader().getResource("images/vert_off.png");
    Image imageOff = Toolkit.getDefaultToolkit().getImage(imageVertOff);
    private URL imageVertOn = getClass().getClassLoader().getResource("images/vert_on.png");
    Image imageOn = Toolkit.getDefaultToolkit().getImage(imageVertOn);
    private URL imageRougeOff = getClass().getClassLoader().getResource("images/rouge_off.png");
    Image imageRoff = Toolkit.getDefaultToolkit().getImage(imageRougeOff);

    /**
     * Creates new form Interface
     */
    public Interface() {
        initComponents();
        //connect();

        setTitle("INTERFACE DE TEST POUR SERRURES DX200I");
        jLabel3.setIcon(new ImageIcon(imageOff));
        //jLabel3.setIcon(new ImageIcon(imageVertOff));
        btnStart.setText("START");
        btnPause.setVisible(false);
        jLabel6.setVisible(false);
        infoText.setText("Configurez le test avant lancement et demandez une connexion");
        jLabel8.setText("0");
        jLabel2.setText("0");
        jLabel4.setText("0");
        statutsEch1.setSelected(false);
        statutEch2.setSelected(false);
        statutEch3.setSelected(false);
        jTextField4.setVisible(false);
        btnValidationConfig.setVisible(false);
        jTextField5.setVisible(false);
        jTextField5.setVisible(false);
        bntAnnulationConfig.setVisible(false);
        btnValidationConfig.setVisible(false);
        // jComboBox1.removeAllItems();

        SerialPort[] ports = SerialPort.getCommPorts();
        portSelection.removeAllItems();
        for (SerialPort p : ports) {

            portSelection.addItem(p.getSystemPortName());
        }

    }

    public void connect() {

        /*
        CommPortIdentifier portID = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        while (portEnum.hasMoreElements()) {
            CommPortIdentifier actualPortID = (CommPortIdentifier) portEnum.nextElement();
            if (PORT.equals(actualPortID.getName())) {
                portID = actualPortID;
                break;
            }
        }

        if (portID == null) {
            montrerError("Connexion impossible. Vérifier les paramètres de connexion");

        }

        try {
            serialPort = (SerialPort) portID.open(this.getClass().getName(), TIMEOUT);
            //Paramètre port série

            serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();

            // input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            jTextArea1.setText("Connexion réussie!");

        } catch (Exception e) {
            montrerError(e.getMessage());
            //System.exit(ERROR);

        }
         */
        System.out.println("Terminal.btnConnexionActionPerformed()");
        try {
            SerialPort[] ports = SerialPort.getCommPorts();
            portComm = ports[portSelection.getSelectedIndex()];
            portComm.setBaudRate(9600);
            portComm.setNumDataBits(8);
            portComm.setParity(0);
            portComm.setNumStopBits(1);
            portComm.openPort();

            if (portComm.isOpen()) {

                infoText.setForeground(Color.BLUE);
                infoText.setText("Connexion réussie");

            } else {

                infoText.setForeground(Color.red);
                infoText.setText("Tentative de connexion échouée");

            }

        } catch (Exception e) {

            infoText.setForeground(Color.red);
            infoText.setText("Connexion échouée");

        }

        portComm.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    return;
                }

                /*
                byte[] newData = new byte[portComm.bytesAvailable()];
                int numRead = portComm.readBytes(newData, newData.length);
                System.out.println("Read " + numRead + " bytes:" + new String(newData, StandardCharsets.UTF_8));
                texteReception.setText(new String(newData, StandardCharsets.UTF_8));
                 */
                try {

                    System.out.println("Réception message");
                    inputLine = input.readLine();
                    System.out.println(inputLine);
                    infoText.setText(inputLine);
                    boolean isCompteur;
                    boolean isActifs;
                    boolean isArret;

                    isCompteur = inputLine.startsWith("@TOTAL");
                    isActifs = inputLine.startsWith("@ACTIFS:");
                    isArret = inputLine.startsWith("@ARRET");
                    if (isCompteur) {

                        String[] recept = inputLine.split(" ");
                        String compteur = recept[3];
                        String ech = recept[2];
                        System.out.println("num echantillon: " + recept[2]);
                        System.out.println("Compteur: " + recept[3]);

                        if (ech.equals("#1:")) {

                            jLabel8.setText(compteur);
                            compteur1 = Long.parseLong(compteur);

                        }

                        if (ech.equals("#2:")) {

                            jLabel2.setText(compteur);
                            compteur2 = Long.parseLong(compteur);
                        }

                        if (ech.equals("#3:")) {

                            jLabel4.setText(compteur);
                            compteur3 = Long.parseLong(compteur);

                        }

                    }

                    if (inputLine.equals("@SEQ")) {

                        nbr_seqs++;

                        if (nbr_seqs == interval) {

                            gestionEnregistrement();

                        }

                    }

                    if (isActifs) {

                        String[] recept = inputLine.split(":");

                        if (recept[1].equals("0")) {

                            actifs[0] = false;
                            if (statutsEch1.isSelected()) {
                                jLabel8.setForeground(Color.RED);
                                jLabel8.setText(String.valueOf(compteur1));
                            }

                        } else {

                            actifs[0] = true;
                        }

                        if (recept[2].equals("0")) {

                            actifs[1] = false;
                            if (statutEch2.isSelected()) {
                                jLabel2.setForeground(Color.RED);
                                jLabel2.setText(String.valueOf(compteur2));
                            }

                        } else {

                            actifs[1] = true;
                        }

                        if (recept[3].equals("0")) {

                            actifs[2] = false;
                            if (statutEch3.isSelected()) {
                                jLabel4.setForeground(Color.RED);
                                jLabel4.setText(String.valueOf(compteur3));
                            }

                        } else {

                            actifs[2] = true;
                        }

                    }   // fin  if (isActifs)

                    if (isArret) {

                        arret_valide = true;
                    }

                } catch (Exception e) {   // Traitement des exceptions

                    System.err.println(e.toString());
                }
            }
        });
    }

    private void envoyerData(String dataToSend) {

        /*
        try {
            output.write(data.getBytes());
        } catch (Exception e) {
            montrerError("Connexion impossible!");
            System.exit(ERROR);
        }
         */
        outputStream = portComm.getOutputStream();
        //  String dataToSend = textEmission.getText();
        try {

            outputStream.write(dataToSend.getBytes());

        } catch (IOException e) {

            infoText.setForeground(Color.red);
            infoText.setText("Erreur de transmission");

        }

    }

    public synchronized void close() {

        if (portComm != null) {
            portComm.closePort();

        }
    }

    public void montrerError(String message) {
        JOptionPane.showMessageDialog(this, message, "Connexion impossible!", JOptionPane.ERROR_MESSAGE);
    }

    public void montrerErrorConfig(String message) {
        JOptionPane.showMessageDialog(this, message, "Aucune configuration!", JOptionPane.ERROR_MESSAGE);
    }

    public void montrerErrorNom(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur nom de fichier!", JOptionPane.ERROR_MESSAGE);
    }

    public void montrerErrorInterval(String message) {
        JOptionPane.showMessageDialog(this, message, "Valeur d'interval incorrecte", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton4 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        SelectFichier = new javax.swing.JFileChooser();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        infoText = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        bntConnexion = new javax.swing.JButton();
        portSelection = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        statutsEch1 = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        statutEch3 = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        statutEch2 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        btnStart = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        btnPause = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jTextField4 = new javax.swing.JTextField();
        btnValidationConfig = new javax.swing.JButton();
        jTextField5 = new javax.swing.JTextField();
        bntAnnulationConfig = new javax.swing.JButton();

        jButton4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton4.setText("STOP");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel5.setMaximumSize(new java.awt.Dimension(50, 50));
        jLabel5.setMinimumSize(new java.awt.Dimension(50, 50));

        jLabel7.setText("jLabel2");

        SelectFichier.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        SelectFichier.setSelectedFiles(null);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
        jLabel1.setText("TEST DX200I");

        infoText.setColumns(20);
        infoText.setRows(5);
        jScrollPane1.setViewportView(infoText);

        bntConnexion.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        bntConnexion.setText("Connexion");
        bntConnexion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bntConnexionActionPerformed(evt);
            }
        });

        portSelection.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        portSelection.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM10", "COM11", "COM12", "COM13", "COM14", "COM15", "COM16", "COM17", "COM18", "COM19", "COM20", " " }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(portSelection, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bntConnexion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(27, 27, 27))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .addComponent(portSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(bntConnexion)
                .addGap(33, 33, 33))
        );

        statutsEch1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        statutsEch1.setText("Echantillon1");
        statutsEch1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statutsEch1ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(51, 51, 255));
        jLabel2.setText("jLabel2");

        statutEch3.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        statutEch3.setText("Echantillon3");
        statutEch3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statutEch3ActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(51, 51, 255));
        jLabel4.setText("jLabel2");

        statutEch2.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        statutEch2.setSelected(true);
        statutEch2.setText("Echantillon2");
        statutEch2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statutEch2ActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 0, 51));
        jButton1.setText("Reset");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 0, 51));
        jButton3.setText("Reset");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton7.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton7.setForeground(new java.awt.Color(255, 0, 51));
        jButton7.setText("Reset");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton9.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton9.setForeground(new java.awt.Color(255, 0, 0));
        jButton9.setText("Set");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton10.setForeground(new java.awt.Color(255, 0, 0));
        jButton10.setText("Set");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton11.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton11.setForeground(new java.awt.Color(255, 0, 0));
        jButton11.setText("Set");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(51, 51, 255));
        jLabel8.setText("jLabel2");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(statutEch3, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                            .addComponent(statutEch2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(42, 42, 42)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton7, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField3)
                            .addComponent(jTextField2)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statutsEch1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(jButton1)
                        .addGap(18, 18, 18)
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton9)
                    .addComponent(jButton10)
                    .addComponent(jButton11))
                .addGap(32, 32, 32))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statutsEch1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1)
                    .addComponent(jButton9)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statutEch2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton10))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(statutEch3, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton11)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jButton7)
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );

        btnStart.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        jLabel3.setMaximumSize(new java.awt.Dimension(50, 50));
        jLabel3.setMinimumSize(new java.awt.Dimension(50, 50));

        btnPause.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnPause.setText("PAUSE");
        btnPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPauseActionPerformed(evt);
            }
        });

        jLabel6.setMaximumSize(new java.awt.Dimension(50, 50));
        jLabel6.setMinimumSize(new java.awt.Dimension(50, 50));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addComponent(btnPause, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(57, 57, 57))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnPause, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jButton8.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton8.setForeground(new java.awt.Color(255, 0, 51));
        jButton8.setText("FERMER");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton12.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jButton12.setForeground(new java.awt.Color(0, 153, 51));
        jButton12.setText("Sauvegardes");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jTextField4.setText("<nom fichier>");

        btnValidationConfig.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        btnValidationConfig.setText("Valider");
        btnValidationConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnValidationConfigActionPerformed(evt);
            }
        });

        jTextField5.setText("1");

        bntAnnulationConfig.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        bntAnnulationConfig.setText("Annuler");
        bntAnnulationConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bntAnnulationConfigActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(423, 423, 423)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(449, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1054, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(49, 49, 49))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(207, 207, 207)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField5)
                    .addComponent(jTextField4)
                    .addComponent(jButton12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnValidationConfig, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bntAnnulationConfig, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(63, 63, 63))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton8)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnValidationConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(bntAnnulationConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 11, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(90, 90, 90)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(53, 53, 53)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed

        arret_valide = false;

        if (!nomFichierInit) {

            montrerErrorConfig("Nom de fichier nom défini!");

        } else {

            if (!test_on) {

                test_pause = false;
                test_on = true;
                test_off = false;

                jLabel8.setText("0");
                jLabel2.setText("0");
                jLabel4.setText("0");
                jTextField1.setText("");
                jTextField2.setText("");
                jTextField3.setText("");
                compteur1 = 0;
                compteur2 = 0;
                compteur3 = 0;

                if (!statutsEch1.isSelected() && !statutEch2.isSelected() && !statutEch3.isSelected()) {

                    test_on = false;
                    montrerErrorConfig("Défaut de configuration");

                } else {
                    //*************************
                    if (statutsEch1.isSelected()) {

                        echantiilons[0] = true;
                        actifs[0] = true;
                        config = "!1:";

                    } else {

                        jLabel8.setForeground(Color.GRAY);
                        echantiilons[0] = false;
                        actifs[0] = false;
                        config = "!0:";
                    }

                    if (statutEch2.isSelected()) {

                        echantiilons[1] = true;
                        actifs[1] = true;
                        config = config + "1:";

                    } else {

                        jLabel2.setForeground(Color.GRAY);
                        echantiilons[1] = false;
                        actifs[1] = false;
                        config = config + "0:";
                    }

                    if (statutEch3.isSelected()) {

                        echantiilons[2] = true;
                        actifs[2] = true;
                        config = config + "1";

                    } else {

                        jLabel4.setForeground(Color.GRAY);
                        echantiilons[2] = false;
                        actifs[2] = false;
                        config = config + "0";
                    }

                    System.out.println("Configuration de test: " + config);
                    envoyerData(config);

                    boolean timeout = false;
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime courant = null;

                    long deb = System.currentTimeMillis();
                    long enCours = System.currentTimeMillis();

                    while (!acquittement && !timeout) {

                        enCours = System.currentTimeMillis();
                        if (enCours - deb > 500) {

                            timeout = true;
                        }

                    }

                    timeout = false;
                    acquittement = false;

                    envoyerData(marche);

                    jLabel3.setIcon(new ImageIcon(imageOn));
                    btnStart.setText("STOP");
                    btnPause.setVisible(true);
                    btnPause.setText("PAUSE");
                    // jTextArea1.setText("Le test est lancé");
                    //**************************
                }

            } else {
                test_pause = false;
                test_on = false;
                test_off = true;
                envoyerData(arret);
                jLabel3.setIcon(new ImageIcon(imageRoff));
                btnStart.setText("START");
                btnPause.setVisible(false);
                jLabel6.setVisible(false);
                infoText.setText("Test interrompu!");
                gestionEnregistrement();
                nomFichierInit = false;
                jTextField4.setText("<nom fichier>");

                try {
                    Sortie.close();
                } catch (IOException ex) {
                    Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }


    }//GEN-LAST:event_btnStartActionPerformed

    private void statutsEch1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statutsEch1ActionPerformed


    }//GEN-LAST:event_statutsEch1ActionPerformed

    private void statutEch2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statutEch2ActionPerformed

    }//GEN-LAST:event_statutEch2ActionPerformed

    private void statutEch3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statutEch3ActionPerformed

    }//GEN-LAST:event_statutEch3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void btnPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPauseActionPerformed

        arret_valide = false;
        if (!test_pause) {

            test_pause = true;
            test_on = true;
            test_off = false;

            jLabel3.setVisible(false);
            btnStart.setVisible(false);
            jLabel6.setVisible(true);
            jLabel6.setIcon(new ImageIcon(imageOff));
            jLabel5.setVisible(true);
            btnPause.setVisible(true);
            btnPause.setText("RELANCER");
            //  jTextArea1.setText("Test en pause");
            envoyerData(pause);

        } else {

            test_pause = false;
            test_on = true;
            test_off = false;

            btnStart.setVisible(true);
            jLabel3.setVisible(true);
            jLabel3.setIcon(new ImageIcon(imageOn));
            btnStart.setText("STOP");

            jLabel6.setVisible(false);
            btnPause.setVisible(true);
            btnPause.setText("PAUSE");
            //jTextArea1.setText("Reprise du test après interruption!");
            envoyerData(marche);

        }


    }//GEN-LAST:event_btnPauseActionPerformed

    private void bntConnexionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bntConnexionActionPerformed

        // PORT = (String) selectionPort.getSelectedItem();
        this.connect();
    }//GEN-LAST:event_bntConnexionActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        envoyerData(RAZ2);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed

        JOptionPane.showMessageDialog(this, "Voulez-vous fermer ce programme?", "Fermeture programme", JOptionPane.INFORMATION_MESSAGE);
        try {

            envoyerData(arret);
            gestionEnregistrement();

            if (test_on) {

                while (!arret_valide) {

                }

                Sortie.close();

            }

        } catch (IOException ex) {

        }
        System.exit(0);

    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        envoyerData(RAZ1);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed

        String compteur1 = jTextField1.getText();
        compteur1 = "#1:" + compteur1;
        envoyerData(compteur1);

    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed

        String compteur2 = jTextField2.getText();
        compteur2 = "#2:" + compteur2;
        envoyerData(compteur2);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed

        if (!test_on) {

            jTextField4.setVisible(true);
            btnValidationConfig.setVisible(true);
            jButton12.setVisible(false);
            jTextField5.setVisible(true);
            bntAnnulationConfig.setVisible(false);

        } else {

            jTextField4.setVisible(false);
            btnValidationConfig.setVisible(true);
            jButton12.setVisible(false);
            jTextField5.setVisible(true);
            bntAnnulationConfig.setVisible(true);
        }


    }//GEN-LAST:event_jButton12ActionPerformed

    private void btnValidationConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnValidationConfigActionPerformed

        if (!test_on) {

            nomFichier = jTextField4.getText();
            System.out.println("Nom de fichier choisi: " + nomFichier);
            int showOpenDialog = SelectFichier.showOpenDialog(this);

            if (nomFichier.equals("") || nomFichier.equals("<nom fichier>")) {

                montrerErrorNom("Défaut de nom!");

            } else {

                enregisterInterval();
                nomFichierInit = true;
            }
        } else {

            enregisterInterval();

        }


    }//GEN-LAST:event_btnValidationConfigActionPerformed

    private void bntAnnulationConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bntAnnulationConfigActionPerformed

        jTextField5.setVisible(false);
        bntAnnulationConfig.setVisible(false);
        btnValidationConfig.setVisible(false);
        jButton12.setVisible(true);

    }//GEN-LAST:event_bntAnnulationConfigActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed

        String compteur3 = jTextField3.getText();
        compteur3 = "#3:" + compteur3;
        envoyerData(compteur3);

    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        envoyerData(RAZ3);
    }//GEN-LAST:event_jButton7ActionPerformed

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
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Interface().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser SelectFichier;
    private javax.swing.JButton bntAnnulationConfig;
    private javax.swing.JButton bntConnexion;
    private javax.swing.JButton btnPause;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnValidationConfig;
    private javax.swing.JTextArea infoText;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JComboBox<String> portSelection;
    private javax.swing.JCheckBox statutEch2;
    private javax.swing.JCheckBox statutEch3;
    private javax.swing.JCheckBox statutsEch1;
    // End of variables declaration//GEN-END:variables

    /*
    @Override
    public void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

            try {

                System.out.println("Réception message");
                inputLine = input.readLine();
                System.out.println(inputLine);
                infoText.setText(inputLine);
                boolean isCompteur;
                boolean isActifs;
                boolean isArret;

                isCompteur = inputLine.startsWith("@TOTAL");
                isActifs = inputLine.startsWith("@ACTIFS:");
                isArret = inputLine.startsWith("@ARRET");
                if (isCompteur) {

                    String[] recept = inputLine.split(" ");
                    String compteur = recept[3];
                    String ech = recept[2];
                    System.out.println("num echantillon: " + recept[2]);
                    System.out.println("Compteur: " + recept[3]);

                    if (ech.equals("#1:")) {

                        jLabel8.setText(compteur);
                        compteur1 = Long.parseLong(compteur);

                    }

                    if (ech.equals("#2:")) {

                        jLabel2.setText(compteur);
                        compteur2 = Long.parseLong(compteur);
                    }

                    if (ech.equals("#3:")) {

                        jLabel4.setText(compteur);
                        compteur3 = Long.parseLong(compteur);

                    }

                }

                if (inputLine.equals("@SEQ")) {

                    nbr_seqs++;

                    if (nbr_seqs == interval) {

                        gestionEnregistrement();

                    }

                }

                if (isActifs) {

                    String[] recept = inputLine.split(":");

                    if (recept[1].equals("0")) {

                        actifs[0] = false;
                        if (statutsEch1.isSelected()) {
                            jLabel8.setForeground(Color.RED);
                            jLabel8.setText(String.valueOf(compteur1));
                        }

                    } else {

                        actifs[0] = true;
                    }

                    if (recept[2].equals("0")) {

                        actifs[1] = false;
                        if (statutEch2.isSelected()) {
                            jLabel2.setForeground(Color.RED);
                            jLabel2.setText(String.valueOf(compteur2));
                        }

                    } else {

                        actifs[1] = true;
                    }

                    if (recept[3].equals("0")) {

                        actifs[2] = false;
                        if (statutEch3.isSelected()) {
                            jLabel4.setForeground(Color.RED);
                            jLabel4.setText(String.valueOf(compteur3));
                        }

                    } else {

                        actifs[2] = true;
                    }

                }   // fin  if (isActifs)

                if (isArret) {

                    arret_valide = true;
                }

            } catch (Exception e) {   // Traitement des exceptions

                System.err.println(e.toString());
            }

        }
    }
    
    */

    public void initFichier() {

        // Initialisation flux de sortie
        try {
            fluxSortie = new FileWriter(nomFichier);
            Sortie = new BufferedWriter(fluxSortie);

            Sortie.write("Date;Heure;Echantillon1;Echantillon2;Echantillon3");
            Sortie.newLine();

        } catch (Exception ex) {

        }

    }

    private void sauvegarder(String chaine) {

        try {
            //
            Sortie.write(chaine);
            Sortie.newLine();
            Sortie.close();

        } catch (IOException ex) {
            Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void gestionEnregistrement() {

        LocalDateTime dateActuelle = LocalDateTime.now();
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter formatterHeure = DateTimeFormatter.ofPattern("HH:mm:ss");
        String date = dateActuelle.format(formatterDate);
        String heure = dateActuelle.format(formatterHeure);

        initFichier();
        ligneEnCours = date + ";" + heure + ";" + compteur1 + ";" + compteur2 + ";" + compteur3;
        sauvegarder(ligneEnCours);
        nbr_seqs = 0;

    }

    private void enregisterInterval() {

        String choixInterval = jTextField5.getText();

        try {
            interval = Integer.parseInt(choixInterval);
            if (interval < 21) {

                Repertoire = SelectFichier.getSelectedFile();
                nomFichier = Repertoire + "\\" + nomFichier + ".csv";
                System.out.println(Repertoire);
                System.out.println("nom fichier complet: " + nomFichier);
                initFichier();
                jTextField4.setVisible(false);
                btnValidationConfig.setVisible(false);
                jTextField5.setVisible(false);
                jButton12.setVisible(true);
                bntAnnulationConfig.setVisible(false);
                infoText.setText("Les fichiers de résultats seront sauvegardés dans le fichier: " + nomFichier);
                initFichier();

            } else {

                montrerErrorInterval("Choisissez un nombre entre 1 et 20");

            }

        } catch (Exception e) {

            montrerErrorInterval("Erreur choix de l'interval");
        }

    }

}
