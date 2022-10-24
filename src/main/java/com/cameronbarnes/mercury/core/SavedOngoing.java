package com.cameronbarnes.mercury.core;

import com.cameronbarnes.mercury.stock.Bin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SavedOngoing {
	
	private final File mSaveDir;
	private int mNumFiles = 0;
	private final ArrayList<Bin> mBins;
	
	public SavedOngoing(File saveDir, int numFiles, ArrayList<Bin> bins) {
		
		mSaveDir = saveDir;
		mNumFiles = numFiles;
		mBins = bins;
		
	}
	
	public int getNumFiles() {
		return mNumFiles;
	}
	
	public ArrayList<Bin> getBins() {
		return mBins;
	}
	
	public File getSaveDir() {
		return mSaveDir;
	}
	
	private String getBinsStr() {
		
		StringBuilder sb = new StringBuilder();
		mBins.forEach(bin -> {sb.append(bin.getBinNum()); sb.append(" ");});
		return sb.toString().trim();
		
	}
	
	@Override
	public String toString() {
		return "Date: " + mSaveDir.getName() + ". Num StockStatus Files: " + mNumFiles + ". Bins: " + getBinsStr();
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
		
		if (mNumFiles != s.mNumFiles)
			return false;
		
		return mBins.stream().allMatch(bin -> s.mBins.stream().anyMatch(bin2 -> bin2.equals(bin)));
		
	}
	
}
