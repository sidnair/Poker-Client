package pokerclient.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import pokerclient.model.GameModel;
import pokerclient.model.GameSettings;
import pokerclient.model.Player;

public class GameServer implements PropertyChangeListener, Runnable {
	
	public static final String PLAYER_QUIT = "A player quit";
    public static final String REMOVE_ABSENT_PLAYERS = "Remove all the missing players";

	public static final int DEFAULT_BB = 10;
	public static final int DEFAULT_SB = 5;
	public static final int DEFAULT_ANTE = 0;
	public static final int DEFAULT_STACK = 2000;
	public static final int DEFAULT_FRAME_WIDTH = 400;
	public static final int DEFAULT_FRAME_HEIGHT = 400;
	public static final int DEFAULT_TIME_BANK = 500 * 1000;
	public static final boolean DEFAULT_TOP_OFF = true;
	public static final int DEFAULT_MAX_PLAYERS = 6;
    
	private ServerSocket server;
	private ArrayList<GameClientWorker> workers;
	private GameModel model;
	private JTextArea messageDisplay;     
	private boolean shouldScroll;
	
	private ReentrantLock workersLock = new ReentrantLock();
	
	public GameServer(int port, String frameName) {
		initModel(port);
		initGUI(frameName);
		redirectSystemStreams();
	}
	
	public GameServer(int port) {
		this(port, Integer.toString(port));
	}
	
	private void initModel(int port) {
		GameSettings settings = new GameSettings(DEFAULT_STACK, DEFAULT_BB,
				DEFAULT_SB, DEFAULT_ANTE, DEFAULT_TIME_BANK,
				DEFAULT_MAX_PLAYERS, DEFAULT_TOP_OFF);
		model = new GameModel(settings, port);
		model.setPropertyChangeListener(this);
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		workers = new ArrayList<GameClientWorker>();
		new Thread(this).start();
	}
	
	private void initGUI(String serverName) {
		JFrame displayFrame = new JFrame("Server" + " - " + serverName);
		displayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		displayFrame.getContentPane().add(makeScrollPane());
		displayFrame.setPreferredSize(
				new Dimension(DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT));
        displayFrame.pack();
        displayFrame.setVisible(true);
	}
	
	private JScrollPane makeScrollPane() {
		messageDisplay = new JTextArea();
		messageDisplay.setEditable(false);
		messageDisplay.setLineWrap(true);
		messageDisplay.setWrapStyleWord(true);
		
		JScrollPane chatScrollPane = new JScrollPane(messageDisplay);
		chatScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		controlScrolling(chatScrollPane);
		return chatScrollPane;
	}

	/**
	 * Avoids scrolling down when reading the logs.
	 * @param chatScrollPane the pane for which scrolling should be limited.
	 */
	private void controlScrolling(JScrollPane chatScrollPane) {
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
		chatScrollPane.getVerticalScrollBar().addAdjustmentListener(
			new AdjustmentListener() {
				public void adjustmentValueChanged(AdjustmentEvent e) {
					if (shouldScroll) {
						e.getAdjustable().setValue(e.getAdjustable().getMaximum());
					}
				}
		});
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
		while (true) {
			try {
				System.out.println("server accepting");
				GameClientWorker w = new GameClientWorker(server.accept(), model, workersLock, workers, this);
				Thread t = new Thread(w);
				t.start();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

  	protected void finalize(){
  		// Objects created in run method are finalized when program terminates
  		// and thread exits
  		try{
	        server.close();
	    } catch (IOException e) {
	        System.err.println("Could not close socket");
	        System.exit(1);
	    }
  	}
  	
  	private boolean isGameModelMessage(String s) {
  		return !s.equals(GameServer.PLAYER_QUIT) && 
  				!s.equals(GameServer.REMOVE_ABSENT_PLAYERS);
  	}

	// Lock should be held before calling this
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		workersLock.lock();
		if (isGameModelMessage(evt.getPropertyName())) {
			for (GameClientWorker gcw : workers) {
				gcw.sendChange(evt);
			}
		} else if (evt.getPropertyName().equals(GameServer.PLAYER_QUIT)) {
			removePlayer(evt);
		} else if (evt.getPropertyName().equals(GameServer.REMOVE_ABSENT_PLAYERS)) {
			removeAbsentPlayers(evt);
		}
		workersLock.unlock();
	}

	// Lock should be held before calling this
	private void removePlayer(PropertyChangeEvent evt) {
		for (int i = 0; i < workers.size(); i++) {
			GameClientWorker gcw = workers.get(i);
			Player gcwPlayer = gcw.getPlayer();
			if (gcwPlayer.equals((Player) evt.getNewValue())) {
				model.sitOutPlayer(gcwPlayer);
				// TODO - verify that these last two steps are okay.
				gcw.setPlaying(false);
				workers.remove(i);
				return;
			}
		}
		throw new AssertionError("No player removed");
	}

	// Lock should be held before calling this
	private void removeAbsentPlayers(PropertyChangeEvent evt) {
		@SuppressWarnings("unchecked")
		ArrayList<Player> absentPlayers = (ArrayList<Player>) evt.getNewValue();
		Iterator<GameClientWorker> workerIter = workers.iterator();
		while (workerIter.hasNext()) {
			GameClientWorker worker = workerIter.next();
			if (absentPlayers.contains(worker.getPlayer())) {
				worker.setPlaying(false);
				workerIter.remove();
			}
		}
		model.notifyPlayersRemoved();
	}
 	
}
