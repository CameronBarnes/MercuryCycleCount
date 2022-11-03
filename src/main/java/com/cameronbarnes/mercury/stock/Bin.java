package com.cameronbarnes.mercury.stock;

import com.cameronbarnes.mercury.gui.forms.CountForm;

import java.util.List;

public class Bin {

	private final String mBinNum;
	private final String mWarehouse;
	private final List<Part> mParts;
	
	public Bin(String binNum, String warehouse, List<Part> parts) {
		mParts = parts;
		mBinNum = binNum;
		mWarehouse = warehouse;
		if (!mBinNum.matches(CountForm.BIN_NO_PATTERN))
			throw new RuntimeException("Invalid Bin Number: " + mBinNum); // TODO handle this with the HomeAPI
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
