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

package com.cameronbarnes.mercury.gui.tables.models;

import com.cameronbarnes.mercury.core.options.IUnprotectedOptions;
import com.cameronbarnes.mercury.core.options.IUnprotectedPartPropertyOptions;
import com.cameronbarnes.mercury.stock.Part;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class CycleCountTableModel extends AbstractTableModel {
	
	private final List<Part> mParts;
	private final IUnprotectedOptions mOptions;
	private final Runnable mUpdateRunnable;
	private final ArrayList<Part.PartProperty> mPartPropertyList = new ArrayList<>();
	
	public CycleCountTableModel(List<Part> parts, IUnprotectedOptions options, Runnable update) {
		super();
		mParts = parts;
		mOptions = options;
		mUpdateRunnable = update;
		updatePartPropertyList();
	}
	
	public void updatePartPropertyList() {
		
		IUnprotectedPartPropertyOptions properties = mOptions.getUnprotectedPartPropertyOptions();
		mPartPropertyList.clear();
		
		mPartPropertyList.add(Part.PartProperty.PART_NUMBER);
		mPartPropertyList.add(Part.PartProperty.PART_DESCRIPTION);
		if (properties.warehouse())
			mPartPropertyList.add(Part.PartProperty.WAREHOUSE);
		if (properties.bin())
			mPartPropertyList.add(Part.PartProperty.BIN);
		if (properties.allocatedQty())
			mPartPropertyList.add(Part.PartProperty.ALLOCATED_QUANTITY);
		if (properties.freeQty())
			mPartPropertyList.add(Part.PartProperty.FREE_QUANTITY);
		if (properties.physicalQty())
			mPartPropertyList.add(Part.PartProperty.PHYSICAL_QUANTITY);
		mPartPropertyList.add(Part.PartProperty.COUNTED_QUANTITY);
		mPartPropertyList.add(Part.PartProperty.ADJUSTMENT);
		if (properties.cost())
			mPartPropertyList.add(Part.PartProperty.COST);
		if (properties.comments())
			mPartPropertyList.add(Part.PartProperty.COMMENTS);
		
	}
	
	@Override
	public int getRowCount() {
		return mParts.size();
	}
	
	@Override
	public int getColumnCount() {
		return mPartPropertyList.size();
	}
	
	private Part.PartProperty getPartPropertyAtColumnIndex(int index) {
		return mPartPropertyList.get(index);
	}
	
	public int getColumnIndexAtProperty(Part.PartProperty property) {
		
		return mPartPropertyList.lastIndexOf(property);
		
	}
	
	@Override
	public Class<?> getColumnClass(int index) {
		
		switch (getPartPropertyAtColumnIndex(index)) {
			case PART_NUMBER, PART_DESCRIPTION, WAREHOUSE, BIN, COMMENTS -> {
				return String.class;
			}
			case PHYSICAL_QUANTITY, ALLOCATED_QUANTITY, FREE_QUANTITY, COUNTED_QUANTITY, ADJUSTMENT -> {
				return Integer.class;
			}
			case COST -> {
				return Double.class;
			}
			default -> {
				return Object.class;
			}
		}
		
	}
	
	@Override
	public String getColumnName(int index) {
		
		switch (getPartPropertyAtColumnIndex(index)) {
			case PART_NUMBER -> {
				return "Part Number";
			}
			case PART_DESCRIPTION -> {
				return "Part Description";
			}
			case WAREHOUSE -> {
				return "WareHouse";
			}
			case BIN -> {
				return "Bin Number";
			}
			case PHYSICAL_QUANTITY -> {
				return "Physical Quantity";
			}
			case ALLOCATED_QUANTITY -> {
				return "Allocated Quantity";
			}
			case FREE_QUANTITY -> {
				return "Free Quantity";
			}
			case COUNTED_QUANTITY -> {
				return "Counted Quantity";
			}
			case COST -> {
				return "Cost";
			}
			case ADJUSTMENT -> {
				return "Adjustment";
			}
			case COMMENTS -> {
				return "Comments";
			}
			default -> {
				return "Error";
			}
		}
		
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Part part = mParts.get(rowIndex);
		
		switch (getPartPropertyAtColumnIndex(columnIndex)) {
			case PART_NUMBER -> {
				return part.getPartNumber();
			}
			case PART_DESCRIPTION -> {
				return part.getPartDescription();
			}
			case WAREHOUSE -> {
				return part.getWarehouse();
			}
			case BIN -> {
				return part.getBinNum();
			}
			case PHYSICAL_QUANTITY -> {
				return part.getPhysicalQuantity();
			}
			case ALLOCATED_QUANTITY -> {
				return part.getAllocatedQuantity();
			}
			case FREE_QUANTITY -> {
				return part.getFreeQuantity();
			}
			case COUNTED_QUANTITY -> {
				return part.getCountedQuantity();
			}
			case COST -> {
				return part.getCost();
			}
			case ADJUSTMENT -> {
				return part.getAdjustment();
			}
			case COMMENTS -> {
				return part.getComments();
			}
			default -> {
				return "Error";
			}
		}
		
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		
		return switch (getPartPropertyAtColumnIndex(columnIndex)) {
			case PHYSICAL_QUANTITY -> mOptions.isAllowedWritePhysicalQuantity();
			case COUNTED_QUANTITY, ADJUSTMENT, COMMENTS -> true;
			default -> false;
		};
		
	}
	
	@Override
	public void setValueAt(Object value,  int rowIndex, int columnIndex) {
		
		Part part = mParts.get(rowIndex);
		
		if (value == null)
			return;
		
		
		switch (getPartPropertyAtColumnIndex(columnIndex)) {
			case PHYSICAL_QUANTITY -> part.setPhysicalQuantity((Integer) value, mOptions);
			case COUNTED_QUANTITY -> part.setCountedQuantity((Integer) value);
			case ADJUSTMENT -> part.setAdjustment((Integer) value);
			case COMMENTS -> part.setComments((String) value);
		}
		
		mUpdateRunnable.run();
		this.fireTableRowsUpdated(rowIndex, rowIndex);
		
	}
	
	public int getIndexOfPartNumber(String partNumber) {
		
		for (int i = 0; i < mParts.size(); i++) {
			if (mParts.get(i).getPartNumber().equalsIgnoreCase(partNumber))
				return i;
		}
		
		return -1;
		
	}
	
}
