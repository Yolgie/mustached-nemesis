package at.jku.embedded.morse.ui;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DLtd;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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
import at.jku.embedded.morse.MorseCode;
import at.jku.embedded.morse.MorseCoder;
import at.jku.embedded.morse.MorseIn;
import at.jku.embedded.morse.MorseOut;
import at.jku.embedded.morse.audio.AudioOut;

public class MorseOutputPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private final JPanel labelPanel = new JPanel();
	private final JPanel contentPanel = new JPanel();
	private final JButton playButton = new JButton("Play");
	private final JButton stopButton = new JButton("Stop");
	private final JLabel statusLabel = new JLabel("Status: Playback...");
	private final JLabel speedLabel = new JLabel("Playbackspeed (ms)");
	private final JTextField speedTextField = new JTextField();
	private final JSplitPane splitPane = new JSplitPane();
	private final JScrollPane leftScrollPane = new JScrollPane();
	private final JScrollPane rightScrollPane = new JScrollPane();
	private final JTextArea leftTextArea = new JTextArea();
	private final JTextArea rightTextArea = new JTextArea();
	private final JPanel leftButtonPane = new JPanel();
	private final JButton leftLoadButton = new JButton("Load from File...");
	private final JButton leftSaveButton = new JButton("Save to File...");
	private final JPanel rightButtonPane = new JPanel();
	private final JButton rightLoadButton = new JButton("Load from File...");
	private final JButton rightSaveButton = new JButton("Save to File...");

	private boolean disabledLeftEvents;
	private boolean disabledRightEvents;
	
	private final AudioOut audioOut = new AudioOut() {
		protected void notifyPlayingIndex(final int index, final int ditIndex, final boolean value) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					playedIndex(index, ditIndex, value);
				}
			});
		}
		
		protected void notifyPlayedDone() {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					stop();
				}
			});
		};
	};
	
	/**
	 * Create the panel.
	 */
	public MorseOutputPanel() {
		initialize();
		initListeners();
		
		speedTextField.setText(String.valueOf(audioOut.getDitLength()));
		chart.setBorder(UIManager.getBorder("ScrollPane.border"));
		chart.setUseAntialiasing(true);
		
		contentPanel.add(chart, "cell 0 2 6 1,grow");
		setPlaying(false);
	}
	
	private void initListeners() {
		leftTextArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateRight();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateRight();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateRight();
			}
		});
		
		rightTextArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateLeft();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateLeft();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateLeft();
			}
		});
		
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
		
		rightLoadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String str = loadStringFromFile();
				if (str != null) {
					rightTextArea.setText(str);
				}
			}
		});
		leftLoadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String str = loadStringFromFile();
				if (str != null) {
					leftTextArea.setText(str);
				}
			}
		});

		rightSaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveStringToFile(rightTextArea.getText());
			}
		});
		leftSaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveStringToFile(leftTextArea.getText());
			}
		});

		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				play();
			}
		});
		
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
			}
		});
	}
	
	private File lastDir;
	
	private void updateDitLength() {
		try {
			int value = Integer.parseInt(speedTextField.getText());
			audioOut.setDitLength(value);
		} catch (NumberFormatException e) {
		}
	}
	
	private String loadStringFromFile() {
		if (lastDir == null) {
			lastDir = new File("./");
		}
		JFileChooser chooser = new JFileChooser(lastDir);
		
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			if (selectedFile != null) {
				try {
					StringBuilder b = new StringBuilder();
					for (String line : Files.readAllLines(selectedFile.toPath(), Charset.defaultCharset())) {
						b.append(line);
					}
					lastDir = selectedFile.getParentFile();
					return b.toString();
				} catch (IOException e) {
					JOptionPane.showConfirmDialog(this, "Error reading. " + e.toString(), "Error", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		return null;
	}
	
	private void saveStringToFile(String string) {
		if (lastDir == null) {
			lastDir = new File("./");
		}
		JFileChooser chooser = new JFileChooser(lastDir);
		
		int result = chooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file != null) {
				try {
					BufferedWriter writer = Files.newBufferedWriter(file.toPath(), Charset.defaultCharset());
					writer.write(string);
					writer.close();
				} catch (IOException e) {
					JOptionPane.showConfirmDialog(this, "Error saving. " + e.toString(), "Error", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	private Future<?> playing;
	private final Chart2D chart = new Chart2D();
	   
	private ITrace2D trace;

	private void play() {
		if (playing != null) {
			return;
		}
		setPlaying(true);
		if (trace != null) {
			chart.removeTrace(trace);
		}
		
		trace = new Trace2DLtd(100);
		trace.setColor(Color.RED);
		trace.setPhysicalUnits("Dit", "Value");
		
		chart.addTrace(trace);
		
		playing = audioOut.play(MorseCode.fromString(rightTextArea.getText()));
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
			statusLabel.setText("Status: playing...");
		}
		
		playButton.setEnabled(!playing);
		stopButton.setEnabled(playing);
		
		leftTextArea.setEditable(!playing);
		rightTextArea.setEditable(!playing);
		speedTextField.setEnabled(!playing);
	}
	
	private boolean prev;
	
	private void playedIndex(int symbolIndex, int ditIndex, boolean value) {
		statusLabel.setText("Status: playing (" + symbolIndex+ "/" + rightTextArea.getText().length()+")");
		
		rightTextArea.requestFocus();
		rightTextArea.setCaretPosition(symbolIndex + 1);
		rightTextArea.setSelectionStart(symbolIndex);
		rightTextArea.setSelectionEnd(symbolIndex + 1);
		
		if (prev != value) {
			trace.addPoint(ditIndex, prev ? 1 : 0);
		}
		trace.addPoint(ditIndex, value ? 1 : 0);
		
		
		this.prev = value;
	}
	
	private void updateRight() {
		if (disabledLeftEvents) {
			return;
		}
		
		MorseCoder coder = new MorseCoder();
		StringWriter w = new StringWriter();
		try {
			coder.encodeText(new StringReader(leftTextArea.getText()), new MorseOut(w));
		} catch (IOException e) {
			e.printStackTrace();
		}
		disabledRightEvents = true;
		try {
			rightTextArea.setText(w.getBuffer().toString());
		} finally {
			disabledRightEvents = false;
		}
	}
	
	private void updateLeft() {
		if (disabledRightEvents) {
			return;
		}
		MorseCoder coder = new MorseCoder();
		StringWriter w = new StringWriter();
		try {
			coder.decodeText(new MorseIn(new StringReader(rightTextArea.getText())), w);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		disabledLeftEvents = true;
		try {
			leftTextArea.setText(w.getBuffer().toString());
		} finally {
			disabledLeftEvents = false;
		}
	}
	
	private void initialize() {
		chart.setPaintLabels(false);
		setLayout(new GridLayout(0, 1, 0, 0));
		
		labelPanel.setBorder(new TitledBorder(null, "Morse Output", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		add(labelPanel);
		labelPanel.setLayout(new MigLayout("", "0[grow]0", "0[grow]0"));
		
		labelPanel.add(contentPanel, "cell 0 0,grow");
		contentPanel.setLayout(new MigLayout("", "[][][][grow][][]", "[][grow][120px]"));
		
		contentPanel.add(playButton, "cell 0 0");
		contentPanel.add(stopButton, "cell 1 0");
		contentPanel.add(statusLabel, "cell 2 0");
		
		contentPanel.add(speedLabel, "cell 4 0,alignx trailing");
		speedTextField.setHorizontalAlignment(SwingConstants.TRAILING);
		speedTextField.setColumns(10);
		speedTextField.setDocument(new LimitedDocument("0123456789"));
		
		contentPanel.add(speedTextField, "cell 5 0,growx");
		splitPane.setResizeWeight(0.5);
		splitPane.setContinuousLayout(true);
		
		contentPanel.add(splitPane, "cell 0 1 6 1,grow");
		
		splitPane.setLeftComponent(leftScrollPane);
		leftTextArea.setWrapStyleWord(true);
		leftTextArea.setRows(5);
		leftTextArea.setLineWrap(true);
		leftTextArea.setDocument(new LimitedDocument(MorseCoder.getAllowedCharacters()));
		
		leftScrollPane.setViewportView(leftTextArea);

		leftButtonPane.setLayout(new GridLayout(1,0));
		leftButtonPane.add(leftLoadButton);
		leftButtonPane.add(leftSaveButton);
		leftScrollPane.setColumnHeaderView(leftButtonPane);
		
		splitPane.setRightComponent(rightScrollPane);
		rightTextArea.setWrapStyleWord(true);
		rightTextArea.setRows(5);
		rightTextArea.setLineWrap(true);
		rightTextArea.setDocument(new LimitedDocument("-. "));
		
		rightScrollPane.setViewportView(rightTextArea);
		
		rightButtonPane.setLayout(new GridLayout(1,0));
		rightButtonPane.add(rightLoadButton);
		rightButtonPane.add(rightSaveButton);
		rightScrollPane.setColumnHeaderView(rightButtonPane);
	}

}
