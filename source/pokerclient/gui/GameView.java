package pokerclient.gui;

import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;

import pokerclient.controller.GameState;
import pokerclient.model.Board;
import pokerclient.model.Card;
import pokerclient.model.GameModel;
import pokerclient.model.Player;
import pokerclient.model.Pot;

/**
 * View of the game of poker.
 */
public class GameView extends AbstractView<GameModel> implements Runnable {
	
	/**
	 * System-independent newline character;
	 */
	public static String newline = System.getProperty("line.separator");
	
	public static final String MEDIA_ROOT = "../media/";
	
	/**
	 * True if the handHistory should autoscroll. 
	 */
	private boolean shouldScroll;
	
	/**
	 * Layered GUI that holds all the different components of the game.
	 */
	private JComponent GUI;
	
	/**
	 * Frame of the game.
	 */
	private PokerFrame displayFrame;
	
	/**
	 * Chat area that displays the hand history.
	 */
	private JTextArea chatBox;
	
	private boolean isActive;
	
	/**
	 * List of the boxes that contain JComponent representations of the players.
	 * These have avatars, cards, stack sizes, and names.
	 */
	private ArrayList<PlayerBoxComponent> boxes;
	
	/**
	 * Button used to fold.
	 */
	private FoldButton fold;
	
	/**
	 * Button used to check or call.
	 */
	private CallButton call;
	
	/**
	 * Button used to bet or raise.
	 */
	private RaiseButton raise;
	
	/**
	 * Slider used to adjust bet size.
	 */
	private BetSizeSlider slider;
	
	/**
	 * Label that contains the fold, call, and raise buttons, slider, and bet
	 * size text field.
	 */
	private JLabel actionLabel;
	
	/**
	 * Text field used to enter bet sizing.
	 */
	private BetSizeTextField betDisplay;
	
	/**
	 * Label that wraps the ImageIcon that constitutes the background image
	 * of the table.
	 */
	private IconComponent bg;
	
	/**
	 * Label that lists the pot names and sizes.
	 */
	private JLabel potLabel;
	
	/**
	 * Scroll pane that wraps the chat area.
	 */
	private JScrollPane chatScrollPane;
	
	/**
	 * Panel that contains the board.
	 */
	private BoardPanel boardPanel;
	
	/**
	 * Displays all the raise sizes the players have made. These do not have
	 * any text if the last raise size was zero.
	 */
	private ArrayList<JLabel> raiseSizes;
	
	/**
	 * Layer to use for the background.
	 */
	private static final Integer BACKGROUND_LAYER = new Integer(0);
	
	/**
	 * Layer to use for the boxes.
	 */
	private static final Integer BOX_LAYER = new Integer(1);
	
	/**
	 * Top row of table.
	 */
	private static final int TABLE_Y_TOP = 5;
	
	/**
	 * Middle row of table.
	 */
	private static final int TABLE_Y_MIDDLE = 155;
	
	/**
	 * Bottom row of table.
	 */
	private static final int TABLE_Y_BOTTOM = 310;
	
	/**
	 * Left column of table.
	 */
	private static final int TABLE_X_LEFT = 70;
	
	/**
	 * Center left column of table.
	 */
	private static final int TABLE_X_CENTER_LEFT = 230;
	
	/**
	 * Center right column of table.
	 */
	private static final int TABLE_X_CENTER_RIGHT = 485;
	
	/**
	 * Right column of table.
	 */
	private static final int TABLE_X_RIGHT = 645;
	
	/**
	 * Small modifier to fix a resizing glitch. 
	 */
	private static final int RESIZE_GLITCH_FIX = 10;
	
	/**
	 * X-coordinate to use for the chat area.
	 */
	private static final int TEXT_X = 0;
	
	/**
	 * Y-coordinate to use for the chat area
	 */
	private static final int TEXT_Y = 350;
	
	/**
	 * Width of the chat area.
	 */
	private static final int TEXT_RIGHT_BOUND = 200;
	
	/**
	 * X-coordinate of the pot labels.
	 */
	private static final int POT_X = (TABLE_X_CENTER_LEFT + 
			TABLE_X_CENTER_RIGHT) / 2 - 10;
	
