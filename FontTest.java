package fontTest;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
//import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.FileReader;
//import java.io.File;
//import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.lang.invoke.MethodHandles;
import java.util.stream.IntStream;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
//import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class FontTest {
	public static String TEST = "ABCDEFGHIJKLMNOPQRSTUVWXYZ\nabcdefghijklmnopqrstuvwxyz àèéìòùç\n1234567890 .,:;!?’ “” +-*\\|/%^<> ~°_@#§()[]{}£$€&\n";
	public static String U_TEST = ""; // see first lines in main()
	public static String helpText = "This program is a simple font display to get a feel of the various fonts' appearance.\n\n"
			+"The fonts are divided into categories:\n - sans-serif\n - serif\n - monospaced\n - irregular/ornate\n"
			+" - other (which includes foreign or technical fonts)\n"
			+"The user has a limited selection of style (bold/italic) and size of the font.\nThe displayed text is editable.\n\n"
			+"The 'Sample' button displays an array of alphanumeric and punctuation characters;\n"
			+"the 'Unicode' button shows all Unicode characters from 0000 to 9999.\n\n"
			+"Warning: the Unicode feature is experimental:\n"
			+" - most fonts don't have a glyph for many Unicode characters;\n"
			+" - the first lines can get messy as some characters represent control codes.";
	public static String[] fontList = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	public static String[] fontTypes;

	public static String path = System.getProperty("user.dir") + "/fontTest/";
	public static JFrame frame = new JFrame("Font Test");
	public static JComboBox<String> sel_font = new JComboBox<String>(), sel_filter = new JComboBox<String>();
	public static JScrollPane testAreaScroll;
	public static JTextArea testArea = new JTextArea(TEST);
	public static JCheckBox check_bold, check_italic;
	public static JSpinner sel_textSize;
	public static JButton btn_sampleText = new JButton("Sample"), btn_unicodeText = new JButton("Unicode"), btn_help = new JButton("?");

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		// Unicode test string
		for (int i = 0; i <= 9999; i++) {
			U_TEST += Character.toString((char) i);
			if (i % 64 == 63) {
				U_TEST += "\n";
			}
		}

		// Types of fonts (used for filter)
		fontTypes = new String[fontList.length];
		BufferedReader reader = null;
		try {
			InputStream is = FontTest.class.getResourceAsStream("/fontTest/fontTypes.txt");
			reader = new BufferedReader(new InputStreamReader(is));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("***  Cannot find fontTypes.txt  ***");
			try {
				reader = new BufferedReader(new FileReader(path + "fontTypes.txt"));
				reader.close();
			} catch (IOException e2) {
				e2.printStackTrace();
				reader = null;
			}
		}
		try {
			for (int i = 0; i < fontTypes.length; i++) {
				String[] flt = reader.readLine().split("\t");
				fontTypes[i] = flt[flt.length - 1];
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(fontTypes.length+"  "+fontTypes[0]+", "+fontTypes[fontTypes.length-1]);

		// *** Layout elements ***

		frame.setMinimumSize(new Dimension(700, 200));
		frame.setPreferredSize(new Dimension(700, 400));
		frame.setResizable(true);
		try {
			frame.setIconImage(new ImageIcon(FontTest.class.getResource("/fontTest/FontTest.png")).getImage());
		} catch (Exception e1) {
			e1.printStackTrace();
			frame.setIconImage(new ImageIcon(path + "FontTest.png").getImage());
		}
		Container pane = frame.getContentPane();
		pane.setBackground(Color.white);
		SpringLayout l = new SpringLayout();
		pane.setLayout(l);

		// create list
		for (String f : fontList) {
			System.out.println(f);
			sel_font.addItem(f);
		}
		sel_font.setSelectedIndex(0);
		sel_font.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent it) {
				changeFont();
			}
		});

		// filter
		JLabel filter_lbl = new JLabel("Filter:");
		sel_filter.addItem("all");
		sel_filter.addItem("sans");
		sel_filter.addItem("serif");
		sel_filter.addItem("mono");
		sel_filter.addItem("fancy");
		sel_filter.addItem("other");
		sel_filter.setSelectedIndex(0);
		sel_filter.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent it) {
				String currentFilter = (String) it.getItem();
				sel_font.removeAllItems();
				if (currentFilter.equals("all")) {
					for (String f : fontList) {
						sel_font.addItem(f);
					}
				} else {
					int[] filteredIndices = IntStream.range(0, fontTypes.length)
							.filter(i -> fontTypes[i].equals(currentFilter)).toArray();
					for (int j : filteredIndices) {
						sel_font.addItem(fontList[j]);
					}
				}
				frame.repaint();
			}
		});

		// enables font preview within ComboBox (see class ComboBoxRenderer below)
		ComboBoxRenderer fontPreview = new ComboBoxRenderer();
		fontPreview.setPreferredSize(new Dimension(300, 20));
		sel_font.setRenderer(fontPreview);
		sel_font.setMaximumRowCount(15);

		check_bold = new JCheckBox("B");
		check_bold.setFont(new Font("Serif", Font.BOLD, 15));
		check_bold.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent c) {
				changeFont();
			}
		});

		check_italic = new JCheckBox("I");
		check_italic.setFont(new Font("Serif", Font.ITALIC, 15));
		check_italic.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent c) {
				changeFont();
			}
		});

		sel_textSize = new JSpinner(new SpinnerNumberModel(15, 8, 50, 1));
		sel_textSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent c) {
				changeFont();
			}
		});

		btn_sampleText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testArea.setText(TEST);
			}
		});
		
		btn_help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, helpText, "Help - FontTest", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		testArea.setEditable(true);
		testArea.setCaretPosition(testArea.getText().length());

		btn_unicodeText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// String s = createUnicodeString();
				// testArea.insert(s, testArea.getCaretPosition());
				testArea.setText(U_TEST);
			}
		});
		testAreaScroll = new JScrollPane(testArea);
		testAreaScroll.setPreferredSize(new Dimension(650, 250));

		changeFont();

		// *** Layout ***

		pane.add(testAreaScroll);
		pane.add(sel_font);
		pane.add(sel_filter);
		pane.add(filter_lbl);
		pane.add(check_bold);
		pane.add(check_italic);
		pane.add(sel_textSize);
		pane.add(btn_sampleText);
		pane.add(btn_unicodeText);
		pane.add(btn_help);
		l.putConstraint(SpringLayout.WEST, sel_font, 20, SpringLayout.WEST, frame);
		l.putConstraint(SpringLayout.NORTH, sel_font, 20, SpringLayout.NORTH, frame);
		l.putConstraint(SpringLayout.WEST, testAreaScroll, 20, SpringLayout.WEST, frame);
		l.putConstraint(SpringLayout.NORTH, testAreaScroll, 20, SpringLayout.SOUTH, sel_filter);
		l.putConstraint(SpringLayout.NORTH, check_bold, 0, SpringLayout.NORTH, sel_font);
		l.putConstraint(SpringLayout.WEST, check_bold, 20, SpringLayout.EAST, sel_font);
		l.putConstraint(SpringLayout.VERTICAL_CENTER, check_italic, 0, SpringLayout.VERTICAL_CENTER, check_bold);
		l.putConstraint(SpringLayout.WEST, check_italic, 10, SpringLayout.EAST, check_bold);
		l.putConstraint(SpringLayout.VERTICAL_CENTER, sel_textSize, 0, SpringLayout.VERTICAL_CENTER, check_italic);
		l.putConstraint(SpringLayout.WEST, sel_textSize, 20, SpringLayout.EAST, check_italic);
		l.putConstraint(SpringLayout.VERTICAL_CENTER, btn_sampleText, 0, SpringLayout.VERTICAL_CENTER, sel_textSize);
		l.putConstraint(SpringLayout.WEST, btn_sampleText, 40, SpringLayout.EAST, sel_textSize);
		l.putConstraint(SpringLayout.WEST, filter_lbl, 20, SpringLayout.WEST, frame);
		l.putConstraint(SpringLayout.NORTH, filter_lbl, 20, SpringLayout.SOUTH, sel_font);
		l.putConstraint(SpringLayout.WEST, sel_filter, 10, SpringLayout.EAST, filter_lbl);
		l.putConstraint(SpringLayout.VERTICAL_CENTER, sel_filter, 0, SpringLayout.VERTICAL_CENTER, filter_lbl);
		l.putConstraint(SpringLayout.WEST, btn_unicodeText, 0, SpringLayout.WEST, btn_sampleText);
		l.putConstraint(SpringLayout.NORTH, btn_unicodeText, 10, SpringLayout.SOUTH, btn_sampleText);
		l.putConstraint(SpringLayout.NORTH, btn_help, 0, SpringLayout.NORTH, btn_sampleText);
		l.putConstraint(SpringLayout.WEST, btn_help, 10, SpringLayout.EAST, btn_sampleText);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.repaint();
	}

	public static void changeFont() {
		String font = (String) sel_font.getSelectedItem();
		int attr = Font.PLAIN + (check_bold.isSelected() ? Font.BOLD : 0) + (check_italic.isSelected() ? Font.ITALIC : 0);
		int size = (int) sel_textSize.getValue();
		testArea.setFont(new Font(font, attr, size));
		frame.repaint();
	}

	@SuppressWarnings({ "serial", "rawtypes" })
	private static class ComboBoxRenderer extends JLabel implements ListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			if (isSelected) {
				setForeground(new Color(0, 0, 200));
				setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			} else {
				setBackground(Color.white);
				setForeground(Color.black);
				setBorder(null);
			}
			String fontName = (String) value;
			setText(fontName);
			Font f = new Font(fontName, Font.PLAIN, 15);
			setFont(f.canDisplay('a') ? f : new Font("Sans", Font.PLAIN, 15));
			setAlignmentY(CENTER_ALIGNMENT);
			return this;
		}
	}

	/*
	 * public static String createUnicodeString() { String u =
	 * JOptionPane.showInputDialog("Insert Unicode character"); return
	 * String.valueOf(Character.toChars(Integer.parseInt(u, 16))); }
	 */
}
