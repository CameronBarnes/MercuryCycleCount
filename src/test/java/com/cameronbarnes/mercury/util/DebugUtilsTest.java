/*
 *     Copyright (c) 2022.  Cameron Barnes
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.cameronbarnes.mercury.util;

import com.cameronbarnes.mercury.gui.forms.CountForm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DebugUtilsTest {
	
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