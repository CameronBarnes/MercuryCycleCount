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

import com.cameronbarnes.mercury.core.Options;
import com.cameronbarnes.mercury.core.Session;
import com.cameronbarnes.mercury.gui.tables.models.IngestBinTableModel;
import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.util.FileSystemUtils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class IngestForm {
	
	public JPanel mPanel;
	private JButton mClearButton;
	private JButton mNextButton;
	private JTable mIngestBinTable;
	private JButton mAddEmptyBin;
	private JButton mImportFromFolder;
	
	private final Session mSession;
	
	public IngestForm(Session session) {
		
		mSession = session;
		$$$setupUI$$$();
		ResourceBundle bundle = mSession.getUnprotectedOptions().getBundle();
		
		mIngestBinTable.setFont(session.getUnprotectedOptions().getFont());
		mIngestBinTable.setModel(new IngestBinTableModel(mSession.getBins()));
		
		mNextButton.addActionListener(e -> mSession.count());
		
		mClearButton.addActionListener(e -> {
			mSession.setBins(new ArrayList<>());
			mIngestBinTable.setModel(new IngestBinTableModel(mSession.getBins()));
			FileSystemUtils.moveAllFromProcessToIngest();
		});
		
		mImportFromFolder.addActionListener(e -> mSession.addIngest(FileSystemUtils.getDirectoryWithFileChooser().orElse(Options.IMPORT_FOLDER)));
		mImportFromFolder.setText(bundle.getString("word_select_folder"));
		
		new DropTarget(mPanel, new DropTargetListener() {
			@Override
			public void dragEnter(DropTargetDragEvent e) {
			
			}
			
			@Override
			public void dragOver(DropTargetDragEvent e) {
			
			}
			
			@Override
			public void dropActionChanged(DropTargetDragEvent e) {
			
			}
			
			@Override
			public void dragExit(DropTargetEvent dte) {
			
			}
			
			@Override
			public void drop(DropTargetDropEvent event) {
				
				event.acceptDrop(DnDConstants.ACTION_COPY);
				
				Transferable transferable = event.getTransferable();
				
				DataFlavor[] flavors = transferable.getTransferDataFlavors();
				
				for (DataFlavor flavor : flavors) {
					
					try {
						
						if (flavor.isFlavorJavaFileListType()) {
							//TODO prompt if we want to move these files or copy them
							//noinspection unchecked
							mSession.addIngest((List<File>) transferable.getTransferData(flavor));
							
						}
						
					}
					catch (IOException | UnsupportedFlavorException e) {
						throw new RuntimeException(e);
					}
					
				}
				
			}
		});
		
		// Some bins have no parts, we cant get stockstatus files for those bins, but they still need to be on the final count report spreadsheet, so we add them here
		mAddEmptyBin.addActionListener(e -> {
			boolean validBin = false;
			while (!validBin) {
				
				String tmp = JOptionPane.showInputDialog(bundle.getString("ingest_name_empty_bin") + ".");
				if (tmp == null)
					return; // User has canceled out of the input window
				final String input = tmp.replace(" ", "-").toUpperCase();
				if (input.isEmpty())
					return;
				if (input.matches(CountForm.BIN_NO_PATTERN)) {
					// We need to check to make sure this bin isn't already in the list, if it is, alert the user and prompt again
					if (mSession.getBins().stream().anyMatch(bin -> bin.getBinNum().equalsIgnoreCase(input))) {
						// Alert the user that the bin they've entered is already in the system
						JOptionPane.showMessageDialog(mPanel, bundle.getString("ingest_bin_already_present_message") + ".", bundle.getString("ingest_bin_already_present_title"), JOptionPane.WARNING_MESSAGE);
						continue;
					}
					validBin = true;
					String wareHouse;
					if (!mSession.getBins().isEmpty()) { // If there's already a bin loaded in we'll use the warehouse value from that
						wareHouse = mSession.getBins().get(0).getWarehouse();
					}
					else {
						wareHouse = "301-Good Parts"; // TODO Allow user input for this value later
					}
					Bin newBin = new Bin(input, wareHouse, new ArrayList<>(), null);
					((IngestBinTableModel) mIngestBinTable.getModel()).addBin(newBin);
					((IngestBinTableModel) mIngestBinTable.getModel()).fireTableDataChanged();
				}
				
			}
		});
		
		mIngestBinTable.getTableHeader().setFont(mIngestBinTable.getTableHeader().getFont().deriveFont(Font.BOLD, 14));
		
	}
	
	public void updateFont() {
		
		mIngestBinTable.setFont(mSession.getUnprotectedOptions().getFont());
	}
	
	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		
		mPanel = new JPanel();
		mPanel.setLayout(new GridLayoutManager(5, 5, new Insets(0, 0, 0, 0), -1, -1));
		mPanel.setVisible(true);
		final Spacer spacer1 = new Spacer();
		mPanel.add(spacer1, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JLabel label1 = new JLabel();
		Font label1Font = this.$$$getFont$$$(null, -1, 24, label1.getFont());
		if (label1Font != null) label1.setFont(label1Font);
		this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("labels", "ingest_load_bin_excel_files"));
		mPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JSeparator separator1 = new JSeparator();
		mPanel.add(separator1, new GridConstraints(2, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		mPanel.add(spacer2, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		Font label2Font = this.$$$getFont$$$(null, -1, 14, label2.getFont());
		if (label2Font != null) label2.setFont(label2Font);
		this.$$$loadLabelText$$$(label2, this.$$$getMessageFromBundle$$$("labels", "ingest_instructions"));
		mPanel.add(label2, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mClearButton = new JButton();
		this.$$$loadButtonText$$$(mClearButton, this.$$$getMessageFromBundle$$$("labels", "word_remove_all"));
		mPanel.add(mClearButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mNextButton = new JButton();
		Font mNextButtonFont = this.$$$getFont$$$(null, Font.BOLD, -1, mNextButton.getFont());
		if (mNextButtonFont != null) mNextButton.setFont(mNextButtonFont);
		this.$$$loadButtonText$$$(mNextButton, this.$$$getMessageFromBundle$$$("labels", "word_caps_next"));
		mPanel.add(mNextButton, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer3 = new Spacer();
		mPanel.add(spacer3, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JScrollPane scrollPane1 = new JScrollPane();
		mPanel.add(scrollPane1, new GridConstraints(3, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		mIngestBinTable = new JTable();
		scrollPane1.setViewportView(mIngestBinTable);
		mAddEmptyBin = new JButton();
		this.$$$loadButtonText$$$(mAddEmptyBin, this.$$$getMessageFromBundle$$$("labels", "ingest_add_empty_bin"));
		mPanel.add(mAddEmptyBin, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		mImportFromFolder = new JButton();
		Font mImportFromFolderFont = this.$$$getFont$$$(null, Font.BOLD, -1, mImportFromFolder.getFont());
		if (mImportFromFolderFont != null) mImportFromFolder.setFont(mImportFromFolderFont);
		mImportFromFolder.setText("Select FolderX");
		mPanel.add(mImportFromFolder, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
	
	private static Method $$$cachedGetBundleMethod$$$ = null;
	
	private String $$$getMessageFromBundle$$$(String path, String key) {
		
		ResourceBundle bundle;
		try {
			Class<?> thisClass = this.getClass();
			if ($$$cachedGetBundleMethod$$$ == null) {
				Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
				$$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
			}
			bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
		}
		catch (Exception e) {
			bundle = ResourceBundle.getBundle(path);
		}
		return bundle.getString(key);
	}
	
	/**
	 * @noinspection ALL
	 */
	private void $$$loadLabelText$$$(JLabel component, String text) {
		
		StringBuffer result = new StringBuffer();
		boolean haveMnemonic = false;
		char mnemonic = '\0';
		int mnemonicIndex = -1;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '&') {
				i++;
				if (i == text.length()) break;
				if (!haveMnemonic && text.charAt(i) != '&') {
					haveMnemonic = true;
					mnemonic = text.charAt(i);
					mnemonicIndex = result.length();
				}
			}
			result.append(text.charAt(i));
		}
		component.setText(result.toString());
		if (haveMnemonic) {
			component.setDisplayedMnemonic(mnemonic);
			component.setDisplayedMnemonicIndex(mnemonicIndex);
		}
	}
	
	/**
	 * @noinspection ALL
	 */
	private void $$$loadButtonText$$$(AbstractButton component, String text) {
		
		StringBuffer result = new StringBuffer();
		boolean haveMnemonic = false;
		char mnemonic = '\0';
		int mnemonicIndex = -1;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '&') {
				i++;
				if (i == text.length()) break;
				if (!haveMnemonic && text.charAt(i) != '&') {
					haveMnemonic = true;
					mnemonic = text.charAt(i);
					mnemonicIndex = result.length();
				}
			}
			result.append(text.charAt(i));
		}
		component.setText(result.toString());
		if (haveMnemonic) {
			component.setMnemonic(mnemonic);
			component.setDisplayedMnemonicIndex(mnemonicIndex);
		}
	}
	
	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		
		return mPanel;
	}
	
}
