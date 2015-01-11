package de.htw.ip;

public class Edge {
	private int direction;
	private int startPosition;
	private int endPosition;
	

	
	public Edge(int startPosition,int endPosition, int direction){
		setStartPosition(startPosition);
		setEndPosition(endPosition);
		this.setDirection(direction);
	}

	public int getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}

	public int getEndPosition() {
		return endPosition;
	}

	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}
/**
 * 
 * @return int 0 = down, 1 = left, 2 =right, 3 = up
 */
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	

}
