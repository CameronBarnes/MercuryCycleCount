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

package com.cameronbarnes.mercury.gui;

import com.cameronbarnes.mercury.core.Main;
import com.cameronbarnes.mercury.core.Options;
import com.cameronbarnes.mercury.core.Session;
import com.cameronbarnes.mercury.gui.dialogs.DisplayTextPaneDialog;
import com.cameronbarnes.mercury.gui.dialogs.TextAreaDialog;
import com.cameronbarnes.mercury.gui.forms.CountForm;
import com.cameronbarnes.mercury.gui.forms.IngestForm;
import com.cameronbarnes.mercury.gui.forms.MainMenu;
import com.cameronbarnes.mercury.gui.forms.ResumeForm;
import com.cameronbarnes.mercury.util.DebugUtils;
import com.cameronbarnes.mercury.util.HomeAPIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class MainFrame extends JFrame {
	
	private CountForm mCountForm;
	private IngestForm mIngestForm;
	
	JMenu optionsMenu;
	
	public MainFrame(Options options) {
		
		this.setSize(1200, 900);
		this.setLocationRelativeTo(null);
		this.setEnabled(true);
		this.setVisible(true);
		this.setTitle(options.getBundle().getString("title"));
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		this.setJMenuBar(createMenuBar(options));
		
	}
	
	private JMenuBar createMenuBar(Options options) {
		
		ResourceBundle bundle = options.getBundle();
		
		JMenuBar menuBar = new JMenuBar();
		
		optionsMenu = new JMenu();
		optionsMenu.setText(bundle.getString("word_options"));
		
		JMenu count = new JMenu();
		count.setText(bundle.getString("word_count"));
		
		// By default, we don't want people editing the physical part quantity, but we'll leave the option here just in case
		JCheckBoxMenuItem editPhysicalCount = new JCheckBoxMenuItem(bundle.getString("menu_bar_allow_edit_physical_count"));
		editPhysicalCount.addActionListener(e -> {
			if (editPhysicalCount.isSelected()) {
				if (JOptionPane.showConfirmDialog(this.rootPane, bundle.getString("menu_bar_allow_edit_physical_count_text"), bundle.getString("menu_bar_allow_edit_physical_count"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					editPhysicalCount.setSelected(false);
				} else {
					options.setAllowedWritePhysicalQuantity(true);
				}
			} else {
				options.setAllowedWritePhysicalQuantity(false);
			}
		});
		editPhysicalCount.setSelected(options.isAllowedWritePhysicalQuantity());
		
		count.add(editPhysicalCount);
		
		JCheckBoxMenuItem showAllPartsProgress = new JCheckBoxMenuItem(bundle.getString("menu_bar_progress_include_all"));
		showAllPartsProgress.addActionListener(e -> {
			options.setShowAllPartsProgress(showAllPartsProgress.isSelected());
			if (mCountForm != null) {
				mCountForm.updateProgressBar();
			}
		});
		showAllPartsProgress.setSelected(options.shouldShowAllPartsProgress());
		
		count.add(showAllPartsProgress);
		
		JCheckBoxMenuItem allowAutoAdjustment = new JCheckBoxMenuItem(bundle.getString("menu_bar_allow_automatic_adjustment"));
		allowAutoAdjustment.addActionListener(e -> {
			options.setAllowedAutoAdjustment(allowAutoAdjustment.isSelected());
			mCountForm.forceUpdateAdjustment();
		});
		allowAutoAdjustment.setSelected(options.isAllowedAutoAdjustment());
		
		count.add(allowAutoAdjustment);
		
		// We'll add check boxes for part details bellow, I think in most cases we won't want to display these, but I'll leave the option here
		JMenu partDetails = new JMenu();
		partDetails.setText(bundle.getString("word_part_details"));
		
		JCheckBoxMenuItem warehouse = new JCheckBoxMenuItem("WareHouse");
		warehouse.addActionListener(e -> {
			options.getPartDetailSettings().replace("WareHouse", warehouse.getState());
			if (mCountForm != null)
				mCountForm.changePartsTableLayout();
		});
		warehouse.setSelected(options.getPartDetailSettings().get("WareHouse"));
		JCheckBoxMenuItem bin = new JCheckBoxMenuItem("Bin");
		bin.addActionListener(e -> {
			options.getPartDetailSettings().replace("Bin", bin.getState());
			if (mCountForm != null)
				mCountForm.changePartsTableLayout();
		});
		bin.setSelected(options.getPartDetailSettings().get("Bin"));
		JCheckBoxMenuItem allocated = new JCheckBoxMenuItem("AllocatedQty");
		allocated.addActionListener(e -> {
			options.getPartDetailSettings().replace("AllocatedQuantity", allocated.getState());
			if (mCountForm != null)
				mCountForm.changePartsTableLayout();
		});
		allocated.setSelected(options.getPartDetailSettings().get("AllocatedQuantity"));
		JCheckBoxMenuItem free = new JCheckBoxMenuItem("FreeQty");
		free.addActionListener(e -> {
			options.getPartDetailSettings().replace("FreeQuantity", free.getState());
			if (mCountForm != null)
				mCountForm.changePartsTableLayout();
		});
		free.setSelected(options.getPartDetailSettings().get("FreeQuantity"));
		JCheckBoxMenuItem comments = new JCheckBoxMenuItem("Comments");
		comments.addActionListener(e -> {
			options.getPartDetailSettings().replace("Comments", comments.isSelected());
			if (mCountForm != null)
				mCountForm.changePartsTableLayout();
		});
		comments.setSelected(options.getPartDetailSettings().get("Comments"));
		
		partDetails.add(warehouse);
		partDetails.add(bin);
		partDetails.add(allocated);
		partDetails.add(free);
		partDetails.add(comments);
		
		count.add(partDetails);
		
		optionsMenu.add(count);
		
		//TODO still need to make the dialog for but reporting and write the API calls for sending that info back to my server
		JMenu feedback = new JMenu();
		feedback.setText(bundle.getString("menu_bar_feedback"));
		
		JMenuItem reportBug = new JMenuItem();
		reportBug.setText(bundle.getString("menu_bar_report_bug"));
		
		JMenuItem suggestion = new JMenuItem();
		suggestion.setText(bundle.getString("menu_bar_feature_request"));
		suggestion.addActionListener(e -> {
			TextAreaDialog dialog = TextAreaDialog.createDialog(this, bundle.getString("menu_bar_feature_request_dialog"), true);
			dialog.setOkButtonText(bundle.getString("word_caps_send"));
			HomeAPIUtils.sendFeedback(dialog.showAndGetResult());
		});
		
		feedback.add(reportBug);
		feedback.add(suggestion);
		
		JSpinner fontSize = new JSpinner(new SpinnerNumberModel(options.getFontSize(), 10, 40, 1));
		fontSize.addChangeListener(e -> {
			options.setFontSize((Integer) fontSize.getValue());
			if (mCountForm != null)
				mCountForm.updateFont();
			if (mIngestForm != null)
				mIngestForm.updateFont();
			MainFrame.this.getRootPane().revalidate();
			MainFrame.this.getRootPane().repaint();
		});
		
		JMenu font = new JMenu();
		font.setText(bundle.getString("word_font"));
		
		font.add(new JLabel(bundle.getString("word_font_size")));
		font.add(fontSize);
		
		optionsMenu.add(font);
		
		menuBar.add(optionsMenu);
		menuBar.add(feedback);
		
		JMenu help = new JMenu();
		help.setText(bundle.getString("word_help"));
		
		menuBar.add(help);
		
		JMenuItem about = new JMenuItem();
		about.setText(bundle.getString("word_about"));
		about.setHorizontalAlignment(SwingConstants.LEFT);
		about.setMaximumSize(new Dimension(60, 40));
		about.addActionListener(e -> {
			String str = String.format(bundle.getString("about_content"), Main.VERSION.toNiceString());
			DisplayTextPaneDialog dialog = DisplayTextPaneDialog.createDialog(this, bundle.getString("word_about"), true);
			dialog.displayText(str, true);
		});
		
		menuBar.add(about);
		
		return menuBar;
		
	}
	
	public void setDebugDataAction(Session session) {
		
		JMenuItem generateDebugData = new JMenuItem();
		generateDebugData.setText("New Debug Data");
		generateDebugData.addActionListener(e -> {
			session.setBins(DebugUtils.generateTestBinData(5));
			DebugUtils.generateTestStockStatusFiles(4, Options.PROCESS_FOLDER);
			SwingUtilities.invokeLater(() -> {
				MainFrame.this.revalidate();
				MainFrame.this.repaint();
			});
		});
		optionsMenu.add(generateDebugData);
		
	}
	
	public boolean isCount() {
		return mCountForm != null;
	}
	
	public void mainMenu(Session session) {
		
		MainMenu mainMenu = new MainMenu(session);
		this.setContentPane(mainMenu.mPanel);
		this.revalidate();
		this.repaint();
		
		mCountForm = null;
		mIngestForm = null;
		
	}
	
	public void ingest(Session session) {
		
		mIngestForm = new IngestForm(session);
		this.setContentPane(mIngestForm.mPanel);
		this.revalidate();
		this.repaint();
		
		mCountForm = null;
		
	}
	
	public void count(Session session) {
		
		mCountForm = new CountForm(session, this);
		this.setContentPane(mCountForm.mPanel);
		this.revalidate();
		this.repaint();
		
		mIngestForm = null;
		
	}
	
	public void resume(Session session) {
		
		ResumeForm resumeForm =  new ResumeForm(session);
		this.setContentPane(resumeForm.mPanel);
		this.revalidate();
		this.repaint();
		
		mCountForm = null;
		mIngestForm = null;
		
	}
	
	public void showChangeLog() {
	
	
	
	}
	
}
