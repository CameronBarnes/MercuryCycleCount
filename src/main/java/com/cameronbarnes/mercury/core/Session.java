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

import com.cameronbarnes.mercury.core.options.IUnprotectedOptions;
import com.cameronbarnes.mercury.core.options.Options;
import com.cameronbarnes.mercury.excel.ExcelExporter;
import com.cameronbarnes.mercury.gui.MainFrame;
import com.cameronbarnes.mercury.stock.Bin;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Session {

	private final MainFrame mMainFrame;
	private ArrayList<Bin> mBins = new ArrayList<>();
	private int mCurrentBin = -1;
	private final Options mOptions;
	private final Ingest mIngest;
	
	public Session(MainFrame frame, Options options) {
		mMainFrame = frame;
		// For debug purposes only
		if (Main.DEBUG) {
			mMainFrame.setDebugDataAction(this);
		}
		mOptions = options;
		mIngest = new Ingest(this);
	}
	
	/**
	 * Changes the state and instructs the window frame to display the main menu for the application
	 */
	public void mainMenu() {
		SwingUtilities.invokeLater(() -> mMainFrame.mainMenu(this));
	}
	
	/**
	 * Triggers the beginning of the ingest process, loading files from the ingest folder and then sorting the results
	 * Then it changes the state and instructs the window frame to display to the Ingest menu form
	 */
	public void ingest() {
		mIngest.ingest(false);
		mBins.sort(Comparator.comparing(Bin::getBinNum));
		SwingUtilities.invokeLater(() -> mMainFrame.ingest(this));
	}
	
	/**
	 * Starts or updates the Ingest process by importing files from the provided directory
	 * Changes the state and displayed menu form to be the Ingest menu if it isn't already
	 * @param dir The directory to import stockstatus.xlsx files from
	 */
	public void addIngest(File dir) {
		mIngest.ingest(true, dir);
		mBins.sort(Comparator.comparing(Bin::getBinNum));
		SwingUtilities.invokeLater(() -> mMainFrame.ingest(this));
	}
	
	/**
	 * Starts or updates the Ingest process by importing files from the provided list
	 * Changes the state and displayed menu form to be the Ingest menu if it isn't already
	 * @param files a list of files to import bins from, these files will be moved
	 */
	public void addIngest(List<File> files) {
		
		mIngest.ingest(true, files);
		mBins.sort(Comparator.comparing(Bin::getBinNum));
		SwingUtilities.invokeLater(() -> mMainFrame.ingest(this));
		
	}
	
	/**
	 * Changes the window to display the main count page
	 */
	public void count() {
		SwingUtilities.invokeLater(() -> mMainFrame.count(this));
	}
	
	/**
	 * Changes the window to display the resume progress menu
	 */
	public void resume() {
		SwingUtilities.invokeLater(() -> mMainFrame.resume(this));
	}
	
	/**
	 * Exports the bins and parts to the final output spreadsheet at the provided file then changes the window to the main menu
	 * Does nothing if the export fails, the export function should handle notifying the user about the error
	 * @param fileOut The file to write the results to, should not exist already
	 */
	public void done(File fileOut) { // TODO handle asking the user if they want to overwrite an existing file, probably handle it where this function is getting called and not actually here
		if (ExcelExporter.exportCycleCount(mBins, fileOut)) {
			mBins = new ArrayList<>();
			SwingUtilities.invokeLater(() -> mMainFrame.mainMenu(this));
		}
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
	
	/**
	 * Sets the current bin value.
	 * Session validate and keeps track of this value here as it's easily accessible by a lot of parts that need to get at it and can be easily validated without too much hassle
	 * @param bin the index of the bin to set as current
	 */
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
