package com.cameronbarnes.mercury.stock;

import com.cameronbarnes.mercury.core.IUnprotectedOptions;

public class Part {
	
	private final String mPartNumber;
	private final String mPartDescription;
	private final String mWarehouse;
	private final String mBinNum;
	private int mPhysicalQuantity;
	private int mCountedQuantity = 0;
	private int mAdjustment = 0;
	private final int mAllocatedQuantity;
	private final int mFreeQuantity;
	private final double mCost;
	
	private String mComments;
	
	public Part(String partNumber, String partDescription, String warehouse, String binNum, int physicalQuantity, int allocatedQuantity, int freeQuantity, double cost) {
		mPartDescription = partDescription;
		mPartNumber = partNumber;
		mWarehouse = warehouse;
		mBinNum = binNum;
		mPhysicalQuantity = physicalQuantity;
		mAllocatedQuantity = allocatedQuantity;
		mFreeQuantity = freeQuantity;
		mCost = cost;
	}
	
	public int getAdjustment() {
		return mAdjustment;
	}
	
	public void setAdjustment(int adjustment) {
		mAdjustment = adjustment;
	}
	
	public void setCountedQuantity(int counted) {
		mCountedQuantity = counted;
	}
	
	public String getPartNumber() {
		return mPartNumber;
	}
	
	public String getPartDescription() {
		return mPartDescription;
	}
	
	public String getWarehouse() {
		return mWarehouse;
	}
	
	public String getBinNum() {
		return mBinNum;
	}
	
	public void setPhysicalQuantity(int physicalQuantity, IUnprotectedOptions options) {
		if (options.isAllowedWritePhysicalQuantity()) {
			mPhysicalQuantity = physicalQuantity;
		}
	}
	
	public int getPhysicalQuantity() {
		return mPhysicalQuantity;
	}
	
	public int getCountedQuantity() {
		return mCountedQuantity;
	}
	
	public int getAllocatedQuantity() {
		return mAllocatedQuantity;
	}
	
	public int getFreeQuantity() {
		return mFreeQuantity;
	}
	
	public double getCost() {
		return mCost;
	}
	
	public boolean needsAdjustment() {
		return mPhysicalQuantity + mAdjustment != mCountedQuantity;
	}
	
	public boolean autoAdjustment(IUnprotectedOptions options) {
		
		if (options.isAllowedAutoAdjustment() && mCountedQuantity > 0) {
			int startAdjust = mAdjustment;
			mAdjustment = mCountedQuantity - mPhysicalQuantity;
			return mAdjustment != startAdjust;
		}
		
		return false;
		
	}
	
	public boolean hasComments() {
		return mComments != null && !mComments.isBlank();
	}
	
	public String getComments() {
		return mComments == null ? "" : mComments;
	}
	
	public void setComments(String comments) {
		mComments = comments;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if (!(o instanceof Part p))
			return false;
		
		return mPartNumber.equals(p.mPartNumber) &&
				       mPartDescription.equals(p.mPartDescription) &&
				       mWarehouse.equals(p.mWarehouse) &&
				       mBinNum.equals(p.mBinNum) &&
				       mPhysicalQuantity == p.mPhysicalQuantity &&
				       mCountedQuantity == p.mCountedQuantity &&
				       mCost == p.mCost &&
				       mAdjustment == p.mAdjustment &&
				       mAllocatedQuantity == p.mAllocatedQuantity &&
				       mFreeQuantity == p.mFreeQuantity;
		
	}
	
	public enum PartProperty {
		PART_NUMBER,
		PART_DESCRIPTION,
		WAREHOUSE,
		BIN,
		PHYSICAL_QUANTITY,
		ALLOCATED_QUANTITY,
		FREE_QUANTITY,
		COUNTED_QUANTITY,
		COST,
		ADJUSTMENT,
		COMMENTS
	}
	
}
