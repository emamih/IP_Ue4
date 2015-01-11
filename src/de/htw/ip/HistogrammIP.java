package de.htw.ip;

public class HistogrammIP {
	//--------------------Histogramm/Iso----------------------------------------------------------
	/**
	 * Makes a Histogramm 0-255 and Calc the easy avarage
	 * @param pixels have SourcePixels
	 * @param histogram used a Histogramm with 0-255
	 */
	public int setAbsoluteHistogramm(int pixels[], int[] histogram) {
		java.util.Arrays.fill(histogram, 0);
		int avarage = 0;
		for(int i = 0; i < pixels.length; i++) {
			int gray = ((pixels[i] & 0xff) + ((pixels[i] & 0xff00) >> 8) + ((pixels[i] & 0xff0000) >> 16)) / 3;
			histogram[gray]++;
			avarage += gray;
		}
		return avarage/pixels.length;
	}

	public int isoData(int histogram[]) {
		int level;

		double result, sum1, sum2, sum3, sum4;
		int min = 0;
		int maxValue = histogram.length - 1;

		//Falls alle Werte nur �ber 128 liegt
		while ((histogram[min]==0) && (min<maxValue))
			min++;
		int max = maxValue;
		while ((histogram[max]==0) && (max>0))
			max--;
		if (min>=max) {
			level = histogram.length/2;
			return level;
		}

		int movingIndex = min;
		do {
			sum1=sum2=sum3=sum4=0.0;
			for (int i=min; i<=movingIndex; i++) {
				sum1 += i*histogram[i];
				sum2 += histogram[i];
			}
			for (int i=(movingIndex+1); i<=max; i++) {
				sum3 += i*histogram[i];
				sum4 += histogram[i];
			}			
			result = (sum1/sum2 + sum3/sum4)/2.0;
			movingIndex++;
		} while ((movingIndex+1)<=result && movingIndex<max-1);

		//.showProgress(1.0);
		level = (int)Math.round(result);
		return level;

	}
}
