/*
 * Created on Sep 30, 2010 by pladd
 *
 */
package com.bottinifuel.jpmc.PositivePay;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class OutputDialog extends JDialog
{

    /**
     * 
     */
    private static final long serialVersionUID = 7728104473945406790L;
    private final JPanel contentPanel = new JPanel();
    private JTextArea textArea;
    private final File outputFile;
    private final Container owner;

    /**
     * Create the dialog.
     */
    public OutputDialog(Frame frame, String title, File outputF)
    {
        super(frame, title, true);
        
        owner = getParent();
        outputFile = outputF;
        
        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new GridLayout(0, 1, 0, 0));

        JScrollPane scrollPane = new JScrollPane();
        contentPanel.add(scrollPane);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        scrollPane.setViewportView(textArea);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        JButton okButton = new JButton("OK");
        okButton.setMnemonic('O');
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
            }
        });
        okButton.setActionCommand("OK");
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
        
        JButton saveButton = new JButton("Save...");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser outfile = new JFileChooser(outputFile.getParent());
                outfile.setSelectedFile(outputFile);
                outfile.setFileFilter(new FileNameExtensionFilter("Text (.txt)", "txt"));
                int rc = outfile.showSaveDialog(owner);
                if (rc == JFileChooser.APPROVE_OPTION)
                {
                    try {
                        File selected = outfile.getSelectedFile();
                        if (!selected.getName().contains("."))
                            selected = new File(selected.getPath() + ".txt");
                        FileOutputStream out = new FileOutputStream(selected);
                        out.write(textArea.getText().getBytes());
                        out.close();
                    }
                    catch (Exception e)
                    {
                        JOptionPane.showMessageDialog(owner, e, "File output error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
        });
        saveButton.setMnemonic('S');
        buttonPane.add(saveButton);
    }

    public JTextArea getTextArea() {
        return textArea;
    }
}
