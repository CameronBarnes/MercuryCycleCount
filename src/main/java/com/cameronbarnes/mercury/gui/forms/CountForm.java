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

package com.cameronbarnes.mercury.gui.forms;

import com.cameronbarnes.mercury.core.Session;
import com.cameronbarnes.mercury.gui.tables.CountFormTable;
import com.cameronbarnes.mercury.gui.tables.celleditors.EnhancedCellEditor;
import com.cameronbarnes.mercury.gui.tables.celleditors.TextAreaCellEditor;
import com.cameronbarnes.mercury.gui.tables.models.CycleCountTableModel;
import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.stock.Part;
import com.cameronbarnes.mercury.util.FileSystemUtils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

public class CountForm {
	
	public JPanel mPanel;
	private JPanel mLeftPanel;
	private JList<Bin> mBinList;
	private JTable mPartsTable;
	private JPanel mRightPanel;
	private JLabel mCurrentBinText;
	private JProgressBar mProgressBar;
	private JButton mDone;
	private JTextField mScanTextField;
	private JScrollPane mScrollPane1;
	private JLabel mNumBins;
	private JLabel mScanDataHereLabel;
	private JLabel mCurrentSelectedBinLabel;
	
	private final Session mSession;
	
	public static final String PN_PATTERN = "\\w+\\.\\w+\\.\\w+";
	public static final String BIN_NO_PATTERN = "[A-Za-z]{4}-\\d{4}";
	
	private CycleCountTableModel mCycleCountTableModel;
	
	// This is to tell functions if the part cell was selected because of scanning a part number into the scan text field
	private boolean mScanEntered = false;
	
	private final TextAreaCellEditor mTextAreaCellEditor;
	
