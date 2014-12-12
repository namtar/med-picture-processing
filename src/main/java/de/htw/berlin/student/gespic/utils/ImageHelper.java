package de.htw.berlin.student.gespic.utils;

/**
 * Helper class that provides subsidiary methods.
 * <p/>
 * Created by matthias.drummer on 05.11.14.
 */
public final class ImageHelper {

	private ImageHelper() {
	}

	public static boolean canAccess(int choordX, int choordY, int matrixX, int matrixY, int imageWidth, int imageHeight) {

		// translate matrixX
		int matrixXTrans = matrixX - 1;
		int matrixYTrans = matrixY - 1;

		// translate matrixY
		if ((choordX + matrixXTrans) < 0 || (choordX + matrixXTrans) > (imageWidth - 1)) {
			return false;
		}
		if ((choordY + matrixYTrans) < 0 || (choordY + matrixYTrans) > (imageHeight - 1)) {
			return false;
		}

		return true;
	}

	public static int normalize(int value) {
		if (value > 255) {
			value = 255;
		} else if (value < 0) {
			value = 0;
		}
		return value;
	}

	public static int doEndian(byte value) {
		return value & 0xff;
	}
}
