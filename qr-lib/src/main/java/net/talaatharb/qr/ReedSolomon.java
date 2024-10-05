package net.talaatharb.qr;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReedSolomon {

	private static final int GF_SIZE = 256; // Size of GF(2^8)
	private static final int[] EXP_TABLE = new int[GF_SIZE * 2];
	private static final int[] LOG_TABLE = new int[GF_SIZE];

	static {
		// Generate the exp and log tables for GF(2^8)
		int x = 1;
		for (int i = 0; i < GF_SIZE; i++) {
			EXP_TABLE[i] = x;
			LOG_TABLE[x] = i;
			x <<= 1;
			if (x >= GF_SIZE)
				x ^= 0x11D; // x^8 + x^4 + x^3 + x + 1
		}
		for (int i = GF_SIZE; i < EXP_TABLE.length; i++) {
			EXP_TABLE[i] = EXP_TABLE[i - GF_SIZE];
		}
	}

	static final int gfAdd(int a, int b) {
		return a ^ b; // Addition in GF(2^8)
	}

	static final int gfMultiply(int a, int b) {
		if (a == 0 || b == 0)
			return 0;
		return EXP_TABLE[(LOG_TABLE[a] + LOG_TABLE[b]) % (GF_SIZE - 1)];
	}

	public static byte[] generateErrorCorrectionCodewords(byte[] data, int numCodewords) {
		int dataLength = data.length;
		int totalLength = dataLength + numCodewords;

		int[] generator = calculateGenerator(numCodewords);

		int[] message = createIntMessage(data, totalLength);

		for (int i = 0; i < dataLength; i++) {
			int coefficient = message[i];
			if (coefficient != 0) {
				for (int j = 0; j < generator.length; j++) {
					message[i + j] ^= gfMultiply(coefficient, generator[j]);
				}
			}
		}

		byte[] ecCodewords = new byte[numCodewords];
		for (int i = 0; i < numCodewords; i++) {
			ecCodewords[i] = (byte) message[dataLength + i];
		}

		return ecCodewords;
	}

	static final int[] createIntMessage(byte[] data, int totalLength) {
		int dataLength = data.length;
		int[] message = new int[totalLength];
		for (int i = 0; i < dataLength; i++) {
			message[i] = data[i] & 0xFF; // Convert to integer
		}
		return message;
	}

	static final int[] calculateGenerator(int numCodewords) {
		int[] generator = new int[numCodewords + 1];
		generator[0] = 1; // Initial generator polynomial
		for (int i = 0; i < numCodewords; i++) {
			int x = EXP_TABLE[i]; // Galois field element
			for (int j = i; j >= 0; j--) {
				generator[j + 1] = gfAdd(generator[j + 1], gfMultiply(generator[j], x));
			}
		}
		return generator;
	}
}
