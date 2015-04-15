package com.github.ioannespaulus.mazerunner;

import java.awt.Dimension;
import java.awt.Point;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Stack;

import org.apache.log4j.Logger;

public class Solver {

	private int width;
	private int height;
	private Point startPos;
	private static MapExplorer explorer;
	private static boolean[][] wasHere; // this is needed for avoiding loops in the maze
	private static Stack<Point> solutionPath;
	private boolean exitFound;
	private static final Logger LOG = Logger.getLogger(Solver.class);
	
	public Solver(MapExplorer me) {
		exitFound = false;
		explorer = me;
		startPos = explorer.getStartPos();
		solutionPath = new Stack<Point>();
		if (startPos.x < 0)
			width = height = -1;
		else {
			Dimension dim = explorer.getDimensions();
			width = dim.width;
			height = dim.height;
		}
		if (width > 0)
			wasHere = new boolean[width][height];
	}

	public boolean solveMaze() {
		if (startPos.x < 0 || width < 0) {
			return false;
		}
		for (int row = 0; row < height; row++)
			for (int col = 0; col < width; col++){
				wasHere[col][row] = false;
			}
		wasHere[startPos.x][startPos.y] = true;
		solutionPath.push(startPos);
		if (startPos.x != 0) { // Checks if not on the western edge
			NeighbourType lookWest = explorer.lookAhead(startPos.x, startPos.y, Direction.WEST);
			if (lookWest == NeighbourType.WAY || lookWest == NeighbourType.EXIT) {
				if (lookWest == NeighbourType.EXIT) exitFound = true;
				if (recursiveSolve(startPos.x-1, startPos.y, Direction.EAST)) // Recalls method one square to the west
					return true;
			}
			if (lookWest == NeighbourType.UNKNOWN) {
				LOG.error("Invalid state!");
				solutionPath.pop();
				return false;
			}
		}
		if (startPos.y != 0) { // Checks if not on the northern edge
			NeighbourType lookNorth = explorer.lookAhead(startPos.x, startPos.y, Direction.NORTH);
			if (lookNorth == NeighbourType.WAY || lookNorth == NeighbourType.EXIT) {
				if (lookNorth == NeighbourType.EXIT) exitFound = true;
				if (recursiveSolve(startPos.x, startPos.y-1, Direction.SOUTH)) // Recalls method one square to the north
					return true;
			}
			if (lookNorth == NeighbourType.UNKNOWN) {
				LOG.error("Invalid state!");
				solutionPath.pop();
				return false;
			}
		}
		if (startPos.x != width - 1) { // Checks if not on the eastern edge
			NeighbourType lookEast = explorer.lookAhead(startPos.x, startPos.y, Direction.EAST);
			if (lookEast == NeighbourType.WAY || lookEast == NeighbourType.EXIT) {
				if (lookEast == NeighbourType.EXIT) exitFound = true;
				if (recursiveSolve(startPos.x+1, startPos.y, Direction.WEST)) // Recalls method one square to the east
					return true;
			}
			if (lookEast == NeighbourType.UNKNOWN) {
				LOG.error("Invalid state!");
				solutionPath.pop();
				return false;
			}
		}
		if (startPos.y != height- 1) { // Checks if not on the southern edge
			NeighbourType lookSouth = explorer.lookAhead(startPos.x, startPos.y, Direction.SOUTH);
			if (lookSouth == NeighbourType.WAY || lookSouth == NeighbourType.EXIT) {
				if (lookSouth == NeighbourType.EXIT) exitFound = true;
				if (recursiveSolve(startPos.x, startPos.y+1, Direction.NORTH)) // Recalls method one square to the south
					return true;
			}
			if (lookSouth == NeighbourType.UNKNOWN) {
				LOG.error("Invalid state!");
				solutionPath.pop();
				return false;
			}
		}
		solutionPath.pop();
		return false;
	}
	
