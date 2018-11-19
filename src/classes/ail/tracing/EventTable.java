package ail.tracing;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.JXTable;

import ail.tracing.events.AbstractEvent;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.JXTableSupport;
import ca.odell.glazedlists.swing.TableComparatorChooser;

public class EventTable extends JXTable {
	private static final long serialVersionUID = 1L;
	private final List<String> columns;
	private final EventList<Map<String, String>> rows;
	private final Map<String, Integer> index;

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
				chooser.setFileFilter(new FileNameExtensionFilter("AIL Trace File", "db"));
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					final JLabel header = new JLabel();
					header.setVerticalAlignment(SwingConstants.TOP);
					header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
					final JTextComponent description = new JTextField();
					final EventTable table = new EventTable(chooser.getSelectedFile(), header, description);
					final JFrame frame = new JFrame();
					frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
					frame.setLayout(new BorderLayout());
					frame.add(header, BorderLayout.WEST);
					frame.add(new JScrollPane(table), BorderLayout.CENTER);
					frame.add(description, BorderLayout.SOUTH);
					frame.setPreferredSize(
							GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize());
					frame.pack();
					frame.setVisible(true);
				}
			}
		});
	}

	public EventTable(final File datafile, final JLabel headers, final JTextComponent description) {
		final List<AbstractEvent> data = new EventStorage(datafile).getAll();
		this.columns = new LinkedList<>();
		this.rows = GlazedLists.eventList(new LinkedList<Map<String, String>>());
		this.index = new LinkedHashMap<>();
		process(data);
		String headersTxt = "<html><br>";
		for (String header : this.index.keySet()) {
			headersTxt += header + "<br>";
		}
		headers.setText(headersTxt + "</html>");
		JXTableSupport.<Map<String, String>>install(EventTable.this, this.rows, new TableFormat<Map<String, String>>() {
			@Override
			public int getColumnCount() {
				return EventTable.this.columns.size();
			}

			@Override
			public String getColumnName(final int columnIndex) {
				return EventTable.this.columns.get(columnIndex);
			}

			@Override
			public Object getColumnValue(final Map<String, String> rowObject, final int columnIndex) {
				final Object value = rowObject.get(getColumnName(columnIndex));
				return (value == null) ? "" : value;
			}
		}, new SortedList<Map<String, String>>(this.rows, null), TableComparatorChooser.SINGLE_COLUMN);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				final int col = EventTable.this.columnAtPoint(evt.getPoint());
				final AbstractEvent event = data.get(col);
				description.setText(col + ": " + event.toString());
			}
		});
	}

	private void process(final List<AbstractEvent> data) {
		for (int i = 0; i < data.size(); ++i) {
			final String index = Integer.toString(i);
			final AbstractEvent event = data.get(i);
			final List<String> lookup = (event == null) ? new ArrayList<>(0) : event.getLookupData();
			for (final String signature : lookup) {
				final Integer existing = this.index.get(signature);
				if (existing == null) {
					this.index.put(signature, this.rows.size());
					final Map<String, String> row = new HashMap<>();
					row.put(index, getDescription(event));
					this.rows.add(row); // TODO: sorting on type?! (i.e.
										// action signatures first)
				} else {
					final Map<String, String> row = this.rows.get(existing);
					row.put(index, getDescription(event));
				}
			}
			if (!this.columns.contains(index)) {
				this.columns.add(index);
			}
		}
	}

	private static String getDescription(AbstractEvent event) {
		return event.getClass().getSimpleName().replace("Event", "");// .substring(0, 1);
	}
}
