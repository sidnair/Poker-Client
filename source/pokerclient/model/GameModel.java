package pokerclient.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
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
	
	private enum Notification {
		START_OF_TURN, START_OF_STREET, SHOWDOWN, END_OF_HAND, ALL_IN,
		PLAYER_JOINED;
	};

	/**
	 * Index of button
	 */
	private int buttonIndex;

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
	 * Lock used to use conditions for receiving actions and updating the GUI.
	 */
	private Lock lock;

	/**
	 * Used to ensure a player action has been processed before proceeding.
	 */
	private Condition actionReceived;

	private Condition playersRemoved;
	
	private Players allPlayers;

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

	private GameSettings settings;
	
	/**
	 * Constructs the game and instantiates players, deck, board, and pots.
	 * 
	 * @param settings configuration params for the game
	 * @param decisionTime decision time
	 */
	public GameModel(GameSettings settings, int id) {
		this.settings = settings;
		flop = this.new Street(3, "Flop", true);
		turn = this.new Street(1, "Turn", false);
		river = this.new Street(1, "River", false);
		allPlayers = new Players();
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
	}

	private void initLock() {
		lock = new ReentrantLock();
		actionReceived = lock.newCondition();
		playersRemoved = lock.newCondition();
	}

	public void run() {
		while (true) {
			updatePlayers();
			while (allPlayers.size() < 2) {
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
	 * @param newPlayer player to add
	 */
	public void addPlayer(Player newPlayer) {
		if (!allPlayers.contains(newPlayer)) {
			lock.lock();
			toAdd.add(newPlayer);
			lock.unlock();
		} else {
			System.err.println("Name " + newPlayer.getName() + " was already"
					+ "taken. Seating was denied");
		}
		updateGUI(Notification.PLAYER_JOINED);
	}

	/**
	 * Add players waiting to join the table.
	 */
	private void addWaitingPlayers() {
		lock.lock();
		for (Player p : toAdd) {
			if (allPlayers.size() > MAX_PLAYERS) {
				// TODO: notify player...
				System.err.println("Table full. " + p.getName()
						+ " was denied seating.");
				lock.unlock();
				return;
			}
			allPlayers.addPlayer(p);
		}
		lock.unlock();
		toAdd.clear();
	}

	/**
	 * Indicates that a player is sitting out and removes them from the game.
	 */
	public void sitOutPlayer(Player player) {
		assert allPlayers.contains(player);
		
		player.sitOut();
		toRemove.add(player);
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
				allPlayers.remove(p);
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
		for (Player p : allPlayers) {
			p.resetHand();
		}
		board.initBoard();
		updateGUI(Notification.END_OF_HAND);
	}

	/**
	 * Initializes the deck, increments positions, makes new pots, resets player
	 * hands, and pays the ante.
	 */
	private void initHand() {
		handCount++;
		deck.init();
		pots.clear();
		pots.add(new Pot());
		for (Player p : allPlayers) {
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
		buttonIndex++;
		firePropertyChange(GameView.UPDATE_BTN, "",
				new Integer(buttonIndex % allPlayers.size()));
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
				updateGUI(Notification.ALL_IN);
				if (isHandContested()) {
					pause(GameSettings.ALL_IN_PAUSE -
							GameSettings.END_OF_STREET_PAUSE);
				}
			}
			dealStreet(numCards);
			updateGUI(Notification.START_OF_STREET);
			updateChat(streetToString(name));
			playStreet(0, false, isFlop);
			pause(GameSettings.END_OF_STREET_PAUSE);
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
		settings.tick();
	}

	private void runPreflop() {
		updateChat(streetToString("Pre-flop"));
		dealPreFlop();
		playStreet(settings.getBigBlind(), true, false);
		pause(GameSettings.END_OF_STREET_PAUSE);
	}
	
	private int getSBOffset() {
		boolean hu = (allPlayers.inHandCount() == 2);
		return hu ? 0 : 1;
	}
	
	private int getBBOffset() {
		boolean hu = (allPlayers.inHandCount() == 2);
		return hu ? 1 : 2;
	}

	private void payBlinds() {
		allPlayers.get(buttonIndex + getSBOffset())
				.paySmallBlind(settings.getSmallBlind());
		allPlayers.get(buttonIndex + getBBOffset())
				.payBigBlind(settings.getBigBlind());
	}

	private void payAntes() {
		for (Player p : allPlayers) {
			p.payAnte(settings.getAnte());
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
		for (Player p : allPlayers.inHand(buttonIndex + 1)) {
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
		if (allPlayers.inHandCount() > 1) {
			updateGUI(Notification.SHOWDOWN);
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
			// TODO - this can fail if everyone has quit.
			assert allPlayers.inHandCount() == 1;
			ship(allPlayers.inHandIterator(0).next(), pots.get(0), 1.0);
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
	 * Plays a single street.
	 * 
	 * @param currentRaise inital raise players are facing - 0 on every street
	 * except pre.
	 * @param flop true when the street is the flop
	 */
	private void playStreet(int raiseSize, boolean preFlop, boolean flop) {
		if (allPlayers.inHandCount() < 2) {
			return;
		}
		
		oldRaise = 0;
		currentRaise = raiseSize;
		int activeStartIndex = buttonIndex + 1;
		if (preFlop) {
			activeStartIndex += getBBOffset();
		}
		
		int cannotPlayCount = 0;
		remainingActiveCount = allPlayers.inHandCount();
		Iterator<Player> iter = allPlayers.inHandIterator(activeStartIndex);
		while (actionUnclosed() && remainingActiveCount > 1 &&
				cannotPlayCount < remainingActiveCount && iter.hasNext()) {
			Player p = iter.next();
			boolean played = mainLogicBody(p, !othersActionUnclosed(p),
					countOtherAllIns(p));
			cannotPlayCount = played ? 0 : cannotPlayCount + 1;
		}
		for (Player p : allPlayers) {
			p.resetStreet();
		}
	}

	private boolean actionUnclosed() {
		return othersActionUnclosed(null);
	}
	
	private boolean othersActionUnclosed(Player p) {
		for (Player pl : allPlayers.inHand(0)) {
			if (p != null && pl.equals(p)) {
				continue;
			}
			if (!pl.isActionClosed()) {
				return true;
			}
		}
		return false;
	}
	
	private int countOtherAllIns(Player p) {
		int allIns = 0;
		for (Player pl : allPlayers.inHand(0)) {
			if (p != null && pl.equals(p)) {
				continue;
			}
			if (pl.isAllIn()) {
				allIns++;
			}
		}
		return allIns;
	}

	private boolean mainLogicBody(Player p, boolean allCalled, int allIns) {
		if (!playerCanAct(p, allIns)) {
			return false;
		}
		
		playerAct(p, allCalled);
		pots = Pot.generatePots(
				new HashSet<Player>(allPlayers.getPlayersCopy()));
		
		return true;
	}

	private boolean playerCanAct(Player p, int allIns) {
		boolean multipleNotAllIn = remainingActiveCount - allIns > 1;
		boolean oneNotAllIn = remainingActiveCount - allIns == 1;
		boolean mustMatchRaise = currentRaise - p.getPutInPotOnStreet() > 0;
		return p.isInHand() && p.hasChips()
				&& (multipleNotAllIn || (oneNotAllIn && mustMatchRaise));
	}

	private void playerAct(Player p, boolean allCalled) {
		if (!allCalled || !p.hasActed()) {
			p.setActive(true);
			p.updateSizing(currentRaise, oldRaise);
			updateGUI(Notification.START_OF_TURN, p);
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
		} else {
			p.setIsClosed(true);
		}
	}

	private void pauseShowdown() {
		try {
			Thread.sleep(allPlayers.inHandCount() > 1 ?
					GameSettings.SHOWDOWN_PAUSE_MULTIPLE :
					GameSettings.SHOWDOWN_PAUSE_SINGLE);
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
		for (Player p : allPlayers.inHand(0)) {
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
		for (Player p : allPlayers.inHand(0)) {
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
	 * @param aStreet street to print
	 * @return String for HHPrinter
	 */
	public String streetToString(String aStreet) {
		String toReturn = "";
		if (aStreet.equals("Pre-flop")) {
			int seat = 1;
			for (Player p : allPlayers.inHand(buttonIndex + getSBOffset())) {
				chatSeatString(seat, p);
				seat++;
			}
		}
		toReturn += aStreet + ": (" + pots.get(MAIN_POT_INDEX).getSize() + ")"
				+ "\n";
		if (board.getCardCount() > 0) {
			toReturn += board.toString() + "\n";
		}
		toReturn += "(" + allPlayers.inHandCount() + " Players)" + "\n";
		if (aStreet.equals("Showdown")) {
			if (allPlayers.inHandCount() > 1 || isEveryoneAllIn()) {
				for (Player p : allPlayers.inHand(buttonIndex)) {
					toReturn += p.getName() + " shows "
							+ p.getHand().toString() + " for "
							+ HandRanker.getHandName(p.getHand(), board) + "\n";
				}
			}
		}
		return toReturn;
	}

	private void chatSeatString(int seat, Player p) {
		updateChat("Seat " + seat);
		if (seat == 1) {
			updateChat(" (SB)");
		} else if (seat == 2) {
			updateChat(" (BB)");
		}
		if (allPlayers.inHandCount() == 2) {
			if (seat == 1) {
				updateChat("/BTN");
			}
		} else if (seat == 3) {
			updateChat(" (BTN)");
		}
		updateChat(": " + p.getName() + "\n");
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
		String s = "POT: " + pots.get(MAIN_POT_INDEX).getSize() + "\n";
		for (Player p : allPlayers) {
			s += p.toString() + "\n";
		}
		return s;
	}

	/**
	 * Returns total number of players playing.
	 * 
	 * @return
	 */
	public int getNumberTotalPlayers() {
		return allPlayers.size();
	}

	private void updateGUI(Notification updateType, Player active) {
		GameState oldState = currentState;
		currentState = new GameState(allPlayers.getPlayersCopy(), active,
				pots, board, allPlayers.indexOf(active));
		if (updateType == Notification.START_OF_TURN) {
			firePropertyChange(GameView.GENERATE_GUI_START_OF_TURN, oldState,
					currentState);
		} else {
			throw new AssertionError("Not a legal update");
		}
	}

	private void updateGUI(Notification updateType) {
		GameState oldState = currentState;
		currentState = new GameState(allPlayers.getPlayersCopy(), pots, board);
		switch (updateType) {
		case ALL_IN:
			firePropertyChange(GameView.GENERATE_GUI_ALL_IN, oldState,
					currentState);
			break;
		case START_OF_STREET:
			firePropertyChange(GameView.GENERATE_GUI_START_OF_STREET, oldState,
					currentState);
			break;
		case SHOWDOWN:
			firePropertyChange(GameView.GENERATE_GUI_SHOWDOWN, oldState,
					currentState);
			break;
		case END_OF_HAND:
			firePropertyChange(GameView.GENERATE_GUI_END_OF_HAND, oldState,
					currentState);
			break;
		case PLAYER_JOINED:
//			 firePropertyChange(GameView.GENERATE_GUI_PLAYER_JOINED, oldState,
//					 currentState);
			break;
		default:
			throw new AssertionError("Not a legal update.");
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
			for (Player p : allPlayers.inHand(0)) {
				Action action = (Action) evt.getNewValue();
				if (p.getName().equals(action.getPlayerName())) {
					takeAction(p, action);
				}
			}
		}
	}

	private void takeAction(Player p, Action action) {
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
	
	public GameSettings getSettings() {
		return settings;
	}

}