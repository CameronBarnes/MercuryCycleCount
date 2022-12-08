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

package com.cameronbarnes.mercury.gui.tables.celleditors;

import com.cameronbarnes.mercury.core.Session;
import com.cameronbarnes.mercury.gui.forms.CountForm;
import com.cameronbarnes.mercury.gui.tables.models.CycleCountTableModel;
import com.cameronbarnes.mercury.stock.Part;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

public class EnhancedCellEditor extends DefaultCellEditor {
	private final Session mSession;
	private final CountForm mCountForm;
	
	private Part mLastPart = null;
	
	public EnhancedCellEditor(Session session, CountForm count) {
		
		super(new JTextField());
		JTextField ftf = (JTextField) getComponent();
		ftf.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				super.focusGained(e);
				SwingUtilities.invokeLater(ftf::selectAll);
			}
		});
		
		mSession = session;
		mCountForm = count;
		
		ftf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "fill");
		ftf.getActionMap().put("fill", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mLastPart != null) {
					ftf.setText(String.valueOf(mLastPart.getPhysicalQuantity()));
					SwingUtilities.invokeLater(EnhancedCellEditor.this::stopCellEditing);
				} // Pretty sure I can just ignore this if the part is null and everything should be fine
			}
		});
		
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
		
		JTextField ftf = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, col);
		ftf.setText(value.toString());
		
		CycleCountTableModel model = (CycleCountTableModel) table.getModel();
		if (model.getColumnIndexAtProperty(Part.PartProperty.COUNTED_QUANTITY) == col) {
			mLastPart = mSession.getBins().get(mSession.getCurrentBin()).getParts().get(row);
		} else {
			mLastPart = null;
		}
		
		return ftf;
		
	}
	
	@Override
	public Object getCellEditorValue() {
		
		JTextField formattedTextField = (JTextField) getComponent();
		
		try {
			return Integer.valueOf(formattedTextField.getText());
		} catch (NumberFormatException e) {
			mCountForm.submitBINorPartNoToField(formattedTextField.getText());
			return null;
		}
		
	}
	
}
