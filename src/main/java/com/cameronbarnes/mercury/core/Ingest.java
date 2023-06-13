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

package com.cameronbarnes.mercury.core;

import com.cameronbarnes.mercury.core.options.Options;
import com.cameronbarnes.mercury.excel.ExcelImporter;
import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.util.FileSystemUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Ingest {
	
	//TODO consider making this stateless by having the session object passed as a function parameter
	private final Session mSession;
	
	public Ingest(Session session) {
		mSession = session;
	}
	
	/**
	 * Import stockstatus files from the import folder
	 * @param add true for add the new bins to the current bins in the session object or false to replace them with only the new ones
	 */
	public void ingest(boolean add) {
		ingest(add, Options.IMPORT_FOLDER);
	}
	
	/**
	 * Import stockstatus files from the import folder
	 * @param add true for add the new bins to the current bins in the session object or false to replace them with only the new ones
	 * @param dir The directory or individual file to import from
	 */
	public void ingest(boolean add, File dir) {
		
		if (dir == null) {
			return;
		}
		File[] files;
		if (dir.isDirectory()) {
			files = dir.listFiles();
			if (files == null) {
				files = new File[]{};
			}
		} else {
			files = new File[]{dir};
		}
		
		ingest(add, List.of(files));
	
	}
	
	/**
	 * Import stockstatus files from the import folder
	 * @param add true for add the new bins to the current bins in the session object or false to replace them with only the new ones
	 * @param files A list of individual files to import from
	 */
	public void ingest(boolean add, List<File> files) {
		
		ArrayList<Bin> bins = new ArrayList<>();
		
		if (files == null || files.isEmpty()) return;
		
		for (File file: files) {
			
			if (file.isDirectory()) continue;
			
			String extension = FileSystemUtils.getLastSubstring(file.toString(), ".");
			if (!extension.equals("xls") && !extension.equals("xlsx")) continue;
			
			ExcelImporter.importBinFromStockStatusFile(file).ifPresent(bin -> {
				
				// We're going to check if any of the existing bins are the same as the one we're trying to add here
				// If they are we're only going to keep the newer of the two
				for (Bin old: mSession.getBins()) {
					
					// For right now I think I'm only going to check for the bin number, but I may change my mind later
					if (old.getBinNum().equals(bin.getBinNum())/* && old.getWarehouse().equals(bin.getWarehouse())*/) {
						
						if (old.getFileTime() != null && old.getFileTime().compareTo(bin.getFileTime()) < 0) {
							
							mSession.getBins().remove(old);
							mSession.getBins().add(bin);
							
							try {
								FileSystemUtils.moveSafe(file.toPath(), Path.of(Options.PROCESS_FOLDER.getPath() + File.separator + file.getName()));
							}
							catch (FileNotFoundException e) { //Pretty sure this shouldn't ever happen, but we'll leave it here just in case
								e.printStackTrace();
							}
							
						}
						
						// If there's a matching bin we'll either remove the old one and add the new above, or discard and continue
						return;
						
					}
					
				}
				
				// If we get here then there is no matching bin, so we'll add this one
				mSession.getBins().add(bin);
				try {
					FileSystemUtils.moveSafe(file.toPath(), Path.of(Options.PROCESS_FOLDER.getPath() + File.separator + file.getName()));
				}
				catch (FileNotFoundException e) { //Pretty sure this shouldn't ever happen, but we'll leave it here just in case
					e.printStackTrace();
				}
				
			});
			
		}
		
		if (!add) {
			mSession.setBins(bins);
		} else {
			mSession.getBins().addAll(bins);
		}
		
	}
	
}
