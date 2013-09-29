package com.starmapper.android.grid;

import com.starmapper.android.constants.ArrayConstants;
import com.starmapper.android.math.Geocentric;
import com.starmapper.android.math.RaDec;
import com.starmapper.android.grid.GridLine;

import java.util.ArrayList;

public class Grid implements ArrayConstants {

	private final ArrayList<GridLine> RaGridLines;
	private final ArrayList<GridLine> DecGridLines;
	
	// Constructor
	public Grid() {
		RaGridLines = new ArrayList<GridLine>();
		DecGridLines = new ArrayList<GridLine>();		
	}
	
	public void buildGrid(int numRaLines, int numDecLines) {
		for (int ra = 0; ra < numRaLines; ra++) {
			RaGridLines.add(newRaLine(ra, numRaLines));
		}
		for (int dec = 0; dec < numDecLines; dec++) {
			DecGridLines.add(newDecLine(dec, numDecLines));
		}
	}
	
	private GridLine newRaLine(int idx, int numRaLines) {
		GridLine line = new GridLine();
		float ra = idx * 360.0f / numRaLines;
		for (int x = 0; x < RA_VERTICES; x++) {
			float dec = 90.0f - 90.0f * x;
			RaDec raDec = new RaDec(ra, dec);
			line.raDec_vertices.add(raDec);
			line.vertices.add(new Geocentric(raDec));
		}
		return line;
	}
	
	private GridLine newDecLine(int idx, int numDecLines) {
		GridLine line = new GridLine();
		float dec = 90.0f - (idx + 1.0f) * 180.0f / (numDecLines + 1.0f);
		for (int x = 0; x < DEC_VERTICES; x++) {
			float ra = x * 360.0f / DEC_VERTICES;
			RaDec raDec = new RaDec(ra, dec);
			line.raDec_vertices.add(raDec);
			line.vertices.add(new Geocentric(raDec));
		}
		// last vertex to connect the circle
		float ra = 0.0f;
		RaDec raDec = new RaDec(ra, dec);
		line.raDec_vertices.add(raDec);
		line.vertices.add(new Geocentric(raDec));
		
		return line;
	}
	
	public ArrayList<GridLine> getRALines() {
		return RaGridLines;
	}
	public ArrayList<GridLine> getDecLines() {
		return DecGridLines;
	}
	public int getNumRALines() {
		return RaGridLines.size();
	}
	public int getNumDecLines() {
		return DecGridLines.size();
	}
}
