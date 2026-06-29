package com.cameralock;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import javax.inject.Inject;
import net.runelite.client.config.Keybind;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class CameraLockOverlay extends Overlay
{
	private static final Color LOCKED_RED = new Color(220, 50, 50);
	private static final Color HINT_COLOR = new Color(180, 180, 180);

	private final CameraLockPlugin plugin;
	private final CameraLockConfig config;
	private final PanelComponent panelComponent = new PanelComponent();

	@Inject
	CameraLockOverlay(CameraLockPlugin plugin, CameraLockConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showOverlay() || !plugin.isLocked())
		{
			return null;
		}

		panelComponent.getChildren().clear();

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("CAMERA LOCKED")
			.color(LOCKED_RED)
			.build());

		Keybind hotkey = config.toggleHotkey();
		if (hotkey.getKeyCode() != KeyEvent.VK_UNDEFINED)
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text(hotkey.toString().toUpperCase() + " TO TOGGLE")
				.color(HINT_COLOR)
				.build());
		}

		return panelComponent.render(graphics);
	}
}
