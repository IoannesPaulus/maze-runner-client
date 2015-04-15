package com.github.ioannespaulus.mazerunner;

import java.awt.Dimension;
import java.awt.Point;

public interface MapExplorer {

	public abstract Point getPos();
	
	public abstract Point getStartPos();
	
	public abstract Dimension getDimensions();
	
	public abstract NeighbourType lookAhead(int posX, int posY, Direction d);
}
