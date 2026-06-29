# Camera Locks

A RuneLite plugin that locks your camera in place and lets you save and restore named camera position presets.

## Features

### Camera Lock
Lock the camera to prevent it from being moved by:
- Arrow keys
- Middle mouse drag
- Scroll wheel zoom

Toggle the lock with a configurable hotkey, or use the **Lock Camera** button in the side panel. A **CAMERA LOCKED** overlay is shown on screen while the lock is active (can be disabled in settings).

### Camera Position Presets
Save your current camera angle (yaw, pitch, and zoom) as a named preset. Clicking **LOCK** on a saved preset instantly moves the camera to that position and locks it in place.

Presets are persisted across sessions.

## Side Panel

The side panel shows:
- Current lock status (LOCKED / UNLOCKED)
- A toggle button to lock or unlock the camera
- A field to save the current camera position as a named preset
- Your list of saved positions, each with a **LOCK** button (apply + lock) and a **✕** delete button

The active preset's row is highlighted in green.

## Configuration

| Setting | Description |
|---|---|
| Toggle hotkey | Keybind to toggle the camera lock on/off |
| Lock arrow keys | Block arrow key camera panning when locked |
| Lock middle mouse | Block middle mouse camera rotation when locked |
| Lock scroll zoom | Block scroll wheel zoom when locked |
| Show lock indicator | Display an overlay while the camera is locked |
