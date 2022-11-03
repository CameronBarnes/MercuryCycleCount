package com.cameronbarnes.mercury.gui.tables.celleditors;

import com.cameronbarnes.mercury.core.Options;
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
