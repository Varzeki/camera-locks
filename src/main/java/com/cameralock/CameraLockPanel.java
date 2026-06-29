package com.cameralock;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class CameraLockPanel extends PluginPanel
{
	private static final Color LOCKED_COLOR = new Color(220, 50, 50);
	private static final Color UNLOCKED_COLOR = new Color(80, 200, 80);
	private static final Color ACTIVE_BORDER_COLOR = new Color(80, 200, 80);

	// Both borders must have identical total insets to prevent row resizing
	private static final Border INACTIVE_ROW_BORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR, 1),
		new EmptyBorder(3, 5, 3, 5)
	);
	private static final Border ACTIVE_ROW_BORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createLineBorder(ACTIVE_BORDER_COLOR, 1),
		new EmptyBorder(3, 5, 3, 5)
	);

	private final CameraLockPlugin plugin;
	private final JLabel statusLabel;
	private final JButton toggleButton;
	private final JPanel presetListPanel;
	private final JTextField nameField;

	private final List<JPanel> presetRows = new ArrayList<>();
	private final List<JButton> lockButtons = new ArrayList<>();

	CameraLockPanel(CameraLockPlugin plugin)
	{
		super();
		this.plugin = plugin;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(new EmptyBorder(10, 10, 10, 10));

		// ── Header row ──────────────────────────────────────────────────────
		JLabel title = new JLabel("Camera Locks");
		title.setForeground(Color.WHITE);
		title.setFont(FontManager.getRunescapeBoldFont());

		statusLabel = new JLabel("UNLOCKED", SwingConstants.RIGHT);
		statusLabel.setForeground(UNLOCKED_COLOR);
		statusLabel.setFont(FontManager.getRunescapeBoldFont());

		JPanel headerRow = row();
		headerRow.add(title, BorderLayout.WEST);
		headerRow.add(statusLabel, BorderLayout.EAST);
		headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, headerRow.getPreferredSize().height));

		// ── Toggle button (centered, stable width) ───────────────────────────
		// Pre-size to "Unlock Camera" width so text swap doesn't cause reflow
		toggleButton = new JButton("Lock Camera");
		toggleButton.setPreferredSize(new Dimension(140, 28));
		toggleButton.setMaximumSize(new Dimension(140, 28));
		toggleButton.setMinimumSize(new Dimension(140, 28));
		toggleButton.addActionListener(e -> plugin.toggleLock());

		JPanel toggleWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		toggleWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		toggleWrapper.setAlignmentX(LEFT_ALIGNMENT);
		toggleWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		toggleWrapper.add(toggleButton);

		// ── Save section ────────────────────────────────────────────────────
		JLabel saveLabel = sectionLabel("Save Position:");

		nameField = new JTextField();
		nameField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameField.setForeground(Color.WHITE);
		nameField.setCaretColor(Color.WHITE);

		JButton addButton = new JButton("+");
		addButton.setPreferredSize(new Dimension(30, 26));
		addButton.setMinimumSize(new Dimension(30, 26));
		addButton.setMaximumSize(new Dimension(30, 26));
		addButton.setFont(addButton.getFont().deriveFont(Font.BOLD, 14f));
		addButton.setToolTipText("Save position");
		addButton.addActionListener(e ->
		{
			String name = nameField.getText().trim();
			if (!name.isEmpty())
			{
				plugin.savePreset(name);
				nameField.setText("");
			}
		});

		JPanel saveRow = row();
		saveRow.add(nameField, BorderLayout.CENTER);
		saveRow.add(addButton, BorderLayout.EAST);
		saveRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

		// ── Preset list section ─────────────────────────────────────────────
		JLabel presetsLabel = sectionLabel("Saved Positions:");

		presetListPanel = new JPanel();
		presetListPanel.setLayout(new BoxLayout(presetListPanel, BoxLayout.Y_AXIS));
		presetListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		presetListPanel.setAlignmentX(LEFT_ALIGNMENT);

		// ── Assemble ────────────────────────────────────────────────────────
		add(headerRow);
		add(Box.createVerticalStrut(10));
		add(toggleWrapper);
		add(Box.createVerticalStrut(18));
		add(saveLabel);
		add(Box.createVerticalStrut(3));
		add(saveRow);
		add(Box.createVerticalStrut(12));
		add(presetsLabel);
		add(Box.createVerticalStrut(4));
		add(presetListPanel);
	}

	/** Full-width BorderLayout row panel with the shared background. */
	private JPanel row()
	{
		JPanel p = new JPanel(new BorderLayout(4, 0));
		p.setBackground(ColorScheme.DARK_GRAY_COLOR);
		p.setAlignmentX(LEFT_ALIGNMENT);
		return p;
	}

	/** Left-aligned section label with a fixed height. */
	private JLabel sectionLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setAlignmentX(LEFT_ALIGNMENT);
		label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));
		return label;
	}

	void setLocked(boolean locked)
	{
		SwingUtilities.invokeLater(() ->
		{
			statusLabel.setText(locked ? "LOCKED" : "UNLOCKED");
			statusLabel.setForeground(locked ? LOCKED_COLOR : UNLOCKED_COLOR);
			toggleButton.setText(locked ? "Unlock Camera" : "Lock Camera");
		});
	}

	void setActivePreset(int index)
	{
		SwingUtilities.invokeLater(() ->
		{
			for (int i = 0; i < presetRows.size(); i++)
			{
				boolean active = (i == index);
				presetRows.get(i).setBorder(active ? ACTIVE_ROW_BORDER : INACTIVE_ROW_BORDER);
				if (i < lockButtons.size())
				{
					JButton btn = lockButtons.get(i);
					btn.setText(active ? "LOCKED" : "LOCK");
					btn.setEnabled(!active);
				}
			}
		});
	}

	void refreshPresets(List<CameraPreset> presets)
	{
		SwingUtilities.invokeLater(() ->
		{
			presetListPanel.removeAll();
			presetRows.clear();
			lockButtons.clear();

			if (presets.isEmpty())
			{
				JLabel empty = sectionLabel("No positions saved yet");
				presetListPanel.add(empty);
			}
			else
			{
				for (int i = 0; i < presets.size(); i++)
				{
					JPanel row = buildPresetRow(presets.get(i), i);
					presetRows.add(row);
					presetListPanel.add(row);
					if (i < presets.size() - 1)
					{
						presetListPanel.add(Box.createVerticalStrut(3));
					}
				}
			}

			presetListPanel.revalidate();
			presetListPanel.repaint();
		});
	}

	private JPanel buildPresetRow(CameraPreset preset, int index)
	{
		JPanel row = row();
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(INACTIVE_ROW_BORDER);

		// Fix the row height so button clicks and border changes don't resize it
		int rowH = 28;
		row.setPreferredSize(new Dimension(0, rowH));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowH));
		row.setMinimumSize(new Dimension(0, rowH));

		JLabel nameLabel = new JLabel(preset.getName());
		nameLabel.setForeground(Color.WHITE);
		nameLabel.setFont(FontManager.getRunescapeSmallFont());

		JButton lockButton = new JButton("LOCK");
		lockButton.setPreferredSize(new Dimension(68, 20));
		lockButton.setMinimumSize(new Dimension(68, 20));
		lockButton.setMaximumSize(new Dimension(68, 20));
		lockButton.setFont(lockButton.getFont().deriveFont(Font.BOLD, 10f));
		lockButton.setToolTipText("Apply and lock to this position");
		lockButton.addActionListener(e -> plugin.applyPreset(preset));
		lockButtons.add(lockButton);

		JButton deleteButton = new JButton("✕");
		deleteButton.setPreferredSize(new Dimension(26, 20));
		deleteButton.setMinimumSize(new Dimension(26, 20));
		deleteButton.setMaximumSize(new Dimension(26, 20));
		deleteButton.setForeground(new Color(200, 50, 50));
		deleteButton.setToolTipText("Delete preset");
		deleteButton.addActionListener(e -> plugin.deletePreset(index));

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
		buttons.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		buttons.add(lockButton);
		buttons.add(deleteButton);

		row.add(nameLabel, BorderLayout.CENTER);
		row.add(buttons, BorderLayout.EAST);
		return row;
	}
}
