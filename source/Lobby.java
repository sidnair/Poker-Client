import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * Lobby that provides a GUI for the player to select a name, port, host, and
 * avatar path.
 *  
 * @author Sid Nair
 *
 */
public class Lobby {

	/**
	 * Frame that contains all the text fields and submit button.
	 */
	private JFrame frame;
	
	/**
	 * Port ID used to connect with the server.
	 */
	private JTextField portID;
	
	/**
	 * Name of the host to connect to.
	 */
	private JTextField hostName;
	
	/**
	 * Name to use for the player.
	 */
	private JTextField playerName;
	
	/**
	 * Path to use for the avatar.
	 */
	private JTextField avatarPath;
	
	/**
	 * Connects to the sever.
	 */
	private JButton submitConnectionButton;

	/**
	 * Iniitializes the frame of the lobby and all its components.
	 */
	public Lobby() {
		initComponents();
	}

	/**
	 * Initializes the frame, layout, and makes the panel with al the ports.
	 */
	private void initComponents() {
		frame = new JFrame("Lobby");
		frame.setLayout(new FlowLayout());
		frame.setContentPane(makePortPanel());
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setVisible(true);
	}

	/**
	 * Creates the port panel for the frame. This contains all the text fields
	 * and submit button.
	 * 
	 * @return
	 */
	private JPanel makePortPanel() {
		JPanel portInfo = new JPanel(new GridLayout (1, 5));
		submitConnectionButton = makeConnectionButton();
		portID = new JTextField("10071");
		portID.setBorder(new TitledBorder("Port ID"));
		//hostName = new JTextField("76.166.189.64");
		hostName = new JTextField("69.228.38.95");
		hostName.setBorder(new TitledBorder("Hostname"));
		playerName = new JTextField();
		playerName.setBorder(new TitledBorder("Player Name"));
		avatarPath = new JTextField("images/avatars/panda.png");
		avatarPath.setBorder(new TitledBorder("Avatar Path"));
		portInfo.add(playerName);
		portInfo.add(portID);
		portInfo.add(hostName);
		portInfo.add(avatarPath);
		portInfo.add(submitConnectionButton);
		return portInfo;
	}

	/**
	 * Makes the connect button with an associated action listener. When clicked
	 * the button runs the main method of the view, passing appropriate values
	 * to the view's constructor. It disposes the frame when complete.
	 * 
	 * @return the JButton to be used to connect to the server
	 */
	private JButton makeConnectionButton() {
		JButton connection = new JButton ("Establish Connection");
		connection.addActionListener(new
				ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
                    GameClient.main(new String[] { hostName.getText(),
                    		portID.getText(),
                    		playerName.getText(), avatarPath.getText() });
                    frame.dispose();
				} catch (NumberFormatException e) {
					ErrorPrinter.printError("Port IDs are numbers.");
				}
			}
		});
		return connection;
	}

	/**
	 * Starts a new Lobby.
	 * @param args
	 */
	public static void main (String[] args) {
		new Lobby();
	}

}