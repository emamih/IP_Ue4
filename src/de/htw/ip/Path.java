package de.htw.ip;
import java.util.Vector;


public class Path  extends Vector<Edge>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean isInner = false;
	 
	 public Path(){
		 
	 }
	 public Path(boolean isInner){
		 this.isInner = isInner;
	 }

	 public boolean isInner() {
	  return isInner;
	 }

	 public void setInner(boolean isInner) {
	  this.isInner = isInner;
	 }
	 
	 public void addEdge(Edge edge) {
		 this.add(edge);
	 }
}