	/**
	 * Y-coordinate of the pot labels.
	 */
	private static final int POT_Y = TABLE_Y_MIDDLE - 50;
	
	/**
	 * Array of coordinates used for the player box components.
	 */
	private static final int[][] coords = new int[][] {
		new int[] {TABLE_X_CENTER_RIGHT, TABLE_Y_TOP},
		new int[] {TABLE_X_RIGHT , TABLE_Y_MIDDLE},
		new int[] {TABLE_X_CENTER_RIGHT - 30 - 50, TABLE_Y_BOTTOM},
		new int[] {TABLE_X_CENTER_LEFT , TABLE_Y_BOTTOM},
		new int[] {TABLE_X_LEFT, TABLE_Y_MIDDLE},
		new int[] {TABLE_X_CENTER_LEFT , TABLE_Y_TOP},
	};
	
	/**
	 * Array of coordinates used for the raise size displays.
	 */
	private static final int[][] raiseSizeCoords = new int[][] {
		new int[] {TABLE_X_CENTER_RIGHT + 75, TABLE_Y_TOP - 35},
		new int[] {TABLE_X_RIGHT - 20, TABLE_Y_MIDDLE - 25},
		new int[] {TABLE_X_CENTER_RIGHT - 30 - 50 + 30, TABLE_Y_BOTTOM - 75},
		new int[] {TABLE_X_CENTER_LEFT + 30, TABLE_Y_BOTTOM - 75},
		new int[] {TABLE_X_LEFT + 80, TABLE_Y_MIDDLE - 25},
		new int[] {TABLE_X_CENTER_LEFT - 20, TABLE_Y_TOP - 35},
	};
	
	/**
	 * Array of coordinates for the button.
	 */
	private static final int[][] buttonCoords = new int[][] {
		new int[] {TABLE_X_CENTER_RIGHT + 85, TABLE_Y_TOP + 75},
		new int[] {TABLE_X_RIGHT - 35, TABLE_Y_MIDDLE + 75},
		new int[] {TABLE_X_CENTER_RIGHT - 30 - 50 + 85, TABLE_Y_BOTTOM + 75},
		new int[] {TABLE_X_CENTER_LEFT  + 85, TABLE_Y_BOTTOM + 75},
		new int[] {TABLE_X_LEFT + 85, TABLE_Y_MIDDLE + 75},
		new int[] {TABLE_X_CENTER_LEFT  - 35, TABLE_Y_TOP + 75},
	};
	
	/**
	 * X-coordinate of board.
	 */
	private static final int BOARD_X = 305;
	
	/**
	 * Y-coordinate of board.
	 */
	private static final int BOARD_Y = 210;
	
	/**
	 * Width of each button.
	 */
	private static final int BUTTONS_WIDTH = 295;
	
	/**
	 * Height of each button.
	 */
	private static final int BUTTONS_HEIGHT = 40;
	
	/**
	 * Height of the slider.
	 */
	private static final int SLIDER_HEIGHT = 50;
	
	/**
	 * X-coordinate of action label.
	 */
	private static final int ACTION_LABEL_X = 500;
	
	/**
	 * Y-coordinate of action label.
	 */
	private static final int ACTION_LABEL_Y = 445;
	
	/**
	 * String used to notify model of player actions taken locally.
	 */
	public static final String PLAYER_ACTION = "Player took action";
	
	public static final String FOLD_MADE = "Fold made";
	
	public static final String RAISE_MADE = "Raise made";
	
	public static final String BET_MADE = "Bet made";
	
	public static final String CALL_MADE = "Call made";
	
	public static final String CHECK_MADE = "Check made";
	
	/**
	 * Used when the timer has expired.
	 */
	public static final String TIMER_EXPIRED = "Timer expired - autofold";
	
	/**
	 * Used to alert the player when necessary.
	 */
	public static final String TIMER_NOTIFICATION = "Timer notification - autofold";
	
	/**
	 * String used to update the GUI.
	 */
	public static final String GENERATE_GUI_START_OF_TURN = "Generate GUI - start of turn";
	
	public static final String GENERATE_GUI_START_OF_STREET = "Generate GUI - start of street";
	
