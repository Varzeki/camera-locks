package com.cameralock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CameraPreset
{
	private String name;
	private int yaw;
	private int pitch;
	private int zoom;
}
