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

import com.cameronbarnes.mercury.core.Options;
import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.stock.Part;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DebugUtils {
	
	public static void generateTestStockStatusFiles(int num, File outFolder) {
		
		for (int i = 0; i < num; i++) {
			
			try {
				//noinspection ResultOfMethodCallIgnored
				new File(outFolder.getAbsoluteFile().getPath() + File.separator + RandomStringUtils.random(10, true, false ) + ".xlsx").createNewFile();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	public static Part generateTestPartData(String binNum, String warehouse, boolean hasComments) {
		
		Options dummyOptions = new Options();
		
		int physQty = ThreadLocalRandom.current().nextInt(1, 101);
		
		Part part = new Part(
				generateRandomPartNumber(),
				RandomStringUtils.random(50, true, false),
				warehouse,
				binNum,
				physQty,
				0,
				physQty,
				ThreadLocalRandom.current().nextDouble(0.01, 200.0)
		);
		
		// I don't want all of them to have comments, as there's some additional different behavior for when they don't
		if (hasComments && ThreadLocalRandom.current().nextInt(0, 10) >= 2) {
			part.setComments(RandomStringUtils.random(ThreadLocalRandom.current().nextInt(10, 40), true, false));
		}
		
		if (physQty == 1)
			part.setCountedQuantity(ThreadLocalRandom.current().nextBoolean() ? 1 : 0);
		else
			part.setCountedQuantity(ThreadLocalRandom.current().nextInt(0, physQty + 1));
		
		if (ThreadLocalRandom.current().nextBoolean())
			part.autoAdjustment(dummyOptions);
		
		return part;
		
	}
	
	public static List<Part> generateTestPartList(String binNum, String wareHouse, boolean hasComments, int num) {
		
		ArrayList<Part> parts = new ArrayList<>(num);
		
		for (int i = 0; i < num; i++) {
			parts.add(i, generateTestPartData(binNum, wareHouse, hasComments && ThreadLocalRandom.current().nextBoolean()));
		}
		
		return parts;
		
	}
	
	public static ArrayList<Bin> generateTestBinData(int numBins) {
		
		ArrayList<Bin> bins = new ArrayList<>();
		
		bins.add(new Bin(generateRandomBinNumber(), RandomStringUtils.random(10, true, true), new ArrayList<>(), null));
		
		for (int i = 1; i < numBins; i++) {
			
			String binNum = generateRandomBinNumber();
			String warehouse = RandomStringUtils.random(10, true, true);
		
			boolean shouldHaveComments = ThreadLocalRandom.current().nextBoolean();
			int numParts = ThreadLocalRandom.current().nextInt(5, 200);
			
			bins.add(new Bin(binNum, warehouse, DebugUtils.generateTestPartList(binNum, warehouse, shouldHaveComments, numParts), null));
			
		}
		
		return bins;
		
	}
	
	public static String generateRandomPartNumber() {
		// I think this is a decent approximation, good enough for sure
		return RandomStringUtils.random(4, true, true) + "."
				+ RandomStringUtils.random(5, true, true) + "."
				+ RandomStringUtils.random(3, true, true);
		
	}
	
	public static String generateRandomBinNumber() {
		return RandomStringUtils.random(4, true, false) + "-" + RandomStringUtils.random(4, false, true);
	}

}
