/**
 * 
 */
package com.xxx.galcon.http;

import org.junit.Before;
import org.junit.Test;

import com.xxx.galcon.model.GameBoard;

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


}
