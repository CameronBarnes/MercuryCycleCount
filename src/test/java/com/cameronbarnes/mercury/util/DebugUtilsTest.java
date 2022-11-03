package com.cameronbarnes.mercury.util;

import com.cameronbarnes.mercury.gui.forms.CountForm;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DebugUtilsTest extends TestCase {
	
	@Test
	public void testGenerateRandomPartNumber() {
		for (int i = 0; i < 20; i++) {
			String generated = DebugUtils.generateRandomPartNumber();
			boolean match = generated.matches(CountForm.PN_PATTERN)
					                && generated.toUpperCase().matches(CountForm.PN_PATTERN)
					                && generated.toLowerCase().matches(CountForm.PN_PATTERN);
			if (!match) {
				System.out.println(generated + " does not match the part number pattern");
			}
			assertTrue(match);
		}
		System.out.println("Random Part Number Generator PASSED");
	}
	
	@Test
	public void testGenerateRandomBinNumber() {
		
		for (int i = 0; i < 20; i++) {
			String generated = DebugUtils.generateRandomBinNumber();
			boolean match = generated.matches(CountForm.BIN_NO_PATTERN)
					&& generated.toUpperCase().matches(CountForm.BIN_NO_PATTERN)
					&& generated.toLowerCase().matches(CountForm.BIN_NO_PATTERN);
			if (!match) {
				System.out.println(generated + " does not match the bin number pattern");
			}
			assertTrue(match);
		}
		System.out.println("Random Bin Number Generator PASSED");
		
	}
	
}