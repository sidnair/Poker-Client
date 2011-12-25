package pokerclient.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.Serializable;

import pokerclient.controller.GameServer;
import pokerclient.controller.GameState;
import pokerclient.gui.GameView;

/**
 * Class used to model a game of poker.
 */
public class GameModel extends AbstractModel implements PropertyChangeListener,
		Serializable, Runnable {

	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = -3228212823720315361L;

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
	 * Pause at the end of the street before the next card is dealt. Should be
	 * no bigger than ALL_IN_PAUSE.
	 */
	private final static int END_OF_STREET_PAUSE = 500;

	/**
	 * Pause at the end of showdown if there is more than one player in the
	 * hand.
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
	private Lock lock;

	/**
	 * Used to ensure a player action has been processed before proceeding.
	 */
	private Condition actionReceived;

	private Condition playersRemoved;

	/**
	 * List of stable players used to provide a constant order to the players
	 * when passing information to the view.
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
	private int bigBlind;

	/**
	 * Default small blind.
	 */
	private int smallBlind;

	/**
	 * Default ante.
	 */
	private int ante;

	/**
	 * Index of sb pre-flop.
	 */
	private static final int SB_INDEX = 0;

	/**
	 * Index of bb pre-flop.
	 */
	private static final int BB_INDEX = 1;

	public static final boolean TOP_OFF = true;

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
	 * Prints hand history to a text file.
	 */
	private HHPrinter printer;

	private ArrayList<Player> toRemove;

	private ArrayList<Player> toAdd;

	private int MAX_PLAYERS = 6;
	
	
	private final Street flop;
	private final Street turn;
	private final Street river;

	/**
	 * Constructs the game and instantiates players, deck, board, and pots.
	 * 
	 * @param bigBlind initial bb
	 * @param smallBlind initial sb
	 * @param ante initial ante
	 * @param initialStackSize initial stacks
	 * @param decisionTime decision time
	 */
	public GameModel(int bigBlind, int smallBlind, int ante,
			int initialStackSize, int id) {
		this.bigBlind = bigBlind;
		this.smallBlind = smallBlind;
		this.ante = ante;
		flop = this.new Street(3, "Flop", true);
		turn = this.new Street(1, "Turn", false);
		river = this.new Street(1, "River", false);
		stablePlayers = new ArrayList<Player>();
		myPlayers = new ArrayList<Player>();
		toRemove = new ArrayList<Player>();
		toAdd = new ArrayList<Player>();
		deck = new Deck();
		board = new Board();
		pots = new ArrayList<Pot>();
		initLock();
		initPrinter(id);
	}

	private void initPrinter(int id) {
		printer = new HHPrinter(Integer.toString(id));
		printer.add("******NEW SESSION*****" + "\t" + new Date().toString()
				+ "\n");
		lock.lock();
		oldState = new GameState(stablePlayers, pots, board);
		lock.unlock();
	}

	private void initLock() {
		lock = new ReentrantLock();
		actionReceived = lock.newCondition();
		playersRemoved = lock.newCondition();
	}

	public void run() {
		while (true) {
			updatePlayers();
			while (stablePlayers.size() < 2) {
				updatePlayers();
			}
			playHand();
		}
	}

	private void updatePlayers() {
		removeAbsentPlayers();
		addWaitingPlayers();
	}

	/**
	 * Adds a new player to the game.
	 * 
	 * @param aPlayer player to add
	 */
	public void addPlayer(Player aPlayer) {
		if (!stablePlayers.contains(aPlayer)) {
			lock.lock();
			toAdd.add(aPlayer);
			lock.unlock();
		} else {
			System.err.println("Name " + aPlayer.getName() + " was already"
					+ "taken. Seating was denied");
		}
		updateGUI(PLAYER_JOINED);
	}

	/**
	 * Add players waiting to join the table.
	 */
	private void addWaitingPlayers() {
		lock.lock();
		for (Player p : toAdd) {
			if (stablePlayers.size() > MAX_PLAYERS) {
				// TODO: notify player...
				System.err.println("Table full. " + p.getName()
						+ " was denied seating.");
				lock.unlock();
				return;
			}
			
			stablePlayers.add(p);
			myPlayers.add(p);
		}
		lock.unlock();
		toAdd.clear();
	}

	/**
	 * Indicates that a player is sitting out and removes them from the game.
	 */
	public void sitOutPlayer(Player aPlayer) {
		for (Player p : activePlayers) {
			if (p.equals(aPlayer)) {
				p.setSittingOut(true);
			}
		}
		toRemove.add(aPlayer);
	}

	/**
	 * Removes a player from the model after making sure that the listener makes
	 * the necessary adjustments.
	 * 
	 * @precondition this shouldn't be called in the middle of a hand.
	 */
	private void removeAbsentPlayers() {
		lock.lock();
		firePropertyChange(GameServer.REMOVE_ABSENT_PLAYERS, "", toRemove);
		try {
			while (toRemove.size() > 0) {
				playersRemoved.await();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	public void notifyPlayersRemoved() {
		lock.lock();
		try {
			for (Player p : toRemove) {
				stablePlayers.remove(p);
				myPlayers.remove(p);
			}
			toRemove.clear();
			playersRemoved.signalAll();
		} finally {
			lock.unlock();
		}
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

	private void updateButtonIndex() {
		int goalIndex;
		if (stablePlayers.size() == 2) {
			goalIndex = 0;
		} else {
			goalIndex = stablePlayers.size() - 1;
		}
		for (int i = 0; i < stablePlayers.size(); i++) {
			if (activePlayers.get(goalIndex) == stablePlayers.get(i)) {
				btnIndex = i;
			}
		}
	}

	/**
	 * Initializes the deck, increments positions, makes new pots, resets player
	 * hands, and pays the ante.
	 */
	private void initHand() {
		handCount++;
		activePlayers = (ArrayList<Player>) myPlayers.clone();
		incrementPositions(myPlayers);
		deck.init();
		pots.clear();
		pots.add(new Pot());
		for (Player p : myPlayers) {
			p.resetHand();
		}
		updateChat("Hand #" + handCount + "\n");
		updateButton();

	}

	/**
	 * Updates the chat box and HHPrinter.
	 * @param s message to append
	 */
	public void updateChat(String s) {
		firePropertyChange(GameView.UPDATE_CHAT, "", s);
		printer.add(s);
	}

	public void updateButton() {
		updateButtonIndex();
		firePropertyChange(GameView.UPDATE_BTN, "", new Integer(btnIndex));
	}
	
	private class Street implements Serializable {
		
		/**
		 * Automatically generated serial ID.
		 */
		private static final long serialVersionUID = -5061259087871349101L;
		
		private int numCards;
		private String name;
		private boolean isFlop;
		
		public Street(int numCards, String name, boolean isFlop) {
			this.numCards = numCards;
			this.name = name;
			this.isFlop = isFlop;
		}
		
		public void run() {
			// TODO - skip showing everything if we're not in results oriented
			// mode. Make sure we update the board in the case in which people
			// are all in

			if (isEveryoneAllIn()) {
				updateGUI(ALL_IN);
				if (isHandContested()) {
					pause(ALL_IN_PAUSE - END_OF_STREET_PAUSE);
				}
			}
			dealStreet(numCards);
			updateGUI(START_OF_STREET);
			updateChat(streetToString(name));
			playStreet(0, false, isFlop);
			pause(END_OF_STREET_PAUSE);
		}
		
	}

	/**
	 * Runs a single hand.
	 */
	public void runHand() {
		payAntes();
		payBlinds();
		runPreflop();
		flop.run();
		turn.run();
		river.run();
		showDown();
		exportHand();
	}

	private void runPreflop() {
		updateChat(streetToString("Pre-flop"));
		dealPreFlop();
		playStreet(bigBlind, true, false);
		pause(END_OF_STREET_PAUSE);
	}

	private void payBlinds() {
		activePlayers.get(SB_INDEX).paySmallBlind(smallBlind);
		activePlayers.get(BB_INDEX).payBigBlind(bigBlind);
	}

	private void payAntes() {
		for (Player p : myPlayers) {
			p.payAnte(ante);
		}
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
	private void dealPreFlop() {
		deck.shuffle();
		for (Player p : myPlayers) {
			Hand h = new Hand();
			for (int i = 0; i < 2; i++) {
				h.setCard(i, deck.nextCard());
			}
			p.setHand(h);
		}
	}
	
	private void dealStreet(int numCards) {
		for (int i = 0; i < numCards; i++) {
			board.addCard(deck.nextCard());
		}
	}

	/**
	 * Showdown determines winner.
	 */
	public void showDown() {
		if (activePlayers.size() > 1) {// TODO: this might cause bugs 6h
			updateGUI(SHOWDOWN);
			updateChat(streetToString("Showdown"));
			for (Pot pot : pots) {
				ArrayList<Player> winners = HandRanker.findWinner(pot, board);
				double split = 1.0 / winners.size();
				for (Player player : winners) {
					ship(player, pot, split);
				}
			}
			pauseShowdown();
		} else {
			updateChat(streetToString("Showdown"));
			// TODO - this can fail if everyone has quit. Don't simply guard to
			// see if size is 1 since this sometimes is 0 when it shouldn't be,
			// so that change would just mask the bug.
			assert activePlayers.size() == 1;
			ship(activePlayers.get(0), pots.get(0), 1.0);
		}
		updateChat("\n-----\n");
	}

	/**
	 * Ships pot to given player.
	 * 
	 * @param player winner
	 * @param pot pot to ship
	 */
	private void ship(Player player, Pot pot, double portion) {
		player.addToStack((int) (pot.getSize() * portion));
		updateChat(getResults(player.getName(), pot));
	}

	/**
	 * Increments the positions of the players.
	 * 
	 * @param anArray
	 *            array of all the players.
	 */
	private void incrementPositions(ArrayList<Player> anArray) {
		ArrayList<Player> temp = new ArrayList<Player>();
		for (Player p : anArray) {
			temp.add(p);
		}
		for (int i = 0; i < temp.size() - 1; i++) {
			anArray.set(i, temp.get(i + 1));
		}
		anArray.set(temp.size() - 1, temp.get(0));
	}

	/**
	 * Decrements the positions of the players
	 * 
	 * @param anArray
	 *            array of all the players.
	 */
	private void decrementPositions(ArrayList<Player> anArray) {
		ArrayList<Player> temp = new ArrayList<Player>();
		for (Player p : anArray) {
			temp.add(p);
		}
		for (int i = temp.size() - 1; i > 0; i--) {
			anArray.set(i, temp.get(i - 1));
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
		ArrayList<Player> tempPlayers = null;
		oldRaise = 0;
		currentRaise = raiseSize;
		if (preFlop) {
			tempPlayers = (ArrayList<Player>) activePlayers.clone();
			incrementPositions(activePlayers);
			incrementPositions(activePlayers);
		}
		if (flop && stablePlayers.size() == 2) {
			decrementPositions(activePlayers);
		}
		if (activePlayers.size() != 1) { // skips if only one player is playing
			remainingActiveCount = activePlayers.size();
			boolean done = false;
			while (!done) {
				done = true;
				for (int i = 0; i < activePlayers.size(); i++) {
					if (remainingActiveCount > 1) {
						done = mainLogic(done, i);
					}
				}
			}
			cleanupLogic(preFlop, tempPlayers);
		}
	}

	private void cleanupLogic(boolean preFlop, ArrayList<Player> tempPlayers) {
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

	private boolean mainLogic(boolean done, int i) {
		activePlayerIndex = i;
		Player p = activePlayers.get(activePlayerIndex);
		// call a shove HU
		boolean allCalled = true;
		int allIns = 0;
		for (Player pl : activePlayers) {
			if (p != pl) {
				if (pl.isAllIn()) {
					allIns++;
				}
				if (!pl.isActionClosed()) {
					allCalled = false;
				}
			}
		}
		done = mainLogicBody(done, i, p, allCalled, allIns);
		return done;
	}

	private boolean mainLogicBody(boolean done, int i, Player p,
			boolean allCalled, int allIns) {
		if (p.isInHand()
				&& p.hasChips()
				&& ((remainingActiveCount - allIns > 1) || (remainingActiveCount
						- allIns >= 1 && (currentRaise
						- p.getPutInPotOnStreet() > 0)))) {
			done = playerAct(done, i, p, allCalled);
		} else {
			handlePotSize(p);
		}
		return done;
	}

	private void handlePotSize(Player p) {
		Player largestRaiserYet = null;
		for (Player player : activePlayers) {
			if (largestRaiserYet == null) {
				largestRaiserYet = player;
			} else if (player.getPutInPotOnStreet() > largestRaiserYet
					.getPutInPotOnStreet()) {
				largestRaiserYet = player;
			}
		}
		if (p.isAllIn() && !(p.equals(largestRaiserYet))) {
			pots = Pot.generatePots(pots
					.get(MAIN_POT_INDEX).getPlayers());
			// pots.addAll(pots.get(MAIN_POT_INDEX).calcSidePots(p.getLastSize()));
			// pots = getUniquePots(pots);
		}
	}

	private boolean playerAct(boolean done, int i, Player p, boolean allCalled) {
		if ((!allCalled) || (!p.hasActed())) {
			p.setActive(true);
			p.updateSizing(currentRaise, oldRaise);
			updateGUI(START_OF_TURN, i);
			p.act();

			lock.lock();
			try {
				while (!playerNotified) {
					actionReceived.await();
				}
				playerNotified = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
			p.setActive(false);
			if (!p.isActionClosed()) {
				done = false;
			}
		} else {
			p.setIsClosed(true);
		}
		return done;
	}

	private void pauseShowdown() {
		try {
			Thread.sleep(activePlayers.size() > 1 ?
					SHOWDOWN_PAUSE_MULTIPLE : SHOWDOWN_PAUSE_SINGLE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void pause(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This returns false if and only if more than two players are not all
	 * in.
	 * @return whether everyone is all in
	 */
	private boolean isEveryoneAllIn() {
		int active = 0;
		for (Player p : activePlayers) {
			if (!p.isAllIn()) {
				active++;
			}
			if (active > 1) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isHandContested() {
		int inHand = 0;
		for (Player p : activePlayers) {
			if (p.isInHand()) {
				inHand++;
			}
			if (inHand > 1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Prints hh for a single street.
	 * 
	 * @param aStreet
	 *            street to print
	 * @return String for HHPrinter
	 */
	public String streetToString(String aStreet) {
		String toReturn = "";
		if (aStreet.equals("Pre-flop")) {
			int seat = 1;
			for (Player p : activePlayers) {
				updateChat("Seat " + seat);
				if (seat == 1) {
					updateChat(" (SB)");
				} else if (seat == 2) {
					updateChat(" (BB)");
				}
				if (activePlayers.size() == 2) {
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
		toReturn += aStreet + ": (" + pots.get(MAIN_POT_INDEX).getSize() + ")"
				+ "\n";
		if (board.getCardCount() > 0) {
			toReturn += board.toString() + "\n";
		}
		toReturn += "(" + activePlayers.size() + " Players)" + "\n";
		if (aStreet.equals("Showdown")) {
			if (activePlayers.size() > 1 || isEveryoneAllIn()) {
				for (Player p : activePlayers) {
					toReturn += p.getName() + " shows "
							+ p.getHand().toString() + " for "
							+ HandRanker.getHandName(p.getHand(), board) + "\n";
				}
			}
		}
		return toReturn;
	}

	/**
	 * Shows results.
	 * 
	 * @param winner
	 *            player who won pot
	 * @return String for HHPrinter
	 */
	public String getResults(String winner, Pot pot) {
		return winner + " wins " + pot.getSize() + "\n";
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
	 * 
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
			currentState = new GameState(stablePlayers, active, pots, board,
					stableIndex);
			firePropertyChange(GameView.GENERATE_GUI_START_OF_TURN, oldState,
					currentState);
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
	 * @param endOfHand
	 *            true if this is the end of the hand.
	 * @param players
	 *            true
	 * @param index
	 */
	private void updateGUI(int updateType) {
		// Lock since stablePlayers is being iterated through in GameState.
		lock.lock();
		currentState = new GameState(stablePlayers, pots, board);
		lock.unlock();
		if (updateType == GameModel.ALL_IN) {
			firePropertyChange(GameView.GENERATE_GUI_ALL_IN, oldState,
					currentState);
		} else if (updateType == GameModel.START_OF_STREET) {
			firePropertyChange(GameView.GENERATE_GUI_START_OF_STREET, oldState,
					currentState);
		} else if (updateType == GameModel.SHOWDOWN) {
			firePropertyChange(GameView.GENERATE_GUI_SHOWDOWN, oldState,
					currentState);
		} else if (updateType == GameModel.SHOWDOWN) {
			firePropertyChange(GameView.GENERATE_GUI_SINGLE_PLAYER_SHOWDOWN,
					oldState, currentState);
		} else if (updateType == GameModel.END_OF_HAND) {
			firePropertyChange(GameView.GENERATE_GUI_END_OF_HAND, oldState,
					currentState);
		} else if (updateType == GameModel.PLAYER_JOINED) {
			// firePropertyChange(GameView.GENERATE_GUI_PLAYER_JOINED, oldState,
			// currentState);
		} else {
			try {
				throw new Exception("Not a legal update");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PLAYER_FOLDED)) {
			remainingActiveCount--;
			for (Pot p : pots) {
				p.removePlayer((Player) evt.getSource());
			}
			firePropertyChange(GameView.FOLD_MADE, "", "");
			actionReceived();
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
						p.raise(action.getSize());
					} else if (action.getAction().equals(Action.ActionType.BET)) {
						p.bet(action.getSize());
					} else if (action.getAction()
							.equals(Action.ActionType.FOLD)) {
						p.fold();
					} else if (action.getAction()
							.equals(Action.ActionType.CALL)) {
						p.call();
					} else if (action.getAction().equals(
							Action.ActionType.CHECK)) {
						p.check();
					}
				}
			}
		}
	}

	private void addMoneyToPots(int paid, Player player) {
		pots.get(MAIN_POT_INDEX).addPlayer(player);
		pots = Pot.generatePots(pots.get(MAIN_POT_INDEX).getPlayers());
	}

	/**
	 * Allows the model to process the action of a player.
	 */
	private void actionReceived() {
		lock.lock();
		try {
			playerNotified = true;
			actionReceived.signalAll();
		} finally {
			lock.unlock();
		}
	}

}