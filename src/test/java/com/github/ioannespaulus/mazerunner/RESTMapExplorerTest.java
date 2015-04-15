package com.github.ioannespaulus.mazerunner;

import java.awt.Dimension;
import java.awt.Point;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RESTMapExplorerTest {

	private static MapExplorer explorer;
	private Point startPos;

	@Before
	public void setUp() {
		try {
			explorer = new RESTMapExplorer("maze-1", new URI("http://localhost:8080/"));
		} catch (URISyntaxException e) {
		}
		startPos = explorer.getStartPos();
	}

	@Test
	public void startPosTest() {
		Assert.assertTrue((startPos.x < 0 && startPos.y < 0) || (startPos.x >= 0 && startPos.y >= 0));
	}
	
	@Test
	public void dimTest() {
		Dimension dim = explorer.getDimensions();
		Assert.assertTrue((dim.width < 0 && dim.height < 0) || (dim.width > 0 && dim.height > 0));
	}
	
	@Test
	public void lookAheadTest() {
		NeighbourType lookWest = explorer.lookAhead(startPos.x, startPos.y, Direction.WEST);
		Assert.assertNotEquals(NeighbourType.START, lookWest);
		Assert.assertTrue(startPos.x < 0 || lookWest != NeighbourType.UNKNOWN);
		NeighbourType lookNorth = explorer.lookAhead(startPos.x, startPos.y, Direction.NORTH);
		Assert.assertNotEquals(NeighbourType.START, lookNorth);
		Assert.assertTrue(startPos.x < 0 || lookNorth != NeighbourType.UNKNOWN);
		NeighbourType lookEast = explorer.lookAhead(startPos.x, startPos.y, Direction.EAST);
		Assert.assertNotEquals(NeighbourType.START, lookEast);
		Assert.assertTrue(startPos.x < 0 || lookEast != NeighbourType.UNKNOWN);
		NeighbourType lookSouth = explorer.lookAhead(startPos.x, startPos.y, Direction.SOUTH);
		Assert.assertNotEquals(NeighbourType.START, lookSouth);
		Assert.assertTrue(startPos.x < 0 || lookSouth != NeighbourType.UNKNOWN);
	}
}
