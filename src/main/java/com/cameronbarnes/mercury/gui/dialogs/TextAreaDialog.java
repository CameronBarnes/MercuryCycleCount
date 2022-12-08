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

package com.cameronbarnes.mercury.gui.dialogs;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Gets input from the user with a JTextArea in a dialog box
 */
public final class TextAreaDialog extends JDialog {
	
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTextArea mTextArea;
	private String mResult;
	
	// This is private because the static function does the same job but better
	private TextAreaDialog(JFrame frame, String title, boolean modal) {
		
		super(frame, title, modal);
		
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);
		
		buttonOK.addActionListener(e -> onOK());
		buttonCancel.addActionListener(e -> onCancel());
		
		// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		
		// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}
	
	/**
	 * Runs when the ok button is pressed
	 */
	private void onOK() {
		mResult = mTextArea.getText();
		dispose();
	}
	
	/**
	 * Runs when either the cancel button is pressed, the window is exited out of, or when the escape button is pressed
	 */
	private void onCancel() {
		dispose();
	}
	
	/**
	 * Sets the text in the JTextArea
	 * @param text text to go in the JTextArea
	 */
	public void setText(String text) {
		mTextArea.setText(text);
		mResult = text;
	}
	
	/**
	 * @param font the new font for the JTextArea
	 */
	public void setAreaFont(Font font) {
		mTextArea.setFont(font);
		mTextArea.repaint();
	}
	
	/**
	 * @param text the new button text for the ok button
	 */
	public void setOkButtonText(String text) {
		buttonOK.setText(text);
	}
	
	/**
	 * Generates a new TextAreaDialog
	 * @param frame the source frame
	 * @param title The title of the new dialog window
	 * @param modal if this dialog should capture input, that is to say if it should prevent input on the rest of the UI while it is open
	 * @return a new TextAreaDialog
	 */
	public static TextAreaDialog createDialog(JFrame frame, String title, boolean modal) {
		
		TextAreaDialog dialog = new TextAreaDialog(frame, title, modal);
		dialog.setLocationRelativeTo(frame);
		dialog.pack();
		dialog.setSize(300, 200);
		
		return dialog;
		
	}
	
	/**
	 * Makes the dialog visible and returns the result value
	 * If the dialog is not modal this will return immediately, which is not ideal, so in that case the separate getResult() function should be called after this
	 * @return The stored result value
	 */
	public String showAndGetResult() {
		this.setVisible(true);
		return mResult;
	}
	
	public String getResult() {
		return mResult;
	}
	
	{
		// GUI initializer generated by IntelliJ IDEA GUI Designer
		// >>> IMPORTANT!! <<<
		// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}
	
	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		
		contentPane = new JPanel();
		contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
		panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		buttonOK = new JButton();
		buttonOK.setText("OK");
		panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		buttonCancel = new JButton();
		buttonCancel.setText("Cancel");
		panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JScrollPane scrollPane1 = new JScrollPane();
		panel3.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		mTextArea = new JTextArea();
		scrollPane1.setViewportView(mTextArea);
	}
	
	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		
		return contentPane;
	}
	
}
