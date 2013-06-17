package at.jku.embedded.morse.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

public class MorseApplication {

	private JFrame frmMorseApplication;
	private final MorseOutputPanel morseOutputPanel = new MorseOutputPanel();
	private final MorseInputPanel morseInputPanel = new MorseInputPanel();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					MorseApplication window = new MorseApplication();
					window.frmMorseApplication.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}); 
	}

	/**
	 * Create the application.
	 */
	public MorseApplication() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmMorseApplication = new JFrame();
		frmMorseApplication.setTitle("Morse Application");
		frmMorseApplication.setBounds(100, 100, 652, 699);
		frmMorseApplication.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmMorseApplication.getContentPane().setLayout(new MigLayout("", "[grow,left]", "[grow][grow]"));
		
		frmMorseApplication.getContentPane().add(morseOutputPanel, "cell 0 0,grow");
		
		frmMorseApplication.getContentPane().add(morseInputPanel, "cell 0 1,grow");
	}

}
