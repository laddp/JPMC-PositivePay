/*
 * Created on Sep 28, 2010 by pladd
 *
 */
package com.bottinifuel.DifferenceCard_PositivePay;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.bottinifuel.jpmc.PositivePay.Check;
import com.bottinifuel.jpmc.PositivePay.OutputDialog;
import com.bottinifuel.jpmc.PositivePay.PositivePayWriter;
import javax.swing.JCheckBox;

public class DifferenceCard_PositivePay_GUI
{

    private JFrame mainFrame;
    private JTextField outputFileName;
    private JButton runButton;
    
    private DefaultListModel inputFiles;
    private JList inputFileList;

    private JFileChooser outputFileChooser = null;
    private JFileChooser inputFileChooser = null;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private JCheckBox allowMismatchedChecks;
    
    /**
     * Launch the application.
     */
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable() {
            public void run()
            {
                try
                {
                    DifferenceCard_PositivePay_GUI window = new DifferenceCard_PositivePay_GUI();
                    window.mainFrame.setVisible(true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * Create the application.
     */
    public DifferenceCard_PositivePay_GUI()
    {
        initialize();
    }

    public void ExitApplication()
    {
        System.exit(0);
    }

    public void updateRunButtonState()
    {
        if (outputFileName.getText().length() == 0)
            runButton.setEnabled(false);
        else
        {
            if (inputFiles.getSize() == 0)
                runButton.setEnabled(false);
            else
                runButton.setEnabled(true);
        }
            
    }

    private void doRun()
    {
        PositivePayWriter payWriter; 
        FileOutputStream outStream;
        File outFile = new File(outputFileName.getText());
        try
        {
            if (!outFile.getName().contains("."))
                outFile = new File(outFile.getPath() + ".csv");
            outStream = new FileOutputStream(outFile, false);
            payWriter = new PositivePayWriter(outStream);
        }
        catch (FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(mainFrame, "Can't open output file", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        File logFile = new File(outputFileName.getText());
        if (!logFile.getName().contains("."))
            logFile = new File(logFile.getPath() + ".txt");
        else
        {
            logFile = new File(logFile.getPath().substring(0, logFile.getPath().lastIndexOf('.')) + ".txt");
        }
        OutputDialog outputDialog = new OutputDialog(mainFrame, "Conversion Output", logFile);
        JTextArea outputTextArea = outputDialog.getTextArea();
        StringBuilder outputText = new StringBuilder();
        Formatter output = new Formatter(outputText);
        NumberFormat CF = NumberFormat.getCurrencyInstance();
        
        for (Object fileO : inputFiles.toArray())
        {
            File file = (File)fileO;
            try {
                DifferenceCardParser p = new DifferenceCardParser(new FileInputStream(file), allowMismatchedChecks.isSelected());
                BigDecimal total = payWriter.WriteChecks(new BigDecimal("885782102"), p.getChecks());
                output.format("\r\n\r\nProcessed check file: " + file + "\r\n");
                output.format("\tTotal # Items: " + p.getChecks().size() + "\r\n");
                output.format("\tTotal Amount:  " + DecimalFormat.getCurrencyInstance().format(total) + "\r\n");

                if (allowMismatchedChecks.isSelected() && p.MismatchedChecksMessages.length() > 0)
                	output.format(
                			"===========================================================\n" +
                			p.MismatchedChecksMessages +
                			"\n===========================================================\n");

                output.format("\r\nItem Summary:\r\n");
                output.format("===========================================================\r\n");
                //             123456 123456 123456789012
                output.format("Date        Num       Amount  Payee\r\n");
                output.format("-----------------------------------------------------------\r\n");
                for (Check check : p.getChecks())
                {
                	if (check.VerifyItemTotals())
                		output.format("%tD %6d %12s  %s\r\n",
                				check.CheckDate, check.Number, CF.format(check.Amount), check.PayeeName);
                	else
                		output.format("%tD %6d %12s  %s\r\n",
                				check.CheckDate, check.Number, CF.format(check.Amount), "*** " + check.PayeeName);
                }
                output.format("===========================================================\r\n");
            }
            catch (FileNotFoundException e)
            {
                JOptionPane.showMessageDialog(mainFrame, "Error opening input file: " + file.toString(), "Input file error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(mainFrame, e, "Parse Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        try {
            outStream.close();
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(mainFrame, "Output file error: " + e, "Output file error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        output.format("\r\n\r\nOutput file: " + outFile + "\r\n");
        output.format("\tTotal # Items:  " + payWriter.getItemCount() + "\r\n");
        output.format("\tTotal Amount:  " + DecimalFormat.getCurrencyInstance().format(payWriter.getTotal()) + "\r\n");

        outputTextArea.append(output.toString());
        outputDialog.pack();
        outputDialog.setVisible(true);
    }
    
    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        mainFrame = new JFrame();
        mainFrame.setTitle("Difference Card to Positive Pay Converter");
        mainFrame.setBounds(100, 100, 492, 268);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().setLayout(new BorderLayout(0, 0));
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainFrame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.setLayout(new GridLayout(0, 1, 0, 0));
        
        JPanel outputPanel = new JPanel();
        bottomPanel.add(outputPanel);
        GridBagLayout gbl_outputPanel = new GridBagLayout();
        gbl_outputPanel.columnWidths = new int[]{57, 296, 79, 0};
        gbl_outputPanel.rowHeights = new int[]{24, 0};
        gbl_outputPanel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_outputPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        outputPanel.setLayout(gbl_outputPanel);
        
        JLabel outputLabel = new JLabel("Output File:");
        outputLabel.setDisplayedMnemonic('O');
        GridBagConstraints gbc_outputLabel = new GridBagConstraints();
        gbc_outputLabel.anchor = GridBagConstraints.EAST;
        gbc_outputLabel.insets = new Insets(5, 5, 5, 5);
        gbc_outputLabel.gridx = 0;
        gbc_outputLabel.gridy = 0;
        outputPanel.add(outputLabel, gbc_outputLabel);
        
        outputFileName = new JTextField();
        outputLabel.setLabelFor(outputFileName);
        GridBagConstraints gbc_outputFileName = new GridBagConstraints();
        gbc_outputFileName.weightx = 1.0;
        gbc_outputFileName.fill = GridBagConstraints.HORIZONTAL;
        gbc_outputFileName.insets = new Insets(0, 0, 0, 5);
        gbc_outputFileName.gridx = 1;
        gbc_outputFileName.gridy = 0;
        outputPanel.add(outputFileName, gbc_outputFileName);
        outputFileName.setColumns(10);
        DocumentListener outputFileListener = new DocumentListener() {
            public void removeUpdate(DocumentEvent e)
            {
                updateRunButtonState();
            }
            public void insertUpdate(DocumentEvent e)
            {
                updateRunButtonState();
            }
            public void changedUpdate(DocumentEvent e)
            {
                updateRunButtonState();
            }
        };
        outputFileName.getDocument().addDocumentListener(outputFileListener);

        JButton outputBrowse = new JButton("Browse...");
        outputBrowse.setMnemonic('B');
        outputBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (outputFileChooser == null)
                {
                    outputFileChooser = new JFileChooser();
                    outputFileChooser.setFileFilter(new FileNameExtensionFilter("Comma Separated Values (.csv)", "csv"));
                    outputFileChooser.setAcceptAllFileFilterUsed(false);
                    outputFileChooser.setCurrentDirectory(new File("S:/Accounting/JPMC/Posititve Pay/Difference Card"));
                }
                String ofName = outputFileName.getText();
                if (ofName == null || ofName.trim().length() == 0)
                {
                    if (inputFiles.isEmpty())
                    {
                        String now = dateFormat.format(new Date());
                        outputFileChooser.setSelectedFile(new File(now + ".csv"));
                    }
                    else
                    {
                        File firstFile = (File)inputFiles.firstElement();
                        String fileName = firstFile.getName();
                        fileName = fileName.split("\\.")[0];
                        File file = new File(fileName + ".csv");
                        outputFileChooser.setSelectedFile(file);
                        outputFileChooser.setCurrentDirectory(firstFile.getParentFile());
                    }
                }
                else
                {
                    File of = new File(ofName); 
                    outputFileChooser.setSelectedFile(of);
                    outputFileChooser.setCurrentDirectory(of.getParentFile());
                }
                int rc = outputFileChooser.showSaveDialog(mainFrame);
                if (rc == JFileChooser.APPROVE_OPTION)
                {
                    File selected = outputFileChooser.getSelectedFile();
                    outputFileName.setText(selected.getAbsolutePath());
                }
            }
        });
        GridBagConstraints gbc_outputBrowse = new GridBagConstraints();
        gbc_outputBrowse.gridx = 2;
        gbc_outputBrowse.gridy = 0;
        outputPanel.add(outputBrowse, gbc_outputBrowse);
        
        JPanel checkboxPanel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) checkboxPanel.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        bottomPanel.add(checkboxPanel);
        
        allowMismatchedChecks = new JCheckBox("Allow Mismatched Checks");
        checkboxPanel.add(allowMismatchedChecks);
        
        JPanel buttonPanel = new JPanel();
        bottomPanel.add(buttonPanel);
        
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                inputFiles.removeAllElements();
                outputFileName.setText("");
            }
        });
        
        runButton = new JButton("Run");
        runButton.setEnabled(false);
        runButton.setMnemonic('R');
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                doRun();
            }
        });
        buttonPanel.add(runButton);
        resetButton.setMnemonic('t');
        buttonPanel.add(resetButton);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                ExitApplication();
            }
        });
        closeButton.setMnemonic('C');
        buttonPanel.add(closeButton);
        
        JPanel inputControlPanel = new JPanel();
        inputControlPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainFrame.getContentPane().add(inputControlPanel, BorderLayout.EAST);
        GridBagLayout gbl_inputControlPanel = new GridBagLayout();
        gbl_inputControlPanel.columnWidths = new int[]{0, 0};
        gbl_inputControlPanel.rowHeights = new int[]{0, 0, 0};
        gbl_inputControlPanel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
        gbl_inputControlPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        inputControlPanel.setLayout(gbl_inputControlPanel);
        
        JButton addButton = new JButton("Add...");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (inputFileChooser == null)
                {
                    inputFileChooser = new JFileChooser();
                    inputFileChooser.setFileFilter(new FileNameExtensionFilter("Portable Document Format (.pdf)", "pdf"));
                    inputFileChooser.setAcceptAllFileFilterUsed(false);
                    inputFileChooser.setMultiSelectionEnabled(true);
                    inputFileChooser.setCurrentDirectory(new File("S:/Accounting/JPMC/Posititve Pay/Difference Card"));
                }

                int rc = inputFileChooser.showOpenDialog(mainFrame);
                if (rc == JFileChooser.APPROVE_OPTION)
                {
                    for (File selected : inputFileChooser.getSelectedFiles())
                    {
                        inputFiles.addElement(selected);
                    }
                    updateRunButtonState();
                }
            }
        });
        addButton.setMnemonic('A');
        GridBagConstraints gbc_addButton = new GridBagConstraints();
        gbc_addButton.anchor = GridBagConstraints.LINE_START;
        gbc_addButton.insets = new Insets(0, 0, 5, 0);
        gbc_addButton.gridx = 0;
        gbc_addButton.gridy = 0;
        inputControlPanel.add(addButton, gbc_addButton);
        
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                int index = inputFileList.getSelectedIndex();
                while (index != -1)
                {
                    inputFiles.removeElementAt(index);
                    index = inputFileList.getSelectedIndex();
                }
                updateRunButtonState();
            }
        });
        removeButton.setMnemonic('e');
        GridBagConstraints gbc_removeButton = new GridBagConstraints();
        gbc_removeButton.anchor = GridBagConstraints.LINE_START;
        gbc_removeButton.gridx = 0;
        gbc_removeButton.gridy = 1;
        inputControlPanel.add(removeButton, gbc_removeButton);
        
        JPanel topPanel = new JPanel();
        FlowLayout fl_topPanel = (FlowLayout) topPanel.getLayout();
        fl_topPanel.setAlignment(FlowLayout.LEFT);
        mainFrame.getContentPane().add(topPanel, BorderLayout.NORTH);
        
        JLabel label = new JLabel("Input Files:");
        label.setDisplayedMnemonic('I');
        topPanel.add(label);
        
        inputFiles = new DefaultListModel();
        
        JPanel panel = new JPanel();
        mainFrame.getContentPane().add(panel, BorderLayout.CENTER);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{72, 258, 0};
        gbl_panel.rowHeights = new int[]{130, 0};
        gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);
        
        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.weighty = 1.0;
        gbc_scrollPane.weightx = 1.0;
        gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
        gbc_scrollPane.anchor = GridBagConstraints.NORTHWEST;
        gbc_scrollPane.gridx = 1;
        gbc_scrollPane.gridy = 0;
        panel.add(scrollPane, gbc_scrollPane);
        inputFileList = new JList(inputFiles);
        label.setLabelFor(inputFileList);
        scrollPane.setViewportView(inputFileList);
        
        updateRunButtonState();
    }
    public JTextField getOutputFileName() {
        return outputFileName;
    }
}
