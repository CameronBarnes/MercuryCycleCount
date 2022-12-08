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

import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.stock.Part;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Comparator;

public class IngestBinTableModel extends AbstractTableModel {
	
	private final ArrayList<Bin> mBins;
	
	public IngestBinTableModel(ArrayList<Bin> bins) {
		super();
		mBins = bins;
	}
	
	@Override
	public int getRowCount() {
		return mBins.size();
	}
	
	@Override
	public int getColumnCount() {
		return 4;
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Bin bin = mBins.get(rowIndex);
		
		return switch (columnIndex) {
			case 0 -> bin.getBinNum();
			case 1 -> bin.getWarehouse();
			case 2 -> bin.getParts().size();
			case 3 -> bin.getParts().stream().mapToInt(Part::getPhysicalQuantity).sum();
			default -> throw new IllegalStateException("Unexpected columnIndex value: " + columnIndex);
		};
		
	}
	
	@Override
	public Class<?> getColumnClass(int index) {
		
		if (index >= 2)
			return Integer.class;
		else
			return String.class;
		
	}
	
	@Override
	public String getColumnName(int index) {
		
		return switch (index) {
			case 0 -> "Bin Number";
			case 1 -> "WareHouse";
			case 2 -> "Different Parts";
			case 3 -> "Total Parts"; // TODO I also want to show differnet numbers of parts so I'll need to edit stuff above for the additional column
			default -> throw new IllegalStateException("Unexpected value: " + index);
		};
		
	}
	
	public void addBin(Bin bin) {
		mBins.add(bin);
		mBins.sort(Comparator.comparing(Bin::getBinNum));
	}
	
}
