import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

public class MyGUI extends JFrame {
    /**
	 */
	private static final long serialVersionUID = 5718057011862604275L;
	private JTextField fileTextField;
    private JTextField imeiTextField;
    private JTextField deviceIdTextField;

    private JButton generateButton;
    private JButton suspendButton;

    private JFileChooser fileChooser;

    private SwingWorker<Void, String> worker;

    public MyGUI() {
        setTitle("LG G6 unlock.bin validator");

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        JPanel filePanel = new JPanel();
        filePanel.add(new JLabel("File:"));
        fileTextField = new JTextField(50);
        filePanel.add(fileTextField);
        JButton fileButton = new JButton("...");
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = getFileChooser().showOpenDialog(MyGUI.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = getFileChooser().getSelectedFile();
                    fileTextField.setText(selectedFile.getAbsolutePath());
                }
            }
        });
        filePanel.add(fileButton);
        inputPanel.add(filePanel, BorderLayout.NORTH);

        JPanel imeiPanel = new JPanel();
        imeiPanel.add(new JLabel("IMEI:"));
        imeiTextField = new JTextField(15);
        imeiPanel.add(imeiTextField);
        inputPanel.add(imeiPanel, BorderLayout.CENTER);

        JPanel deviceIdPanel = new JPanel();
        deviceIdPanel.add(new JLabel("Device ID:"));
        deviceIdTextField = new JTextField(50);
        deviceIdPanel.add(deviceIdTextField);
        inputPanel.add(deviceIdPanel, BorderLayout.SOUTH);

        getContentPane().add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        generateButton = new JButton("Generate");
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (worker != null && !worker.isDone()) {
                    JOptionPane.showMessageDialog(MyGUI.this, "A generation process is already running.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String file = fileTextField.getText();
                String imei = imeiTextField.getText();
                String deviceId = deviceIdTextField.getText();
                if (file.isEmpty() || imei.isEmpty() || deviceId.isEmpty()) {
                    JOptionPane.showMessageDialog(MyGUI.this, "Please fill in all fields.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
					worker = new GenerationWorker(imei, deviceId, file, false);
					worker.execute();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
            }
        });
        buttonPanel.add(generateButton);

        suspendButton = new JButton("Suspend");
        suspendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (worker == null || worker.isDone()) {
                    JOptionPane.showMessageDialog(MyGUI.this, "No generation process is running.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                worker.cancel(true);
            }
        });
        buttonPanel.add(suspendButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JFileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileFilter() {
				@Override
				public String getDescription() {
					return "unlock.bin";
				}
				@Override
				public boolean accept(File f) {
					return !f.isFile() || this.getDescription().equals(f.getName());
				}
			});
        }
        return fileChooser;
    }
}