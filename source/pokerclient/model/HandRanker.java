package pokerclient.model;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Determines the winner out of all the eligible players in a pot. This 
 * interfaces with a hand evaluator written by the University of Alberta by
 * essentially wrapping the Hands past into EvalHands used by their ranker. 
 */
public class HandRanker implements Serializable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 5842528799774827759L;
	
	/**
	 * Constant used when the first hand wins.
	 */
	private final static int FIRST_WINS = 1;
	
	/**
	 * Constant used when the second hand wins.
	 */
	private final static int SECOND_WINS = -1;
	
	/**
	 * Constant used when the two hands tie.
	 */
	private final static int TIE = 0; 
	
	/**
	 * Determines the winner or winners, returning a list of people with the
	 * best hand.
	 * 
	 * @param pot pot that contains the players to be tested
	 * 
	 * @return list of winners
	 */
	public static ArrayList<Player> findWinner(Pot pot, Board board) {
		EvalHandEvaluator evaluator = new EvalHandEvaluator();
		// It's unlikely that we'll have more than 2 ties.
		ArrayList<Player> winners = new ArrayList<Player>(2);
		EvalHand bestHandYet = null;
		
		for (Player player : pot) {
			EvalHand newHand = makeEvalHand(player.getHand(), board);
			int result = compareHand(evaluator, newHand, bestHandYet);
			if (result == FIRST_WINS) {
				bestHandYet = newHand;
				winners.clear();
				winners.add(player);
			} else if (result == TIE) {
				winners.add(player);
			} else if (result == SECOND_WINS) {
				// Keep current player in winners
			} else {
				throw new AssertionError("No valid result detected.");
			}
		}
		return winners;
	}
	
	/**
	 * Returns the name of the hand
	 * 
	 * @param hand to evaluate and read
	 * @param board board which the hand can use
	 * @return name of the hand
	 */
	public static String getHandName(Hand hand, Board board) {
		return EvalHandEvaluator.nameHand(makeEvalHand(hand, board));
	}
	
	/**
	 * Converts a hand to an EvalHand. An EvalHand has seven cards - the 
	 * player's hand and the five cards on the board. 
	 * 
	 * @param hand hand the player holds
	 * @param board board the player can use
	 * @return an EvalHand version of the hand
	 */
	private static EvalHand makeEvalHand(Hand hand, Board board) {
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
	private static int compareHand(EvalHandEvaluator evaluator, 
			EvalHand handOne, EvalHand handTwo) {
		if (handOne == null) {
			return SECOND_WINS;
		} else if (handTwo == null) {
			return FIRST_WINS;
		} else {
			return evaluator.compareHands(handOne, handTwo);
		}
	}

}
