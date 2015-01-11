package de.htw.ip;

import java.awt.Color;
import java.awt.Point;
import java.util.Iterator;
import java.util.Queue;
import java.util.Stack;
import java.util.Vector;


public class LabelingIP {
	public static final int LABEL_COuNT_OFFSET = 2;
	int countLabels;
	//--------------------Labeling----------------------------------------------------------
	/**
	 * 
	 * @param dstPixels
	 * @param width
	 * @param height
	 * @param style enum LabelingStyles {DEPTH_FIRST, BREADTH_FIRST}
	 */
	public void regionLabeling(int dstPixels[], int width, int height, int labelingStyle) {
		countLabels = LABEL_COuNT_OFFSET;
		for(int i=0; i<height; i++) {
			for(int j=0; j<width; j++) {
				int pixelIndex = (i*width) + j;
				if (dstPixels[pixelIndex]== 0xFF000000){
					switch (labelingStyle) {
					case 1:
						floodFillDeph(dstPixels, new Point(j, i),width, countLabels);
						countLabels++;
						break;
					case 2:
						floodFillBreadth(dstPixels, new Point(j, i),width, countLabels);
						countLabels++;
						break;
					}
				}
			}
		}
		colorLabel(dstPixels, countLabels-LABEL_COuNT_OFFSET);
		System.out.println(" Objekts: " + (countLabels-LABEL_COuNT_OFFSET ));
	}
	/**
	 * FloodFillDeph
	 * Iteratives Verfahren mit Stack (depth first).
	 * @param pixels
	 * @param width
	 * @param height
	 * @param countLabels System.out.println(" Seq. Objekts: " + countCleanedLabels);
	 */
	void floodFillDeph(int pixels[], Point p, int width, int countLabels) {
		Stack<Point> pointsStack = new Stack<Point>();
		pointsStack.push(p);

		while (!pointsStack.isEmpty()) {
			Point tmpPoint = pointsStack.pop();
			int pixelIndex = (tmpPoint.y*width) + tmpPoint.x;

			if (pixels[pixelIndex]==0xff000000 ) {
				pixels[pixelIndex] = countLabels ;
				pointsStack.push(new Point(tmpPoint.x+1	, tmpPoint.y	));
				pointsStack.push(new Point(tmpPoint.x	, tmpPoint.y+1	));
				pointsStack.push(new Point(tmpPoint.x	, tmpPoint.y-1	));
				pointsStack.push(new Point(tmpPoint.x-1	, tmpPoint.y	));
			}
//			Binarize.countMemmoryCosts = Binarize.countMemmoryCosts < pointsStack.size()?pointsStack.size():Binarize.countMemmoryCosts;
		}
	} 

	/**
	 * floodFillBreadth
	 * Iteratives Verfahren mit Queue (breadth first).
	 * @param pixels
	 */
	void floodFillBreadth(int pixels[], Point p, int width, int m) {

		Queue<Point> pointsQueue = new java.util.LinkedList<Point>();
		pointsQueue.offer(p);
		while (!pointsQueue.isEmpty()) {
			Point tmpPoint = pointsQueue.poll();
			int pixelIndex = (tmpPoint.y*width) + tmpPoint.x;
			if (pixels[pixelIndex]==0xff000000 ) {
				pixels[pixelIndex]=m ;
				pointsQueue.offer(new Point(tmpPoint.x+1	, tmpPoint.y	));
				pointsQueue.offer(new Point(tmpPoint.x	, tmpPoint.y+1	));
				pointsQueue.offer(new Point(tmpPoint.x	, tmpPoint.y-1	));
				pointsQueue.offer(new Point(tmpPoint.x-1	, tmpPoint.y	));
			}
//			Binarize.countMemmoryCosts = Binarize.countMemmoryCosts<pointsQueue.size()?pointsQueue.size():Binarize.countMemmoryCosts;
		}
	}

