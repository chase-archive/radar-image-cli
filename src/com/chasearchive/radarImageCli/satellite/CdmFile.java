package com.chasearchive.radarImageCli.satellite;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public abstract class CdmFile {
	String locationOnDisk;
	HashMap<String, DataField> permaFields = new HashMap<>(); // very few fields, stored for rapid loading
	HashMap<String, DataField> swapFields = new HashMap<>(); // fields that can be swapped out. avoids loading all data at once to save memory
	
	public static CdmFile loadFromFile(File f) throws IOException {
		return null;
	}
}
