package com.cameralock;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.VarClientID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.input.MouseWheelListener;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@Slf4j
@PluginDescriptor(
	name = "Camera Locks",
	description = "Lock the camera and save/restore named camera position presets",
	tags = {"camera", "lock", "zoom", "pan", "preset"}
)
public class CameraLockPlugin extends Plugin implements KeyListener, MouseListener, MouseWheelListener
{
	static final int YAW_TOLERANCE = 15;
	static final int PITCH_TOLERANCE = 8;
	static final int ZOOM_TOLERANCE = 50;

	private static final String CONFIG_GROUP = "cameralock";
	private static final String PRESETS_KEY = "presets";
	private static final Type PRESET_LIST_TYPE = new TypeToken<List<CameraPreset>>(){}.getType();

	@Inject
	private CameraLockConfig config;

	@Inject
	private Client client;

	@Inject
	private KeyManager keyManager;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	@Inject
	private CameraLockOverlay overlay;

	private CameraLockPanel panel;

	@Getter
	private boolean locked = false;

	@Getter
	private CameraPreset guidancePreset = null;
	private int guidancePresetIndex = -1;

	private int activePresetIndex = -1;
	private boolean middleMouseDown = false;
	private NavigationButton navButton;

	private final HotkeyListener toggleHotkeyListener = new HotkeyListener(() -> config.toggleHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			toggleLock();
		}
	};

	@Provides
	CameraLockConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CameraLockConfig.class);
	}

	@Override
	protected void startUp()
	{
		locked = false;
		guidancePreset = null;
		guidancePresetIndex = -1;
		activePresetIndex = -1;
		middleMouseDown = false;

		panel = new CameraLockPanel(this);

		navButton = NavigationButton.builder()
			.tooltip("Camera Locks")
			.icon(buildIcon())
			.priority(10)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		panel.refreshPresets(loadPresets());

		keyManager.registerKeyListener(toggleHotkeyListener);
		keyManager.registerKeyListener(this);
		mouseManager.registerMouseListener(this);
		mouseManager.registerMouseWheelListener(this);
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		locked = false;
		guidancePreset = null;
		guidancePresetIndex = -1;
		activePresetIndex = -1;
		middleMouseDown = false;
		client.setCameraSpeed(1f);

		clientToolbar.removeNavigation(navButton);
		navButton = null;
		panel = null;

		keyManager.unregisterKeyListener(toggleHotkeyListener);
		keyManager.unregisterKeyListener(this);
		mouseManager.unregisterMouseListener(this);
		mouseManager.unregisterMouseWheelListener(this);
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (guidancePreset == null)
		{
			return;
		}

		int yaw = client.getCameraYaw();
		int pitch = client.getCameraPitch();
		int zoom = client.getVarcIntValue(VarClientID.CAMERA_ZOOM_BIG);

		boolean yawOk = Math.abs(yawDiff(guidancePreset.getYaw(), yaw)) <= YAW_TOLERANCE;
		boolean pitchOk = Math.abs(guidancePreset.getPitch() - pitch) <= PITCH_TOLERANCE;
		boolean zoomOk = Math.abs(guidancePreset.getZoom() - zoom) <= ZOOM_TOLERANCE;

		if (yawOk && pitchOk && zoomOk)
		{
			guidancePreset = null;
			activePresetIndex = guidancePresetIndex;
			guidancePresetIndex = -1;
			locked = true;
			client.setCameraSpeed(1f);
			panel.setLocked(true);
			panel.setActivePreset(activePresetIndex);
		}
	}

	/** Signed circular difference: positive = target is clockwise from current. */
	static int yawDiff(int target, int current)
	{
		int diff = target - current;
		if (diff > 1024) diff -= 2048;
		if (diff < -1024) diff += 2048;
		return diff;
	}

	void toggleLock()
	{
		if (guidancePreset != null)
		{
			// Cancel guidance
			guidancePreset = null;
			guidancePresetIndex = -1;
			client.setCameraSpeed(1f);
			panel.setGuidancePreset(-1);
			panel.setLocked(false);
			return;
		}

		locked = !locked;
		if (!locked)
		{
			middleMouseDown = false;
			activePresetIndex = -1;
			panel.setActivePreset(-1);
		}
		panel.setLocked(locked);
	}

	void savePreset(String name)
	{
		int yaw = client.getCameraYaw();
		int pitch = client.getCameraPitch();
		int zoom = client.getVarcIntValue(VarClientID.CAMERA_ZOOM_BIG);

		List<CameraPreset> presets = loadPresets();
		presets.add(new CameraPreset(name, yaw, pitch, zoom));
		storePresets(presets);
		panel.refreshPresets(presets);
		panel.setActivePreset(activePresetIndex);
	}

	void startGuidance(CameraPreset preset)
	{
		List<CameraPreset> presets = loadPresets();
		int index = presets.indexOf(preset);

		// Unlock the camera so the user can move it
		locked = false;
		middleMouseDown = false;

		guidancePreset = preset;
		guidancePresetIndex = index;
		activePresetIndex = -1;

		client.setCameraSpeed(0.1f);

		panel.setLocked(false);
		panel.setActivePreset(-1);
		panel.setGuidancePreset(index);
	}

	void deletePreset(int index)
	{
		List<CameraPreset> presets = loadPresets();
		if (index < 0 || index >= presets.size())
		{
			return;
		}
		presets.remove(index);
		storePresets(presets);

		if (guidancePresetIndex == index)
		{
			guidancePreset = null;
			guidancePresetIndex = -1;
		}
		else if (guidancePresetIndex > index)
		{
			guidancePresetIndex--;
		}

		if (activePresetIndex == index)
		{
			activePresetIndex = -1;
		}
		else if (activePresetIndex > index)
		{
			activePresetIndex--;
		}

		panel.refreshPresets(presets);
		panel.setGuidancePreset(guidancePresetIndex);
		panel.setActivePreset(activePresetIndex);
	}

	private List<CameraPreset> loadPresets()
	{
		String json = configManager.getConfiguration(CONFIG_GROUP, PRESETS_KEY);
		if (json == null || json.isEmpty())
		{
			return new ArrayList<>();
		}
		try
		{
			List<CameraPreset> result = gson.fromJson(json, PRESET_LIST_TYPE);
			return result != null ? result : new ArrayList<>();
		}
		catch (Exception e)
		{
			log.error("Failed to deserialize camera presets", e);
			return new ArrayList<>();
		}
	}

	private void storePresets(List<CameraPreset> presets)
	{
		configManager.setConfiguration(CONFIG_GROUP, PRESETS_KEY, gson.toJson(presets));
	}

	private static BufferedImage buildIcon()
	{
		final int S = 16;
		BufferedImage img = new BufferedImage(S, S, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(new Color(255, 152, 31));
		g.fillRoundRect(1, 4, 12, 9, 2, 2);
		g.fillRoundRect(5, 2, 4, 3, 1, 1);

		g.setColor(new Color(18, 18, 24));
		g.fillOval(4, 5, 7, 7);
		g.setColor(new Color(80, 44, 6));
		g.fillOval(5, 6, 5, 5);
		g.setColor(new Color(255, 230, 180, 150));
		g.fillOval(6, 7, 2, 2);

		g.setColor(new Color(180, 90, 10));
		g.fillOval(11, 4, 3, 3);

		g.dispose();
		return img;
	}

	// region KeyListener

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (!locked || !config.lockArrowKeys()) return;
		int code = e.getKeyCode();
		if (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN
			|| code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT)
		{
			e.consume();
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if (!locked || !config.lockArrowKeys()) return;
		int code = e.getKeyCode();
		if (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN
			|| code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT)
		{
			e.consume();
		}
	}

	// endregion

	// region MouseListener

	@Override
	public MouseEvent mousePressed(MouseEvent mouseEvent)
	{
		if (!locked || !config.lockMiddleMouse()) return mouseEvent;
		if (SwingUtilities.isMiddleMouseButton(mouseEvent))
		{
			middleMouseDown = true;
			mouseEvent.consume();
		}
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseReleased(MouseEvent mouseEvent)
	{
		if (SwingUtilities.isMiddleMouseButton(mouseEvent) && middleMouseDown)
		{
			middleMouseDown = false;
			if (locked && config.lockMiddleMouse()) mouseEvent.consume();
		}
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseDragged(MouseEvent mouseEvent)
	{
		if (locked && config.lockMiddleMouse() && middleMouseDown) mouseEvent.consume();
		return mouseEvent;
	}

	@Override public MouseEvent mouseClicked(MouseEvent e) { return e; }
	@Override public MouseEvent mouseEntered(MouseEvent e) { return e; }
	@Override public MouseEvent mouseExited(MouseEvent e) { return e; }
	@Override public MouseEvent mouseMoved(MouseEvent e) { return e; }

	// endregion

	// region MouseWheelListener

	@Override
	public MouseWheelEvent mouseWheelMoved(MouseWheelEvent mouseWheelEvent)
	{
		if (locked && config.lockScroll()) mouseWheelEvent.consume();
		return mouseWheelEvent;
	}

	// endregion
}
