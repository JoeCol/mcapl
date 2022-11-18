package responsibility.BasicResponsibility;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import java.awt.Component;
import javax.swing.Box;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class SettingsDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JSpinner spnWidth = new JSpinner();
	private JSpinner spnHeight = new JSpinner();
	private JTextField txtFileLocation = new JTextField();
	private JSpinner spnDirt = new JSpinner();
	public boolean updateSettings = false;
	public Settings updatedSettings;

	/**
	 * Create the dialog.
	 * @param settings 
	 */
	public SettingsDialog(Settings settings) {
		updatedSettings = settings;
		setResizable(false);
		setModal(true);
		setTitle("Settings");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setVgap(0);
			flowLayout.setHgap(0);
			flowLayout.setAlignment(FlowLayout.LEFT);
			contentPanel.add(panel);
			{
				JLabel lblWidthOfWorld = new JLabel("Width of World");
				panel.add(lblWidthOfWorld);
			}
			{
				panel.add(spnWidth);
				spnWidth.setValue(settings.getWidthOfMap());
			}
		}
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setVgap(0);
			flowLayout.setHgap(0);
			flowLayout.setAlignment(FlowLayout.LEFT);
			contentPanel.add(panel);
			{
				JLabel lblHeightOfWorld = new JLabel("Height of World");
				panel.add(lblHeightOfWorld);
			}
			{
				
				panel.add(spnHeight);
				spnHeight.setValue(settings.getHeightOfMap());
			}
		}
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setHgap(0);
			flowLayout.setVgap(0);
			flowLayout.setAlignment(FlowLayout.LEFT);
			contentPanel.add(panel);
			{
				JLabel lblWorldFileLocation = new JLabel("World File Location");
				panel.add(lblWorldFileLocation);
			}
			{
				
				panel.add(txtFileLocation);
				txtFileLocation.setColumns(25);
				txtFileLocation.setText(settings.getWorldFileLocation());
			}
			{
				JButton btnNewButton = new JButton("Choose");
				btnNewButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						fc.setDialogTitle("Choose World File");
						fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
						fc.setApproveButtonText("Choose");
						
						if (fc.showOpenDialog(btnNewButton) == JFileChooser.APPROVE_OPTION)
						{
							txtFileLocation.setText(fc.getSelectedFile().getPath());
							try {
								BufferedReader is = new BufferedReader(new FileReader(fc.getSelectedFile()));
								int height = 0;
								int width = 0;
								String line = is.readLine();
								while (line != null)
								{
									height++;
									width = Math.max(width, line.length());
									line = is.readLine();
								}
								spnHeight.setValue(height);
								spnWidth.setValue(width);
								settings.setHeightOfMap(height);
								settings.setWidthOfMap(width);
								settings.setWorldFileLocation(txtFileLocation.getText());
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				});
				panel.add(btnNewButton);
			}
		}
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setVgap(0);
			flowLayout.setHgap(0);
			flowLayout.setAlignment(FlowLayout.LEFT);
			contentPanel.add(panel);
			{
				JLabel lblDirtApperanceChange = new JLabel("Dirt Apperance Chance");
				panel.add(lblDirtApperanceChange);
			}
			{
				panel.add(spnDirt);
				spnDirt.setValue(settings.getDirtAppearanceChange());
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						updateSettings = true;
						settings.setHeightOfMap((int)spnHeight.getValue());
						settings.setWidthOfMap((int)spnWidth.getValue());
						settings.setDirtAppearanceChange((int)spnDirt.getValue());
						settings.setWorldFileLocation(txtFileLocation.getText());
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
