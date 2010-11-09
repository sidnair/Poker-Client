import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class GameServer implements PropertyChangeListener, Runnable {
	
	private ServerSocket server;
	private ArrayList<GameClientWorker> workerList;
	private static String newLine = System.getProperty("line.separator");
	private GameModel model;
	private JTextArea messageDisplay; 
    public static final String PLAYER_QUIT = "A player quit";
    public static final String REMOVE_ABSENT_PLAYERS = "Remove all the missing players";
	private boolean shouldScroll;
	
	public GameServer(int port, String frameName) {
		initModel(port);
	}
	
	public GameServer(int port) {
		initModel(port);
        initGUI(Integer.toString(port));
        redirectSystemStreams();
	}
	
	private void initModel(int port) {
		model = new GameModel(10, 5, 0, 2000, port);
		model.setPropertyChangeListener(this);
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		workerList = new ArrayList<GameClientWorker>();
		new Thread(this).start();
	}
	
	private void initGUI(String serverName) {
		JFrame displayFrame = new JFrame("Server" + " - " + serverName);
		displayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		messageDisplay = new JTextArea();
		displayFrame.getContentPane().add(makeScrollPane());
		displayFrame.setPreferredSize(new Dimension (400, 400));
        displayFrame.pack();
        displayFrame.setVisible(true);
	}
	
	private JScrollPane makeScrollPane() {
		messageDisplay.setEditable(false);
		messageDisplay.setLineWrap(true);
		messageDisplay.setWrapStyleWord(true);
		JScrollPane chatScrollPane = new JScrollPane(messageDisplay);
		chatScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		messageDisplay.addMouseListener(new 
				MouseAdapter() {
			public void mouseEntered(MouseEvent evt) {
				shouldScroll = false;
			}
			
			public void mouseExited(MouseEvent evt) {
				shouldScroll = true;
			}
		});
		chatScrollPane.getVerticalScrollBar().addMouseListener(new 
				MouseAdapter() {
			public void mouseEntered(MouseEvent evt) {
				shouldScroll = false;
			}
			
			public void mouseExited(MouseEvent evt) {
				shouldScroll = true;
			}
		});
		chatScrollPane.getVerticalScrollBar().addAdjustmentListener(new 
				AdjustmentListener() {  
					public void adjustmentValueChanged(AdjustmentEvent e) {
						if (shouldScroll) {
							e.getAdjustable().setValue(e.getAdjustable().getMaximum());
						}
					}
				});
		return chatScrollPane;
	}
	
	private void updateTextArea(final String text) {  
		  SwingUtilities.invokeLater(new Runnable() {  
		    public void run() {  
		      messageDisplay.append(text);  
		    }  
		  });  
		}  
		  
		private void redirectSystemStreams() {  
		  OutputStream out = new OutputStream() {  
		    @Override  
		    public void write(int b) throws IOException {  
		      updateTextArea(String.valueOf((char) b));  
		    }  
		  
		    @Override  
		    public void write(byte[] b, int off, int len) throws IOException {  
		      updateTextArea(new String(b, off, len));  
		    }  
		  
		    @Override  
		    public void write(byte[] b) throws IOException {  
		      write(b, 0, b.length);  
		    }  
		  };  
		  
		  System.setOut(new PrintStream(out, true));  
		  System.setErr(new PrintStream(out, true));  
		}  

	public void run(){
		  new Thread(model).start();
		  while(true){
			    GameClientWorker w;
			    try{
			      Socket s = server.accept();
                              System.out.print("***NEW CONNECTION***" + "\t" +
			    		  s.getInetAddress().getHostName());
                              String name = null;
                              String path = null;
                              ObjectOutputStream out = null;
                              ObjectInputStream in = null;
                              try {
                                  out = new ObjectOutputStream(s.getOutputStream());
                                  in = new ObjectInputStream(s.getInputStream());
                                  name = (String) (in.readObject());
                                  path = (String) (in.readObject());
                                  System.out.println(name + " \t" + path);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
	                    System.exit(-1);
					}
					Player temp = new Player(name, path, model);
					w = new GameClientWorker(in, out, temp, this);
					Thread t = new Thread(w);
					workerList.add(w);
	                t.start();
					model.addPlayer(temp);
			    } catch (IOException e) {
                                e.printStackTrace();
			      System.out.println("Accept failed: " + "...");
			      System.exit(-1);
			    }
			  }
	}

  	protected void finalize(){
  		//Objects created in run method are finalized when program terminates and thread exits
  		try{
	        server.close();
	    } catch (IOException e) {
	        System.out.println("Could not close socket");
	        System.exit(-1);
	    }
  	}
  	
  	private boolean isNotWorkerMessage(String s) {
  		return !(s.equals(GameServer.PLAYER_QUIT) || 
  				s.equals(GameServer.REMOVE_ABSENT_PLAYERS));
  	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (isNotWorkerMessage(evt.getPropertyName())) {
			for (GameClientWorker gcw : workerList) {
				gcw.sendChange(evt);
			}
		} else if (evt.getPropertyName().equals(GameServer.PLAYER_QUIT)) {
			for (int i = 0; i < workerList.size(); i++) {
				if (workerList.get(i).getPlayer().equals((Player) evt.getNewValue())) {
					model.removePlayer(workerList.get(i).getPlayer());
				}
			}
		} else if (evt.getPropertyName().equals(GameServer.REMOVE_ABSENT_PLAYERS)) {
			ArrayList<Integer> toRemove = new ArrayList<Integer>();
			for (int i = 0; i < workerList.size(); i++) {
				for (Player p : (ArrayList<Player>) evt.getNewValue()) {
					if (workerList.get(i).getPlayer().equals(p)) {
						toRemove.add(i);
					}
				}
			}
			int counter = 0;
			for (Integer i : toRemove) {
				workerList.get(i - counter).setPlaying(false);
				workerList.remove(i - counter);
				counter++;
			}
			model.notifyPlayersRemoved();
		}
	}
	
	public static void main(String[] args) {
		if (args.length == 1) {
			GameServer gs = new GameServer(Integer.parseInt(args[0]));
		} else if (args.length == 2) {
			GameServer gs = new GameServer(Integer.parseInt(args[0]), args[1]);
		}
	}
 	
}
