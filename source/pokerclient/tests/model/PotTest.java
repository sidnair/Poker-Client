package pokerclient.tests.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import pokerclient.model.Player;
import pokerclient.model.Pot;

public class PotTest {
	
	private static final int NUM_PLAYERS = 3;
	private static final int DEFAULT_STACK = 2000;
	
	private ArrayList<Player> players;
	
	String[] names = { "foo", "bar", "baz" };
	
	@Before
	public void setUp() {
		players = new ArrayList<Player>();
		for (int i = 0; i < NUM_PLAYERS; i++) {
			Player p = new Player(names[i], null, null);
			p.setStack(DEFAULT_STACK);
			players.add(p);
		}
	}
	
	private void pay(int[] amts) {
		assertEquals(amts.length, players.size());
		for (int i = 0; i < amts.length; i++) {
			Player p = players.get(i);
			p.setTotalPutInPot(amts[i]);
			p.setStack(p.getStack() - amts[i]);
		}
	}

	@Test
	public void testSinglePot() {
		pay(new int[] {100, 100, 100});
		ArrayList<Pot> pots = Pot.generatePots(new HashSet<Player>(players));
		assertEquals(1, pots.size());
		assertEquals(300, pots.get(0).getSize());
	}
	
	@Test
	public void testMultiplePots() {
		pay(new int[] {200, 100, 100});
		ArrayList<Pot> pots = Pot.generatePots(new HashSet<Player>(players));
		assertEquals(1, pots.size());
		assertEquals(400, pots.get(0).getSize());
	}

}
