/*
 *     Copyright (c) 2023.  Cameron Barnes
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

package com.cameronbarnes.mercury.core.options;

public class PartPropertyOptions implements IUnprotectedPartPropertyOptions {
	
	private boolean mWarehouse = false;
	private boolean mBin = false;
	private boolean mAllocatedQty = false;
	private boolean mFreeQty = false;
	private boolean mComments = true;
	private boolean mPhysicalQty = true;
	private boolean mCountedQty = true;
	private boolean mPartDescription = true;
	private boolean mCost = false;
	
	
	@Override
	public boolean warehouse() {
		return mWarehouse;
	}
	
	public void setWarehouse(boolean enabled) {
		mWarehouse = enabled;
	}
	
	@Override
	public boolean bin() {
		return mBin;
	}
	
	public void setBin(boolean enabled) {
		mBin = enabled;
	}
	
	@Override
	public boolean allocatedQty() {
		return  mAllocatedQty;
	}
	
	public void setAllocatedQty(boolean enabled) {
		mAllocatedQty = enabled;
	}
	
	@Override
	public boolean freeQty() {
		return mFreeQty;
	}
	
	public void setFreeQty(boolean enabled) {
		mFreeQty = enabled;
	}
	
	@Override
	public boolean comments() {
		return mComments;
	}
	
	public void setComments(boolean enabled) {
		mComments = enabled;
	}
	
	@Override
	public boolean physicalQty() {
		return mPhysicalQty;
	}
	
	public void setPhysicalQty(boolean enabled) {
		mPhysicalQty = enabled;
	}
	
	@Override
	public boolean countedQty() {
		return mCountedQty;
	}
	
	public void setCountedQty(boolean enabled) {
		mCountedQty = enabled;
	}
	
	@Override
	public boolean description() {
		return mPartDescription;
	}
	
	public void setPartDescription(boolean enabled) {
		mPartDescription = enabled;
	}
	
	@Override
	public boolean cost() {
		return mCost;
	}
	
	public void setCost(boolean enabled) {
		mCost = enabled;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if (!(o instanceof PartPropertyOptions opt))
			return false;
		
		return mWarehouse == opt.mWarehouse && mBin == opt.mBin && mAllocatedQty == opt.mAllocatedQty &&
				       mFreeQty == opt.mFreeQty && mComments == opt.mComments && mPhysicalQty == opt.mPhysicalQty &&
					   mCountedQty == opt.mCountedQty && mPartDescription == opt.mPartDescription && mCost == opt.mCost;
		
	}
	
}
