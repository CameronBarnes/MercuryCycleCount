package com.cameronbarnes.mercury.util;

import com.cameronbarnes.mercury.core.Options;
import com.cameronbarnes.mercury.core.SavedOngoing;
import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.stock.Part;
import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.platform.commons.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class FileSystemUtilsTest extends TestCase {
	
	public void testReadWriteOptions() {
		
		//Setup object with non-default values
		Options options = new Options();
		options.setAllowedAutoAdjustment(false);
		options.setAllowedWritePhysicalQuantity(true);
		
		options.getPartDetailSettings().replace("AllocatedQuantity", true);
		options.getPartDetailSettings().replace("FreeQuantity", true);
		
		File file = new File("options_test.json");
		
		//Do the write here
		FileSystemUtils.writeOptions(options, file);
		
		assertTrue(file.exists());
		System.out.println("WRITE Options PASSED");
		
		//Do the read here
		Options read = FileSystemUtils.readOptions(file);
		assertNotNull(read);
		System.out.println("READ Options PASSED");
		
		//Make sure they're the same before and after
		assertEquals(options, read);
		System.out.println("READ/WRITE Options PASSED\n");
		
		//Cleanup
		file.delete();
		
	}
	
	public void testReadWriteSaveSession() {
		
		System.out.println("Test READ/WRITE SavedOngoing to DISK");
		
		//The dir to do these tests in, make sure it doesn't exist
		File outDir = new File("." + File.separator + "testOut").getAbsoluteFile();
		FileSystemUtils.deleteDir(outDir);
		assertFalse(outDir.exists());
		
		//Test for 4 first, as that's what's expected
		ArrayList<Bin> bins =  DebugUtils.generateTestBinData(5);
		DebugUtils.generateTestStockStatusFiles(4, Options.PROCESS_FOLDER);
		
		SavedOngoing expectedOut = new SavedOngoing(outDir, 0, bins);
		
		//Do the save/write here
		System.out.println();
		FileSystemUtils.saveOngoing(bins, outDir);
		assertTrue(outDir.exists());
		// We want to make sure this is actually creating dummy stockstatus files, obviously they're just blank, but we want to make sure they're getting moved here
		assertTrue(Arrays.stream(Objects.requireNonNull(new File(outDir.getPath() + File.separator + "stockstatus").listFiles())).anyMatch(file -> file.getName().matches("\\w{10}\\.xlsx")));
		System.out.println("WRITE SavedOngoing 5 PASSED");
		
		Optional<SavedOngoing> result = FileSystemUtils.getSavedSessionFromDir(outDir);
		assertTrue(result.isPresent());
		System.out.println("READ SavedOngoing 5 PASSED");
		
		assertEquals(expectedOut, result.get());
		System.out.println("READ/WRITE SavedOngoing 5 PASSED");
		
		//Cleanup
		FileSystemUtils.deleteDir(outDir);
		assertFalse(outDir.exists());
		System.out.println("FileSystemUtils.deleteDir() Test PASSED");
		
		//Test for 10, which would not be an unreasonable number to expect
		bins =  DebugUtils.generateTestBinData(10);
		
		expectedOut = new SavedOngoing(outDir, 0, bins);
		
		//Do the save/write here
		FileSystemUtils.saveOngoing(bins, outDir);
		
		result = FileSystemUtils.getSavedSessionFromDir(outDir);
		assertTrue(result.isPresent());
		
		assertEquals(expectedOut, result.get());
		System.out.println("READ/WRITE SavedOngoing 10 PASSED");
		
		//Cleanup
		FileSystemUtils.deleteDir(outDir);
		assertFalse(outDir.exists());
		
		//Test for 50, which is a lot, but I want to make sure it behaves well at larger data sizes
		bins =  DebugUtils.generateTestBinData(50);
		
		expectedOut = new SavedOngoing(outDir, 0, bins);
		
		//Do the save/write here
		FileSystemUtils.saveOngoing(bins, outDir);
		
		result = FileSystemUtils.getSavedSessionFromDir(outDir);
		assertTrue(result.isPresent());
		
		assertEquals(expectedOut, result.get());
		System.out.println("READ/WRITE SavedOngoing 50 PASSED");
		
		//Cleanup
		FileSystemUtils.deleteDir(outDir);
		assertFalse(outDir.exists());
		
		//Test for 100, which is ridiculous, but if anything is going to fail, it would be this
		bins = DebugUtils.generateTestBinData(100);
		
		expectedOut = new SavedOngoing(outDir, 0, bins);
		
		//Do the save/write here
		FileSystemUtils.saveOngoing(bins, outDir);
		
		result = FileSystemUtils.getSavedSessionFromDir(outDir);
		assertTrue(result.isPresent());
		
		assertEquals(expectedOut, result.get());
		System.out.println("READ/WRITE SavedOngoing 100 PASSED\n");
		
		//Cleanup
		FileSystemUtils.deleteDir(outDir);
		assertFalse(outDir.exists());
		
	}
	
}