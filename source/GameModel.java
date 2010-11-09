
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.Serializable;

/**
 * Class used to model a game of poker.
 * 
 * @author Sid Nair
 *
 */
public class GameModel extends AbstractModel implements PropertyChangeListener, 
		Serializable, Runnable {
	
	private int currentPotIndex;
	
	private int nextPotIndex;

        private GameState currentState;
        private GameState oldState;
	
    private final static int START_OF_TURN = 0;
    
    private final static int START_OF_STREET = 1;
    
    private final static int SHOWDOWN = 2;
    
    private final static int END_OF_HAND = 3;
    
    private final static int ALL_IN = 4;
    
    private final static int PLAYER_JOINED = 5;
           
	/**
	 * Pause between dealing each card in all in situations.
	 */
	private final static int ALL_IN_PAUSE = 3000;
	
	/**
	 * Pause at the end of showdown if there is more than one player in the hand.
	 */
	private final static int SHOWDOWN_PAUSE_MULTIPLE = 3000;
	
	/**
	 * Pause at end of showdown if just one player is involved in the hand.
	 */
	private final static int SHOWDOWN_PAUSE_SINGLE = 1000;
	
	/**
	 * Index of button
	 */
	private int btnIndex;	
	/**
	 * True if everyone's all in and are racing.
	 */
	private boolean everyoneAllIn;
	
	/**
	 * Count of remaining active players.
	 */
	private int remainingActiveCount; 
	
	/**
	 * Size of current raise.
	 */
	private int currentRaise;
	
	/**
	 * Size of previous raise.
	 */
	private int oldRaise;	
	
	/**
	 * Used when a player folds.
	 */
	public final static String PLAYER_FOLDED = "Player folded";

	/**
	 * Used when a player raises.
	 */
	public final static String PLAYER_RAISED = "Player raised";
	
	public final static String PLAYER_BET = "Player bet";
	
	public final static String PLAYER_CHECKED = "Player checked";
	
	/**
	 * Used when a player calls.
	 */
	public final static String PLAYER_CALLED = "Player called";
		
	/**
	 * Used when a player puts money into the pot.
	 */
	public final static String MONEY_PAID = "Money paid";
	
	/**
	 * Used when the model must update the chat in the view.
	 */
	public final static String CHAT_UPDATE = "Chat updated";
	
	/**
	 * Index of currently active player.
	 */
	private int activePlayerIndex;
	
	/**
	 * Lock used to use conditions for receiving actions and updating the GUI. 
	 */
	private Lock myLock;
	
	/**
	 * Used to ensure a player action has been processed before proceeding.
	 */
	private Condition actionReceived;
	
	private Condition playersRemoved;
	
	/**
	 * Used to ensure that a GUI has been updated before proceeding.
	 */
	private Condition sufficientPlayers;
	
	/**
	 * List of stable players used to provide a constant order to the players
	 * when pasing information to the view.
	 */
	private ArrayList<Player> stablePlayers;
	
	/**
	 * All the players in the game.
	 */
	private ArrayList<Player> myPlayers;
	
	/**
	 * Players currently in the game.
	 */
	private ArrayList<Player> activePlayers;
	
	/**
	 * Pots in the active hand.
	 */
	private ArrayList<Pot> pots;
	
	/**
	 * Initial stack size.
	 */
	private static int initialStacks;
	
	/**
	 * Number of time in milliseconds that a player has to act.
	 */
	private static int decisionTime;
	
	/**
	 * Index of main pot in the array of pots.
	 */
	public static final int MAIN_POT_INDEX = 0;
	
	/**
	 * Deck of 52 cards.
	 */
	private Deck deck;
	
	/**
	 * Community cards.
	 */
	private Board board;
	
	/**
	 * Default big blind.
	 */
	private static int bigBlind;
	
	/**
	 * Default small blind.
	 */
	private static int smallBlind;
	
	/**
	 * Default ante.
	 */
	private static int ante = 0;
	
	/**
	 * Index of sb pre-flop.
	 */
	private static final int SB_INDEX = 0;
	
	/**
	 * Index of bb pre-flop.
	 */
	private static final int BB_INDEX = 1;
	
	/**
	 * Number of hands that have been played.
	 */
	private int handCount;
	
	/**
	 * True when the player has notified the model of its action and his/her
	 * action has been processed. 
	 */
	private boolean playerNotified;
	
	/**
	 * True when the GUI has notified the model that it has been repainted.
	 */
	private boolean GUINotified;
	
	/**
	 * Prints hand history to a text file.
	 */
	private HHPrinter printer;
	
	/**
	 * Used to check a list of hands and determine the best one.
	 */
	private HandRanker myRanker;
	
	private ArrayList<Player> toRemove;
	
	private ArrayList<Player> toAdd;
	
	private boolean removed;

	/**
	 * Constructs the game and instantiates players, deck, board, and pots.
	 * 
	 * @param aBB initial bb
	 * @param aSB initial sb
	 * @param anAnte initial ante
	 * @param stacks initial stacks
	 * @param aDecisionTime decision time
	 */
	public GameModel(int aBB, int aSB, int anAnte, int stacks, int id) {
		myRanker = new HandRanker();
		stablePlayers = new ArrayList<Player>();
		myPlayers = new ArrayList<Player>();
		toRemove = new ArrayList<Player>();
		toAdd = new ArrayList<Player>();
		deck = new Deck();
		board = new Board();
		pots = new ArrayList<Pot>();
		bigBlind = aBB;
		smallBlind = aSB;
		ante = anAnte;
		initialStacks = stacks;
		myLock = new ReentrantLock();
		actionReceived = myLock.newCondition();
		sufficientPlayers = myLock.newCondition();
		playersRemoved = myLock.newCondition();
		printer = new HHPrinter(Integer.toString(id));
		printer.add("******NEW SESSION*****" + "\t" + 
				new Date().toString() + "\n");
                oldState = new GameState(stablePlayers, pots, board);
	}
	
	public void run() {
		while(true) {
			removeAllPlayers();
			addAllPlayers();
			while (stablePlayers.size() < 2) {
				myLock.lock();
				try {
					try {
						removeAllPlayers();
						updateGUI(END_OF_HAND);
						sufficientPlayers.await();
						removeAllPlayers();
						addAllPlayers();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} finally {
					myLock.unlock();
				}
			}
			playHand();
		}
	}
	
	/**
	 * Adds a new player to the game.
	 * 
	 * @param aPlayer player to add
	 */
	public void addPlayer(Player aPlayer) {
		if (!stablePlayers.contains(aPlayer)) {
			toAdd.add(aPlayer);
		} else {
			System.out.println("Name " + aPlayer.getName() + " was already" +
					"taken. Seating was denied");
		}
		myLock.lock();
		try {
			sufficientPlayers.signalAll();
		} finally {
			myLock.unlock();
		}
		updateGUI(PLAYER_JOINED);
	}
	
	private void addAllPlayers() {
		for (Player p : toAdd) {
			int numberPlayers = stablePlayers.size();
			if (numberPlayers < 6) {
				stablePlayers.add(p);
				//my players should insert the new player into the position after
				//the stable player it is after. if it is after the last player,
				//not sure if it should be added to the front or end
				if (numberPlayers <= 1) {
					myPlayers.add(p);
				} else {
					Player shouldPrecede = stablePlayers.get(numberPlayers - 1);
					for (int i = 0; i < myPlayers.size(); i++) {
						if (shouldPrecede.equals(myPlayers.get(i))) {
							myPlayers.add(i + 1, p);
						}
					}
				}
			} else {
				System.out.println("Too many players. " + p.getName() + 
						" was " + "denied seating.");
			}
		}
		toAdd.clear();
	}
	
	/**
	 * Remove a player from the game.
	 */
	public void removePlayer(Player aPlayer) {
		for (Player p : activePlayers) {
			if (p.equals(aPlayer)) {
				p.setSittingOut(true);
			}
		}
		toRemove.add(aPlayer);
	}
	
	private void removeAllPlayers() {
		//TODO: don't let this happen during a hand
		removed = false;
		firePropertyChange(GameServer.REMOVE_ABSENT_PLAYERS, toAdd, toRemove);
		myLock.lock();
		try {
			while (!removed) {
				try {
					playersRemoved.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} finally {
			myLock.unlock();
		}
		for (Player p : toRemove) {
			stablePlayers.remove(p);
			myPlayers.remove(p);
		}
		toRemove.clear();
	}
	
	public void notifyPlayersRemoved() {
		myLock.lock();
		try {
			playersRemoved.signalAll();
			removed = true;
		} finally {
			myLock.unlock();
		}
	}
	
	public Player getLastPlayer() {
		return (stablePlayers.get(stablePlayers.size() - 1));
	}
	
	public static int getBigBlind() {
		return bigBlind;
	}
	
	public static int getSmallBlind() {
		return smallBlind;
	}
	
	public static int getAntes() {
		return ante;
	}
	
	/**
	 * Returns inital stack sizes. Pure accessor.
	 * 
	 * @return initial stacks
	 */
	public static int getInitialStacks() {
		return initialStacks;
	}
	
	private void initHand() {
		handCount++;
		init();
		updateButton();
	}
	
	/**
	 * Runs the game indefinitely.
	 */
	public void playHand() {
		initHand();
		runHand();
		for (Player p : myPlayers) {
			p.resetHand();
			board.initBoard();
		}
		updateGUI(END_OF_HAND);
	}
	
	private void findButtonIndex() {
		int goalIndex; 
		if (stablePlayers.size() == 2) {
			goalIndex = 0;
		} else {
			goalIndex = stablePlayers.size() - 1;
		}
		for (int i = 0; i < stablePlayers.size(); i++) {
			if (activePlayers.get(goalIndex)== stablePlayers.get(i)) {
				btnIndex = i;
			}
		}
	}
	
	/**
	 * Initializes the deck, increments positions, makes new pots, resets
	 * player hands, and pays the ante.
	 */
	private void init() {
		activePlayers = (ArrayList<Player>) myPlayers.clone();
		incrementPositions(myPlayers);
		deck.init();
		initPots();
		updateChat("Hand #" + handCount + "\n");
	}
	
	private void initPots() {
		pots.clear();
		Pot.resetCounter();
		currentPotIndex = 0;
		nextPotIndex = 0;
		pots.add(new Pot());
		for (Player p : myPlayers) {
			p.resetHand();
			p.payAnte(ante);
		}
	}
	
	/**
	 * Fires a property change to the view to update the chatBox and updates the
	 * HHPrinter.
	 *  
	 * @param s String to add
	 */
	public void updateChat(String s) {
		firePropertyChange(GameView.UPDATE_CHAT, new String(), s);
		printer.add(s);
	}
	
	public void updateButton() {
		findButtonIndex();
		firePropertyChange(GameView.UPDATE_BTN, new String(),
				new Integer(btnIndex));
	}
	
	/**
	 * Runs a single hand.
	 */
	public void runHand() {
		//pays blinds
		activePlayers.get(SB_INDEX).paySmallBlind(smallBlind);
		activePlayers.get(BB_INDEX).payBigBlind(bigBlind);
		deal();
		updateChat(toString("Pre-flop"));
		playStreet(bigBlind, true, false);
		pauseAllIns();
		dealFlop(); 
		if (activePlayers.size() > 1 || everyoneAllIn) { 
			updateChat(toString("Flop"));
		}
		playStreet(0, false, true);
		pauseAllIns();
		dealTurn();
		if (activePlayers.size() > 1 || everyoneAllIn) {
			updateChat(toString("Turn"));
		}
		playStreet(0, false, false);
		pauseAllIns();
		dealRiver();
		if (activePlayers.size() > 1 || everyoneAllIn) {
			updateChat(toString("River"));
		}
		playStreet(0, false, false);
		showDown();
		exportHand();
	}
	
	/**
	 * Exports the hand history to text. 
	 */
	private void exportHand() {
		printer.saveHand();
	}
	
	/**
	 * Deals hands to all of the players.
	 */
	private void deal() {
		deck.shuffle();
		for (Player p : myPlayers) {
			Hand h = new Hand();
			for (int i = 0; i < 2; i++) {
				h.setCard(i, deck.nextCard());
			}
			p.setHand(h);
		}
	}
	/**
	 * Deals the flop.
	 */
	public void dealFlop() {
		for (int i = 1; i <= 3; i++) {
			board.addCard(deck.nextCard());
		}
		updateGUI(START_OF_STREET);
	}
	
	/**
	 * Deals the turn.
	 */
	public void dealTurn() {
		board.addCard(deck.nextCard());
		updateGUI(START_OF_STREET);
	}
	
	/**
	 * Deals the river.
	 */
	public void dealRiver() {
		board.addCard(deck.nextCard());
		updateGUI(START_OF_STREET);
	}
	
	/**
	 * Showdown determines winner.
	 */
	public void showDown() {
		if (activePlayers.size() > 1) {//TODO: this might cause bugs 6h
			updateGUI(SHOWDOWN);
			updateChat(toString("Showdown"));
			for (Pot pot : pots) {
				ArrayList<Player> winners = myRanker.findWinner(pot, board);
				double split = 1.0 / winners.size();
				for (Player player : winners) {
					ship(player, pot, split);
				}
			}
		} else {
			//updateGUI(SINGLE_PLAYER_SHOWDOWN);
			updateChat(toString("Showdown"));
			try {
				ship(activePlayers.get(0), pots.get(0), 1.0);
			} catch (IndexOutOfBoundsException e) { //occurs when everyone has quit
				System.exit(-1);
			}
		}
		updateChat("\n-----\n");
		pauseShowdown();
	}
	
	/**
	 * Ships pot to given player.
	 * 
	 * @param aPlayer winner
	 * @param aPot pot to ship
	 */
	private void ship(Player aPlayer, Pot aPot, double portion) {
		aPlayer.add((int) (aPot.getSize() * portion));
		updateChat(getResults(aPlayer.getName(), aPot));
	}
	
	/**
	 * Increments the positions of the players.
	 * 
	 * @param anArray array of all the players.
	 */
	private void incrementPositions(ArrayList<Player> anArray) {
		ArrayList<Player> temp = new ArrayList<Player>();
		for (Player p : anArray) {
			temp.add(p);
		}
		for (int i = 0; i < temp.size() - 1; i++) {
			anArray.set(i, temp.get(i+1));
		}
		anArray.set(temp.size() - 1, temp.get(0));
	}
	
	/**
	 * Decrements the positions of the players
	 * 
	 * @param anArray array of all the players.
	 */
	private void decrementPositions(ArrayList<Player> anArray) {
		ArrayList<Player> temp = new ArrayList<Player>();
		for (Player p : anArray) {
			temp.add(p);
		}
		for (int i = temp.size() - 1; i > 0; i--) {
			anArray.set(i, temp.get(i-1));
		}
		anArray.set(0, temp.get(temp.size() - 1));
	}
	
	/**
	 * Plays a single street.
	 * 
	 * @param currentRaise inital raise players are facing - 0 on every street
	 * except pre.
	 * @param flop true when the street is the flop
	 */
	private void playStreet(int raiseSize, boolean preFlop, boolean flop) {
		currentPotIndex = pots.size() - 1;
		playerNotified = true;
		ArrayList<Player> tempPlayers = null;
		oldRaise = 0;
		currentRaise = raiseSize;
		if (preFlop) {
			tempPlayers = (ArrayList<Player>) activePlayers.clone(); //used
			incrementPositions(activePlayers);
			incrementPositions(activePlayers);
		} if (flop && stablePlayers.size() == 2) {
			decrementPositions(activePlayers);
		}
		if (activePlayers.size() != 1) { //skips if only one player is playing
			remainingActiveCount = activePlayers.size();
			boolean done = false;
			while (!done) {
				done = true;
				for (int i = 0; i < activePlayers.size(); i++) {
					if (activePlayers.size() > 1) {
						activePlayerIndex = i;
						Player p = activePlayers.get(activePlayerIndex);
						boolean haveChips = false; //if you use this, you can't call a shove HU
						boolean allCalled = true;
						int allIns = 0;
						for (Player pl : activePlayers) {
							if (p != pl) {
								if (pl.hasChips()) {
									haveChips = true;
								} else { 
									allIns++;
								}
								if (!pl.actionIsClosed()) {
									allCalled = false;
								}
							}
						}
						System.out.println("control flow");
						System.out.println(p.isInHand() + " and " + p.hasChips() + " and");
						System.out.println("\t" + (remainingActiveCount - allIns > 1) + " OR ");
						System.out.println((remainingActiveCount - allIns >= 1) + " and " + (currentRaise - p.getPutInPot() > 0) + "\t" + currentRaise + " " + p.getPutInPot());
						System.out.println(p.isInHand() && p.hasChips() &&
								((remainingActiveCount - allIns > 1) || 
										(remainingActiveCount - allIns >= 1 &&  
											(currentRaise - p.getPutInPot() > 0))));
						System.out.println("---------");
						if (p.isInHand() && p.hasChips() &&
								((remainingActiveCount - allIns > 1) || 
								(remainingActiveCount - allIns >= 1 &&  
									(currentRaise - p.getPutInPot() > 0)))) {
							if ((!allCalled) || (!p.hasActed())) {
								p.setActive(true);
								myLock.lock();
								try {
									while (!playerNotified) {
										actionReceived.await();
									}
									playerNotified = false;
								} catch (InterruptedException e) {
									e.printStackTrace();
								} finally {
									myLock.unlock();
								}
								p.updateSizing(currentRaise, oldRaise);
								updateGUI(START_OF_TURN, i);
								p.act();
								p.setActive(false);
								if (!p.actionIsClosed()) {
									done = false;
								}
							} else {
								p.setIsClosed(true);
							}
						} else {
							Player largestRaiserYet = null;
							for (Player player : activePlayers) {
								if (largestRaiserYet == null) {
									largestRaiserYet = player;
								} else if (player.getPutInPot() > largestRaiserYet.getPutInPot()) {
									largestRaiserYet = player;
								}
							}
							if (p.isAllIn() && !(p.equals(largestRaiserYet))) {
								pots.addAll(pots.get(MAIN_POT_INDEX).calcSidePots(p.getLastSize()));
								pots = getUniquePots(pots);
							}
						}
					}
				}
			}
			if (preFlop) {
				activePlayers.clear();
				for (Player p : tempPlayers) {
					if (p.isInHand()) {
						activePlayers.add(p);
					}
				}
			} else {
				ArrayList<Player> temp = new ArrayList<Player>();
				for (Player p : activePlayers) {
					if (p.isInHand()) {
						temp.add(p);
					}
				}
				activePlayers = temp;
			}
			for (Player p : activePlayers) {
				p.resetStreet();
			}
		}
	}
	
	private void pauseShowdown() {
		if (activePlayers.size() > 1) {
			try {
				Thread.sleep(SHOWDOWN_PAUSE_MULTIPLE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			try {
				Thread.sleep(SHOWDOWN_PAUSE_SINGLE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void pauseAllIns() {
		everyoneAllIn = true;
		int hasStackCounter = 0;
		if (activePlayers.size() == 1) {
			everyoneAllIn = false;
		} else {
			for (Player p : activePlayers) {
				if (!p.isAllIn()) {
					hasStackCounter++;
					if (hasStackCounter >= 2) {
						everyoneAllIn = false;
						break;
					}
				}
			}
		}
		if (everyoneAllIn) {
			try {
				updateGUI(ALL_IN);
				Thread.sleep(ALL_IN_PAUSE);
				//updateGUI(false, true, activePlayerIndex); //TODO: needed?
			} catch (InterruptedException e) {
				e.printStackTrace();
			} //slows down if everyone's all-in
		}
	}
	
	/**
	 * Prints hh for a single street.
	 * 
	 * @param aStreet street to print 
	 * @return String for HHPrinter
	 */
	public String toString(String aStreet) {
		String toReturn = "";
		if (aStreet.equals("Pre-flop")) {
			int seat = 1; 
			for (Player p : activePlayers) {
				updateChat("Seat " + seat);
				if (seat == 1) {
					updateChat(" (SB)");
				} else if (seat == 2) {
					updateChat(" (BB)");
				} if (activePlayers.size() == 2) {
					if (seat == 1) {
						updateChat("/BTN");
					}
				} else if (seat == 3) {
					updateChat(" (BTN)");
				}
				updateChat(": " + p.getName() + "\n");
				seat++;
			}
		}
		toReturn += aStreet + ": (" + pots.get(MAIN_POT_INDEX).getSize() 
			+ ")" + "\n" ;
		if (board.getSize() > 0) {
			toReturn += board.toString() + "\n";
		}
		toReturn += "(" + activePlayers.size() + " Players)" + "\n";
		if (aStreet.equals("Showdown")) {
			if (activePlayers.size() > 1 || everyoneAllIn) {
				for (Player p : activePlayers) {
					toReturn += p.getName() + " shows " + p.getHand().toString() 
						+ " for " + myRanker.getHandName(p.getHand(), 
								board) + "\n";
				}
			}
		}
		return toReturn;
	}
	
	/**
	 * Shows results.
	 * 
	 * @param winner player who won pot
	 * @return String for HHPrinter
	 */
	public String getResults(String winner, Pot pot) {
		String toReturn = winner + " wins " +  
			pot.getSize() + "\n"; 
		return toReturn;
	}
	
	/**
	 * Returns information about each player's hand
	 */
	public String toString() {
		String toReturn = "POT: " + pots.get(MAIN_POT_INDEX).getSize() + "\n";
		for (Player p : myPlayers) {
			toReturn += p.toString() + "\n";
		}
		return toReturn;
	}
	
	/**
	 * Returns total number of players playing.
	 * @return
	 */
	public int getNumberTotalPlayers() {
		return myPlayers.size(); 
	}

	
	private void updateGUI(int updateType, int index) {
		Player active = activePlayers.get(index);
		if (updateType == GameModel.START_OF_TURN) {
			int stableIndex = stablePlayers.size();
			for (int i = 0; i < stablePlayers.size(); i++) {
				if (stablePlayers.get(i).isActive()) {
					stableIndex = i;
					break;
				}
			}
			currentState = new GameState(stablePlayers, active, pots, board, stableIndex);
			firePropertyChange(GameView.GENERATE_GUI_START_OF_TURN, oldState, currentState);
		} else {
			try {
				throw new Exception("Not a legal update");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
	/**
	 * Updates the GUI when necessary.
	 * 
	 * @param endOfHand true if this is the end of the hand.
	 * @param players true 
	 * @param index
	 */
	private void updateGUI(int updateType) {
		currentState = new GameState(stablePlayers, pots, board);
		if (updateType == GameModel.ALL_IN) {
			firePropertyChange(GameView.GENERATE_GUI_ALL_IN, oldState, currentState);
		}else if (updateType == GameModel.START_OF_STREET) {
			firePropertyChange(GameView.GENERATE_GUI_START_OF_STREET, oldState, currentState);
		} else if (updateType == GameModel.SHOWDOWN) {
			firePropertyChange(GameView.GENERATE_GUI_SHOWDOWN, oldState, currentState);
		} else if (updateType == GameModel.SHOWDOWN) {
			firePropertyChange(GameView.GENERATE_GUI_SINGLE_PLAYER_SHOWDOWN, oldState, currentState);
		} else 	if (updateType == GameModel.END_OF_HAND) {
			firePropertyChange(GameView.GENERATE_GUI_END_OF_HAND, oldState, currentState);
		} else 	if (updateType == GameModel.PLAYER_JOINED) {
			//firePropertyChange(GameView.GENERATE_GUI_PLAYER_JOINED, oldState, currentState);
		}  else {
			try {
				throw new Exception("Not a legal update");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
		/*
		Player active = null;
		try {
			active = activePlayers.get(index);
		} catch (IndexOutOfBoundsException e) {
			active = null;
		}
		//GUINotified = false;
        currentState = new GameState((ArrayList<Player>) stablePlayers.clone(),
        		active, (ArrayList<Pot>) pots.clone(), board);
		if (!players) {
			if (endOfHand) {
				firePropertyChange(GameView.GENERATE_GUI_FULLY, oldState, currentState);
			} else {
				firePropertyChange(GameView.GENERATE_GUI, oldState, currentState);
			}
		} else {
			firePropertyChange(GameView.GENERATE_GUI, oldState, currentState);
		}*/
		/*
		myLock.lock();
		try {
			if (!GUINotified) {
				GUIUpdated.await();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			myLock.unlock();
		}
        oldState = currentState;*/
		
	public void notifyMe() {
		myLock.lock();
		try {
			sufficientPlayers.signalAll();
			GUINotified = true;
		} finally {
			myLock.unlock();
		}	
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PLAYER_FOLDED)) {
			remainingActiveCount--;
			actionReceived();
			for (Pot p : pots) {
				p.removePlayer((Player) evt.getSource());
			}
			firePropertyChange(GameView.FOLD_MADE, new String(), new String());
		} else if (evt.getPropertyName().equals(PLAYER_RAISED)) {
			oldRaise = currentRaise;
			currentRaise = (Integer.parseInt(evt.getNewValue().toString()));
			firePropertyChange(GameView.RAISE_MADE, new String(), new String());
			actionReceived();
		} else if (evt.getPropertyName().equals(PLAYER_BET)) {
			oldRaise = currentRaise;
			currentRaise = (Integer.parseInt(evt.getNewValue().toString()));
			firePropertyChange(GameView.BET_MADE, new String(), new String());
			actionReceived();
		} else if (evt.getPropertyName().equals(PLAYER_CALLED)) {
			firePropertyChange(GameView.CALL_MADE, new String(), new String());
			actionReceived();
		} else if (evt.getPropertyName().equals(PLAYER_CHECKED)) {
			firePropertyChange(GameView.CHECK_MADE, new String(), new String());
			actionReceived();
		} else if (evt.getPropertyName().equals(MONEY_PAID)) {
			int paid = Integer.parseInt(evt.getNewValue().toString());
			addMoneyToPots(paid, (Player) evt.getSource());
		} else if (evt.getPropertyName().equals(CHAT_UPDATE)) {
			updateChat((String) evt.getNewValue());
		} else if (evt.getPropertyName().equals(GameView.PLAYER_ACTION)) {
			for (Player p : activePlayers) {
				Action action = (Action) evt.getNewValue();
				if (p.getName().equals(action.getPlayerName())) {
					if (action.getAction().equals(Action.ActionType.RAISE)) {
						p.raiseTo(action.getSize(), false);
					} else if (action.getAction().equals(Action.ActionType.BET)) {
						p.raiseTo(action.getSize(), true);
					} else if (action.getAction().equals(Action.ActionType.FOLD)) {
						p.fold();
					} else if (action.getAction().equals(Action.ActionType.CALL)) {
						p.call();
					} else if (action.getAction().equals(Action.ActionType.CHECK)) {
						p.check();
					}
				}
			}
		}
	}
	
	private void addMoneyToPots(int paid, Player player) {
		Pot current = pots.get(MAIN_POT_INDEX);
		if (player.getLastSize() < current.getLastRaiseSize()) {
			current.add(paid, player, false);
		} else {
			current.add(paid, player, true);
		}
		pots.addAll(current.calcSidePots(player.getLastSize()));
		pots = getUniquePots(pots);
	}
	
	/**
	 * Combines pots if they have the same players.
	 */
	private ArrayList<Pot> getUniquePots(ArrayList<Pot> pots) {
		for (Pot pot1 : pots) {
			for (Pot pot2 : pots) {
				if (pot1 != pot2) {
					if (pot1.getPlayerSet().equals(pot2.getPlayerSet())) {
						pot1.merge(pot2);
					} else if (pot1.getNumberEligible() == 1 && pot2.getNumberEligible() == 1) {
						if (pot1.getSize() <= pot2.getSize()) {
							for (Player p : pot2.getPlayerSet()) {
								pot1.add(pot1.getSize(), p, true);
								pot2.remove(pot1.getSize());
							}
						} else if (pot1.getSize() > pot2.getSize()) {
							for (Player p : pot1.getPlayerSet()) {
								pot2.add(pot2.getSize(), p, true);
								pot1.remove(pot2.getSize());
							}
						} 
					}
				}
			}
		}
		ArrayList<Pot> tempPots = new ArrayList<Pot>();
		for (int i = 0; i < pots.size(); i++) {
			if (pots.get(i).getSize() > 0 || i == 0) {
				tempPots.add(pots.get(i));
			}
		}
		return tempPots;
	}
	
	/**
	 * Allows the model to process the action of a player.
	 */
	private void actionReceived() {
		myLock.lock();
		try {
			playerNotified = true;
			actionReceived.signalAll();
		} finally {
			myLock.unlock();
		}
	}
	
	//TODO - side pot tests if a player is all-in
		//FIXED? if short, big, bb, bb gets in side pot with big
	//TODO - networking
		//lobby to start tables
		//add/remove players, but only during
		//make sure you can't click buttons when you're not supposed to (incl. arrins)
	//TODO - button pic
	
}