	public boolean recursiveSolve(int x, int y, Direction cameFrom) {
		if ((x != explorer.getPos().x) && (y != explorer.getPos().y)) {
			LOG.error("Invalid state!");
			return false;
		}
		if (wasHere[x][y]) {
			LOG.info("Oops! Already been here! (" + x + ", " + y + ")");
			return false;
		}
		wasHere[x][y] = true;
		solutionPath.push(new Point(x, y));
		if (exitFound) return true; // If we've reached the exit
		if ((x != 0) && (cameFrom != Direction.WEST)) { // Checks if not on the western edge and we are not going backwards
			NeighbourType lookWest = explorer.lookAhead(x, y, Direction.WEST);
			if (lookWest == NeighbourType.WAY || lookWest == NeighbourType.EXIT) {
				if (lookWest == NeighbourType.EXIT) exitFound = true;
				if (recursiveSolve(x-1, y, Direction.EAST)) // Recalls method one square to the west
					return true;
			}
			if (lookWest == NeighbourType.UNKNOWN) {
				LOG.error("Invalid state!");
				solutionPath.pop();
				return false;
			}
		}
		if ((y != 0) && (cameFrom != Direction.NORTH)) { // Checks if not on the northern edge and we are not going backwards
			NeighbourType lookNorth = explorer.lookAhead(x, y, Direction.NORTH);
			if (lookNorth == NeighbourType.WAY || lookNorth == NeighbourType.EXIT) {
				if (lookNorth == NeighbourType.EXIT) exitFound = true;
				if (recursiveSolve(x, y-1, Direction.SOUTH)) // Recalls method one square to the north
					return true;
			}
			if (lookNorth == NeighbourType.UNKNOWN) {
				LOG.error("Invalid state!");
				solutionPath.pop();
				return false;
			}
		}
		if ((x != width - 1) && (cameFrom != Direction.EAST)) { // Checks if not on the eastern edge and we are not going backwards
			NeighbourType lookEast = explorer.lookAhead(x, y, Direction.EAST);
			if (lookEast == NeighbourType.WAY || lookEast == NeighbourType.EXIT) {
				if (lookEast == NeighbourType.EXIT) exitFound = true;
				if (recursiveSolve(x+1, y, Direction.WEST)) // Recalls method one square to the east
					return true;
			}
			if (lookEast == NeighbourType.UNKNOWN) {
				LOG.error("Invalid state!");
				solutionPath.pop();
				return false;
			}
		}
		if ((y != height- 1) && (cameFrom != Direction.SOUTH)) { // Checks if not on the southern edge and we are not going backwards
			NeighbourType lookSouth = explorer.lookAhead(x, y, Direction.SOUTH);
			if (lookSouth == NeighbourType.WAY || lookSouth == NeighbourType.EXIT) {
				if (lookSouth == NeighbourType.EXIT) exitFound = true;
				if (recursiveSolve(x, y+1, Direction.NORTH)) // Recalls method one square to the south
					return true;
			}
			if (lookSouth == NeighbourType.UNKNOWN) {
				LOG.error("Invalid state!");
				solutionPath.pop();
				return false;
			}
		}
		solutionPath.pop();
		return false;
	}
	
	public void printSolution(String code) {
		if (solutionPath.empty()) {
			System.out.println("Something went wrong! Maze solved, but solution path not found.");
		}
		String printString = "Solution path for " + code + ": ";
		for (Point p : solutionPath)
		{
			printString += "(" + p.x + ", " + p.y + ") - ";
		}
		printString = printString.substring(0, printString.length() - 3);
		System.out.println(printString);
	}
	
	public static void main(String[] args) {

		if (args.length < 2) {
			System.out.println("Usage: java Solver <maze_code> <service_url>");
			System.exit(0);
		}

		URI url;
		try {
			url = new URI(args[1]);
			MapExplorer explorer = new RESTMapExplorer(args[0], url);
			Solver solver = new Solver(explorer);
			if (solver.solveMaze())
				solver.printSolution(args[0]);
		} catch (URISyntaxException e) {
			System.out.println("<service_url> parameter incorrect");
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}

}
