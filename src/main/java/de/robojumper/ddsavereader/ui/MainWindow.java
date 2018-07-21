package de.robojumper.ddsavereader.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import de.fuerstenau.buildconfig.BuildConfig;
import de.robojumper.ddsavereader.ui.State.SaveFile;
import de.robojumper.ddsavereader.ui.State.Status;

public class MainWindow {

    private JFrame frame;
    private JTextField gameDataPathBox, savePathBox, workshopPathBox;
    private JLabel saveFileStatus, gameDataStatus, workshopStatus;
    private JTabbedPane tabbedPane;
    private JLabel saveStatus;
    private JButton saveButton;

    private State state = new State();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainWindow window = new MainWindow();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public MainWindow() {
        initialize();
        initSettings();
    }

    private void initSettings() {
        state.setFileSaveStatusReceiver(n -> {
            EventQueue.invokeLater(() -> {
                saveStatus.setIcon(state.canSave() ? Status.OK.icon : Status.ERROR.icon);
                saveButton.setEnabled(state.canSave());
            });
        });
        state.init();
        updateSaveDir();
        updateGameDir();
        updateModsDir();
        updateFiles();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }

        frame = new JFrame();
        frame.setBounds(100, 100, 800, 500);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptExit();
            }
        });
        frame.setTitle(BuildConfig.DISPLAY_NAME + "/" + BuildConfig.VERSION);

        JMenuBar menuBar = new JMenuBar();
        frame.getContentPane().add(menuBar, BorderLayout.NORTH);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem mntmExit = new JMenuItem("Exit");
        mntmExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptExit();
            }
        });
        fileMenu.add(mntmExit);

        JMenu mnHelp = new JMenu("Help");
        menuBar.add(mnHelp);

        JMenuItem mntmAbout = new JMenuItem("About");
        mntmAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] options = { "OK", "Go to GitHub Page" };
                int result = JOptionPane.showOptionDialog(frame,
                        BuildConfig.DISPLAY_NAME + " " + BuildConfig.VERSION + "\nBy: /u/robojumper\nGitHub: "
                                + BuildConfig.GITHUB_URL,
                        "About", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                switch (result) {
                case 0:
                    break;
                case 1:
                    try {
                        Desktop.getDesktop().browse(new URI(BuildConfig.GITHUB_URL));
                    } catch (IOException | URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                }
                ;
            }
        });
        mnHelp.add(mntmAbout);

        JPanel contentPanel = new JPanel();
        frame.getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));

        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(null);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.PAGE_AXIS));

        JPanel savePathPanel = new JPanel();
        savePathPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Save File Directory",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        inputPanel.add(savePathPanel);
        savePathPanel.setLayout(new BoxLayout(savePathPanel, BoxLayout.LINE_AXIS));

        saveFileStatus = new JLabel("");
        saveFileStatus.setIcon(Resources.WARNING_ICON);
        savePathPanel.add(saveFileStatus);

        savePathBox = new JTextField();
        savePathBox.setEditable(false);
        savePathPanel.add(savePathBox);
        savePathBox.setColumns(10);

        JButton chooseSavePathButton = new JButton("Browse...");
        chooseSavePathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (confirmLoseChanges()) {
                    directoryChooser("", s -> state.setSaveDir(s));
                    updateSaveDir();
                }
            }
        });
        savePathPanel.add(chooseSavePathButton);

        JPanel gameDataPathPanel = new JPanel();
        gameDataPathPanel.setBorder(
                new TitledBorder(null, "Game Data Directory", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        inputPanel.add(gameDataPathPanel);
        gameDataPathPanel.setLayout(new BoxLayout(gameDataPathPanel, BoxLayout.LINE_AXIS));

        gameDataStatus = new JLabel("");
        gameDataStatus.setIcon(Resources.OK_ICON);
        gameDataPathPanel.add(gameDataStatus);

        gameDataPathBox = new JTextField();
        gameDataPathBox.setEditable(false);
        gameDataPathPanel.add(gameDataPathBox);
        gameDataPathBox.setColumns(10);

        JButton chooseGamePathButton = new JButton("Browse...");
        chooseGamePathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (confirmLoseChanges()) {
                    directoryChooser("", s -> state.setGameDir(s));
                    updateGameDir();
                }
            }
        });
        gameDataPathPanel.add(chooseGamePathButton);

        JPanel workshopPathPanel = new JPanel();
        workshopPathPanel.setBorder(
                new TitledBorder(null, "Workshop Directory", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        inputPanel.add(workshopPathPanel);
        workshopPathPanel.setLayout(new BoxLayout(workshopPathPanel, BoxLayout.LINE_AXIS));

        workshopStatus = new JLabel("");
        workshopStatus.setIcon(Resources.OK_ICON);
        workshopPathPanel.add(workshopStatus);

        workshopPathBox = new JTextField();
        workshopPathBox.setEditable(false);
        workshopPathPanel.add(workshopPathBox);
        workshopPathBox.setColumns(10);

        JButton chooseWorkshopPathButton = new JButton("Browse...");
        chooseWorkshopPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (confirmLoseChanges()) {
                    directoryChooser("", s -> state.setModsDir(s));
                    updateModsDir();
                }
            }
        });
        workshopPathPanel.add(chooseWorkshopPathButton);
        contentPanel.add(inputPanel, BorderLayout.NORTH);

        JPanel panel = new JPanel();
        contentPanel.add(panel, BorderLayout.CENTER);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        panel.add(tabbedPane);

        JPanel dummyTab = new JPanel();
        tabbedPane.addTab("Dummy Tab", null, dummyTab, "*");
        dummyTab.setLayout(new BoxLayout(dummyTab, BoxLayout.LINE_AXIS));

        TextArea textArea = new TextArea();
        dummyTab.add(textArea);

        JPanel buttonPanel = new JPanel();
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

        JButton discardChangesButton = new JButton("Discard File Changes");
        discardChangesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = ((Tab) tabbedPane.getSelectedComponent()).fileName;
                SaveFile s = state.getSaveFile(fileName);
                Tab t = (Tab) tabbedPane.getSelectedComponent();
                t.area.setText(s.originalContents);
                t.area.setCaretPosition(0);
            }
        });
        buttonPanel.add(discardChangesButton);

        Component horizontalGlue = Box.createHorizontalGlue();
        buttonPanel.add(horizontalGlue);

        saveStatus = new JLabel("");
        saveStatus.setIcon(Resources.OK_ICON);
        buttonPanel.add(saveStatus);

        saveButton = new JButton("Save All Changes");
        buttonPanel.add(saveButton);

        JButton reloadButton = new JButton("Reload All");
        reloadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                state.loadFiles();
                updateFiles();
            }
        });
        buttonPanel.add(reloadButton);
    }

    private static final void directoryChooser(String def, Consumer<String> onSuccess) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            onSuccess.accept(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void updateSaveDir() {
        savePathBox.setText(state.getSaveDir());
        saveFileStatus.setIcon(state.getSaveStatus().icon);
    }

    private void updateGameDir() {
        gameDataPathBox.setText(state.getGameDir());
        gameDataStatus.setIcon(state.getGameStatus().icon);
    }

    private void updateModsDir() {
        workshopPathBox.setText(state.getModsDir());
        workshopStatus.setIcon(state.getModsStatus().icon);
    }

    private void updateFiles() {
        tabbedPane.removeAll();
        for (SaveFile f : state.saveFiles()) {
            Tab compPanel = new Tab();
            compPanel.fileName = f.name;
            compPanel.setLayout(new BoxLayout(compPanel, BoxLayout.LINE_AXIS));
            JTextArea a = new JTextArea(f.contents);
            compPanel.area = a;
            a.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void removeUpdate(DocumentEvent e) {
                    update(e);
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    update(e);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    update(e);
                }

                private void update(DocumentEvent e) {
                    try {
                        state.changeFile(f.name, e.getDocument().getText(0, e.getDocument().getLength()));
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                    updateTitle(compPanel);
                }
            });
            JScrollPane sp = new JScrollPane(a);
            compPanel.add(sp);
            tabbedPane.addTab((f.changed() ? "*" : "") + f.name, compPanel);
        }
    }

    private void updateTitle(Tab t) {
        SaveFile f = state.getSaveFile(t.fileName);
        tabbedPane.setTitleAt(tabbedPane.indexOfComponent(t), (f.changed() ? "*" : "") + f.name);
    }

    protected void attemptExit() {
        if (confirmLoseChanges()) {
            state.save();
            System.exit(0);
        }
    }

    protected boolean confirmLoseChanges() {
        if (state.getNumUnsavedChanges() > 0) {
            int result = JOptionPane.showConfirmDialog(frame,
                    "You have " + state.getNumUnsavedChanges()
                            + " unsaved changes! Are you sure you want to discard them?",
                    "Discard Changes", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        return true;
    }

    private class Tab extends JPanel {
        private static final long serialVersionUID = 7066962308849880236L;
        private String fileName;
        private JTextArea area;
    }
}