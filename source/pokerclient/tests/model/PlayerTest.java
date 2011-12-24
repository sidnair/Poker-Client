package pokerclient.tests.model;

import static org.junit.Assert.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.junit.Before;
import org.junit.Test;

import pokerclient.model.Player;

public class PlayerTest {
	
	// TODO - can check things like put in pot, total put in pot, isCheckable,
	// etc. more thoroughly. Right now, they're mostly sanity checks that should
	// notice blatant breakage.

	private final static String NAME = "name";
	private final static String PATH = "path";
	private final static int START_STACK = 2000;
	private final static int BIG_BLIND = 10;
	
	private Player p;
	
	@Before
	public void SetUp() {
		p = new Player(NAME, PATH, START_STACK, BIG_BLIND, new DummyListener());
	}
	
	@Test
	public void testConstructor() {
		assertEquals(NAME, p.getName());
		assertEquals(PATH, p.getAvatarPath());
		assertEquals(START_STACK, p.getStack());
	}
	
	@Test
	public void testPayBigBlind() {
		p.payBigBlind(BIG_BLIND);
		assertEquals(START_STACK - BIG_BLIND, p.getStack());
		assertEquals(true, p.isBigBlind());
		assertEquals(true, p.isBlind());
	}
	
	@Test
	public void testPaySmallBlind() {
		p.paySmallBlind(BIG_BLIND / 2);
		assertEquals(START_STACK - BIG_BLIND / 2, p.getStack());
		assertEquals(true, p.isSmallBlind());
		assertEquals(true, p.isBlind());
	}
	
	@Test
	public void testPayAnte() {
		p.payAnte(1);
		assertEquals(START_STACK - 1, p.getStack());
		assertEquals(false, p.isBlind());
	}
	
	@Test
	public void testFailsOnNegativeStack() {
		try {
			p.pay(START_STACK + 1);
			fail("No assertion error thrown.");
		} catch (AssertionError e){
		}
	}
	
	@Test
	public void testHandlesAllIn() {
		p.pay(START_STACK);
		assertEquals(0, p.getStack());
		assertEquals(true, p.isAllIn());
	}
	
	@Test
	public void testFailsOnTooLargeRaise() {
		try {
			p.raise(START_STACK + 1);
			fail("Should fail on too large raise.");
		} catch (AssertionError e) {
		}
	}
	
	@Test
	public void testFailsOnTooLargeBet() {
		try {
			p.bet(START_STACK + 1);
			fail("Should fail on too large bet.");
		} catch (AssertionError e) {
			
		}
	}
	
	@Test
	public void testFailsOnTooSmallRaise() {
		try {
			p.raise(BIG_BLIND / 2);
			fail("Should fail on too small raise.");
		} catch (AssertionError e) {
		}
	}
	
	@Test
	public void testFailsOnTooSmallBet() {
		try {
			p.bet(BIG_BLIND / 2);
			fail("Should fail on too small bet.");
		} catch (AssertionError e) {
		}
	}
	
	@Test
	public void testAllowsShortShove() {
		p.setStack(BIG_BLIND / 2);
		p.raise(BIG_BLIND / 2);
		assertEquals(0, p.getStack());
		assertEquals(true, p.isAllIn());
	}
	
	@Test
	public void testDisllowsShortNearShove() {
		try {
			p.setStack(BIG_BLIND / 2 + 1);
			p.raise(BIG_BLIND / 2);
			fail("Should fail on illegal raise.");
		} catch (AssertionError e) {
			
		}
	}
	
	@Test
	public void testFoldUpdatesIsInHand() {
		p.fold();
		assertEquals(false, p.isInHand());
	}
	
	@Test
	public void testFoldClosesAction() {
		p.fold();
		assertEquals(true, p.isActionClosed());
	}
	
	@Test
	public void testCallClosesAction() {
		// TODO - add a raise
		p.updateSizing(BIG_BLIND * 3, BIG_BLIND);
		p.call();
		assertEquals(true, p.isActionClosed());
		assertEquals(START_STACK - BIG_BLIND * 3, p.getStack());
	}
	
	@Test
	public void testCheckClosesAction() {
		p.check();
		assertEquals(true, p.isActionClosed());
	}
	
	@Test
	public void testBetOpensAction() {
		p.bet(BIG_BLIND * 2);
		assertEquals(false, p.isActionClosed());
	}
	
	@Test
	public void testRaiseOpensAction() {
		p.raise(BIG_BLIND * 2);
		assertEquals(false, p.isActionClosed());
	}
	
	@Test
	public void testTopOff() {
		p.bet(100);
		assertEquals(START_STACK - 100, p.getStack());
		p.resetHand();
		assertEquals(START_STACK, p.getStack());
	}
	
	private class DummyListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
		}
	}
	
}
