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

package com.cameronbarnes.mercury.stock;

import com.cameronbarnes.mercury.gui.forms.CountForm;

import java.nio.file.attribute.FileTime;
import java.util.List;

public class Bin {

	private final String mBinNum;
	private final String mWarehouse;
	private final List<Part> mParts;
	// This is so that we can compare which bin is newest if we try to add two stock status files for the same bin
	private final FileTime mFileTime;
	
	public Bin(String binNum, String warehouse, List<Part> parts, FileTime fileTime) {
		mParts = parts;
		mBinNum = binNum;
		mWarehouse = warehouse;
		mFileTime = fileTime;
		if (!mBinNum.matches(CountForm.BIN_NO_PATTERN))
			throw new RuntimeException("Invalid Bin Number: " + mBinNum); // TODO handle this with the HomeAPI
	}
	
	public FileTime getFileTime() {
		return mFileTime;
	}
	
	public String getBinNum() {
		return mBinNum;
	}
	
	public String getWarehouse() {
		return mWarehouse;
	}
	
	public List<Part> getParts() {
		return mParts;
	}
	
	public boolean isDone() {
		return mParts.stream().noneMatch(Part::needsAdjustment);
	}
	
	@Override
	public String toString() {
		return isDone() ? getBinNum() + ": Complete" : getBinNum() + "                     .";
	}
	
	public boolean isEmpty() {
		return mParts.isEmpty();
	}
	
	@Override
	public boolean equals(Object o) {
		
		if (!(o instanceof Bin b))
			return false;
		
		if (!mBinNum.equals(b.mBinNum) || !mWarehouse.equals(b.mWarehouse))
			return false;
		
		if (b.mParts.size() != mParts.size())
			return false;
		
		return mParts.stream().allMatch(part -> b.mParts.stream().anyMatch(part2 -> part2.equals(part)));
		
	}
	
}
