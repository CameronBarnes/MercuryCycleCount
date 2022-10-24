package com.cameronbarnes.mercury.core;

import com.cameronbarnes.mercury.excel.ExcelImporter;
import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Ingest {
	
	private final Session mSession;
	
	public Ingest(Session session) {
		mSession = session;
	}
	
	public void ingest(boolean add) {
		ingest(add, Options.IMPORT_FOLDER);
	}
	
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
	
	public void ingest(boolean add, List<File> files) {
		
		ArrayList<Bin> bins = new ArrayList<>();
		
		if (files == null || files.isEmpty()) return;
		
		for (File file: files) {
			
			if (file.isDirectory()) continue;
			
			String extension = FileSystemUtils.getLastSubstring(file.toString(), ".");
			if (!extension.equals("xls") && !extension.equals("xlsx")) continue;
			
			ExcelImporter.importBinFromStockStatusFile(file).ifPresent(bin -> {
				if (mSession.getBins().stream().noneMatch(bin1 -> bin1.getBinNum().equals(bin.getBinNum()))) {
					bins.add(bin);
				}
			});
			
			try {
				Files.move(file.toPath(), Path.of(Options.PROCESS_FOLDER.getPath() + File.separator + file.getName()));
			}
			catch (IOException e) { //Pretty sure this shouldn't ever happen, but we'll leave it here just in case
				e.printStackTrace();
			}
			
		}
		
		if (!add) {
			mSession.setBins(bins);
		} else {
			mSession.getBins().addAll(bins);
		}
		
	}
	
}