	public static final String GENERATE_GUI_SHOWDOWN = "Generate GUI - showdown";
	
	public static final String GENERATE_GUI_END_OF_HAND = "Generate GUI - end of hand";
	
	public static final String GENERATE_GUI_ALL_IN = "Generate GUI - everyone's all-in";
	
	public static final String GENERATE_GUI_PLAYER_JOINED = "Generate GUI - a player joined";
	
	/**
	 * Updates the components of the action label.
	 */
	public static final String UPDATE_ACTION_LABEL = "Update Action Label";
	
	/**
	 * Updates the button's in the GUI.
	 */
	public static final String UPDATE_BTN = "UpdateBTN";
	
	/**
	 * Updates the hand history in the chat area.
	 */
	public static final String UPDATE_CHAT = "UpdateChat";
	
	/**
	 * Physical representation of the button.
	 */
	private IconComponent buttonGraphic;
	
	private PropertyChangeListener listener;
	
	private String playerName;
	
	private BankTimer timer;
	
	private boolean cardsFlipped;
	
	private int bigBlind;
	
	private AudioClip timerStartSound;
	
	private AudioClip timerNotificationSound;
	
	private AudioClip checkSound;
	
	private AudioClip callSound;
	
	private AudioClip foldSound;
	
	private AudioClip raiseSound;
	
	private AudioClip betSound;
	
	private boolean godMode;
	
	private double aspectRatio;
	private double totalScale;
	
	private JPanel boxPanel;
	private JPanel raiseSizePanel;
	private JPanel bgPanel;
	private JPanel buttonPanel;
	private JPanel potLabelPanel;
	private JPanel actionLabelPanel;
	private JPanel boardPanelWrapper;
	private JPanel chatPanel;
	private double lastWidth; //must be double to avoid integer division


	/**
	 * Constructor that initializes basic variables
	 */
	public GameView(PropertyChangeListener listener, String playerName, int time,
			int bigBlind, boolean godMode) {
		super();
		this.listener = listener;
		shouldScroll = true;
		buttonGraphic = new IconComponent(MEDIA_ROOT + "images/btn.jpg");
		this.playerName = playerName;
		timer = new BankTimer(time, this);
		cardsFlipped = false;
		this.bigBlind = bigBlind;
		totalScale = 1;
		initSounds();
		this.godMode = godMode;
	}

