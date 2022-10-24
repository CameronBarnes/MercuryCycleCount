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
	
	Session mSession;
	
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
