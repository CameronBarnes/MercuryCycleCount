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
	
	private final Session mSession;
	
	public static final String PN_PATTERN = "[\\d|\\w]+\\.[\\d|\\w]+\\.[\\d|\\w]+";
	public static final String BIN_NO_PATTERN = "[A-Z]{4}-\\d{4}";
	
	private CycleCountTableModel mCycleCountTableModel;
	
	private boolean mScanEntered = false;
	
	private TextAreaCellEditor mTextAreaCellEditor;
	
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
		
		mNumBins.setText("Number of bins: " + mSession.getBins().size());
		
		mDone.addActionListener(e -> {
			if (mSession.getBins().stream().allMatch(Bin::isDone)) {
				FileSystemUtils.getFileForSaveOutput().ifPresent(mSession::done);
			}
			else {
				JOptionPane.showMessageDialog(mPanel, "All Part Physical counts must match Counted Quantity + Adjustment");
			}
		});
		
		mBinList.addListSelectionListener(e -> updatePartsTable(mBinList.getSelectedIndex()));
		
		mScanTextField.addActionListener(e -> {
			String input = mScanTextField.getText();
			if (input.matches(PN_PATTERN)) { // Checking to see if we think the entered value is a part number
				int pos = mCycleCountTableModel.getIndexOfPartNumber(input);
				if (pos >= 0) { // previous value returns -1 if the part is not found
					int col = mCycleCountTableModel.getColumnIndexAtProperty(Part.PartProperty.COUNTED_QUANTITY);
					if (col >= 0) {
						int currentValue = (int) mCycleCountTableModel.getValueAt(pos, col);
						if (currentValue != 0) { // We're going to check to make sure we're not overwriting a value we want, as there may be more than one box holding all the parts, so we'll allow the user to add to the current value if they want
							// We'll allow the user to overwrite the value if they want, we're just going to ask first
							// We're going to use a custom option dialog here to make the options clearer
							String prompt = "You've already entered a value for PN: " + input + "\nYes to add new input to that value or no to replace it.";
							String[] choices = new String[]{"Add", "Replace"};
							if (JOptionPane.showOptionDialog(mPanel, prompt, "Already Entered", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, choices, choices[0]) == 0) {
								int add = 0;
								boolean good = false;
								while (!good) { // This is here to ensure the entered value is an int, and deal with NumberFormatException(s) from strings being entered instead
									try {
										add += Integer.parseInt(JOptionPane.showInputDialog("Enter the number of parts to add to PN: " + input));
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
							else {
								mCycleCountTableModel.setValueAt(0, pos, col);
								mPartsTable.changeSelection(pos, col, false, false);
								mPartsTable.editCellAt(pos, col);
								mPartsTable.grabFocus();
								mScanEntered = true;
							}
						}
						else {
							mPartsTable.changeSelection(pos, col, false, false);
							mPartsTable.editCellAt(pos, col);
							mPartsTable.grabFocus();
							mScanEntered = true;
						}
					}
				}
			}
			else if (input.matches(BIN_NO_PATTERN)) { // We think the string entered is a bin number
				for (int i = 0; i < mSession.getBins().size(); i++) {
					if (mSession.getBins().get(i).getBinNum().equalsIgnoreCase(input)) {
						mSession.setCurrentBin(i);
						mBinList.setSelectedIndex(i);
					}
				}
			}
			mScanTextField.setText("");
		});
		
		mScrollPane1.addPropertyChangeListener(evt -> SwingUtilities.invokeLater(() -> mBinList.repaint()));
		
		mPartsTable.setDefaultEditor(Integer.class, new EnhancedCellEditor(mSession, this));
		mTextAreaCellEditor = new TextAreaCellEditor(frame, "Comments Editor");
		mPartsTable.setDefaultEditor(String.class, mTextAreaCellEditor);
		
		updateProgressBar();
		
	}
	
	public void submitBINorPartNoToField(String text) {
	
		if (text.matches(PN_PATTERN) || text.matches(BIN_NO_PATTERN)) {
			SwingUtilities.invokeLater(() -> {
				mScanTextField.setText(text);
				mScanTextField.postActionEvent();
			});
		}
	
	}
	
	public void changePartsTableLayout() {
		mCycleCountTableModel.fireTableStructureChanged();
	}
	
	public void updateProgressBar() {
		
		if (mSession.getUnprotectedOptions().shouldShowAllPartsProgress()) {
			
			int totalParts = mSession.getBins().stream().mapToInt(bin -> bin.getParts().size()).sum();
			int completedParts = totalParts - mSession.getBins().stream().mapToInt(bin -> (int) bin.getParts().stream().filter(Part::needsAdjustment).count()).sum();
			
			mProgressBar.setMaximum(totalParts);
			mProgressBar.setValue(completedParts);
			
		}
		else {
			
			int parts = mSession.getBins().get(mSession.getCurrentBin()).getParts().size();
			int completedParts = (int) (parts - mSession.getBins().get(mSession.getCurrentBin()).getParts().stream().filter(Part::needsAdjustment).count());
			
			mProgressBar.setMaximum(parts);
			mProgressBar.setValue(completedParts);
			
		}
		
	}
	
	public void forceUpdateAdjustment() {
		
		updateAdjustment();
		mCycleCountTableModel.fireTableDataChanged();
		
	}
	
	private void updateAdjustment() {
		
		mSession.getBins().get(mSession.getCurrentBin()).getParts().forEach(part -> part.autoAdjustment(mSession.getUnprotectedOptions()));
	}
	
	public void updateFont() {
		
		mBinList.setFont(mSession.getUnprotectedOptions().getFont());
		mPartsTable.setFont(mSession.getUnprotectedOptions().getFont());
		mTextAreaCellEditor.setFont(mSession.getUnprotectedOptions().getFont());
		
	}
	
	private void updatePartsTable(int bin) {
		
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
		
		if (bin >= mSession.getBins().size())
			bin = 0;
		if (mSession.getBins().isEmpty()) {
			mCycleCountTableModel = new CycleCountTableModel(new ArrayList<>(), mSession.getUnprotectedOptions(), runnable);
			mPartsTable.setModel(mCycleCountTableModel);
			mCurrentBinText.setText("None");
			mSession.setCurrentBin(-1);
		}
		else {
			mCycleCountTableModel = new CycleCountTableModel(mSession.getBins().get(bin).getParts(), mSession.getUnprotectedOptions(), runnable);
			mPartsTable.setModel(mCycleCountTableModel);
			mCurrentBinText.setText(mSession.getBins().get(bin).getBinNum());
			mSession.setCurrentBin(bin);
		}
		
	}
	
	private void createUIComponents() {
		
		mPartsTable = new CountFormTable(mSession);
		mPartsTable.setFont(mSession.getUnprotectedOptions().getFont());
		
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
		mNumBins.setText("");
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
		final JLabel label1 = new JLabel();
		Font label1Font = this.$$$getFont$$$(null, -1, 14, label1.getFont());
		if (label1Font != null) label1.setFont(label1Font);
		label1.setText("Current Selected Bin: ");
		panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
		mRightPanel.add(mProgressBar, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mScrollPane1 = new JScrollPane();
		mRightPanel.add(mScrollPane1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		mScrollPane1.setViewportView(mPartsTable);
		final JLabel label2 = new JLabel();
		Font label2Font = this.$$$getFont$$$(null, -1, 14, label2.getFont());
		if (label2Font != null) label2.setFont(label2Font);
		label2.setText("Scan Data Here:");
		mRightPanel.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
