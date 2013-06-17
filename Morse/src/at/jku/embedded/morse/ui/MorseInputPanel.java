package at.jku.embedded.morse.ui;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DLtd;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;
import at.jku.embedded.morse.MorseCoder;
import at.jku.embedded.morse.audio.AudioIn;

public class MorseInputPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private final JPanel labelPanel = new JPanel();
	private final JPanel contentPanel = new JPanel();
	private final JButton recordButton = new JButton("Record");
	private final JButton stopButton = new JButton("Stop");
	private final JLabel statusLabel = new JLabel("Status: Stopped...");
	private final JLabel speedLabel = new JLabel("Recordspeed (ms)");
	private final JTextField speedTextField = new JTextField();
	private final JSplitPane splitPane = new JSplitPane();
	private final JScrollPane leftScrollPane = new JScrollPane();
	private final JScrollPane rightScrollPane = new JScrollPane();
	private final JTextArea leftTextArea = new JTextArea();
	private final JTextArea rightTextArea = new JTextArea();

	private final AudioIn audioIn = new AudioIn() {
		
		protected void notifyDitReceived(final int ditIndex,final int symbolIndex,final boolean value) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					notifyRecordDit(symbolIndex, ditIndex, value);
				}
			});
		};
		
		protected void notifySignalProcessed(final float[] points) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					notifySignal(points);
				}
			});
		};
		
	};
	
	/**
	 * Create the panel.
	 */
	public MorseInputPanel() {
		initialize();
		initListeners();
		
		speedTextField.setText(String.valueOf(audioIn.getDitLength()));
		chart.setBorder(UIManager.getBorder("ScrollPane.border"));
		chart.setUseAntialiasing(true);
		
		contentPanel.add(chart, "cell 0 1 8 1,grow");
		setPlaying(false);
	}
	
	private void initListeners() {
		speedTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateDitLength();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateDitLength();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateDitLength();
			}
		});
		
		recordButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				start();
			}
		});
		
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
			}
		});
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				load();
			}
		});
	}
	
	private File lastDir;
	
	private void load() {
		if (lastDir == null) {
			lastDir = new File("./");
		}
		JFileChooser chooser = new JFileChooser(lastDir);
		
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			if (selectedFile != null) {
				start();
				audioIn.load(selectedFile);
			}
		}
	}
	
	private void updateDitLength() {
		try {
			int value = Integer.parseInt(speedTextField.getText());
			audioIn.setDitLength(value);
		} catch (NumberFormatException e) {
		}
	}
	

	private Future<?> playing;
	private final Chart2D chart = new Chart2D();
	   
	private ITrace2D traceReal;
	private ITrace2D traceParsed;

	private void start() {
		if (playing != null) {
			return;
		}
		setPlaying(true);
		
		if (traceReal != null) {
			chart.removeTrace(traceReal);
			chart.removeTrace(traceParsed);
		}
		
		traceReal = new Trace2DLtd(100);
		traceReal.setColor(Color.BLUE);
		traceReal.setPhysicalUnits("Dit", "Value");
		
		traceParsed = new Trace2DLtd();
		traceParsed.setColor(Color.RED);
		traceParsed.setPhysicalUnits("Dit", "Value");
		
		chart.addTrace(traceReal);
		chart.addTrace(traceParsed);
		
		playing = audioIn.record();
	}  
	
	private void stop() {
		if (playing != null) {
			playing.cancel(false);
			setPlaying(false);
			
			playing = null;
		}
	}
	
	private void setPlaying(boolean playing) {
		if (!playing) {
			statusLabel.setText("Status: stopped.");
		} else {
			statusLabel.setText("Status: recording...");
		}
		
		recordButton.setEnabled(!playing);
		stopButton.setEnabled(playing);
		
		leftTextArea.setEditable(!playing);
		rightTextArea.setEditable(!playing);
		speedTextField.setEnabled(!playing);
	}
	
	private boolean prev;
	private final JButton btnReset = new JButton("Reset");
	private final JButton btnLoad = new JButton("Load");
	
	
	int index = 0;
	private void notifySignal(float[] points) {
		for (float f : points) {
			traceReal.addPoint(index++, f);
		}
	}
	
	private void notifyRecordDit(int symbolIndex, int ditIndex, boolean value) {
		statusLabel.setText("Status: recording (" + symbolIndex + ")");
		
		rightTextArea.requestFocus();
		rightTextArea.setCaretPosition(symbolIndex + 1);
		rightTextArea.setSelectionStart(symbolIndex);
		rightTextArea.setSelectionEnd(symbolIndex + 1);
		
		if (prev != value) {
			traceParsed.addPoint(ditIndex, prev ? 1 : 0);
		}
		traceParsed.addPoint(ditIndex, value ? 1 : 0);
		
		this.prev = value;
	}
	
	private void initialize() {
		setLayout(new GridLayout(0, 1, 0, 0));
		
		labelPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Morse Input", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		add(labelPanel);
		labelPanel.setLayout(new MigLayout("", "0[grow]0", "0[grow]0"));
		
		labelPanel.add(contentPanel, "cell 0 0,grow");
		contentPanel.setLayout(new MigLayout("", "[][][][][][grow][][]", "[][120px][grow]"));
		
		contentPanel.add(btnLoad, "cell 0 0");
		
		contentPanel.add(recordButton, "cell 1 0");
		contentPanel.add(stopButton, "cell 2 0");
		
		contentPanel.add(btnReset, "cell 3 0");
		contentPanel.add(statusLabel, "cell 4 0");
		
		contentPanel.add(speedLabel, "cell 6 0,alignx trailing");
		speedTextField.setHorizontalAlignment(SwingConstants.TRAILING);
		speedTextField.setColumns(10);
		speedTextField.setDocument(new LimitedDocument("0123456789"));
		
		contentPanel.add(speedTextField, "cell 7 0,growx");
		chart.setPaintLabels(false);
		splitPane.setResizeWeight(0.5);
		splitPane.setContinuousLayout(true);
		
		contentPanel.add(splitPane, "cell 0 2 8 1,grow");
		
		splitPane.setLeftComponent(leftScrollPane);
		leftTextArea.setWrapStyleWord(true);
		leftTextArea.setRows(5);
		leftTextArea.setLineWrap(true);
		leftTextArea.setDocument(new LimitedDocument(MorseCoder.getAllowedCharacters()));
		
		leftScrollPane.setViewportView(leftTextArea);
		
		splitPane.setRightComponent(rightScrollPane);
		rightTextArea.setWrapStyleWord(true);
		rightTextArea.setRows(5);
		rightTextArea.setLineWrap(true);
		rightTextArea.setDocument(new LimitedDocument("-. "));
		
		rightScrollPane.setViewportView(rightTextArea);
	}

}
