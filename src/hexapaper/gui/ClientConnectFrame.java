package hexapaper.gui;

import hexapaper.source.HPSklad;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import core.JNumberTextField;


public class ClientConnectFrame extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HPSklad sk = HPSklad.getInstance();
	JFrame frame;

	public ClientConnectFrame() {
		frame = new JFrame(sk.str.ConnectFrame);
		frame.setSize(300, 200);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setResizable(false);
		setLayout(new GridLayout(4, 1, 0, 10));
		setBorder(new TitledBorder(sk.str.ConnectFrame));
		init();
		frame.getContentPane().add(this);
		frame.setVisible(true);
	}

	JTextField ipfield;
	JNumberTextField portfield;
	JTextField namefield;

	protected void init() {
		JPanel first = new JPanel(new GridLayout(1, 2, 10, 0));
		JLabel polhex = new JLabel(sk.str.ipField);
		ipfield = new JTextField(sk.c.IP);
		ipfield.addFocusListener(new Listener());
		first.add(polhex);
		first.add(ipfield);

		JPanel second = new JPanel(new GridLayout(1, 2, 10, 0));
		JLabel numRow = new JLabel(sk.str.portField);
		portfield = new JNumberTextField();
		portfield.setNumber(sk.c.port);
		portfield.addFocusListener(new Listener());
		second.add(numRow);
		second.add(portfield);

		JPanel third = new JPanel(new GridLayout(1, 2, 10, 0));
		JLabel numCol = new JLabel(sk.str.nameField);
		namefield = new JTextField(sk.c.lastName);
		namefield.addFocusListener(new Listener());
		third.add(numCol);
		third.add(namefield);

		JButton hotovo = new JButton(sk.str.Connect);
		hotovo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				connect();
			}
		});

		add(first);
		add(second);
		add(third);
		add(hotovo);
	}

	private void connect() {
		sk.c.IP = ipfield.getText();
		sk.c.port = portfield.getInt();
		sk.c.lastName = namefield.getText();
		sk.c.saveConfig();
		sk.connect();
		frame.setVisible(false);
	}

	private class Listener extends FocusAdapter {
		@Override
		public void focusGained(FocusEvent e) {
			((JTextField) e.getComponent()).setSelectionStart(0);
			((JTextField) e.getComponent()).setSelectionEnd(((JTextField) e.getComponent()).getText().length());
		}
	}

}
