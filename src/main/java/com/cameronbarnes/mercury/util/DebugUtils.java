package com.cameronbarnes.mercury.util;

import com.cameronbarnes.mercury.core.Options;
import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.stock.Part;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class DebugUtils {
	
	public static void generateTestStockStatusFiles(int num, File outFolder) {
		
		for (int i = 0; i < num; i++) {
			
			try {
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
				RandomStringUtils.random(20, true, true),
				RandomStringUtils.random(50, true, false),
				warehouse,
				binNum,
				physQty,
				0,
				physQty,
				ThreadLocalRandom.current().nextDouble(0.01, 200.0)
		);
		
		// I dont want all of them to have comments, as there's some additional different behavior for when they dont
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
	
	public static ArrayList<Bin> generateTestBinData(int numBins) {
		
		ArrayList<Bin> bins = new ArrayList<>();
		
		bins.add(new Bin(RandomStringUtils.random(10, true, true), RandomStringUtils.random(10, true, true), new ArrayList<>()));
		
		for (int i = 1; i < numBins; i++) {
			
			String binNum = RandomStringUtils.random(10, true, true);
			String warehouse = RandomStringUtils.random(10, true, true);
		
			boolean shouldHaveComments = ThreadLocalRandom.current().nextBoolean();
			int numParts = ThreadLocalRandom.current().nextInt(5, 200);
			ArrayList<Part> parts = new ArrayList<>();
			
			for (; numParts > 0; numParts--) {
				
				parts.add(generateTestPartData(binNum, warehouse, shouldHaveComments));
				
			}
			
			bins.add(new Bin(binNum, warehouse, parts));
			
		}
		
		return bins;
		
	}

}
