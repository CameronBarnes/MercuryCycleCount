package com.cameronbarnes.mercury.core;

import com.cameronbarnes.mercury.excel.ExcelExporter;
import com.cameronbarnes.mercury.gui.MainFrame;
import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.util.FileSystemUtils;

import javax.swing.*;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Session {

	private final MainFrame mMainFrame;
	private ArrayList<Bin> mBins = new ArrayList<>();
	private int mCurrentBin = -1;
	private final Options mOptions;
	private final Ingest mIngest;
	
	public Session(MainFrame frame, Options options) {
		mMainFrame = frame;
		mOptions = options;
		mIngest = new Ingest(this);
	}
	
	public void mainMenu() {
		SwingUtilities.invokeLater(() -> mMainFrame.mainMenu(this));
	}
	
	public void ingest() {
		mIngest.ingest(false);
		mBins.sort(Comparator.comparing(Bin::getBinNum));
		SwingUtilities.invokeLater(() -> mMainFrame.ingest(this));
	}
	
	public void addIngest(File dir) {
		mIngest.ingest(true, dir);
		mBins.sort(Comparator.comparing(Bin::getBinNum));
		SwingUtilities.invokeLater(() -> mMainFrame.ingest(this));
	}
	
	public void addIngest(List<File> files) {
		
		mIngest.ingest(true, files);
		mBins.sort(Comparator.comparing(Bin::getBinNum));
		SwingUtilities.invokeLater(() -> mMainFrame.ingest(this));
		
	}
	
	public void count() {
		SwingUtilities.invokeLater(() -> mMainFrame.count(this));
	}
	
	public void resume() {
		SwingUtilities.invokeLater(() -> mMainFrame.resume(this));
	}
	
	public void done(File fileOut) {
		ExcelExporter.exportCycleCount(mBins, fileOut);
		if (mBins != null && !mBins.isEmpty()) {
			FileSystemUtils.clearProcessFolder();
		}
		mBins = new ArrayList<>();
		SwingUtilities.invokeLater(() -> mMainFrame.mainMenu(this));
	}
	
	public IUnprotectedOptions getUnprotectedOptions() {
		return mOptions;
	}
	
	public void setBins(ArrayList<Bin> bins) {
		mBins = bins;
	}
	
	public ArrayList<Bin> getBins() {
		return mBins;
	}
	
	public void setCurrentBin(int bin) {
		if (bin >= mBins.size() || bin < 0) {
			bin = 0;
		}
		if (mBins.isEmpty()) {
			bin = -1;
		}
		mCurrentBin = bin;
	}
	
	public int getCurrentBin() {
		return mCurrentBin;
	}

}
