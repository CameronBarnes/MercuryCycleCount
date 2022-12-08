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
import com.cameronbarnes.mercury.core.SavedOngoing;
import com.cameronbarnes.mercury.stock.Bin;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class FileSystemUtilsTest extends TestCase {
	
	@Test
	public void testReadWriteSaveSession() {
		
		//The dir to do these tests in, make sure it doesn't exist
		File outDir = new File("." + File.separator + "testOut").getAbsoluteFile();
		FileSystemUtils.deleteDir(outDir);
		assertFalse(outDir.exists());
		
		//Test for 4 first, as that's what's expected
		ArrayList<Bin> bins =  DebugUtils.generateTestBinData(5);
		DebugUtils.generateTestStockStatusFiles(4, Options.PROCESS_FOLDER);
		
		SavedOngoing expectedOut = new SavedOngoing(outDir, bins);
		
		//Do the save/write here
		FileSystemUtils.saveOngoing(bins, outDir);
		assertTrue(outDir.exists());
		// We want to make sure this is actually creating dummy stockstatus files, obviously they're just blank, but we want to make sure they're getting moved here
		assertTrue(Arrays.stream(Objects.requireNonNull(new File(outDir.getPath() + File.separator + "stockstatus").listFiles())).anyMatch(file -> file.getName().matches("\\w{10}\\.xlsx")));
		
		Optional<SavedOngoing> result = FileSystemUtils.getSavedSessionFromDir(outDir);
		assertTrue(result.isPresent());
		
		assertEquals(expectedOut, result.get());
		System.out.println("READ/WRITE SavedOngoing 5 PASSED");
		
		//Cleanup
		FileSystemUtils.deleteDir(outDir);
		assertFalse(outDir.exists());
		System.out.println("FileSystemUtils.deleteDir() Test PASSED");
		
		//Test for 100, which is ridiculous, but if anything is going to fail, it would be this
		bins = DebugUtils.generateTestBinData(100);
		
		expectedOut = new SavedOngoing(outDir, bins);
		
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