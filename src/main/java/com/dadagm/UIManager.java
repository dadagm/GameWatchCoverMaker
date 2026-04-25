package com.dadagm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Locale;

public class UIManager {
    private JFrame frame;
    private JTextArea logArea;
    private JTextField romsPathField;
    private JTextField coversPathField;
    private JCheckBox addSubtitleCheckBox;
    private JButton romsSelectButton;
    private JButton coversSelectButton;
    private JButton startButton;
    private JButton incrementalUpdateButton;
    private JButton closeButton;
    private JButton aboutButton;
    private JLabel romsLabel;
    private JLabel coversLabel;
    private String extractPath = "";
    private ConfigManager configManager;
    private I18nManager i18nManager;
    private ImageProcessor imageProcessor;
    
    public UIManager() {
        configManager = ConfigManager.getInstance();
        i18nManager = I18nManager.getInstance();
        imageProcessor = new ImageProcessor();
    }
    
    public void createAndShowGUI() {
        frame = new JFrame(i18nManager.getString("window.title") + " v" + configManager.getVersion());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(650, 550);
        frame.setLayout(new BorderLayout());

        JPanel pathPanel = new JPanel(new GridLayout(2, 3));

        romsLabel = new JLabel(i18nManager.getString("roms.label"));
        String executablePath = getExecutableDirectory2();
        String romsDefaultPath = executablePath + File.separator + "roms";
        romsPathField = new JTextField(romsDefaultPath);
        romsPathField.setEditable(false);
        romsSelectButton = new JButton(i18nManager.getString("select.button"));
        romsSelectButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                romsPathField.setText(selectedFile.getAbsolutePath());
            }
        });

        coversLabel = new JLabel(i18nManager.getString("covers.label"));
        String coversDefaultPath = executablePath + File.separator + "covers";
        coversPathField = new JTextField(coversDefaultPath);
        coversPathField.setEditable(false);
        coversSelectButton = new JButton(i18nManager.getString("select.button"));
        coversSelectButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                coversPathField.setText(selectedFile.getAbsolutePath());
            }
        });
        
        pathPanel.add(romsLabel);
        pathPanel.add(romsPathField);
        pathPanel.add(romsSelectButton);
        pathPanel.add(coversLabel);
        pathPanel.add(coversPathField);
        pathPanel.add(coversSelectButton);
        
        JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        languagePanel.setLayout(new BoxLayout(languagePanel, BoxLayout.Y_AXIS));
        
        JComboBox<String> languageComboBox = new JComboBox<>();
        languageComboBox.addItem("English");
        languageComboBox.addItem("中文");
        
        Locale currentLocale = i18nManager.getCurrentLocale();
        if (currentLocale.equals(Locale.CHINA)) {
            languageComboBox.setSelectedIndex(1);
        } else {
            languageComboBox.setSelectedIndex(0);
        }
        
        languageComboBox.addActionListener(e -> {
            int selectedIndex = languageComboBox.getSelectedIndex();
            if (selectedIndex == 0) {
                i18nManager.switchLanguage(Locale.ENGLISH);
            } else if (selectedIndex == 1) {
                i18nManager.switchLanguage(Locale.CHINA);
            }
            updateUITexts();
        });
        
        aboutButton = new JButton(i18nManager.getString("about.button"));
        aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String aboutMessage = i18nManager.getString("about.message").replace("v1.1", "v" + configManager.getVersion());
                
                ImageIcon logoIcon = null;
                try {
                    File iconFile = new File("gnwtools.jpeg");
                    if (iconFile.exists()) {
                        ImageIcon originalIcon = new ImageIcon(iconFile.getAbsolutePath());
                        Image originalImage = originalIcon.getImage();
                        int newWidth = originalImage.getWidth(null) / 2;
                        int newHeight = originalImage.getHeight(null) / 2;
                        Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                        logoIcon = new ImageIcon(scaledImage);
                    }
                } catch (Exception ex) {
                }
                
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                
                // 添加文本
                JTextArea textArea = new JTextArea(aboutMessage);
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setOpaque(false);
                panel.add(textArea, BorderLayout.CENTER);
                
                if (logoIcon != null) {
                    JPanel clickablePanel = new JPanel();
                    clickablePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    clickablePanel.setToolTipText("点击提取图片");
                    clickablePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
                    
                    JLabel imageLabel = new JLabel(logoIcon);
                    clickablePanel.add(imageLabel);
                    
                    final int[] clickCount = {0}; 
                    clickablePanel.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                            evt.consume();
                            
                            clickCount[0]++;
                            System.out.println("num: " + clickCount[0]);
                            
                            if (clickCount[0] == 3) {
                                clickCount[0] = 0;
                                
                                JFileChooser fileChooser = new JFileChooser();
                                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                fileChooser.setDialogTitle(i18nManager.getString("extract.dialog.title"));
                                
                                SwingUtilities.invokeLater(() -> {
                                    int result = fileChooser.showOpenDialog(frame);
                                    if (result == JFileChooser.APPROVE_OPTION) {
                                        File selectedFile = fileChooser.getSelectedFile();
                                        extractPath = selectedFile.getAbsolutePath();
                                        new Thread(() -> extractBoxFrontFiles()).start();
                                    }
                                });
                            }
                        }
                    });
                    
                    panel.add(clickablePanel, BorderLayout.NORTH);
                }
                
                JOptionPane.showMessageDialog(frame, panel, i18nManager.getString("about.button"), JOptionPane.PLAIN_MESSAGE);
            }
        });
        
        JPanel languageAndAboutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        languageAndAboutPanel.add(languageComboBox);
        
        JPanel aboutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        aboutPanel.add(aboutButton);
        
        languagePanel.add(languageAndAboutPanel);
        languagePanel.add(aboutPanel);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(pathPanel, BorderLayout.CENTER);
        topPanel.add(languagePanel, BorderLayout.EAST);
        
        frame.add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 3, 5, 5)); // 每行3个按钮，5像素间距

      
        startButton = new JButton(i18nManager.getString("start.button"));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> processImages()).start();
            }
        });
        buttonPanel.add(startButton);

        incrementalUpdateButton = new JButton(i18nManager.getString("incremental.update.button"));
        incrementalUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> incrementalUpdateImages()).start();
            }
        });
        buttonPanel.add(incrementalUpdateButton);

        closeButton = new JButton(i18nManager.getString("close.button"));
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        buttonPanel.add(closeButton);

        addSubtitleCheckBox = new JCheckBox(i18nManager.getString("subtitle.checkbox"));
        addSubtitleCheckBox.setSelected(false); 
        buttonPanel.add(addSubtitleCheckBox);

        buttonPanel.add(new JPanel());

        frame.add(buttonPanel, BorderLayout.SOUTH);
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true); 
        JScrollPane scrollPane = new JScrollPane(logArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    baos.write(b);
                    String str = new String(baos.toByteArray(), "UTF-8");
                    if (str.endsWith("\n")) {
                        logArea.append(str);
                        logArea.setCaretPosition(logArea.getDocument().getLength());
                        baos.reset();
                    }
                }
            }, true, "UTF-8");
            System.setOut(printStream);
            System.setErr(printStream);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(i18nManager.getString("instruction.text"));

        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }
    
    public void updateUITexts() {
        frame.setTitle(i18nManager.getString("window.title") + " v" + configManager.getVersion());
        
        romsLabel.setText(i18nManager.getString("roms.label"));
        coversLabel.setText(i18nManager.getString("covers.label"));
        romsSelectButton.setText(i18nManager.getString("select.button"));
        coversSelectButton.setText(i18nManager.getString("select.button"));
        addSubtitleCheckBox.setText(i18nManager.getString("subtitle.checkbox"));
        startButton.setText(i18nManager.getString("start.button"));
        incrementalUpdateButton.setText(i18nManager.getString("incremental.update.button"));
        closeButton.setText(i18nManager.getString("close.button"));
        aboutButton.setText(i18nManager.getString("about.button"));
        
        logArea.setText("");
        System.out.println(i18nManager.getString("instruction.text"));
        
        frame.revalidate();
        frame.repaint();
    }
    
    private String getExecutableDirectory2() {
        try {
            ProcessHandle currentProcess = ProcessHandle.current();
            java.util.Optional<ProcessHandle.Info> processInfo = java.util.Optional.ofNullable(currentProcess.info());
            java.util.Optional<String[]> argsOptional = processInfo.flatMap(info -> java.util.Optional.ofNullable(info.command().orElse("").split(" ")));
            if (argsOptional.isPresent()) {
                String[] argsArray = argsOptional.get();
                if (argsArray.length > 0) {
                    File exeFile = new File(argsArray[0]);
                    if (exeFile.exists()) {
                        return exeFile.getParent();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return System.getProperty("user.dir");
    }
    
    private void processImages() {
        imageProcessor.processImages(
            romsPathField.getText(),
            coversPathField.getText(),
            addSubtitleCheckBox.isSelected()
       );
    }
    
    private void incrementalUpdateImages() {
        imageProcessor.incrementalUpdateImages(
            romsPathField.getText(),
            coversPathField.getText(),
            addSubtitleCheckBox.isSelected()

        );
    }
    
    private void extractBoxFrontFiles() {
        imageProcessor.extractBoxFrontFiles(extractPath);
    }
    
    public JFrame getFrame() {
        return frame;
    }
}