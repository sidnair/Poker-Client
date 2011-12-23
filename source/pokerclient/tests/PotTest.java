package pokerclient.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import pokerclient.model.Player;
import pokerclient.model.Pot;

public class PotTest {
	
	private static final int NUM_PLAYERS = 3;
	
	private ArrayList<Player> players;
	
	String[] names = { "foo", "bar", "baz" };
	
	@Before
	public void setUp() {
		players = new ArrayList<Player>();
		for (int i = 0; i < NUM_PLAYERS; i++) {
			players.add(new Player(names[i], null, null));
		}
	}

	@Test
	public void testSinglePot() {
		players.get(0).setLastSize(100);
		players.get(1).setLastSize(100);
		players.get(2).setLastSize(100);
		
		ArrayList<Pot> pots = Pot.generatePots(new HashSet<Player>(players));
		assertEquals(1, pots.size());
		assertEquals(300, pots.get(0).getSize());
	}
	
	@Test
	public void testMultiplePots() {
		players.get(0).setLastSize(200);
		players.get(1).setLastSize(100);
		players.get(2).setLastSize(100);
		
		ArrayList<Pot> pots = Pot.generatePots(new HashSet<Player>(players));
		assertEquals(2, pots.size());
		assertEquals(300, pots.get(0).getSize());
		assertEquals(100, pots.get(1).getSize());
	}

}
