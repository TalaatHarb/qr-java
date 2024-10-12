package net.talaatharb.qr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class QRFormatBitsTest {

	@ParameterizedTest
    @CsvSource({
        "0,0,111011111000100", // L, mask 0
        "0,1,111001011110011", // L, mask 1
        "0,2,111110110101010", // L, mask 2
        "0,3,111100010011101", // L, mask 3
        "0,4,110011000101111", // L, mask 4
        "0,5,110001100011000", // L, mask 5
        "0,6,110110001000001", // L, mask 6
        "0,7,110100101110110"  // L, mask 7
    })
    void testCalculateFormatBitsForErrorCorrectionL(int ecLevel, int maskPattern, String expectedFormatBits) {
        assertEquals(Integer.parseInt(expectedFormatBits, 2), QRGenerator.calculateFormatBits(ecLevel, maskPattern));
    }

    @ParameterizedTest
    @CsvSource({
        "1,0,101010000010010", // M, mask 0
        "1,1,101000100100101", // M, mask 1
        "1,2,101111001111100", // M, mask 2
        "1,3,101101101001011", // M, mask 3
        "1,4,100010111111001", // M, mask 4
        "1,5,100000011001110", // M, mask 5
        "1,6,100111110010111", // M, mask 6
        "1,7,100101010100000"  // M, mask 7
    })
    void testCalculateFormatBitsForErrorCorrectionM(int ecLevel, int maskPattern, String expectedFormatBits) {
        assertEquals(Integer.parseInt(expectedFormatBits, 2), QRGenerator.calculateFormatBits(ecLevel, maskPattern));
    }

    @ParameterizedTest
    @CsvSource({
        "2,0,011010101011111", // Q, mask 0
        "2,1,011000001101000", // Q, mask 1
        "2,2,011111100110001", // Q, mask 2
        "2,3,011101000000110", // Q, mask 3
        "2,4,010010010110100", // Q, mask 4
        "2,5,010000110000011", // Q, mask 5
        "2,6,010111011011010", // Q, mask 6
        "2,7,010101111101101"  // Q, mask 7
    })
    void testCalculateFormatBitsForErrorCorrectionQ(int ecLevel, int maskPattern, String expectedFormatBits) {
        assertEquals(Integer.parseInt(expectedFormatBits, 2), QRGenerator.calculateFormatBits(ecLevel, maskPattern));
    }

    @ParameterizedTest
    @CsvSource({
        "3,0,001011010001001", // H, mask 0
        "3,1,001001110111110", // H, mask 1
        "3,2,001110011100111", // H, mask 2
        "3,3,001100111010000", // H, mask 3
        "3,4,000011101100010", // H, mask 4
        "3,5,000001001010101", // H, mask 5
        "3,6,000110100001100", // H, mask 6
        "3,7,000100000111011"  // H, mask 7
    })
    void testCalculateFormatBitsForErrorCorrectionH(int ecLevel, int maskPattern, String expectedFormatBits) {
        assertEquals(Integer.parseInt(expectedFormatBits, 2), QRGenerator.calculateFormatBits(ecLevel, maskPattern));
    }

    @ParameterizedTest
    @CsvSource({
        "4,0", // Invalid error correction level
        "-1,0" // Invalid error correction level
    })
    void testInvalidErrorCorrectionLevel(int ecLevel, int maskPattern) {
        assertThrows(IllegalArgumentException.class, () -> QRGenerator.calculateFormatBits(ecLevel, maskPattern));
    }
}
