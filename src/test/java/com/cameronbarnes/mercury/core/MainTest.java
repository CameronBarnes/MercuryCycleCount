package com.cameronbarnes.mercury.core;

import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

public class MainTest extends TestCase {
	
	@Test
	public void testDebug() {
		
		assertFalse(Main.DEBUG); // Debug should be false for any release
		
	}
	
}