	private void initSounds() {
		try {
			timerNotificationSound = java.applet.Applet.newAudioClip(
					Utils.pathToURL(MEDIA_ROOT + "sounds/longBeep.wav"));
			timerStartSound = java.applet.Applet.newAudioClip(
					Utils.pathToURL(MEDIA_ROOT + "sounds/shortBeep.wav"));
			checkSound = java.applet.Applet.newAudioClip(
					Utils.pathToURL(MEDIA_ROOT + "sounds/check.wav"));
			callSound = java.applet.Applet.newAudioClip(
					Utils.pathToURL(MEDIA_ROOT + "sounds/call.wav"));
			betSound = java.applet.Applet.newAudioClip(
					Utils.pathToURL(MEDIA_ROOT + "sounds/bet.wav"));
			raiseSound = java.applet.Applet.newAudioClip(
					Utils.pathToURL(MEDIA_ROOT + "sounds/raise.wav"));
			foldSound = java.applet.Applet.newAudioClip(
					Utils.pathToURL(MEDIA_ROOT + "sounds/fold.wav"));
		} catch (MalformedURLException e) {
			System.err.println("Error initializing sounds.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Initializes and draws all the graphical components. 
	 */
	public void run() {
		boardPanel = new BoardPanel(new int[] {BOARD_X, BOARD_Y});
		chatBox = new JTextArea();
		boxes = new ArrayList<PlayerBoxComponent>();
		makeActionLabel();
		displayFrame = new PokerFrame("Poker" + " - " + playerName);
		displayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		makeChatScrollPane(546); //TO DO: FIX MAGIC
		GUI = new JLayeredPane();
		raiseSizes = new ArrayList<JLabel>();
		generatePotDisplay();
		boxPanel = makePanel(coords);
		raiseSizePanel = makePanel(raiseSizeCoords);
		buttonPanel = makePanel(buttonCoords);
		buttonPanel.add(buttonGraphic);
		potLabelPanel = makePanel(new int[][] {{POT_X, POT_Y}});
		potLabelPanel.add(potLabel);
		GUI.add(raiseSizePanel, BOX_LAYER);
		GUI.add(boxPanel, BOX_LAYER);
		GUI.add(buttonPanel, BOX_LAYER);
		//GUI.add(buttonGraphic, BOX_LAYER);
		GUI.add(potLabelPanel, BOX_LAYER);
		//GUI.add(potLabel, BOX_LAYER);
		chatPanel = makePanel(new int[][]{{TEXT_X, TEXT_Y}});
		chatPanel.add(chatScrollPane);
		GUI.add(chatPanel, BOX_LAYER);
		bg = new IconComponent(MEDIA_ROOT + "images/bg.jpg");
		bgPanel = makePanel(new int[][] {{0 ,0}});
		bgPanel.add(bg);
		GUI.add(bgPanel, BACKGROUND_LAYER);
		actionLabelPanel = makePanel(new int[][] {{ACTION_LABEL_X, ACTION_LABEL_Y}});
		actionLabelPanel.add(actionLabel);
		boardPanelWrapper = makePanel(new int[][] {{BOARD_X, BOARD_Y}});
		boardPanelWrapper.add(boardPanel);
		GUI.add(actionLabelPanel, BOX_LAYER);
		GUI.add(boardPanelWrapper, BOX_LAYER);
		GUI.setPreferredSize(new Dimension(bg.getWidth(), bg.getHeight()));
		GUI.setOpaque(true); //content panes must be opaque
		GUI.setVisible(true);
		displayFrame.setContentPane(GUI);
        displayFrame.pack();
        displayFrame.setSize(displayFrame.getWidth() - RESIZE_GLITCH_FIX,
        		displayFrame.getHeight() - RESIZE_GLITCH_FIX);
        aspectRatio = 1.0 * displayFrame.getHeight() / displayFrame.getWidth();

        displayFrame.setResizable(false);
        displayFrame.setVisible(true);
        
    	displayFrame.addComponentListener(new ComponentAdapter() {
    		public void componentResized(ComponentEvent evt) {
    			
//    			displayFrame.setSize(1024, 800);
				//maintains aspect ratio
    			// aspect ratio is messed up... w is 11, h is 2147483647
				displayFrame.setSize(displayFrame.getWidth(), (int) (displayFrame.getWidth() * aspectRatio));
				
		        double ratio = 1;
		        double currentWidth = displayFrame.getWidth();
		        if (currentWidth != 0 && lastWidth !=0) {
		        	ratio = currentWidth  / lastWidth;
		        }
		        if (lastWidth != currentWidth) {
		        	lastWidth = currentWidth;
			        totalScale *= ratio;
					IconComponent.setRatio(totalScale);
		        }
				updatePanel(boxPanel);
				updatePanel(raiseSizePanel);
				updatePanel(bgPanel);
				updatePanel(buttonPanel);
				updatePanel(potLabelPanel);
				updatePanel(boardPanelWrapper);
				updatePanel(actionLabelPanel);
				updatePanel(chatPanel);
    		}
		});
		displayFrame.addMouseWheelListener( new 
				MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent evt) {
				if (displayFrame.isFocused() && actionLabel.isVisible()) {
					int notches = evt.getWheelRotation();
					if (notches < 0) { //up
						slider.increment(bigBlind);
					} else if (notches > 0) {
						slider.increment(-bigBlind);
					}
				}
			}
		});
	}
	
	private JPanel makePanel(int[][] someCoords) {
		JPanel panel = new JPanel(new PokerLayoutManager(someCoords));
		panel.setOpaque(false);
		return panel;
	}
	
	private void updatePanel(JPanel panel) {
		panel.setBounds(GUI.getBounds());
		panel.setPreferredSize(displayFrame.getSize());
		LayoutManager layout = panel.getLayout();
		layout.layoutContainer(panel);
	}
	
	private void updatePanel(JPanel panel, double ratio) {
		panel.setBounds(GUI.getBounds());
		panel.setPreferredSize(displayFrame.getSize());
		LayoutManager layout = panel.getLayout();
		if (layout instanceof PokerLayoutManager) {
			((PokerLayoutManager) layout).layoutContainer(panel, ratio);
		} else {
			layout.layoutContainer(panel);
		}
	}
	
	public void adjustCoords(double ratio, int[][] array) {
		for (int[] ii : array) {
			for (int i : ii) {
				i *= ratio;
			}
		}
	}
	
	public static void fixBounds(JComponent component, double ratio) {		
		component.setBounds((int) (component.getX() * ratio), 
				(int) (component.getY() * ratio), 
				(int) (component.getWidth() * ratio),
				(int) (component.getHeight() * ratio));
	}
	
	/**
	 * Generates the GUI, updating the player boxes, pot display, action label,
	 * board, repainting the frame, and notifying the associated models when
	 * this process is complete.
	 * 
	 * @param gs GameState that contains info with which to update the GUI
	 */
	private void generateGUI(GameState gs, String updateType) {
		if (timer.isRunning()) {
			timer.reset();
		}
		if (updateType.equals(GameView.GENERATE_GUI_START_OF_TURN)) {
			isActive = gs.getActiveName().equals(this.playerName);
			generateBoxes(gs.getAllPlayers(), gs);
			updatePotDisplay(gs.getPots());
			updateActionLabel(gs);
			timer.setBar(boxes.get(gs.getStableIndex()).getBar(), isActive);
			timer.start();
			if (isActive) {
				timerStartSound.play();
			}
		} else if (updateType.equals(GameView.GENERATE_GUI_ALL_IN)) {
			//generateRaiseSizes(gs); //won't do anything because the put in pot for this street is 0
			if (!cardsFlipped) {
				generateBoxes(gs.getAllPlayers(), gs);
				updatePotDisplay(gs.getPots());
				showdownGUI(gs);
				cardsFlipped = true;
			}
		} else if (updateType.equals(GameView.GENERATE_GUI_START_OF_STREET)) {
			updateBoard(gs.getBoard());
			updatePotDisplay(gs.getPots());
			if (!cardsFlipped) {
				generateBoxes(gs.getAllPlayers(), gs);
			}
		} else if (updateType.equals(GameView.GENERATE_GUI_SHOWDOWN)) {
			if (!cardsFlipped) {
				showdownGUI(gs);
			}
		} else if (updateType.equals(GameView.GENERATE_GUI_END_OF_HAND)) {
			generateBoxes(gs.getAllPlayers(), gs);
			updatePotDisplay(gs.getPots());
			boardPanel.reset();
			cardsFlipped = false;
		}  else if (updateType.equals(GameView.GENERATE_GUI_PLAYER_JOINED)) {
			generateBoxes(gs.getAllPlayers(), gs);
			updatePotDisplay(gs.getPots());
		}
		displayFrame.repaint();
	}
	
	private void showdownGUI(GameState gs) {
		int index = 0;
		Iterator<Player> iter = gs.getAllPlayers();
		while (iter.hasNext()) {
			Player p = iter.next();
			if (p.isInHand()) {
				boxes.get(index).setHand(p.getHand(), true, gs.getFoldedStatus(index));
			}
			index++;
		}
	}
	
	//makes button to refresh the GUI. For testing purposes.
	private void makeRefreshButton() {
		JButton refresh = new JButton("Refresh");
		refresh.addActionListener( new ActionListener()
		{
			public void actionPerformed(ActionEvent evt) {
				displayFrame.repaint();
				GUI.repaint();
				for (PlayerBoxComponent pbc : boxes) {
					pbc.repaint();
				}
			}
		});
		refresh.setBounds(new Rectangle(0, 0, 100, 100));
		GUI.add(refresh, BOX_LAYER);
	}
	
	/**
	 * Updates the board JComponent with the board from the model.
	 * 
	 * @param board board fom the model
	 */
	private void updateBoard(Board board) {
		Iterator<Card> iter = board.iterator();
		int index = 0;
		while (iter.hasNext()) {
			Card c = iter.next();
			boardPanel.setCard(c, index);
			index ++;
		}
	}
	
	/**
	 * Updates the chat box with a new String to add.
	 *  
	 * @param hh hand history to append.
	 */
	private void updateChatBox(String hh) {
		chatBox.append(hh);
	}
	
	/**
	 * Creates a chat scroll pane, wrapping a chat text area and making mouse
	 * listeners that autoscroll down if the mouse isn't over the chat area.
	 * 
	 * @param bottomBound bottom bound to use for the box.
	 */
	private void makeChatScrollPane(int bottomBound) {
		chatBox.setEditable(false);
		chatBox.setLineWrap(true);
		chatBox.setWrapStyleWord(true);
		chatScrollPane = new JScrollPane(chatBox);
		chatScrollPane.setBorder(null);
		chatScrollPane.setBounds(0, 0, TEXT_RIGHT_BOUND, bottomBound 
				- TEXT_Y - RESIZE_GLITCH_FIX/2 + 4);
		chatBox.addMouseListener(new 
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
	}
	
	/**
	 * Creates the background image of the table, wrapping it as a JLabel.
	 * 
	 * @return background of the table
	 */
	/*private JLabel generateBackground() {
		ImageIcon i = createImageIcon(); 
		JLabel bg = new JLabel(i);
		return bg;
	}*/
	
	/**
	 * Generates the display with the pots.
	 */
	private void generatePotDisplay() {
		potLabel = new JLabel();
		potLabel.setBounds(0, 0, 250, 80); //TODO - 150
		potLabel.setForeground(Color.WHITE);
		potLabel.setOpaque(false);
	}
	
	/**
	 * Updates the pot display when the information about the pots have changed.
	 * 
	 * @param pots all the pots of the model
	 */
	private void updatePotDisplay(Iterator<Pot> pots) {
        if (pots.hasNext()) {
			Pot pot = pots.next();
			String potInfo = "<HTML>Main pot: " + pot;
			/*for (Player player : pot) {
				potInfo += "\t" + player.getName();
			}*/
			potInfo += "<br>";
			int potCount = 1;
			while (pots.hasNext()) {
				Pot p = pots.next();
				potInfo += "Side pot " + potCount + ": " + p;
				/*for (Player player : p) {
					potInfo += "\t" + player.getName();
				}*/
				potInfo += "<br>";
				potCount++;
			}
			potInfo += "</HTML>";
			potLabel.setText(potInfo);
        } else {
            potLabel.setText("");
        }
	}
	
	/**
	 * Creates the action label, making the buttons and sliders.
	 */
	private void makeActionLabel() {
		actionLabel = new JLabel();
		actionLabel.setLayout(new GridLayout(2, 1));
		actionLabel.add(makeButtons());
		actionLabel.add(makeSlider());
		actionLabel.setBounds(0, 0, BUTTONS_WIDTH, 
				SLIDER_HEIGHT + BUTTONS_HEIGHT);
	}
	
	/**
	 * Updates actino label based on the game state.
	 * 
	 * @param gs contains information with which to update the label.
	 */
	private void updateActionLabel(GameState gs) {
		if (gs.getActiveName().equals(this.playerName)) { //TODO: add !gs.activeIsNull() condition?
			slider.setMaximum(gs.getMaxBet());
			slider.setMinimum(gs.getMinBet());
			slider.setValue(gs.getMinBet());
			String name = gs.getActiveName();
			call.setPlayerStatus(name);
			raise.setPlayerStatus(name);
			fold.setPlayerStatus(name);
			call.updateText(gs.isCheckable(), gs.getToCall(), gs.getPlayerStack());
			raise.updateText(gs.isBettable(), !gs.getCanRaise());
			actionLabel.setVisible(true);
		} else {
			actionLabel.setVisible(false);
		}
	}
	
	/**
	 * Makes a new slider and text display.
	 * @return
	 */
	private JLabel makeSlider() {
		JLabel sliderLabel = new JLabel();
		sliderLabel.setLayout(new GridLayout(2, 0));
		slider = new BetSizeSlider (JSlider.HORIZONTAL,
                0, 0, 0);
		betDisplay = new BetSizeTextField();
		slider.addChangeListener(betDisplay);
		slider.addChangeListener(raise);
		for (KeyListener kl : slider.getKeyListeners()) {
			betDisplay.addKeyListener(kl);
		}
		sliderLabel.add(slider);
		sliderLabel.add(betDisplay);
		sliderLabel.setBounds(0, 0, BUTTONS_WIDTH, SLIDER_HEIGHT); 
		return sliderLabel;
	}
	
	/**
	 * Creates the fold, call, and raise buttons.
	 * @return
	 */
	private JLabel makeButtons() {
		JLabel buttons = new JLabel();
		buttons.setLayout(new GridLayout(0, 3));
		fold = new FoldButton(this);
		call = new CallButton(this);
		raise = new RaiseButton(this);
		buttons.add(addButton(fold));
		buttons.add(addButton(call));
		buttons.add(addButton(raise));
		buttons.setBounds(0, 0, BUTTONS_WIDTH, BUTTONS_HEIGHT);
		return buttons;
	}
	
	/**
	 * Formats the bounds for a single button.
	 * @param aButton
	 * @return button with bounds formatted
	 */
	private JButton addButton(JButton aButton) {
		aButton.setBounds(0, 0, aButton.getPreferredSize().width, 
				aButton.getPreferredSize().height);
		return aButton;
	}
	
	/**
	 * Generates all of the player box components.
	 * 
	 * @param players for whom to make boxes. 
	 */
	private void generateBoxes(Iterator<Player> players, GameState gs) {
		for (int i = 0; i < boxes.size(); i++) {
			if (players.hasNext()) {
				Player tempPlayer = players.next();
				updateBox(tempPlayer, i, gs);
				updateRaiseSize(tempPlayer, i, false);
			} else {
				GUI.remove(boxes.get(i));
				boxes.remove(i);
			}
		}
		while(players.hasNext()) {
			Player tempPlayer = players.next();
			int index = boxes.size();
			generateRaiseSizeLabel(tempPlayer, index, false);
			generateBox(((PokerLayoutManager) (boxPanel.getLayout())).getCoords(index), 
					tempPlayer, 
					index, gs.getActiveName(), 
					gs.getFoldedStatus(index));
			//GUI.add(boxes.get(index), BOX_LAYER);
			boxPanel.add(boxes.get(index));
		}
	}
	
	private void generateTotalRaiseSizes(GameState gs) {
		Iterator<Player> players = gs.getAllPlayers();
		for (int i = 0; i < boxes.size(); i++) {
			if (players.hasNext()) {
				updateRaiseSize(players.next(), i, true);
			}
		}
		int index = boxes.size();
		while(players.hasNext()) {
			generateRaiseSizeLabel(players.next(), index, true);
			index++;
		}
	}
	
	private void generateRaiseSizes(GameState gs) {
		Iterator<Player> players = gs.getAllPlayers();
		for (int i = 0; i < boxes.size(); i++) {
			if (players.hasNext()) {
				updateRaiseSize(players.next(), i, false);
			}
		}
		int index = boxes.size();
		while(players.hasNext()) {
			generateRaiseSizeLabel(players.next(), index, false);
			index++;
		}
	}
	
	/**
	 * updates the raise sizes labels.
	 * 
	 * @param p player with the relevant raise size.
	 * @param i index of the label.
	 */
	private void updateRaiseSize(Player p, int i, boolean total) {
		//GUI.remove(raiseSizes.get(i));
		raiseSizePanel.remove(raiseSizes.get(i));
		raiseSizes.set(i, new JLabel());
		raiseSizes.get(i).setBounds(raiseSizeCoords[i][0], 
				raiseSizeCoords[i][1], 150, 150);
		raiseSizes.get(i).setForeground(Color.WHITE);
		raiseSizes.get(i).setOpaque(false);
		int temp;
		if (total) {
			temp = p.getTotalPutInPot();
		} else {
			temp = p.getPutInPotOnStreet();
		}
		if (temp != 0) {
			raiseSizes.get(i).setText(Integer.toString(temp));
		} else {
			raiseSizes.get(i).setText("");
		}
		raiseSizePanel.add(raiseSizes.get(i), BOX_LAYER);
		//GUI.add(raiseSizes.get(i), BOX_LAYER);
	}
	
	/**
	 * Makes new raise size labels.
	 * @param p player with the relevant raise size.
	 * @param i index of the label.
	 */
	private void generateRaiseSizeLabel(Player p, int i, boolean total) {
		raiseSizes.add(new JLabel());
		updateRaiseSize(p, i, total);
	}
	
	/**
	 * Updates a player box component with necessary information from the player.
	 * @param p player to use for box
	 * @param i index of box
	 */
	private void updateBox(Player p, int i, GameState gs) {
		boxes.get(i).setAvatar(p.getAvatarPath());
		//boxes.get(i).setCoords(coords[i]);
		boxes.get(i).setPlayerName(p.getName());
		//false if we aren't this player
		boxes.get(i).setHand(p.getHand(), p.getName().equals(this.playerName) || godMode, gs.getFoldedStatus(i));
		boxes.get(i).setActive(p.getName().equals(gs.getActiveName()));
		boxes.get(i).setStack(Integer.toString(p.getStack()));
	}
	
	/**
	 * Generates a new box for a single player.
	 * 
	 * @param path directory of avatar
	 * @param xy starting x and y coordinates
	 * @param name name of player
	 * @param stack stack size of player
	 * @param aHand hand of player
	 * @param isActive true if player is making a decision
	 * @return
	 */
	private void generateBox(int[] xy, Player p, int index, String activeName, 
			boolean folded) {
		boxes.add(new PlayerBoxComponent(xy[0], xy[1], p.getAvatarPath(), 
				p.getHand(), p.getName(), p.getName().equals(activeName),
				p.getName().equals(this.playerName) || godMode, folded,
				Integer.toString(p.getStack())));
	}
    
    private boolean isGUIUpdate(String s) {
    	return s.equals(GameView.GENERATE_GUI_START_OF_TURN) ||
    			s.equals(GameView.GENERATE_GUI_ALL_IN) ||
    			s.equals(GameView.GENERATE_GUI_START_OF_STREET) ||
    			s.equals(GameView.GENERATE_GUI_SHOWDOWN) ||
    			s.equals(GameView.GENERATE_GUI_END_OF_HAND) || 
    			s.equals(GameView.GENERATE_GUI_PLAYER_JOINED);
    }
	
    /**
     * Updates the view when a certain property change event is propagated.
     */
	public void propertyChange(PropertyChangeEvent event) {
		String eventName = event.getPropertyName();
		if (isGUIUpdate(eventName)) {
			generateGUI((GameState) event.getNewValue(), eventName);
		} else if (eventName.equals(GameView.UPDATE_CHAT)) {
			updateChatBox((String) event.getNewValue());
        } else if (eventName.equals(GameView.PLAYER_ACTION)) {
        	actionLabel.setVisible(false);
        	listener.propertyChange(event);
        } else if (eventName.equals(GameView.RAISE_MADE)) {
        	raiseSound.play();
        } else if (eventName.equals(GameView.BET_MADE)) {
        	betSound.play();
        } else if (eventName.equals(GameView.CHECK_MADE)) {
        	checkSound.play();
        } else if (eventName.equals(GameView.CALL_MADE)) {
        	callSound.play();
        } else if (eventName.equals(GameView.FOLD_MADE)) {
        	foldSound.play();
        } else if (eventName.equals(GameView.TIMER_EXPIRED)) {
        	if (isActive) {
        		fold.doClick();
        	}
        	//TODO: sometimes it is the other player's turn for a split second, i think
        } else if (eventName.equals(GameView.TIMER_NOTIFICATION)) {
    		timerNotificationSound.play();
        } else if (eventName.equals(GameView.UPDATE_BTN)) {
        	buttonGraphic.setBounds(
        			buttonCoords[Integer.parseInt(event.getNewValue().toString())][0], 
        			buttonCoords[Integer.parseInt(event.getNewValue().toString())][1],  
        			buttonGraphic.getWidth(), buttonGraphic.getHeight());
        }
	}

}