	public CountForm(Session session, JFrame frame) {
		
		mSession = session;
		
		$$$setupUI$$$();
		
		mBinList.setFont(session.getUnprotectedOptions().getFont());
		mPartsTable.setFont(session.getUnprotectedOptions().getFont());
		mPartsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		mBinList.setModel(new AbstractListModel<>() {
			@Override
			public int getSize() {
				
				return mSession.getBins().size();
			}
			
			@Override
			public Bin getElementAt(int index) {
				
				return mSession.getBins().get(index);
			}
		});
		
		// We want to select the first bin that hasn't been completed yet
		int binToSelect = 0;
		for (Bin bin : mSession.getBins()) {
			if (bin.isDone()) {
				binToSelect++;
				continue;
			}
			break;
		}
		
		updatePartsTable(binToSelect);
		
		ResourceBundle bundle = mSession.getUnprotectedOptions().getBundle();
		
		mNumBins.setText(bundle.getString("count_number_of_bins") + ": " + mSession.getBins().size());
		
		mDone.setText(bundle.getString("word_caps_done"));
		mDone.addActionListener(e -> {
			// If all bins are completed then we'll export to the output Excel file
			if (!(mSession.getBins().isEmpty() || mSession.getBins().stream().allMatch(Bin::isEmpty)) && mSession.getBins().stream().allMatch(Bin::isDone)) {
				FileSystemUtils.getFileForSaveOutput().ifPresent(mSession::done);
			}
			else if (mSession.getBins().isEmpty() || mSession.getBins().stream().allMatch(Bin::isEmpty)) { // No bins or all bins are empty, so we're just going to exit out to the main menu
				SwingUtilities.invokeLater(mSession::mainMenu);
			}
			else { // Some bins still need to be completed, so we'll let the user know and cancel the export
				JOptionPane.showMessageDialog(mPanel, bundle.getString("count_export_adjustment_required"));
			}
		});
		
		// When a different bin is selected in the bin list we need to update the parts table to show the parts for that bin
		mBinList.addListSelectionListener(e -> updatePartsTable(mBinList.getSelectedIndex()));
		
		mScanTextField.addActionListener(e -> {
			String input = mScanTextField.getText();
			if (input.matches(PN_PATTERN)) { // Checking to see if we think the entered value is a part number
				int pos = mCycleCountTableModel.getIndexOfPartNumber(input);
				if (pos >= 0) { // previous value returns -1 if the part is not found
					int col = mCycleCountTableModel.getColumnIndexAtProperty(Part.PartProperty.COUNTED_QUANTITY);
					if (col >= 0) { // previous value returns -1 if the part property is not currently being displayed, this shouldn't ever be possible with counted quantity but this check is good to have just in case
						int currentValue = (int) mCycleCountTableModel.getValueAt(pos, col);
						if (currentValue != 0) { // We're going to check to make sure we're not overwriting a value we want, as there may be more than one box holding all the parts, so we'll allow the user to add to the current value if they want
							// We'll allow the user to overwrite the value if they want, we're just going to ask first
							// We're going to use a custom option dialog here to make the options clearer
							String prompt = bundle.getString("count_pn_value_already_entered") + ": " + input;
							String[] choices = new String[]{bundle.getString("word_add"), bundle.getString("word_replace")};
							if (JOptionPane.showOptionDialog(mPanel, prompt, bundle.getString("count_already_entered_title"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, choices, choices[0]) == 0) {
								int add = 0;
								boolean good = false;
								while (!good) { // This is here to ensure the entered value is an int, and deal with NumberFormatException(s) from strings being entered instead
									try {
										add += Integer.parseInt(JOptionPane.showInputDialog(bundle.getString("count_pn_number_parts_add") + ": " + input));
										good = true;
									}
									catch (NumberFormatException ignored) {
									}
								}
								if (add == 0)
									return; // If we're not adding anything there's no reason to set the value or fire an update event
								mCycleCountTableModel.setValueAt(currentValue + add, pos, col);
								mCycleCountTableModel.fireTableCellUpdated(pos, col);
							}
							else { // User opted to replace the previous count value, so we'll do that here. // TODO consider merging this with the else bellow
								mCycleCountTableModel.setValueAt(0, pos, col);
								mPartsTable.changeSelection(pos, col, false, false);
								mPartsTable.editCellAt(pos, col);
								mPartsTable.grabFocus();
								mScanEntered = true;
							}
						}
						else { // Previous count value is 0 so we don't have to worry about overwriting anything
							mPartsTable.changeSelection(pos, col, false, false);
							mPartsTable.editCellAt(pos, col);
							mPartsTable.grabFocus();
							mScanEntered = true;
						}
					} // TODO maybe send back some error data if this if fails because it should be impossible
				}
			}
			else if (input.matches(BIN_NO_PATTERN)) { // We think the string entered is a bin number
				for (int i = 0; i < mSession.getBins().size(); i++) { // Find the index of the selected bin and set it in the session and the bin list
					if (mSession.getBins().get(i).getBinNum().equalsIgnoreCase(input)) {
						mSession.setCurrentBin(i);
						mBinList.setSelectedIndex(i);
					}
				}
			}
			mScanTextField.setText(""); // We can clear the text field now that we're done processing the input
		});
		
		mScrollPane1.addPropertyChangeListener(evt -> SwingUtilities.invokeLater(() -> mBinList.repaint()));
		
		// We set a custom cell editor for Integers so that we can allow the user to fill the cell with the full part count at a single key press if they wish
		mPartsTable.setDefaultEditor(Integer.class, new EnhancedCellEditor(mSession, this));
		// We use a custom cell editor for strings, which should only end up being used on comments as every other string cant be edited, as we want comments to be able to be more than one line
		mTextAreaCellEditor = new TextAreaCellEditor(frame, bundle.getString("count_comments_editor_title"));
		mPartsTable.setDefaultEditor(String.class, mTextAreaCellEditor);
		
		updateProgressBar();
		
	}
	
	/**
	 * This does the same thing as entering text in the mScanTextField, we use this in case text is mistakenly entered into a count cell
	 *
	 * @param text the bin or part number, or invalid text if it's just garbage input
	 */
	public void submitBINorPartNoToField(String text) {
		
		if (text.matches(PN_PATTERN) || text.matches(BIN_NO_PATTERN)) {
			SwingUtilities.invokeLater(() -> {
				mScanTextField.setText(text);
				mScanTextField.postActionEvent();
			});
		}
		
	}
	
	/**
	 * If the layout of the parts table has changed or should change the whole thing needs to be redrawn, this will let the table model know that needs to happen
	 */
	public void changePartsTableLayout() {
		
		mCycleCountTableModel.fireTableStructureChanged();
	}
	
	/**
	 * Any time the number of completed parts may have changed we'll call this to make sure the progress bar is accurate
	 */
	public void updateProgressBar() {
		
		if (mSession.getUnprotectedOptions().shouldShowAllPartsProgress()) {
			
			int totalParts = mSession.getBins().stream().mapToInt(bin -> bin.getParts().size()).sum();
			int completedParts = totalParts - mSession.getBins().stream().mapToInt(bin -> (int) bin.getParts().stream().filter(Part::needsAdjustment).count()).sum();
			
			mProgressBar.setMaximum(totalParts);
			mProgressBar.setValue(completedParts);
			
		}
		else {
			
			if (!mSession.getBins().isEmpty()) {
				int parts = mSession.getBins().get(mSession.getCurrentBin()).getParts().size();
				int completedParts = (int) (parts - mSession.getBins().get(mSession.getCurrentBin()).getParts().stream().filter(Part::needsAdjustment).count());
				
				mProgressBar.setMaximum(parts);
				mProgressBar.setValue(completedParts);
			} {
				mProgressBar.setMaximum(100);
				mProgressBar.setValue(100);
			}
			
		}
		
	}
	
	/**
	 * Forces an automatic adjustment of all parts in the current bin, if allowed by the options of course
	 * Used any time a part value is changed or if the automatic adjustment option is changed
	 */
	public void forceUpdateAdjustment() {
		
		if (updateAdjustment()) { // We don't want to force a redraw if we don't need to
			mCycleCountTableModel.fireTableDataChanged();
		}
		
	}
	
	/**
	 * Updates the adjustment value on all parts in the current bin, if allowed by settings
	 *
	 * @return true if a part was updated, false otherwise
	 */
	private boolean updateAdjustment() {
		
		return mSession.getBins().get(mSession.getCurrentBin()).getParts().stream().anyMatch(part -> part.autoAdjustment(mSession.getUnprotectedOptions()));
	}
	
	/**
	 * Updates the fonts of all the different UI elements that don't have set larger fonts already
	 * Called when the font size gets changed
	 */
	public void updateFont() {
		
		mBinList.setFont(mSession.getUnprotectedOptions().getFont());
		mPartsTable.setFont(mSession.getUnprotectedOptions().getFont());
		mTextAreaCellEditor.setFont(mSession.getUnprotectedOptions().getFont());
		
	}
	
	/**
	 * Sets the current bin to the provided value and updates the parts table to display parts from that bin
	 *
	 * @param bin the index of the bin to set as current and update the parts table with
	 */
	private void updatePartsTable(int bin) {
		
		// This handles when a value is entered in the cell editor, if it's the count value mScanEntered should be true, and we make sure to select the mScanTextField again
		Runnable runnable = () -> {
			mBinList.repaint();
			if (mScanEntered) {
				SwingUtilities.invokeLater(() -> {
					mScanTextField.selectAll();
					mScanTextField.grabFocus();
					mScanTextField.requestFocus();
					mScanTextField.requestFocusInWindow();
				});
				mScanEntered = false;
			}
			updateAdjustment();
			updateProgressBar();
		};
		
		ResourceBundle bundle = mSession.getUnprotectedOptions().getBundle();
		
		if (bin >= mSession.getBins().size())
			bin = 0;
		if (mSession.getBins().isEmpty()) { // If there are no bins then we give it an empty bin to look at
			mCycleCountTableModel = new CycleCountTableModel(new ArrayList<>(), mSession.getUnprotectedOptions(), runnable);
			mPartsTable.setModel(mCycleCountTableModel);
			mCurrentBinText.setText(bundle.getString("word_none"));
			mSession.setCurrentBin(-1);
		}
		else {
			mCycleCountTableModel = new CycleCountTableModel(mSession.getBins().get(bin).getParts(), mSession.getUnprotectedOptions(), runnable);
			mPartsTable.setModel(mCycleCountTableModel);
			mCurrentBinText.setText(mSession.getBins().get(bin).getBinNum() + "  |  " + bundle.getString("count_number_of_parts") + ": " + mSession.getBins().get(bin).getParts().size());
			mSession.setCurrentBin(bin);
		}
		
	}
	
	/**
	 * Handles creating the UI components that I've marked as custom in the form
	 */
	private void createUIComponents() {
		
		ResourceBundle bundle = mSession.getUnprotectedOptions().getBundle();
		
		mPartsTable = new CountFormTable(mSession);
		mPartsTable.setFont(mSession.getUnprotectedOptions().getFont());
		mCurrentSelectedBinLabel = new JLabel();
		mCurrentSelectedBinLabel.setText(bundle.getString("count_current_selected_bin") + ": ");
		mScanDataHereLabel = new JLabel();
		mScanDataHereLabel.setText(bundle.getString("count_scan_data_here") + ": ");
		
	}
	
	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		
		createUIComponents();
		mPanel = new JPanel();
		mPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		final JSplitPane splitPane1 = new JSplitPane();
		mPanel.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
		mLeftPanel = new JPanel();
		mLeftPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
		splitPane1.setLeftComponent(mLeftPanel);
		final Spacer spacer1 = new Spacer();
		mLeftPanel.add(spacer1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		mNumBins = new JLabel();
		Font mNumBinsFont = this.$$$getFont$$$(null, -1, 14, mNumBins.getFont());
		if (mNumBinsFont != null) mNumBins.setFont(mNumBinsFont);
		mNumBins.setText("Number of Bins: ");
		mLeftPanel.add(mNumBins, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JScrollPane scrollPane1 = new JScrollPane();
		mLeftPanel.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		mBinList = new JList();
		scrollPane1.setViewportView(mBinList);
		mRightPanel = new JPanel();
		mRightPanel.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
		splitPane1.setRightComponent(mRightPanel);
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
		mRightPanel.add(panel1, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		Font mCurrentSelectedBinLabelFont = this.$$$getFont$$$(null, -1, 14, mCurrentSelectedBinLabel.getFont());
		if (mCurrentSelectedBinLabelFont != null) mCurrentSelectedBinLabel.setFont(mCurrentSelectedBinLabelFont);
		mCurrentSelectedBinLabel.setText("Current Selected Bin: ");
		panel1.add(mCurrentSelectedBinLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		panel1.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		mCurrentBinText = new JLabel();
		Font mCurrentBinTextFont = this.$$$getFont$$$(null, -1, 14, mCurrentBinText.getFont());
		if (mCurrentBinTextFont != null) mCurrentBinText.setFont(mCurrentBinTextFont);
		mCurrentBinText.setText("None");
		panel1.add(mCurrentBinText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mDone = new JButton();
		mDone.setText("Done");
		panel1.add(mDone, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer3 = new Spacer();
		mRightPanel.add(spacer3, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final JSeparator separator1 = new JSeparator();
		mRightPanel.add(separator1, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		mProgressBar = new JProgressBar();
		mProgressBar.setStringPainted(true);
		mRightPanel.add(mProgressBar, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mScrollPane1 = new JScrollPane();
		mRightPanel.add(mScrollPane1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		mScrollPane1.setViewportView(mPartsTable);
		Font mScanDataHereLabelFont = this.$$$getFont$$$(null, -1, 14, mScanDataHereLabel.getFont());
		if (mScanDataHereLabelFont != null) mScanDataHereLabel.setFont(mScanDataHereLabelFont);
		mScanDataHereLabel.setText("Scan Data Here:");
		mRightPanel.add(mScanDataHereLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mScanTextField = new JTextField();
		mRightPanel.add(mScanTextField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
	}
	
	/**
	 * @noinspection ALL
	 */
	private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
		
		if (currentFont == null) return null;
		String resultName;
		if (fontName == null) {
			resultName = currentFont.getName();
		}
		else {
			Font testFont = new Font(fontName, Font.PLAIN, 10);
			if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
				resultName = fontName;
			}
			else {
				resultName = currentFont.getName();
			}
		}
		Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
		boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
		Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
		return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
	}
	
	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		
		return mPanel;
	}
	
}
