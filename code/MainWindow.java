import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainWindow
{
    /* Properties */
    //
        private Parser parser;
        public Parser GetParser() { if (parser == null) parser = new Parser(); return parser; }
    //


    public void CreateWindow()
    {
        // For now this is always null
        String[] args = null;

        JFrame frm = new JFrame("WorldWeaver");
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frm.setLocationRelativeTo(null);
        frm.setLocation(50, 50);
        frm.setPreferredSize(new Dimension(1024, 768));

        JTextArea txtOutput = new JTextArea(5, 20);
        txtOutput.setLineWrap(true);
        txtOutput.setWrapStyleWord(true);
        txtOutput.setFont(new Font("monospaced", Font.PLAIN, 13));
        txtOutput.setEditable(false);
        txtOutput.setBackground(Color.BLACK);
        txtOutput.setForeground(Color.WHITE);
        txtOutput.setBorder(BorderFactory.createLineBorder(new Color (0, 0, 0, 0), 10));
        // UIManager.put("TextArea.margin", new Insets(10,10,10,10));

        JScrollPane scr = new JScrollPane(txtOutput);
        scr.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scr.setBorder(BorderFactory.createLineBorder(new Color (180, 180, 180, 255), 1));

        JTextField txtInput = new JTextField();
        txtInput.setBackground(Color.BLACK);
        txtInput.setForeground(Color.WHITE);
        txtInput.setBorder(BorderFactory.createLineBorder(new Color (0, 0, 0, 0), 4));

        frm.add(scr, BorderLayout.CENTER);
        frm.add(txtInput, BorderLayout.SOUTH);

        frm.addWindowListener( new WindowAdapter() {
            public void windowOpened( WindowEvent e )
            {
                txtInput.requestFocus();
            }
        }); 

        frm.pack();
        frm.setVisible(true);

        GetParser().SetWindow(frm);
        GetParser().SetOutputField(txtOutput);
        GetParser().SetInputField(txtInput);
        GetParser().Listener(args);
    }
}
