package com.chasearchive.radarImageCli.satellite;

import java.io.IOException;

public interface SatelliteImage {
	public float dataFromField(String key);
	public float dataFromField(String key, int... indices);
	public float fromSwap(String key);
	public float fromSwap(String key, int... indices);
	public void loadIntoSwap(String... key) throws IOException;
}
