import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

public class Tttboard extends JFrame implements ActionListener {
    JButton button11, button21, button31,
            button12, button22, button32,
            button13, button23, button33, start, reset, newGame;
    boolean myTurn;
    BufferedReader reader;
    BufferedWriter writer;
    Thread connection;
    Process prologProcess;
    String prolog;
    String ttt;

    public Tttboard(String prolog, String ttt) {
        this.prolog = prolog;
        this.ttt = ttt;
        button11 = new JButton("");
        button21 = new JButton("");
        button31 = new JButton("");
        button12 = new JButton("");
        button22 = new JButton("");
        button32 = new JButton("");
        button13 = new JButton("");
        button23 = new JButton("");
        button33 = new JButton("");
        start = new JButton("Start");
        reset = new JButton("Reset");
        newGame = new JButton("New Game");

        button11.setActionCommand("(1,1).");
        button21.setActionCommand("(2,1).");
        button31.setActionCommand("(3,1).");
        button12.setActionCommand("(1,2).");
        button22.setActionCommand("(2,2).");
        button32.setActionCommand("(3,2).");
        button13.setActionCommand("(1,3).");
        button23.setActionCommand("(2,3).");
        button33.setActionCommand("(3,3).");
        start.setActionCommand("start");
        reset.setActionCommand("reset");
        newGame.setActionCommand("new_game");

        Font font = new Font("monospaced", Font.PLAIN, 64);
        button11.setFont(font);
        button21.setFont(font);
        button31.setFont(font);
        button12.setFont(font);
        button22.setFont(font);
        button32.setFont(font);
        button13.setFont(font);
        button23.setFont(font);
        button33.setFont(font);

        button11.addActionListener(this);
        button21.addActionListener(this);
        button31.addActionListener(this);
        button12.addActionListener(this);
        button22.addActionListener(this);
        button32.addActionListener(this);
        button13.addActionListener(this);
        button23.addActionListener(this);
        button33.addActionListener(this);

        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startNewGame(prolog, ttt);
            }
        });
        
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        
        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String n = JOptionPane.showInputDialog("Enter the number of games:");
                if (n != null && !n.trim().isEmpty()) {
                    try {
                        int numberOfGames = Integer.parseInt(n.trim());
                        startNGames(numberOfGames);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid number.");
                    }
                }
            }
        });
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 3));
        panel.add(button11);
        panel.add(button21);
        panel.add(button31);
        panel.add(button12);
        panel.add(button22);
        panel.add(button32);
        panel.add(button13);
        panel.add(button23);
        panel.add(button33);
        panel.add(start);
        panel.add(reset);
        panel.add(newGame);

        this.setTitle("Tic Tac Toe sxk230025");
        Border panelBorder = BorderFactory.createLoweredBevelBorder();
        panel.setBorder(panelBorder);
        this.getContentPane().add(panel);
        this.setSize(400, 400);
        this.setLocation(900, 300);
        this.myTurn = true;

        Connector connector = new Connector(54321);
        connector.start();

        Socket socket;
        try {
            socket = new Socket("127.0.0.1", 54321);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (Exception ex) {
            System.out.println(ex);
        }

        connection = new Thread() {
            public void run() {
                while (true) {
                    try {
                        String s = reader.readLine();
                        computerMove(s);
                    } catch (Exception exx) {
                        System.out.println(exx);
                    }
                }
            }
        };
        connection.start();

        Thread show = new Thread() {
            public void run() {
                setVisible(true);
            }
        };
        EventQueue.invokeLater(show);

        try {
            prologProcess = Runtime.getRuntime().exec(prolog + " -f " + ttt);
        } catch (Exception exx) {
            System.out.println(exx);
        }

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent w) {
                if (prologProcess != null) prologProcess.destroy();
                System.exit(0);
            }
        });
    }

    void startNewGame(String prolog, String ttt) {
        connection.stop();
        new Tttboard(prolog, ttt);
    }

    void resetGame() {
        connection.stop();
        new Tttboard(prolog, ttt);
    }

    void startNGames(int n) {
        int i = 0;
        while(i<n){
            connection.stop();
            new Tttboard(prolog, ttt);
            i++;
        }
       
    }

    public static void main(String[] args) {
        String prolog = "C:\\Program Files\\swipl\\bin\\swipl";
        String ttt = "C:\\ttt-sxk230025\\ttt.pl";
        boolean noArgs = true;
        try {
            prolog = args[0];
            ttt = args[1];
            noArgs = false;
        } catch (Exception exx) {
            System.out.println("usage: java TicTacToe  <where prolog>  <where ttt>");
        }
        if (noArgs) {
            Object[] message = new Object[4];
            message[0] = new Label("  prolog command");
            message[1] = new JTextField(prolog);
            message[2] = new Label("  where ttt.pl ");
            message[3] = new JTextField(ttt);
            try {
                int I = JOptionPane.showConfirmDialog(null, message, "Where are Prolog and ttt.pl? ", JOptionPane.OK_CANCEL_OPTION);
                if (I == 2 | I == 1) System.exit(0);
                System.out.println(I);
                new Tttboard(((JTextField) message[1]).getText().trim(), ((JTextField) message[3]).getText().trim());
            } catch (Exception exx) {
            }
        } else {
            new Tttboard(prolog, ttt);
        }
    }

    void computerMove(String s) {
        String[] c = s.split(",");
        int x = Integer.parseInt(c[0].trim());
        int y = Integer.parseInt(c[1].trim());

        if (x == 1) {
            if (y == 1) button11.setText("O");
            else if (y == 2) button12.setText("O");
            else if (y == 3) button13.setText("O");
        } else if (x == 2) {
            if (y == 1) button21.setText("O");
            else if (y == 2) button22.setText("O");
            else if (y == 3) button23.setText("O");
        } else if (x == 3) {
            if (y == 1) button31.setText("O");
            else if (y == 2) button32.setText("O");
            else if (y == 3) button33.setText("O");
        }
        if (winner()) {
            JFrame frameC = new JFrame("Winner!");
            frameC.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Label label = new Label("Winner - Computer");
            frameC.setSize(300, 300);
            frameC.setLocation(600, 600);
            frameC.add(label);
            frameC.setVisible(true);
            connection.stop();
        } else myTurn = true;
    }

    public void actionPerformed(ActionEvent action) {
        if (!myTurn) return;
        String s = ((JButton) action.getSource()).getText();
        if (!s.equals("")) return;
        ((JButton) (action.getSource())).setText("X");
        try {
            writer.write(action.getActionCommand() + "\n");
            writer.flush();
        } catch (Exception exx) {
            System.out.println(exx);
        }
        myTurn = false;
        // Method to display the winner
    private void displayWinner() {
        JFrame frameH = new JFrame("Winner!");
        frameH.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameH.setSize(400, 400);
        frameH.setLocationRelativeTo(null); // Center the window
        frameH.add(new Label("Winner - human"));
        frameH.setVisible(true);
        connection.stop(); // Assuming 'connection' is a field or a valid object in the current context
}

// Usage within a condition
        if (winner()) {
            displayWinner();
}

    }

    boolean winner() {
        return line(button11, button21, button31) ||
                line(button12, button22, button32) ||
                line(button13, button23, button33) ||
                line(button11, button12, button13) ||
                line(button21, button22, button23) ||
                line(button31, button32, button33) ||
                line(button11, button22, button33) ||
                line(button13, button22, button31);
    }

    boolean line(JButton b, JButton c, JButton d) {
        if (!b.getText().equals("") && b.getText().equals(c.getText()) && c.getText().equals(d.getText())) {
            if (b.getText().equals("O")) {
                b.setBackground(Color.blue);
                c.setBackground(Color.blue);
                d.setBackground(Color.blue);
            } else {
                b.setBackground(Color.yellow);
                c.setBackground(Color.yellow);
                d.setBackground(Color.yellow);
            }
            return true;
        } else return false;
    }
}
