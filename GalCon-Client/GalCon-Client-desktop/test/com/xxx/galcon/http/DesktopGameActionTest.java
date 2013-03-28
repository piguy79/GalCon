/**
 * 
 */
package com.xxx.galcon.http;

import org.junit.Before;
import org.junit.Test;

/**
 * Class used to test HTTP interactions with the Server.
 * 
 * @author conormullen
 * 
 */
public class DesktopGameActionTest {

	public DesktopGameAction desktopGameAction;

	@Before
	public void setup() {
		desktopGameAction = new DesktopGameAction("localhost", 3000);
	}

	@Test
	public void runJoinGameTest() throws ConnectionException {
		desktopGameAction.generateGame("conor");
	}

}
