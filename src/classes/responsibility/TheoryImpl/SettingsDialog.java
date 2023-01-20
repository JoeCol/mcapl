package responsibility.TheoryImpl;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.awt.event.ActionEvent;

public class SettingsDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8101986934383656658L;
	private final JPanel contentPanel = new JPanel();
	private JSpinner spnWidth = new JSpinner();
	private JSpinner spnHeight = new JSpinner();
	private JSpinner spnSimSteps = new JSpinner();
	private JSpinner spnDirtInterval = new JSpinner();
	private JSpinner spnBadDirtInterval = new JSpinner();
	private JTextField txtFileLocation = new JTextField();
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
		setBounds(100, 100, 530, 350);
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
			flowLayout.setVgap(0);
			flowLayout.setHgap(0);
			flowLayout.setAlignment(FlowLayout.LEFT);
			contentPanel.add(panel);
			{
				JLabel lblSimSteps = new JLabel("Simulation Steps");
				panel.add(lblSimSteps);
			}
			{
				
				panel.add(spnSimSteps);
				spnSimSteps.setValue(settings.getSimulationSteps());
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
				JLabel lblDirtInterval = new JLabel("Dirt Interval");
				panel.add(lblDirtInterval);
			}
			{
				
				panel.add(spnDirtInterval);
				spnDirtInterval.setValue(settings.getDirtInterval());
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
				JLabel lblBadDirtInterval = new JLabel("Bad Dirt Interval");
				panel.add(lblBadDirtInterval);
			}
			{
				panel.add(spnBadDirtInterval);
				spnBadDirtInterval.setValue(settings.getBadDirtInterval());
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
						settings.setWorldFileLocation(txtFileLocation.getText());
						settings.setBadDirtInterval((int)spnBadDirtInterval.getValue());
						settings.setDirtInterval((int)spnDirtInterval.getValue());
						settings.setSimulationSteps((int)spnSimSteps.getValue());
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
