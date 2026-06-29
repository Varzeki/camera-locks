package com.cameralock;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarClientID;
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
	private static final Color ALIGN_ORANGE = new Color(255, 152, 31);
	private static final Color OK_GREEN = new Color(80, 200, 80);
	private static final Color HINT_COLOR = new Color(180, 180, 180);

	private final CameraLockPlugin plugin;
	private final CameraLockConfig config;
	private final Client client;
	private final PanelComponent panelComponent = new PanelComponent();

	@Inject
	CameraLockOverlay(CameraLockPlugin plugin, CameraLockConfig config, Client client)
	{
		this.plugin = plugin;
		this.config = config;
		this.client = client;
		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		CameraPreset guidance = plugin.getGuidancePreset();
		if (guidance != null)
		{
			return renderGuidance(graphics, guidance);
		}

		if (plugin.isLocked() && config.showOverlay())
		{
			return renderLocked(graphics);
		}

		return null;
	}

	private Dimension renderGuidance(Graphics2D graphics, CameraPreset target)
	{
		int yaw = client.getCameraYaw();
		int pitch = client.getCameraPitch();
		int zoom = client.getVarcIntValue(VarClientID.CAMERA_ZOOM_BIG);

		int yd = CameraLockPlugin.yawDiff(target.getYaw(), yaw);
		int pd = target.getPitch() - pitch;
		int zd = target.getZoom() - zoom;

		boolean yOk = Math.abs(yd) <= CameraLockPlugin.YAW_TOLERANCE;
		boolean pOk = Math.abs(pd) <= CameraLockPlugin.PITCH_TOLERANCE;
		boolean zOk = Math.abs(zd) <= CameraLockPlugin.ZOOM_TOLERANCE;

		panelComponent.getChildren().clear();

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("ALIGN CAMERA")
			.color(ALIGN_ORANGE)
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left(target.getName())
			.leftColor(HINT_COLOR)
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left(yawLabel(yd, yOk))
			.leftColor(yOk ? OK_GREEN : Color.WHITE)
			.right(yOk ? "" : String.valueOf(Math.abs(yd)))
			.rightColor(HINT_COLOR)
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left(pitchLabel(pd, pOk))
			.leftColor(pOk ? OK_GREEN : Color.WHITE)
			.right(pOk ? "" : String.valueOf(Math.abs(pd)))
			.rightColor(HINT_COLOR)
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left(zoomLabel(zd, zOk))
			.leftColor(zOk ? OK_GREEN : Color.WHITE)
			.right(zOk ? "" : String.valueOf(Math.abs(zd)))
			.rightColor(HINT_COLOR)
			.build());

		return panelComponent.render(graphics);
	}

	private Dimension renderLocked(Graphics2D graphics)
	{
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

	private static String yawLabel(int diff, boolean ok)
	{
		if (ok) return "✓ TURN";
		return diff > 0 ? "→ RIGHT" : "← LEFT";
	}

	private static String pitchLabel(int diff, boolean ok)
	{
		if (ok) return "✓ TILT";
		return diff > 0 ? "↑ UP" : "↓ DOWN";
	}

	private static String zoomLabel(int diff, boolean ok)
	{
		if (ok) return "✓ ZOOM";
		return diff > 0 ? "ZOOM IN" : "ZOOM OUT";
	}
}
