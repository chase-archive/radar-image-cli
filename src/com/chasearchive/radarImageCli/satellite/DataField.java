package com.chasearchive.radarImageCli.satellite;

import java.io.IOException;
import java.util.HashMap;

import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

public class DataField {
	private int[] shape;
	private int[] shapeMult;
	private float[] data;
	private String annotation;
	private HashMap<String, DataField> bundledFields = new HashMap<>();
	
	public static DataField fromCdmVar(Variable var) {
		DataField field = new DataField();
		Array arr = null;
		try {
			arr = var.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		field.shape = var.getShape();
		field.shapeMult = new int[field.shape.length];
		int runningMult = 1;
		for(int i = field.shape.length - 1; i >= 0; i--) {
			field.shapeMult[i] = runningMult;
			runningMult = field.shape[i];
		}
		
		field.data = new float[(int) var.getSize()];
		for(int i = 0; i < var.getSize(); i++) {
			field.data[i] = arr.getFloat(i);
		}
		
		return field;
	}
	
	public void processOffsets() {
		boolean allNeededFieldsPresent = 
				(bundledFields.containsKey("scale_factor")
						&& bundledFields.containsKey("add_offset"));
		
		if(!allNeededFieldsPresent) {
			return;
		}
		
		float scaleFactor = bundledFields.get("scale_factor").getData();
		float addOffset = bundledFields.get("add_offset").getData();
		float fillValue = -1024;
		if(bundledFields.containsKey("fill_value")) {
			fillValue = bundledFields.get("fill_value").getData();
		}
		
		for(int i = 0; i < data.length; i++) {
			if(bundledFields.containsKey("fill_value")) {
				if(data[i] == fillValue) {
					data[i] = -1024;
					continue;
				}
			}
			data[i] = (float) (scaleFactor * data[i] + addOffset);
		}
	}

	public static DataField fromNexradAttr(Attribute attr) {
		DataField field = new DataField();
		field.shape = new int[1];
		field.shapeMult = new int[field.shape.length];
		field.shapeMult[0] = 1;
		
		Object value = attr.getValue(0);

		field.data = new float[1];
		if(value.getClass() == Integer.valueOf(0).getClass()) {
			field.data[0] = (float) (int) value;
		} else if(value.getClass() == Double.valueOf(0).getClass()) {
			field.data[0] = (float) (double) value;
		}
		
		return field;
	}
	
	public static DataField fromNexradAttrToStr(Attribute attr) {
		DataField field = new DataField();
		field.shape = new int[1];
		field.shapeMult = new int[field.shape.length];
		field.shapeMult[0] = 1;
		
		String value = attr.getStringValue();

		field.data = new float[1];
		field.data[0] = -1024.0f;
		
		field.annotation = value;
		
		return field;
	}
	
	public static DataField fromNumber(double d) {
		DataField field = new DataField();
		field.shape = new int[1];
		field.shapeMult = new int[field.shape.length];
		field.shapeMult[0] = 1;

		field.data = new float[1];
		field.data[0] = (float) d;
		
		return field;
	}
	
	public static DataField fromNumber(float f) {
		DataField field = new DataField();
		field.shape = new int[1];
		field.shapeMult = new int[field.shape.length];
		field.shapeMult[0] = 1;

		field.data = new float[1];
		field.data[0] = f;
		
		return field;
	}
	
	public float getData() {
		return getData(0);
	}
	
	public float getData(int... indices) {
		int idx = 0;
		
		int minLength = Integer.min(indices.length, shapeMult.length);
		for(int i = 0; i < indices.length; i++) {
			int idxComp = indices[minLength - 1 - i];
			int mult = shapeMult[minLength - 1 - i];
			
			idx += idxComp * mult;
		}
		
		return data[idx];
	}
	
	// I nearly WAY overcomplicated writing this one
	public float[] array1D() {
		return data;
	}
	
	public float[][] array2D() {
		int[] array2DShape = new int[2];

		for(int i = 0; i < array2DShape.length; i++) {
			array2DShape[i] = 1;
		}
		
		for(int i = 0; i < shape.length; i++) {
			int idx = Integer.min(i, shape.length - 1);
			
			array2DShape[idx] *= shape[i];
		}
		
		float[][] array = new float[array2DShape[0]][array2DShape[1]];
		for(int i = 0; i < array.length; i++) {
			for(int j = 0; j < array[i].length; j++) {
				array[i][j] = getData(i, j);
			}
		}
		
		return array;
	}
	
	public float[][][] array3D() {
		int[] array3DShape = new int[3];

		for(int i = 0; i < array3DShape.length; i++) {
			array3DShape[i] = 1;
		}
		
		for(int i = 0; i < shape.length; i++) {
			int idx = Integer.min(i, shape.length - 1);
			
			array3DShape[idx] *= shape[i];
		}
		
		float[][][] array = new float[array3DShape[0]][array3DShape[1]][array3DShape[2]];
		for(int i = 0; i < array.length; i++) {
			for(int j = 0; j < array[i].length; j++) {
				for(int k = 0; k < array[i][j].length; k++) {
					array[i][j][k] = getData(i, j, k);
				}
			}
		}
		
		return array;
	}
	
	public int[] getShape() {
		return shape;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}
	
	public void bundleField(String str, DataField f) {
		this.bundledFields.put(str, f);
	}
	
	public DataField getBundledField(String str) {
		return this.bundledFields.get(str);
	}
}
