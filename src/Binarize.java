// Copyright (C) 2014 by Klaus Jung
// All rights reserved.
// Date: 2014-10-02

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;

import de.htw.ip.Edge;
import de.htw.ip.HistogrammIP;
import de.htw.ip.LabelingIP;
import de.htw.ip.Path;
import de.htw.ip.VektorIP;


public class Binarize extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int border = 10;
	private static final int maxWidth = 400;
	private static final int maxHeight = 400;
	private static final File openPath = new File(".");
	private static final String title = "Binarisierung";
	private static final String author = "Hady Emami";
	private static final String initalOpen = "w2.png";

	private static final int FPS_MIN = 0;
	private static final int FPS_MAX = 255;
	private static final int FPS_INIT = 0;

	static final int LABEL_COuNT_OFFSET = 2;

	private static JFrame frame;

	private ImageView srcView;				// source image view
	private ImageView dstView;				// binarized image view

	private JComboBox<String> methodList;	// the selected binarization method

	private JSlider trashholdSlider ;
	private JSlider zoomSlider ;
	private JCheckBox outline;
	private JCheckBox contureChk;
	private JCheckBox polyChk;
	private int histogramGray[] ;
	private JLabel statusLine;				// to print some status text
	private int trashold;
	static int countMemmoryCosts = 0;
	static int countLabels = 0;
	static int BACKGROUND = -1;
	private LabelingIP imglabeling;
	private HistogrammIP hisCalc;

	//--------------------system----------------------------------------------------------
	public Binarize() {
		super(new BorderLayout(border, border));

		imglabeling = new LabelingIP();
		hisCalc =new HistogrammIP();
		// load the default image
		File input = new File(initalOpen);

		if(!input.canRead()) input = openFile(); // file not found, choose another image

		srcView = new ImageView(input);
		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));

		// create an empty destination image
		dstView = new ImageView(srcView.getImgWidth(), srcView.getImgHeight());
		dstView.setMaxSize(new Dimension(maxWidth, maxHeight));


		// load image button
		JButton load = new JButton("Bild oeffnen");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File input = openFile();
				if(input != null) {
					srcView.loadImage(input);
					srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
					PreCalc();
					binarizeImage();
				}
			}        	
		});

		zoomSlider = new JSlider(JSlider.HORIZONTAL,1, 20, 5);   
		zoomSlider.setMajorTickSpacing(5);                                       
		zoomSlider.setMinorTickSpacing(1);                                        
		zoomSlider.setPaintTicks(true);                                            
		zoomSlider.setPaintLabels(true);                                            
		zoomSlider.addChangeListener(new ChangeListener() {  
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider) e.getSource();
				dstView.doZoom(slider.getValue());
				srcView.doZoom(slider.getValue());
				binarizeImage();
			}
		});

		trashholdSlider = new JSlider(JSlider.HORIZONTAL,FPS_MIN, FPS_MAX, FPS_INIT);
		trashholdSlider.setMajorTickSpacing(100);
		trashholdSlider.setMinorTickSpacing(25);
		trashholdSlider.setPaintTicks(true);
		trashholdSlider.setPaintLabels(true);
		trashholdSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider) e.getSource();
				trashold =slider.getValue();
				binarizeImage();
			}
		});

		// Outline

		outline = new JCheckBox("outline");
		outline.setMnemonic(KeyEvent.VK_G); 
		outline.setSelected(false);
		outline.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				binarizeImage();
			}
		});

		// contureChk
		contureChk = new JCheckBox("cont");
		contureChk.setMnemonic(KeyEvent.VK_G); 
		contureChk.setSelected(false);

		contureChk.addItemListener(
				// using anonymous class without a name
				new java.awt.event.ItemListener(){
					// method executes when status of the check box changes:
					public void itemStateChanged( java.awt.event.ItemEvent evt ) {
						JCheckBox source = ( JCheckBox ) evt.getItemSelectable();

						if ( source == contureChk ) {
							System.out.println( "!Contre" + source.isSelected() );
							dstView.setConturesVisible(contureChk.isSelected());
							dstView.repaint();
						}
					}//itemStateChanged
				}//anonymous class
				);

		polyChk = new JCheckBox("Path");
		polyChk.setMnemonic(KeyEvent.VK_G); 
		polyChk.setSelected(false);
		polyChk.addItemListener(
				// using anonymous class without a name
				new java.awt.event.ItemListener()
				{
					// method executes when status of the check box changes:
					public void itemStateChanged( java.awt.event.ItemEvent evt ) {
						JCheckBox source = ( JCheckBox ) evt.getItemSelectable();

						if ( source == polyChk ) {
							System.out.println( "!poly" + source.isSelected() );
							dstView.setPolyVisible(polyChk.isSelected());
							dstView.repaint();
						}
					}//itemStateChanged
				}//anonymous class
				);
		// selector for the binarization method
		JLabel methodText = new JLabel("Methode:");
		String[] methodNames = {"Schwellwert 50% ","Schwellwert Manuel","Schwellwert Histogram", "Schwellwert Iso-Data","depth first","breadth first","sequentiellen"};

		methodList = new JComboBox<String>(methodNames);
		methodList.setSelectedIndex(0);		// set initial method
		methodList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				binarizeImage();
			}
		});

		// some status text
		statusLine = new JLabel(" ");

		// arrange all controls
		JPanel controls = new JPanel(new GridBagLayout());
		GridLayout c = new GridLayout();
		controls.setLayout(c);
		controls.add(load);
		controls.add(methodText);
		controls.add(methodList);
		controls.add(trashholdSlider);
		controls.add(zoomSlider);
		controls.add(outline);
		controls.add(contureChk);
		controls.add(polyChk);
		JPanel images = new JPanel(new GridLayout());
		images.add(srcView);
		images.add(dstView);


		add(controls, BorderLayout.NORTH);
		add(images, BorderLayout.CENTER);
		add(statusLine, BorderLayout.SOUTH);

		setBorder(BorderFactory.createEmptyBorder(border,border,border,border));

		// perform the initial binarization
		PreCalc();
		binarizeImage();
	}
	@SuppressWarnings("unchecked")
	protected void PreCalc() {
		//preCallation
				Vector<Path> tmpCont = getContours(srcView.getPixels(), srcView.getImgWidth());
				dstView.setConturePaths(tmpCont);	
				try {
					Vector<Path> tmpPoly = getPolygons(tmpCont,srcView.getImgWidth());
					dstView.setPolyPaths(tmpPoly);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}System.out.println("preeeeeeecalc");
	}
	private File openFile() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(openPath);
		int ret = chooser.showOpenDialog(this);
		if(ret == JFileChooser.APPROVE_OPTION) {
			frame.setTitle(title + chooser.getSelectedFile().getName());
			return chooser.getSelectedFile();
		}
		return null;		
	}

	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame(title + " - " + author + " - " + initalOpen);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		JComponent newContentPane = new Binarize();
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);

		//		frame.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		frame.setSize(800,600);
		frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	protected void binarizeImage() {

		String methodName = (String)methodList.getSelectedItem();

		//Allocate the size of the array
		int width = srcView.getImgWidth();
		int height = srcView.getImgHeight();
		// get pixels arrays
		int srcPixels[] = srcView.getPixels();
		int dstPixels[] = java.util.Arrays.copyOf(srcPixels, srcPixels.length);
		histogramGray= new int [256];
		dstView.setZoom(zoomSlider.getValue());
		srcView.setZoom(zoomSlider.getValue());

		String message = "Binarisieren mit \"" + methodName + "\"";

		statusLine.setText(message);

		long startTime = System.currentTimeMillis();

		switch(methodList.getSelectedIndex()) {
		//Todouranus.f4.htw-berlin.deb
		case 0:	// 50% Schwellwert
			trashholdSlider.setValue(128);
			trashholdSlider.setEnabled(false);
			binarize(dstPixels);
			if (outline.isSelected()) createOutline( dstPixels);
			break;
		case 1:	//Schwellwert
			trashholdSlider.setEnabled(true);
			binarize(dstPixels);
			if (outline.isSelected()) createOutline( dstPixels);
			break;
		case 2:	// Histogramm
			//set Histogramm and calc Avarage of this
			trashold = hisCalc.setAbsoluteHistogramm(srcPixels, histogramGray);
			binarize(dstPixels);
			if (outline.isSelected()) createOutline( dstPixels);
			trashholdSlider.setEnabled(false);
			trashholdSlider.setValue(trashold);
			break;
		case 3:	// ISO-Data-Algorithmus
			trashold = hisCalc.setAbsoluteHistogramm(srcPixels, histogramGray);
			trashholdSlider.setEnabled(false);
			trashold = hisCalc.isoData(histogramGray);
			trashholdSlider.setValue(trashold);
			binarize(dstPixels);
			if (outline.isSelected()) createOutline( dstPixels);
			break;
		case 4:	// "depth first",
			trashholdSlider.setEnabled(true);
			binarize(dstPixels);
			imglabeling.regionLabeling(dstPixels, width, height, 1);
			System.out.println("DEPH ArrySize " + countMemmoryCosts);
			message = " DephAlg : Objects " + (countLabels-imglabeling.LABEL_COuNT_OFFSET ) + " COST: " + countMemmoryCosts ;
			break;
		case 5:	// ,"breadth first"
			trashholdSlider.setEnabled(true);
			binarize(dstPixels);
			imglabeling.regionLabeling(dstPixels, width, height, 2);
			System.out.println("breadth ArrySize " + countMemmoryCosts);
			message = "BreadthAlg : Objects " + (countLabels-LABEL_COuNT_OFFSET )+ " Cost: " + countMemmoryCosts ;
			break;
		case 6:	// "sequentiel"
			trashold =  hisCalc.setAbsoluteHistogramm(srcPixels, histogramGray);
			trashholdSlider.setEnabled(false);
			trashold = hisCalc.isoData(histogramGray);
			trashholdSlider.setValue(trashold);
			binarize(dstPixels);
			imglabeling.SequentialLabeling(dstPixels, width, height);
			message = "sequentiel : Objects " + (countLabels) + " Cost: " + countMemmoryCosts ;
			break;
		}
		long time = System.currentTimeMillis() - startTime;

		dstView.setPixels(dstPixels, width, height);
		srcView.setPixels(srcPixels, width, height);

		//dstView.saveImage("out.png");

		//		frame.pack();

		statusLine.setText(message + " in " + time + " ms" + " Trashold: " + trashold + " MaxArraySize: " + countMemmoryCosts);
	}

	// -----------------------------Potrace 1. Step: Contres-----------------------------------------------------------
	private Path findContur(int[] srcPixels, int[] dstPixels, int startPixelPosition, int width){

		Path path = new Path();
		//is innner outline
		if(srcPixels[startPixelPosition-1]!=0xFF000000){
			path.setInner(true);
		}

		// 0 = runter, 1 = links, 2 =rechts, 3 = hoch
		int direction = 0;
		int currentPixel = startPixelPosition;
		do {
			switch (direction) {
			//runter
			case 0:
				if (dstPixels[currentPixel-1]==0xFF000000){
					direction = 1;
					Edge e = new Edge (currentPixel, currentPixel-1, direction);
					path.add(e);
					currentPixel = currentPixel-1;
				} else if (dstPixels[currentPixel]==0xFF000000){
					Edge e = new Edge (currentPixel, currentPixel+width, direction);
					path.add(e);
					currentPixel = currentPixel+width;
				} else {
					direction = 2;
					Edge e = new Edge (currentPixel, currentPixel+1, direction);
					path.add(e);
					currentPixel = currentPixel+1;
				}
				break;
				//links
			case 1:
				if (dstPixels[currentPixel-width-1]==0xFF000000){
					direction = 3;
					Edge e = new Edge (currentPixel, currentPixel-width, direction);
					path.add(e);
					currentPixel = currentPixel-width;
				} else if (dstPixels[currentPixel-1]==0xFF000000){
					Edge e = new Edge (currentPixel, currentPixel-1, direction);
					path.add(e);
					currentPixel = currentPixel-1;
				} else {
					direction = 0;
					Edge e = new Edge (currentPixel, currentPixel+width, direction);
					path.add(e);
					currentPixel = currentPixel+width;
				}
				break;
				//rechts
			case 2:
				if (dstPixels[currentPixel]==0xFF000000){
					direction = 0;
					Edge e = new Edge (currentPixel, currentPixel+width, direction);
					path.add(e);
					currentPixel = currentPixel+width;
				} else if (dstPixels[currentPixel-width]==0xFF000000){
					Edge e = new Edge (currentPixel, currentPixel+1, direction);
					path.add(e);
					currentPixel = currentPixel+1;
				} else {
					direction = 3;
					Edge e = new Edge (currentPixel, currentPixel-width, direction);
					path.add(e);
					currentPixel = currentPixel-width;
				}
				break;
				//hoch
			case 3:
				if (dstPixels[currentPixel-width]==0xFF000000){
					direction = 2;
					Edge e = new Edge (currentPixel, currentPixel+1, direction);
					path.add(e);
					currentPixel = currentPixel+1;
				} else if (dstPixels[currentPixel-width-1]==0xFF000000){
					Edge e = new Edge (currentPixel, currentPixel-width, direction);
					path.add(e);
					currentPixel = currentPixel-width;
				} else {
					direction = 1;
					Edge e = new Edge (currentPixel, currentPixel-1, direction);
					path.add(e);
					currentPixel = currentPixel-1;
				}
				break;

			default:
				break;
			}

		} while (startPixelPosition != currentPixel);

		//invert inner pixels;
		for (Iterator<Edge> iterator = path.iterator(); iterator.hasNext();) {
			Edge edge = iterator.next();
			if(edge.getDirection()==0){
				int i = edge.getStartPosition();
				for(int j = i; j <= i+(width-(i%width)); j++){
					if (dstPixels[j]==0xFF000000){
						dstPixels[j] = 0xFFFFFFFF;
					} else{
						dstPixels[j] = 0xFF000000;
					}
				}
			} else if(edge.getDirection()==3){
				int i = edge.getEndPosition();
				for(int j = i; j <= i+(width-(i%width)); j++){
					if (dstPixels[j]==0xFF000000){
						dstPixels[j] = 0xFFFFFFFF;
					} else{
						dstPixels[j] = 0xFF000000;
					}
				}
			}
		}

		return path;
	}

	public Vector<Path> getContours(int[] srcPixels , int width) {
		int[] tempPixels = Arrays.copyOf(srcPixels, srcPixels.length);
		Vector<Path> paths = new Vector<Path>();

		for (int i = 0; i < tempPixels.length; i++){
			if((tempPixels[i])==0xFF000000){
				paths.add(findContur(srcPixels, tempPixels, i, width));
			}
		}
		return paths;
	}

	// -----------------------------Potrace 2. Step: Polygone-----------------------------------------------------------
	/**
	 * 
	 * @param paths   0 = down, 1 = left, 2 =right, 3 = up
	 * @throws Exception 
	 */
	private Vector<Path> getPolygons(Vector<Path> conturePaths, int width) throws Exception{
		Vector<Path> polyPaths = new Vector<Path>();
		for (Path conturePath : conturePaths) {
			Path streightPath  = getStreightPath(conturePath, width);
			polyPaths.add(streightPath);
		}
		return polyPaths;	
	}
	
	private Path getStreightPath(Path conturePath, int width) throws Exception{
		int[] straightPath = new int[conturePath.size()];
		boolean[] directions = new boolean[4];


		//		for (int i = 0; i < conturePath.size(); i++) {
		for (Edge startEdge : conturePath) {
			Point startPoint = new Point((startEdge.getStartPosition())%width, startEdge.getStartPosition()/width);

			int tmpI=0;
			Edge nextEdge  = conturePath.get(conturePath.indexOf(startEdge)+tmpI);
			Point nextPoint =new Point((nextEdge.getStartPosition())%width, nextEdge.getStartPosition()/width);

			VektorIP startVector = new VektorIP(startPoint.getX(), startPoint.getY());
			VektorIP nextVector = new VektorIP(nextPoint.getX(), nextPoint.getY());
			VektorIP vector = startVector.sub(nextVector);
			VektorIP constrain1 = new VektorIP(0,0);
			VektorIP constrain2 = new VektorIP(0,0);

			Arrays.fill(directions, Boolean.FALSE);
			directions[startEdge.getDirection()]=true;

			while (conturePath.indexOf(startEdge)+tmpI < conturePath.size()){
				//direktion check 
				if (directions[0] &&  directions[1] &&  directions[2] && directions[3]) break;

				//conbstrains
				if (constrain1.xmul2D(vector) < 0) break;
				if (constrain2.xmul2D(vector) > 0) break;
				nextEdge = conturePath.get(conturePath.indexOf(startEdge)+tmpI);
				nextPoint.setLocation((nextEdge.getStartPosition())%width, nextEdge.getStartPosition()/width);
				nextVector.setValue2D(nextPoint.getX(), nextPoint.getY());
				vector = startVector.sub(nextVector);
				updateConstrains(vector,constrain1,constrain2);		
				tmpI++;
			}
			straightPath[conturePath.indexOf(startEdge)] = conturePath.indexOf(nextEdge)-2;
//			straightPath[conturePath.indexOf(startEdge)]=(conturePath.indexOf(nextEdge)-1) % (conturePath.size()-2);
		}
		return getOptimalPolygonPath(straightPath, conturePath);
	}

	private Path getOptimalPolygonPath(int[] straightPath, Path conturePath) {
		Vector<Path> possiblePaths = new Vector<Path>();
		//TODO alle pfade auslesen
//		for (int startPos = 0; startPos < straightPath.length; startPos++) {
			Path tmpPath = new Path();
			int tmpStartPos = 0;//startPos
			int tmpNextPos = 0;
			do {
				tmpNextPos = straightPath[tmpStartPos]%(straightPath.length-2);
				Edge edge =new Edge(conturePath.get(tmpStartPos).getStartPosition(), 
									conturePath.get(tmpNextPos).getStartPosition(), 0);
				tmpPath.add(edge);
				tmpStartPos = tmpNextPos;
			} while ( (tmpNextPos) % ((straightPath.length-3)) != 0);
			
			possiblePaths.add(tmpPath);
//			TODO nach schrittanzahl, hier länge und Penalty bewerten
//		}
		return possiblePaths.get(0);
	}
	private void updateConstrains (VektorIP vector, VektorIP constrain1, VektorIP constrain2) throws Exception{

		VektorIP tmpVector= new VektorIP(0,0);
		//constrain üpdate?
		if (!(Math.abs(vector.getValue(0))<=1 && Math.abs(vector.getValue(1))<=1)){
			//constrain 1 üpdate!!
			if (vector.getValue(1)>=0 && (vector.getValue(1)>0 || vector.getValue(0)<0)){ 
				tmpVector.setValue(0, vector.getValue(0)+1);
			} else tmpVector.setValue(0, vector.getValue(0)-1);
			if (vector.getValue(0)<=0 && (vector.getValue(0)<0 || vector.getValue(1)<0)){ 
				tmpVector.setValue(1, vector.getValue(1)+1);
			} else tmpVector.setValue(1, vector.getValue(1)-1);
			//zweisen
			if (constrain1.xmul2D(tmpVector)>=0) {
				constrain1.setValue(0, tmpVector.getValue(0));
				constrain1.setValue(1, tmpVector.getValue(1));
			}

			//constrain 2 üpdate!!
			tmpVector= new VektorIP(0,0);
			if (vector.getValue(1)<=0 && (vector.getValue(1)<0 || vector.getValue(0)<0)){ 
				tmpVector.setValue(0, vector.getValue(0)+1);
			} else tmpVector.setValue(0, vector.getValue(0)-1);

			if (vector.getValue(0)>=0 && (vector.getValue(0)>0 || vector.getValue(1)<0)){ 
				tmpVector.setValue(1, vector.getValue(1)+1);
			} else tmpVector.setValue(1, vector.getValue(1)-1);

			if (constrain2.xmul2D(tmpVector)<=0) {
				constrain2.setValue(0, tmpVector.getValue(0));
				constrain2.setValue(1, tmpVector.getValue(1));
			} 
		}			
	}
	
	int[] erodePixels(int[] srcPixels) {
		int pixelsLength = srcPixels.length;

		int width = srcView.getImgWidth();
		int height = srcView.getImgHeight();

		int newPixel[] = new int[pixelsLength];

		for(int i=1; i<height-1; i++) {
			for(int j=1; j<width-1; j++) {
				//positionen fï¿½r kernal
				int pixel = (i*width) + j;
				int topPixel = pixel-width;
				int rightPixel = pixel+1;
				int bottomPixel = pixel+width;
				int leftPixel = pixel-1;
				//grauwert fï¿½r position des Kernel ermitteln
				int top 	= ((srcPixels[topPixel] & 0xff) + ((srcPixels[topPixel] & 0xff00) >> 8) + ((srcPixels[topPixel] & 0xff0000) >> 16)) / 3;
				int right 	= ((srcPixels[rightPixel] & 0xff) + ((srcPixels[rightPixel] & 0xff00) >> 8) + ((srcPixels[rightPixel] & 0xff0000) >> 16)) / 3;
				int bottom 	= ((srcPixels[bottomPixel] & 0xff) + ((srcPixels[bottomPixel] & 0xff00) >> 8) + ((srcPixels[bottomPixel] & 0xff0000) >> 16)) / 3;
				int left 	= ((srcPixels[leftPixel] & 0xff) + ((srcPixels[leftPixel] & 0xff00) >> 8) + ((srcPixels[leftPixel] & 0xff0000) >> 16)) / 3;
				int mid 	= ((srcPixels[pixel] & 0xff) + ((srcPixels[pixel] & 0xff00) >> 8) + ((srcPixels[pixel] & 0xff0000) >> 16)) / 3;

				newPixel[pixel] = (top == bottom && bottom == right && right == left && left == mid && mid == 0) ? 0xff000000 : 0xffffffff;
			}
		}
		return newPixel;
	}

	void createOutline(int[] dstPixels) {
		int erodePixels[] = erodePixels(dstPixels);
		for(int i=0; i<dstPixels.length; i++) {

			int originalPixel = ((dstPixels[i] & 0xff) + ((dstPixels[i] & 0xff00) >> 8) + ((dstPixels[i] & 0xff0000) >> 16)) / 3;
			int erodePixel = ((erodePixels[i] & 0xff) + ((erodePixels[i] & 0xff00) >> 8) + ((erodePixels[i] & 0xff0000) >> 16)) / 3;
			dstPixels[i] = (originalPixel == 0 && originalPixel != erodePixel) ? 0xffFF0000 : 0x0fffffff;
		}
	}

	void binarize(int pixels[]) {
		for(int i = 0; i < pixels.length; i++) {
			int gray = ((pixels[i] & 0xff) + ((pixels[i] & 0xff00) >> 8) + ((pixels[i] & 0xff0000) >> 16)) / 3;
			pixels[i] = gray < trashold ? 0xff000000 : 0xffffffff;
		}
	}
}

