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

import com.cameronbarnes.mercury.core.Main;
import com.cameronbarnes.mercury.core.Options;
import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.stock.Part;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SerializationUtilsTest {
	
	@Test
	public void testOptionsSerialization() {
		
		//Setup object with non-default values
		Options options = new Options();
		options.setAllowedAutoAdjustment(false);
		options.setAllowedWritePhysicalQuantity(true);
		
		options.getPartDetailSettings().replace("AllocatedQuantity", true);
		options.getPartDetailSettings().replace("FreeQuantity", true);
		options.setFontSize(32);
		options.setVersion(new Main.Version(12, 3, 52, Main.ReleaseType.ALPHA));
		
		String optStr = SerializationUtils.serializeOptions(options);
		String verStr = SerializationUtils.serializeVersion(options.getVersion());
		
		Options read = SerializationUtils.deserializeOptions(optStr).orElse(new Options());
		SerializationUtils.deserializeVersion(verStr).ifPresent(read::setVersion);
		
		//Make sure they're the same before and after
		assertEquals(options, read);
		System.out.println("Options Serialization PASSED\n");
		
	}
	
	@Test
	public void testPartListSerialization() {
		
		long start = System.currentTimeMillis();
		
		// I'll test with comments first
		List<Part> partList = DebugUtils.generateTestPartList(DebugUtils.generateRandomBinNumber(), RandomStringUtils.random(10, true, true), true, 10);
		String serialized = SerializationUtils.serializePartList(partList);
		for (Part part: SerializationUtils.deserializePartList(serialized)) {
			assertTrue(partList.stream().anyMatch(part1 -> part1.equals(part)));
		}
		
		// Then I'll test without comments
		partList = DebugUtils.generateTestPartList(DebugUtils.generateRandomBinNumber(), RandomStringUtils.random(10, true, true), false, 10);
		serialized = SerializationUtils.serializePartList(partList);
		for (Part part: SerializationUtils.deserializePartList(serialized)) {
			assertTrue(partList.stream().anyMatch(part1 -> part1.equals(part)));
		}
		
		// Then I'll test a very large number of parts, what should be way more than is ever in a bin
		partList = DebugUtils.generateTestPartList(DebugUtils.generateRandomBinNumber(), RandomStringUtils.random(10, true, true), true, 400);
		serialized = SerializationUtils.serializePartList(partList);
		for (Part part: SerializationUtils.deserializePartList(serialized)) {
			assertTrue(partList.stream().anyMatch(part1 -> part1.equals(part)));
		}
		
		// Testing for the case of an empty part list
		partList = new ArrayList<>();
		serialized = SerializationUtils.serializePartList(partList);
		List<Part> out = SerializationUtils.deserializePartList(serialized);
		assertNotNull(out);
		assertTrue(out.isEmpty());
		
		System.out.printf("PartList Serialization Test PASSED in %1dms with 420 parts.\n", System.currentTimeMillis() - start);
		
	}
	
	@Test
	public void testBinSerialization() {
		
		long start = System.currentTimeMillis();
		int numParts = 0;
		
		// Testing with standard generated test data with a normal number of bins
		ArrayList<Bin> bins = DebugUtils.generateTestBinData(5);
		for (Bin bin: bins) {
			numParts += bin.getParts().size();
			String serialized = SerializationUtils.serializeBin(bin);
			Optional<Bin> bout = SerializationUtils.deserializeBin(serialized);
			assertTrue(bout.isPresent());
			assertEquals(bin, bout.get());
		}
		
		// Testing with custom constructed bins, we'll test a few bins with a very large number of parts
		// This is pretty ridiculous, but if anything is going to break it should be this
		for (int i = 0; i < 10; i++) {
			
			numParts += 500;
			List<Part> parts = DebugUtils.generateTestPartList(DebugUtils.generateRandomBinNumber(), RandomStringUtils.random(10, true, true), true, 500);
			Bin bin = new Bin(parts.get(0).getBinNum(), parts.get(0).getWarehouse(), parts, null);
			String serialized = SerializationUtils.serializeBin(bin);
			Optional<Bin> out = SerializationUtils.deserializeBin(serialized);
			assertTrue(out.isPresent());
			assertEquals(bin, out.get());
			
		}
		
		System.out.printf("Bin Serialization test PASSED in %1dms, with %2d parts.\n", System.currentTimeMillis() - start, numParts);
	
	}
	
}
