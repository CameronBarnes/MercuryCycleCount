package com.cameronbarnes.mercury.gui.tables;

import com.cameronbarnes.mercury.core.Session;
import com.cameronbarnes.mercury.gui.tables.cellrenderers.HighlightCellRenderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public class CountFormTable extends JTable {
	
	private final HighlightCellRenderer mHighlightCellRenderer;
	private final Session mSession;
	
	public CountFormTable(Session session) {
		mSession = session;
		mHighlightCellRenderer = new HighlightCellRenderer(mSession);
		this.getTableHeader().setFont(this.getTableHeader().getFont().deriveFont(Font.BOLD, 14));
	}
	
	@Override
	public boolean editCellAt(int row, int coll, EventObject e) {
		boolean result = super.editCellAt(row, coll, e);
		final Component editor = getEditorComponent();
		if (!(editor instanceof JTextField)) {
			return result;
		}
		if (e instanceof MouseEvent) {
			EventQueue.invokeLater(() -> {
				editor.setFont(mSession.getUnprotectedOptions().getFont());
				((JTextField) editor).selectAll();
			});
		}
		else {
			editor.setFont(mSession.getUnprotectedOptions().getFont());
			((JTextField) editor).selectAll();
		}
		return result;
		
	}
	
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		if (mHighlightCellRenderer != null)
			mHighlightCellRenderer.setFont(font);
	}
	
	@Override
	public TableCellRenderer getCellRenderer(int row, int col) {
		return mHighlightCellRenderer;
	}
	
	@Override
	public int getRowHeight() {
		return this.getGraphics().getFontMetrics(mSession.getUnprotectedOptions().getFont()).getHeight() + 2;
	}
	
	@Override
	public String getToolTipText(MouseEvent e) {
	
		int rowIndex = rowAtPoint(e.getPoint());
		int colIndex = columnAtPoint(e.getPoint());
		
		try {
			Object result = getValueAt(rowIndex, colIndex);
			if (result.getClass() == String.class)
				return (String) result;
			else
				return String.valueOf(result);
		} catch (RuntimeException ignored) {}  // This means it's not over the part of the table with data in it
		
		return null;
	
	}
	
}
