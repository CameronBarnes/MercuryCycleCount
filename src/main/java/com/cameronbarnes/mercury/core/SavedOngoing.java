package com.cameronbarnes.mercury.core;

import com.cameronbarnes.mercury.stock.Bin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

public final class SavedOngoing {
	
	private final File mSaveDir;
	private final ArrayList<Bin> mBins;
	
	/**
	 * This class handles holding the data for saving ongoing cycle count progress to disk and reading it from the disk.
	 * The actual read and write functions are handled by FileSystemUtils but the data is stored here
	 * @param saveDir The directory to save to or load from
	 * @param bins the bins to save or the bins loaded from the disk
	 */
	public SavedOngoing(File saveDir, ArrayList<Bin> bins) {
		
		mSaveDir = saveDir;
		mBins = bins;
		
	}
	
	public int getNumStockStatusFiles() {
		return (int) Arrays.stream(Objects.requireNonNull(new File(mSaveDir.getAbsolutePath() + File.separator + "stockstatus").listFiles())).dropWhile(File::isDirectory).count();
	}
	
	public ArrayList<Bin> getBins() {
		return mBins;
	}
	
	public File getSaveDir() {
		return mSaveDir;
	}
	
	/**
	 *
	 * @return the bin number of each bin separated by a space
	 */
	private String getBinsStr() {
		
		StringBuilder sb = new StringBuilder();
		mBins.forEach(bin -> {sb.append(bin.getBinNum()); sb.append(" ");});
		return sb.toString().trim();
		
	}
	
	@Override
	public String toString() {
		return "Date: " + mSaveDir.getName() + ". Num StockStatus Files: " + getNumStockStatusFiles() + ". Bins: " + getBinsStr();
	}
	
	public String toStringWLocale(ResourceBundle bundle) {
		return bundle.getString("word_date") + ": " + mSaveDir.getName() + ". " + bundle.getString("saved_ongoing_text1") + ": " + getNumStockStatusFiles() + ". " + bundle.getString("word_bins") + ": " + getBinsStr();
	}
	
	@Override
	public boolean equals(Object o) {
		
		if (!(o instanceof SavedOngoing s))
			return false;
		
		try {
			if (!s.mSaveDir.getCanonicalPath().equals(mSaveDir.getCanonicalPath()))
				return false;
		}
		catch (IOException e) {
			return false;
		}
		
		if (getNumStockStatusFiles() != s.getNumStockStatusFiles())
			return false;
		
		return mBins.stream().allMatch(bin -> s.mBins.stream().anyMatch(bin2 -> bin2.equals(bin)));
		
	}
	
}
