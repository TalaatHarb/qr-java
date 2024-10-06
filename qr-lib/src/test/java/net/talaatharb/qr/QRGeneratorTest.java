package net.talaatharb.qr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class QRGeneratorTest {

	@ParameterizedTest
	@CsvSource({
		"A,0010000000001001010",
		"AB,001000000001000111001101",
		"ABC,001000000001100111001101001100"
		})
	void testEncodeAlphaNumeric(String text, String expected) {
		String encoded = QRGenerator.encodeAlphanumeric(text);
		
		assertEquals(expected, encoded);
	}
}
