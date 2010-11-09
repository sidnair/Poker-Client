import java.io.Serializable;
import java.util.ArrayList;

/**
 * Determines the winner out of all the eligible players in a pot. This 
 * interfaces with a hand evaluator written by the University of Alberta by
 * essentially wrapping the Hands past into EvalHands and ranking those
 * using the imported ranker. 
 *  
 * @author Sid Nair
 *
 */
public class HandRanker implements Serializable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 5842528799774827759L;
	
	/**
	 * Default size to use for the array list of ties. This is set
	 * at such a low number since these lists will rarely exceed this size.
	 * Thus, this keeps the memory overhead at a minimum. Occasionally, the 
	 * lists will have to be made bigger, but this is an acceptable consequence.
	 * This is especially necessary since this lists are stored in memory
	 * even when the ranking method isn't written since creating a new list
	 * each time is inefficient.
	 */
	private final static int DEFAULT_TIE_LIST_SIZE = 2;

	/**
	 * Default size to use for the ArrayList of winners. This is set to one by
	 * default since there is only one winner.
	 */
	private final static int DEFAULT_WINNER_LIST_SIZE = 1;
	
	/**
	 * Constant used when the first hand wins.
	 */
	private final static int FIRST_WINS = 1;
	
	/**
	 * Constant used when the first hand wins.
	 */
	private final static int SECOND_WINS = -1;
	
	/**
	 * Constant used when the first hand wins.
	 */
	private final static int TIE = 0; 
	
	/**
	 * Hand evaluator created by the University of Alberta that actually ranks
	 * the hands. 
	 */
	private EvalHandEvaluator evaluator;
	
	/**
	 * Constructor that instantiates the EvalHandEvaluator used to check the 
	 * hands and the arraylists used to keep track of the winners and tiers.
	 */
	public HandRanker() {
		evaluator = new EvalHandEvaluator();
		winner = new ArrayList<Player>(DEFAULT_TIE_LIST_SIZE);
		tiers = new ArrayList<Player>(DEFAULT_WINNER_LIST_SIZE);
	}
	
	/**
	 * List of players who have tied for the current pot.
	 */
	private ArrayList<Player> tiers;
	
	/**
	 * ArrayList used to store the winner of the hand. Since there can only be
	 * one winner, this could actually be a player. However, it needs to be
	 * an ArrayList to allow the findWinner method to have the same return
	 * type for ties and winners. This is a class variable so that the 
	 * findWinner method doesn't need to create a new ArrayList for the winner
	 * each time it runs.
	 */
	private ArrayList<Player> winner;
	
	/**
	 * Determines the winner or winners, returning a list of people with the
	 * best hand.
	 * 
	 * @param pot pot that contains the players to be tested
	 * 
	 * @return list of winners
	 */
	public ArrayList<Player> findWinner(Pot pot, Board board) {
		EvalHand bestHandYet = null;
		Player currentWinner = null;
		tiers.clear();
		for (Player player : pot) {
			EvalHand newHand = makeEvalHand(player.getHand(), board);
			int result = compareHand(newHand, bestHandYet);
			if (result == FIRST_WINS) {
				bestHandYet = newHand;
				currentWinner = player;
				tiers.clear();
			} else if (result == TIE) {
				if (!tiers.contains(currentWinner)) {
					tiers.add(currentWinner);
				}
				tiers.add(player);
				currentWinner = player;
			} else if (result == SECOND_WINS) {
				tiers.clear(); //keep currently selected best hand and player
			} else {
				System.out.println("No valid result detected");
				System.exit(-1);
			}
		}
		if (tiers.size() > 0) {
			//used to test for bugs - shouldn't actually happen
			/*if (tiers.size() == 1) {
				System.out.println("1 tie, but 1 winner. Exiting.");
				System.exit(-1);
			}*/
			return tiers;
		}
		if (winner.size() == 0) {
			winner.add(currentWinner);
		} else {
			winner.set(0, currentWinner);
		}
		return winner;
		
		/*
		 * EvalHand bestHandYet = null;
		Player currentWinner = null;
		winners.clear();
		tiers.clear();
		for (Player player : pot) {
			EvalHand newHand = makeEvalHand(player.getHand(), board);
			int result = compareHand(newHand, bestHandYet);
			if (result == FIRST_WINS) {
				bestHandYet = newHand;
				winners.clear();
				currentWinner = player;
				winners.add(player);
				tiers.clear();
			} else if (result == TIE) {
				tiers.add(currentWinner);
				tiers.add(player);
				currentWinner = player;
			} else if (result == SECOND_WINS) {
				tiers.clear();
				//keep currently selected best hand and player
			} else {
				System.out.println("No valid result detected");
				System.exit(-1);
			}
		}
		if (tiers.size() > 0) {
			if (tiers.size() == 1) {
				System.out.println("1 tie, but 1 winner. Exiting.");
				System.exit(-1);
			}
			winners = tiers;
		}
		return winners;
		 */
	}
	
	/**
	 * Returns the name of the hand
	 * 
	 * @param hand to evaluate and read
	 * @param board board which the hand can use
	 * @return name of the hand
	 */
	@SuppressWarnings("static-access")
	public String getHandName(Hand hand, Board board) {
		return evaluator.nameHand(makeEvalHand(hand, board));
	}
	
	/**
	 * Converts a hand to an EvalHand. An EvalHand has seven cards - the 
	 * player's hand and the five cards on the board. 
	 * 
	 * @param hand hand the player holds
	 * @param board board the player can use
	 * @return an EvalHand version of the hand
	 */
	private EvalHand makeEvalHand(Hand hand, Board board) {
		String cardNames = "";
		for (Card c : hand.getHand()) {
			cardNames += c.toString() + " ";
		}
		for (Card c : board) {
			cardNames += c.toString() + " ";
		}
		cardNames = cardNames.substring(0, cardNames.length() - 1);
		return new EvalHand(cardNames);
	}
	
	/**
	 * Compares two hands.
	 * 
	 * @param handOne first hand to return
	 * @param handTwo second hand to return
	 * @return int corresponding with the result of the comparison
	 */
	private int compareHand(EvalHand handOne, EvalHand handTwo) {
		if (handOne == null) {
			return SECOND_WINS;
		} else if (handTwo == null) {
			return FIRST_WINS;
		} else {
			return evaluator.compareHands(handOne, handTwo);
		}
	}

}
