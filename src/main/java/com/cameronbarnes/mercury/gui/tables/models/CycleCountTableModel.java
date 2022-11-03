package com.cameronbarnes.mercury.gui.tables.models;

import com.cameronbarnes.mercury.core.IUnprotectedOptions;
import com.cameronbarnes.mercury.stock.Part;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public class CycleCountTableModel extends AbstractTableModel {
	
	private final List<Part> mParts;
	private final IUnprotectedOptions mOptions;
	private final Runnable mUpdateRunnable;
	
	public CycleCountTableModel(List<Part> parts, IUnprotectedOptions options, Runnable update) {
		super();
		mParts = parts;
		mOptions = options;
		mUpdateRunnable = update;
	}
	
	@Override
	public int getRowCount() {
		return mParts.size();
	}
	
	@Override
	public int getColumnCount() {
		Map<String, Boolean> properties = mOptions.getPartDetailSettings();
		int numRows = 6; // This is how many we're definitely going to show, PN, Description, PhysicalQty, CountedQty, Cost, and Adjustment values
		if (properties.get("WareHouse"))
			numRows++;
		if (properties.get("Bin"))
			numRows++;
		if (properties.get("AllocatedQuantity"))
			numRows++;
		if (properties.get("FreeQuantity"))
			numRows++;
		if (properties.get("Comments"))
			numRows++;
		
		return numRows;
	}
	
	private Part.PartProperty getPartPropertyAtColumnIndex(int index) {
		
		Map<String, Boolean> properties = mOptions.getPartDetailSettings();
		boolean warehouse = properties.get("WareHouse");
		boolean bin = properties.get("Bin");
		boolean allocatedQty = properties.get("AllocatedQuantity");
		boolean freeQty = properties.get("FreeQuantity");
		boolean comments = properties.get("Comments");
		
		LinkedList<Part.PartProperty> out = new LinkedList<>();
		out.add(Part.PartProperty.PART_NUMBER);
		out.add(Part.PartProperty.PART_DESCRIPTION);
		if (warehouse)
			out.add(Part.PartProperty.WAREHOUSE);
		if (bin)
			out.add(Part.PartProperty.BIN);
		out.add(Part.PartProperty.PHYSICAL_QUANTITY);
		if (allocatedQty)
			out.add(Part.PartProperty.ALLOCATED_QUANTITY);
		if (freeQty)
			out.add(Part.PartProperty.FREE_QUANTITY);
		out.add(Part.PartProperty.COUNTED_QUANTITY);
		out.add(Part.PartProperty.COST);
		out.add(Part.PartProperty.ADJUSTMENT);
		if (comments)
			out.add(Part.PartProperty.COMMENTS);
		
		return out.get(index);
		
	}
	
	public int getColumnIndexAtProperty(Part.PartProperty property) {
		
		Map<String, Boolean> properties = mOptions.getPartDetailSettings();
		boolean warehouse = properties.get("WareHouse");
		boolean bin = properties.get("Bin");
		boolean allocatedQty = properties.get("AllocatedQuantity");
		boolean freeQty = properties.get("FreeQuantity");
		boolean comments = properties.get("Comments");
		
		LinkedList<Part.PartProperty> out = new LinkedList<>();
		out.add(Part.PartProperty.PART_NUMBER);
		out.add(Part.PartProperty.PART_DESCRIPTION);
		if (warehouse)
			out.add(Part.PartProperty.WAREHOUSE);
		if (bin)
			out.add(Part.PartProperty.BIN);
		out.add(Part.PartProperty.PHYSICAL_QUANTITY);
		if (allocatedQty)
			out.add(Part.PartProperty.ALLOCATED_QUANTITY);
		if (freeQty)
			out.add(Part.PartProperty.FREE_QUANTITY);
		out.add(Part.PartProperty.COUNTED_QUANTITY);
		out.add(Part.PartProperty.COST);
		out.add(Part.PartProperty.ADJUSTMENT);
		if (comments)
			out.add(Part.PartProperty.COMMENTS);
		
		for (int i = 0; i < out.size(); i++) {
			if (out.get(i) == property)
				return i;
		}
		
		return -1;
		
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
