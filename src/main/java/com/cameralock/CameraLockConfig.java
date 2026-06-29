package com.cameralock;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("cameralock")
public interface CameraLockConfig extends Config
{
	@ConfigItem(
		keyName = "toggleHotkey",
		name = "Toggle hotkey",
		description = "Hotkey to toggle camera lock on/off",
		position = 0
	)
	default Keybind toggleHotkey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "lockArrowKeys",
		name = "Lock arrow keys",
		description = "Block arrow key camera panning when locked",
		position = 2
	)
	default boolean lockArrowKeys()
	{
		return true;
	}

	@ConfigItem(
		keyName = "lockMiddleMouse",
		name = "Lock middle mouse",
		description = "Block middle mouse camera rotation when locked",
		position = 3
	)
	default boolean lockMiddleMouse()
	{
		return true;
	}

	@ConfigItem(
		keyName = "lockScroll",
		name = "Lock scroll zoom",
		description = "Block scroll wheel zoom when locked",
		position = 4
	)
	default boolean lockScroll()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showOverlay",
		name = "Show lock indicator",
		description = "Display an overlay on screen when the camera is locked",
		position = 5
	)
	default boolean showOverlay()
	{
		return true;
	}
}
