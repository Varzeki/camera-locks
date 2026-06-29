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
	private static final Color GUIDANCE_BORDER_COLOR = new Color(255, 152, 31);

	// All three borders must have identical total insets to prevent row resizing on state changes
	private static final Border INACTIVE_ROW_BORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR, 1),
		new EmptyBorder(3, 5, 3, 5)
	);
	private static final Border ACTIVE_ROW_BORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createLineBorder(ACTIVE_BORDER_COLOR, 1),
		new EmptyBorder(3, 5, 3, 5)
	);
	private static final Border GUIDANCE_ROW_BORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createLineBorder(GUIDANCE_BORDER_COLOR, 1),
		new EmptyBorder(3, 5, 3, 5)
	);

	private final CameraLockPlugin plugin;
	private final JLabel statusLabel;
	private final JButton toggleButton;
	private final JPanel presetListPanel;
	private final JTextField nameField;

	private final List<JPanel> presetRows = new ArrayList<>();
	private final List<JButton> lockButtons = new ArrayList<>();
	private int guidanceIndex = -1;

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

		// ── Toggle button (stable width so text swap does not cause reflow) ─
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
		addButton.setToolTipText("Save current camera position");
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

	private JPanel row()
	{
		JPanel p = new JPanel(new BorderLayout(4, 0));
		p.setBackground(ColorScheme.DARK_GRAY_COLOR);
		p.setAlignmentX(LEFT_ALIGNMENT);
		return p;
	}

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

	/**
	 * Highlight a preset row as "aligning" (orange border, disabled button showing "···").
	 * Pass -1 to clear all guidance highlighting.
	 */
	void setGuidancePreset(int index)
	{
		SwingUtilities.invokeLater(() ->
		{
			// Clear previous guidance row
			if (guidanceIndex >= 0 && guidanceIndex < presetRows.size())
			{
				presetRows.get(guidanceIndex).setBorder(INACTIVE_ROW_BORDER);
				lockButtons.get(guidanceIndex).setText("LOCK");
				lockButtons.get(guidanceIndex).setEnabled(true);
			}

			guidanceIndex = index;

			if (index >= 0 && index < presetRows.size())
			{
				presetRows.get(index).setBorder(GUIDANCE_ROW_BORDER);
				lockButtons.get(index).setText("···");
				lockButtons.get(index).setEnabled(false);
				statusLabel.setText("ALIGNING");
				statusLabel.setForeground(GUIDANCE_BORDER_COLOR);
				toggleButton.setText("Cancel");
			}
		});
	}

	/**
	 * Mark a preset row as locked (green border, "LOCKED" button).
	 * Clears any guidance styling. Pass -1 to clear all active highlighting.
	 */
	void setActivePreset(int index)
	{
		SwingUtilities.invokeLater(() ->
		{
			guidanceIndex = -1;
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
			guidanceIndex = -1;

			if (presets.isEmpty())
			{
				JLabel empty = sectionLabel("No positions saved yet");
				presetListPanel.add(empty);
			}
			else
			{
				for (int i = 0; i < presets.size(); i++)
				{
					JPanel rowPanel = buildPresetRow(presets.get(i), i);
					presetRows.add(rowPanel);
					presetListPanel.add(rowPanel);
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
		JPanel rowPanel = row();
		rowPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		rowPanel.setBorder(INACTIVE_ROW_BORDER);

		int rowH = 28;
		rowPanel.setPreferredSize(new Dimension(0, rowH));
		rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowH));
		rowPanel.setMinimumSize(new Dimension(0, rowH));

		JLabel nameLabel = new JLabel(preset.getName());
		nameLabel.setForeground(Color.WHITE);
		nameLabel.setFont(FontManager.getRunescapeSmallFont());

		JButton lockButton = new JButton("LOCK");
		lockButton.setPreferredSize(new Dimension(68, 20));
		lockButton.setMinimumSize(new Dimension(68, 20));
		lockButton.setMaximumSize(new Dimension(68, 20));
		lockButton.setFont(lockButton.getFont().deriveFont(Font.BOLD, 10f));
		lockButton.setToolTipText("Guide camera to this position and lock");
		lockButton.addActionListener(e -> plugin.startGuidance(preset));
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

		rowPanel.add(nameLabel, BorderLayout.CENTER);
		rowPanel.add(buttons, BorderLayout.EAST);
		return rowPanel;
	}
}
