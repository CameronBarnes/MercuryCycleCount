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

package com.cameronbarnes.mercury.gui.tables.cellrenderers;

import com.cameronbarnes.mercury.core.Session;
import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.stock.Part;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class HighlightCellRenderer extends DefaultTableCellRenderer {
	
	private final Color mHighlightGoodColour;
	private final Color mHighlightBadColour;
	private final Color mHighlightAdjustedColour;
	
	private final Color mHighlightSelectedGoodColour;
	private final Color mHighlightSelectedBadColour;
	private final Color mHighlightSelectedAdjustedColour;
	
	private final Session mSession;
	
	public HighlightCellRenderer(Session session) {
		mHighlightGoodColour = new Color(0, 255, 0, 128);
		mHighlightBadColour = new Color(255, 0, 0, 128);
		mHighlightAdjustedColour = new Color(255, 174, 0, 255);
		mHighlightSelectedGoodColour = new Color(0, 255, 0, 220);
		mHighlightSelectedBadColour = new Color(255, 64, 90, 255);
		mHighlightSelectedAdjustedColour = new Color(255, 115,0, 255);
		mSession = session;
		setFont(mSession.getUnprotectedOptions().getFont()); // Idk why we're having issues with font, but I'm just going to set this everywhere I possibly can and hope that resolves the issue
		setOpaque(true);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object object, boolean isSelected, boolean hasFocus, int row, int col) {
	
		Component component = super.getTableCellRendererComponent(new JTable(), object, isSelected, hasFocus, row, col);
		((JLabel) component).setOpaque(true);
		
		if (mSession.getCurrentBin() >= 0) {
			
			Bin bin = mSession.getBins().get(mSession.getCurrentBin());
			Part part = bin.getParts().get(row);
			
			if (part.needsAdjustment()) {
				setBackground(isSelected ? mHighlightSelectedBadColour : mHighlightBadColour);
			} else if (part.getAdjustment() != 0) {
				setBackground(isSelected ? mHighlightSelectedAdjustedColour : mHighlightAdjustedColour);
			}else {
				setBackground(isSelected ? mHighlightSelectedGoodColour : mHighlightGoodColour);
			}
			
		}
		
		component.setFont(mSession.getUnprotectedOptions().getFont());
		return component;
	
	}
	
}
