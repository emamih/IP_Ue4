package de.htw.ip;
import java.util.Arrays;

/**
 * class für Vektoren mit n-dime mit Typ double .
 */
public class VektorIP {

	double[] v = null;

	/**
	 * constrctor für einen n-dim Vektor-->  Null-Vektor.
	 * @param n Dimension > 1
	 * @throws Exception 
	 */
	public VektorIP(int n) throws Exception {
		if(n>0)
			this.v = new double[n];
		else 
			throw new Exception("DDim ist kleiner als 1");
	}
	/**
	 * constrctor für einen 2-dim Vektor
	 * @param n Dimension > 1
	 * @throws Exception 
	 */
	public VektorIP(double x, double y) throws Exception {
		if(x>=0 || x<v.length || y>=0 || y<v.length){
			this.v = new double[2];
			this.v[0] = x;
			this.v[1] = y;
		}

	}


	/**
	 * Gibt die Komponente i aus.
	 * @param i
	 * @return
	 * @throws Exception
	 */
	public double getValue(int i) throws Exception {
		if(i>=0 || i<v.length)
			return v[i];
		else 
			throw new Exception("dieser index nicht erlabt" );
	}

	/**
	 * Überschreibt den Wert der Komponente i.
	 * @param i
	 * @param vi
	 * @throws Exception
	 */
	public void setValue(int i, double vi) throws Exception {
		if(i>=0 || i<v.length)
			this.v[i] = vi;
		else 
			throw new Exception("dieser index nicht erlabt" );
	}


	public void setValue2D(double vj, double vi) throws Exception {
		if(v.length==2){
			this.v[0] = vj;
			this.v[1] = vi;
		}else 
			throw new Exception("dieser index nicht erlabt" );
	}

	/**
	 * Gibt die Dim aus.
	 * @return
	 */
	public int getDimension() {
		return this.v.length;
	}

	/**
	 * Addiert zum Vektor  b daz (== Dimension!).
	 * @param b 
	 * @return
	 * @throws Exception 
	 */
	public VektorIP add(VektorIP b) throws Exception {
		if(this.v.length==b.v.length) {
			VektorIP res = new VektorIP(v.length);
			for(int i=0; i<v.length; i++) {
				res.v[i] = this.v[i] + b.v[i];
			}
			return res;
		}
		else 
			throw new Exception("Dim müssen gleich sein");
	}

	/**
	 * Subtrahiert vom Vektor  b (== Dimension!).
	 * @param b 
	 * @return
	 * @throws Exception 
	 */
	public VektorIP sub(VektorIP b) throws Exception {
		if(this.v.length==b.v.length) {
			VektorIP res = new VektorIP(v.length);
			for(int i=0; i<v.length; i++) {
				res.v[i] = this.v[i] - b.v[i];
			}
			return res;
		}
		else 
			throw new Exception("Dim müssen gleich sein.");
	}

	/**
	 * Multipliziert den Vektor skalaren Wert b.
	 * @param b skalarer Wert 
	 * @return
	 * @throws Exception 
	 */
	public VektorIP mul(double b) throws Exception {
		VektorIP res = new VektorIP(v.length);
		for(int i=0; i<v.length; i++) {
			res.v[i] *=b;
		}
		return res;
	}

	/**
	 * Dividiert den Vektor this durch den skalaren Wert b.
	 * @param b skalarer Wert 
	 * @return
	 * @throws Exception 
	 */
	public VektorIP div(double b) throws Exception {
		VektorIP res = new VektorIP(v.length);
		for(int i=0; i<v.length; i++) {
			res.v[i] /= b;
		}
		return res;
	}

	/**
	 * Multipliziert/skalarpro Vektor mit Vektor b skalar (gleiche Dimension!).
	 * @param b zweiter Vektor 
	 * @return
	 * @throws Exception 
	 */
	public double multiScalar(VektorIP b) throws Exception {
		if(this.v.length==b.v.length) {
			double res = 0;
			for(int i=0; i<v.length; i++) {
				res +=this.v[i] * b.v[i];
			}
			return res;
		}
		else 
			throw new Exception("Dim müssen gleich sein");
	}

	/**
	 * Berechnet die Länge d. V.
	 * @return
	 * @throws Exception
	 */
	public double getLength() throws Exception {
		return Math.sqrt(this.multiScalar(this)); 
	}

	/**
	 * normalisierten Vektor (Vektor gleicher Richtung mit Länge 1.0).
	 * @return
	 * @throws Exception
	 */
	public VektorIP getNormalized() throws Exception {
		return this.div(this.getLength());
	}


	/**
	 * Kreuzprodukt für 2-dim. Vektoren.
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public double xmul2D(VektorIP b) throws Exception {
		if(this.v.length==2 && b.v.length==2) {
			double res =  this.v[0]*b.v[1] - this.v[1]*b.v[0];
			return res;
		}
		else 
			throw new Exception("Dim müssen sich gleichen");
	}

	/**
	 * Berechnet den senkr. V. (gleiche Länge) auf einen 2-dim. V.
	 * @return
	 * @throws Exception
	 */
	public VektorIP ortho2D() throws Exception {
		if(this.v.length==2) {
			VektorIP res = new VektorIP(2);
			res.v[0] = -this.v[1];
			res.v[1] =  this.v[0];
			return res;
		}
		else 
			throw new Exception("Dim müssen sich gleichen");
	}

	public String toString() {
		return Arrays.toString(this.v);
	}


}	
