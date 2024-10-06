package net.talaatharb.qr;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class QRGeneratorTest {

	@ParameterizedTest
	@CsvSource({ "A,0010000000001001010", "AB,001000000001000111001101", "ABC,001000000001100111001101001100",
			"HELLO WORLD,00100000010110110000101101111000110100010111001011011100010011010100001101" })
	void testEncodeAlphaNumeric(String text, String expected) {
		String encoded = QRGenerator.encodeAlphanumeric(text);

		assertEquals(expected, encoded);
	}

	@Test
	void testDataBitsConvertsCorrectly() {
		String encoded = "00100000010110110000101101111000110100010111001011011100010011010100001101"; // HELLO WORLD
		var dataBits = QRGenerator.dataBits(encoded);

		var expected = new byte[] { (byte) 32, (byte) 91, (byte) 11, (byte) 120, (byte) 209, (byte) 114, (byte) 220,
				(byte) 77, (byte) 67, (byte) 64 };

		assertArrayEquals(expected, dataBits);
	}

	@Test
	void testDataBitsPadding() {
		var dataBits = new byte[] { (byte) 32, (byte) 91, (byte) 11, (byte) 120, (byte) 209, (byte) 114, (byte) 220,
				(byte) 77, (byte) 67, (byte) 64 }; // HELLO WORLD

		var expected = new byte[] { (byte) 32, (byte) 91, (byte) 11, (byte) 120, (byte) 209, (byte) 114, (byte) 220,
				(byte) 77, (byte) 67, (byte) 64, (byte) 236, (byte) 17, (byte) 236, (byte) 17, (byte) 236, (byte) 17,
				(byte) 236, (byte) 17, (byte) 236 };

		var result = QRGenerator.padDataBits(dataBits, 74, QRGenerator.DATABITS_SIZE);

		assertArrayEquals(expected, result);
	}

	@Test
	void testMaskAvoidsReservedAreas() {
		int matrixSize = QRGenerator.MATRIX_SIZE;
		final int[][] qrMatrix = new int[matrixSize][matrixSize];
		for (int i = 0; i < 8; i++) {
			var appliedMask = QRGenerator.applyMask(i, qrMatrix, QRGenerator::isReservedArea);

			for (int row = 0; row < matrixSize; row++) {
				for (int col = 0; col < matrixSize; col++) {
					if (QRGenerator.isReservedArea(row, col)) {
						assertEquals(qrMatrix[row][col], appliedMask[row][col]);
					}
				}
			}
		}
	}

	@Test
	void testDataCalculation() {
		String text = "HELLO WORLD";
		var dataBitsWithEC = QRGenerator.calculateFinalData(text);

		byte[] expected = new byte[] { (byte) 32, (byte) 91, (byte) 11, (byte) 120, (byte) 209, (byte) 114, (byte) 220,
				(byte) 77, (byte) 67, (byte) 64, (byte) 236, (byte) 17, (byte) 236, (byte) 17, (byte) 236, (byte) 17,
				(byte) 236, (byte) 17, (byte) 236, (byte) 209, (byte) 239, (byte) 196, (byte) 207, (byte) 78,
				(byte) 195, (byte) 109 }; // HELLO WORLD + padding + EC

		assertArrayEquals(expected, dataBitsWithEC);
	}
}