	/**
	 * SequentialLabeling
	 * @param pixels
	 * @param width
	 * @param height
	 */
	public void SequentialLabeling(int pixels[], int width, int height) {
		int m = LABEL_COuNT_OFFSET;
		int[] n = new int[4];
		Vector<Point> setCollision = new Vector<Point>();

		for (int y = 1; y < height; y++) {
			for (int x = 1; x < width; x++) {
				int index = (y*width)+x;
				if (pixels[index]==0xff000000 ){
					//all neigbor pixels backgrond |���
					n[0] = pixels[((y-1)*width)+x+1];
					n[1] = pixels[((y-1)*width)+x];
					n[2] = pixels[((y-1)*width)+x-1];
					n[3] = pixels[(y*width)+x-1];
					if (	n[0] == -1 && n[1] == -1 && 
							n[2] == -1 && n[3] == -1) {
						pixels[(y*width)+x]=m;
						m++;

					}else {
						int minLabel = Integer.MAX_VALUE;
						for (int i = 0; i < 4; i++) {
							int ni = n[i];
							if (ni >= 2 && ni < minLabel)
								minLabel = ni;
						}
						pixels[(y*width)+x] = minLabel;


						//Kollision regestrieren
						for (int i = 0; i < 4; i++) {
							int ni = n[i];
							int PixelVale = pixels[(y*width)+x] ;
							if (ni >= 2 && ni !=PixelVale ) {
								Point p = new Point(ni, PixelVale);
								if (!setCollision.contains(p))
									setCollision.add(p);
							}
						}
					}
				}
			}
		}
		Vector<Vector> tmpVector= resultCollision(setCollision, m);
		countLabels = RelabelImage(pixels, tmpVector);
		colorLabel(pixels, countLabels);
		System.out.println(" Seq. Objekts: " + countLabels);
	}

	/**
	 * RelabelImage
	 * @param pixels of dstImage
	 * @param Vector of Vector of relatet Labels
	 * @return amount of Labels
	 */
	private int RelabelImage( int pixels[], Vector<Vector> collisionOrderedCollection){

		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i]>0){
				for (Iterator iteratori  = collisionOrderedCollection.iterator(); iteratori.hasNext();) {
					Vector<Integer> tmpVector = (Vector<Integer>) iteratori.next();

					if (tmpVector.contains(pixels[i])){
						//+1 weil index 0 sein kann  �nd 0 belegt ist
						pixels[i]= collisionOrderedCollection.indexOf(tmpVector);
					}
				}
			}
		}
		//gibt die anzahl der labels zr�ck
		return collisionOrderedCollection.size();
	}


	private Vector<Vector> resultCollision(Vector<Point> collisionsVector, int labelCount) {
		int tmpIndexX=-1; 
		int tmpIndexY=-1; 
		Vector<Vector> collisionOrderedCollection = new Vector<Vector>(labelCount);
		//Vektor mit der menge an Vectoren wie labels mit der labelnmmer f�llen <3..m>
		for (int i = 2; i < labelCount; i++) {
			Vector<Integer> tmpVector = new Vector<Integer>();
			tmpVector.add(i);
			collisionOrderedCollection.addElement( tmpVector);
		}
		//Search for CollisioID in Vector
		for (Iterator iteratorPoint = collisionsVector.iterator(); iteratorPoint.hasNext();) {
			Point point = (Point) iteratorPoint.next();
			int x = point.x;
			int y = point.y;
			int tmpStackIndex = 0;

			for (int i = 0; i < collisionOrderedCollection.size(); i++) {
				Vector<Integer> tmpVector = collisionOrderedCollection.elementAt(i);
				for (Iterator iterator  = tmpVector.iterator(); iterator.hasNext();) {
					int collsionDigit = (Integer) iterator.next();
					if (collsionDigit == point.x) tmpIndexX= i ;
					else if (collsionDigit == point.y) tmpIndexY= i ;
				}
			}
			//when not same Container
			if (tmpIndexX!=tmpIndexY) {
				//Swap one into other
				collisionOrderedCollection.get(tmpIndexX).addAll(collisionOrderedCollection.get(tmpIndexY));
				//delete Vector after swap
				collisionOrderedCollection.remove(tmpIndexY);
			}
		}
		return collisionOrderedCollection;
	}

	void colorLabel(int pixels[], int labelCount) {
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i]>0) pixels[i]=Color.getHSBColor(pixels[i]/( labelCount*0.9f),  0.8f, 1).getRGB();
		}
	}
}
