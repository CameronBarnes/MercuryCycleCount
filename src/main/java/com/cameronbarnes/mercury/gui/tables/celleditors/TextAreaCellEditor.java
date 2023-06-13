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

import com.cameronbarnes.mercury.core.options.Options;
import com.cameronbarnes.mercury.gui.dialogs.TextAreaDialog;
import com.cameronbarnes.mercury.gui.tables.models.CycleCountTableModel;
import com.cameronbarnes.mercury.stock.Part;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

public class TextAreaCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
	
	private String mResults = "";
	private final JButton mButton;
	private final TextAreaDialog mTextAreaDialog;
	protected static final String EDIT = "edit";
	
	public TextAreaCellEditor(JFrame frame, String title) {
		
		mButton = new JButton();
		mButton.setActionCommand(EDIT);
		mButton.setBorderPainted(false);
		mButton.addActionListener(this);
		
		mTextAreaDialog = TextAreaDialog.createDialog(frame, title, true);
		
	}
	
	public void setFont(Font font) {
		mTextAreaDialog.setAreaFont(font);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
			mResults = mTextAreaDialog.showAndGetResult();
			fireEditingStopped();
		}
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		
		ResourceBundle bundle = Options.getDefaultBundle();
		
		Object partNum = table.getValueAt(row, ((CycleCountTableModel) table.getModel()).getColumnIndexAtProperty(Part.PartProperty.PART_NUMBER));
		if (partNum != null) {
			mTextAreaDialog.setTitle(bundle.getString("word_part") + ": " + value + " " + bundle.getString("word_comments"));
		}
		mTextAreaDialog.setText(value == null ? "" : value.toString());
		return mButton;
		
	}
	
	@Override
	public Object getCellEditorValue() {
		return mResults;
	}
	
}
