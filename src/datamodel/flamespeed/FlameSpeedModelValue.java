package datamodel.flamespeed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import util.Tools;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import datamodel.ModelValue;



public class FlameSpeedModelValue extends ModelValue {
	
	public double value;

	public FlameSpeedModelValue(){
		type = ModelValue.FLAME_SPEED;

	}
	@Override
	public void setValue(File f) {
		read(f);

	}

	public void read(File f){
		List<String> lines = null;
		try {
			lines = FileUtils.readLines(f, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : lines) {
			Iterable<String> results = Splitter.on(CharMatcher.anyOf(","))
					.trimResults().omitEmptyStrings().split(line);
			String[] pieces = Iterables.toArray(results, String.class);
			if (line.startsWith("Flame_speed")) {
				value = Double.parseDouble(pieces[pieces.length-1]);
			}
			
		}

	}
	@Override
	public double getSSQValue() {
		return Math.pow(value, 2);
	}
}
