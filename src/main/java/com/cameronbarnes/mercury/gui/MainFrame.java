package com.cameronbarnes.mercury.gui;

import com.cameronbarnes.mercury.core.Options;
import com.cameronbarnes.mercury.core.Session;
import com.cameronbarnes.mercury.gui.forms.CountForm;
import com.cameronbarnes.mercury.gui.forms.IngestForm;
import com.cameronbarnes.mercury.gui.forms.MainMenu;
import com.cameronbarnes.mercury.gui.forms.ResumeForm;

import javax.swing.*;

public class MainFrame extends JFrame {
	
	private CountForm mCountForm;
	private IngestForm mIngestForm;
	public MainFrame(Options options) {
		
		this.setSize(1200, 900);
		this.setLocationRelativeTo(null);
		this.setEnabled(true);
		this.setVisible(true);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		
		JMenu optionsMenu = new JMenu();
		optionsMenu.setText("Options");
		
		// By default we dont want people editing the physical part quantity, but we'll leave the option here just in case
		JCheckBoxMenuItem editPhysicaCount = new JCheckBoxMenuItem("Allow Edit Physical Count");
		editPhysicaCount.addActionListener(e -> {
			if (editPhysicaCount.isSelected()) {
				if (JOptionPane.showConfirmDialog(this.rootPane, "Allow part physical count numbers to be edited?", "Allow Edit Physical Count", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					editPhysicaCount.setSelected(false);
				} else {
					options.setAllowedWritePhysicalQuantity(true);
				}
			} else {
				options.setAllowedWritePhysicalQuantity(false);
			}
		});
		editPhysicaCount.setSelected(options.isAllowedWritePhysicalQuantity());
		
		optionsMenu.add(editPhysicaCount);
		
		JCheckBoxMenuItem showAllPartsProgress = new JCheckBoxMenuItem("Include All Parts in Progress Bar");
		showAllPartsProgress.addActionListener(e -> {
			options.setShowAllPartsProgress(showAllPartsProgress.isSelected());
			if (mCountForm != null) {
				mCountForm.updateProgressBar();
			}
		});
		showAllPartsProgress.setSelected(options.shouldShowAllPartsProgress());
		
		optionsMenu.add(showAllPartsProgress);
		
		JCheckBoxMenuItem allowAutoAdjustment = new JCheckBoxMenuItem("Allow Automatic Adjustment");
		allowAutoAdjustment.addActionListener(e -> {
			options.setAllowedAutoAdjustment(allowAutoAdjustment.isSelected());
			mCountForm.forceUpdateAdjustment();
		});
		allowAutoAdjustment.setSelected(options.isAllowedAutoAdjustment());
		
		optionsMenu.add(allowAutoAdjustment);
		
		// We'll add check boxes for part details bellow, I think in most cases we wont want to display these, but I'll leave the option here
		JMenu partDetails = new JMenu();
		partDetails.setText("Part Details");
		
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
		
		optionsMenu.add(partDetails);
		
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
		
		optionsMenu.add(new JLabel("Font Size"));
		optionsMenu.add(fontSize);
		
		menuBar.add(optionsMenu);
		
		JMenu help = new JMenu();
		help.setText("Help");
		
		menuBar.add(help);
		
		JMenu about = new JMenu();
		about.setText("About");
		
		menuBar.add(about);
		
		this.setJMenuBar(menuBar);
		
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
	
}
