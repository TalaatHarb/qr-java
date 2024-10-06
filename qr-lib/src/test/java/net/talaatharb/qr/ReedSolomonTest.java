package net.talaatharb.qr;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

class ReedSolomonTest {

	private static final Random RANDOM = new Random();

	@Test
	void testAdditionIsXOR() {
		int a = Integer.parseInt("00110011", 2); // 51
		int b = Integer.parseInt("11100011", 2); // -29
		int c = Integer.parseInt("11010000", 2);

		assertEquals(c, ReedSolomon.gfAdd(a, b));
	}

	@Test
	void testMultiplicationByZero() {
		int a = RANDOM.nextInt(127);
		int b = 0;

		assertEquals(0, ReedSolomon.gfMultiply(a, b));
	}

	@Test
	void testMultiplicationCommutative() {
		int a = RANDOM.nextInt(127);
		int b = RANDOM.nextInt(127);

		assertEquals(ReedSolomon.gfMultiply(b, a), ReedSolomon.gfMultiply(a, b));
	}

	@Test
	void testCreateIntMessage() {
		byte[] data = new byte[] { (byte) Integer.parseInt("00110011", 2), (byte) Integer.parseInt("11100011", 2) };

		int totalLength = 5;

		var message = ReedSolomon.createIntMessage(data, totalLength);

		// resulting message is the right length
		assertEquals(totalLength, message.length);

		// First elements of the message are exactly the data
		assertEquals(data[0], (byte) message[0]);
		assertEquals(data[1], (byte) message[1]);

		// Last elements are zero padding
		assertEquals(0, message[2]);
		assertEquals(0, message[3]);
		assertEquals(0, message[4]);
	}

	@Test
	void testCreateGenerators() {
		Integer codeWordCount = QRGenerator.CODE_WORD_COUNT;
		int[] generator = ReedSolomon.calculateGenerator(codeWordCount);
		assertEquals(codeWordCount + 1, generator.length);
		assertArrayEquals(new int[] { 1, 127, 122, 154, 164, 11, 68, 117 }, generator);
	}

	@Test
	void testGenerateErrorCorrectionCodewords() {
		Integer codeWordCount = QRGenerator.CODE_WORD_COUNT;

		byte[] data = new byte[] { (byte) 32, (byte) 91, (byte) 11, (byte) 120, (byte) 209, (byte) 114, (byte) 220,
				(byte) 77, (byte) 67, (byte) 64, (byte) 236, (byte) 17, (byte) 236, (byte) 17, (byte) 236, (byte) 17,
				(byte) 236, (byte) 17, (byte) 236 }; // HELLO WORLD + padding
		var errorCorrectionCodeWords = ReedSolomon.generateErrorCorrectionCodewords(data, codeWordCount);

		assertArrayEquals(new byte[] {(byte) 209, (byte) 239, (byte) 196, (byte) 207, (byte) 78,
				(byte) 195, (byte) 109 }, errorCorrectionCodeWords);
	}

}
