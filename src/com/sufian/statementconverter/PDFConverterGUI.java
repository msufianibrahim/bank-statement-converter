package com.sufian.statementconverter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.io.File;


public class PDFConverterGUI extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5780044222528465029L;
	private JLabel statementTypeLabel, inputFileLabel, outputPathLabel;
    private JComboBox<String> statementTypeComboBox;
    private JTextField inputFileTextField, outputPathTextField;
    private JButton chooseFileButton, chooseOutputPathButton, convertButton;
	protected static final String MAYBANK_CREDIT = "MAYBANK CREDIT";
	protected static final String MAYBANK_DEBIT = "MAYBANK DEBIT";
	protected static final String CIMB_CREDIT = "CIMB CREDIT";
	protected static final String TOUCHNGO = "TOUCH N GO";
	
	String globalInputFilePath = "";

    public PDFConverterGUI() {
    	
    	try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // Set window title
        setTitle("PDF Converter");

        // Set window size
        setSize(400, 200);

        // Create components
        statementTypeLabel = new JLabel("Statement type:");
        inputFileLabel = new JLabel("Input File:");
        outputPathLabel = new JLabel("Output Path:");

        String[] statementTypes = {"Choose statement type", CIMB_CREDIT, MAYBANK_CREDIT, MAYBANK_DEBIT, TOUCHNGO}; // Add your statement types here
        statementTypeComboBox = new JComboBox<>(statementTypes);

        inputFileTextField = new JTextField(20);
        inputFileTextField.setEditable(false);

        outputPathTextField = new JTextField(20);
        outputPathTextField.setEditable(false);

        chooseFileButton = new JButton("Choose File");
        chooseOutputPathButton = new JButton("Choose Output Path");
        convertButton = new JButton("Convert");
        

        // Add action listeners
        chooseFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                String inputPath = "";
                if(!globalInputFilePath.equals("") && globalInputFilePath != null) {
                	inputPath = globalInputFilePath;
                } else {
                	inputPath = "C:\\";
                }
                FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Files", "pdf");
                fileChooser.setFileFilter(filter);
                fileChooser.setCurrentDirectory(new File(inputPath));
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    inputFileTextField.setText(selectedFile.getAbsolutePath());
                }
                String absoluteInputFilePath = inputFileTextField.getText();
                globalInputFilePath = absoluteInputFilePath.substring(0, absoluteInputFilePath.lastIndexOf('\\'));
            }
        });

        chooseOutputPathButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                String outputPath = "";
                if(!globalInputFilePath.equals("") && globalInputFilePath != null) {
                	outputPath = globalInputFilePath;
                } else {
                	outputPath = "C:\\";
                }
                fileChooser.setCurrentDirectory(new File(outputPath));
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    outputPathTextField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        convertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Get the selected statement type
                String selectedStatementType = (String) statementTypeComboBox.getSelectedItem();

                // Get the absolute path of the input file
                String absoluteInputFilePath =	inputFileTextField.getText();
                System.out.println(absoluteInputFilePath);
                String inputFileName = "";
                // Extract the file name from the input file path
                if(!absoluteInputFilePath.equals("")) {
                	String inputFileNameWithExtension = new File(absoluteInputFilePath).getName();
                
                	// Remove the file extension from the input file name
                	inputFileName = inputFileNameWithExtension.substring(0, inputFileNameWithExtension.lastIndexOf('.'));
                }

                // Get the selected output directory
                String outputDirectory = outputPathTextField.getText();
                String outputFilePath = "";
                if(!outputDirectory.equals("")) {
                	// Create the output file path by appending the input file name to the output directory
                	outputFilePath = outputDirectory + File.separator + inputFileName + ".xlsx";
                }
                if(!absoluteInputFilePath.equals("") && !outputFilePath.equals("") && !selectedStatementType.equals("Choose statement type")) {
                	PDFReader.readPDF(selectedStatementType, absoluteInputFilePath, outputFilePath);
                } else {
                	JOptionPane.showMessageDialog(null, "Please select a valid bank statement type, a bank statement file and a directory for output file");
                }
            }
        });




        // Create panel to hold components
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(statementTypeLabel);
        panel.add(statementTypeComboBox);
        panel.add(inputFileLabel);
        panel.add(inputFileTextField);
        panel.add(outputPathLabel);
        panel.add(outputPathTextField);

        // Create panel to hold buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(chooseFileButton);
        buttonPanel.add(chooseOutputPathButton);
        buttonPanel.add(convertButton);

        // Add panels to content pane
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        // Center the window
        setLocationRelativeTo(null);

        // Make window visible
        setVisible(true);

        // Close application when window is closed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        new PDFConverterGUI();
    }
}
