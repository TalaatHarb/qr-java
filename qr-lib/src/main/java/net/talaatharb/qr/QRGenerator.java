package net.talaatharb.qr;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QRGenerator {

	static final int DATABITS_SIZE = 19;
	static final String MODE_INDICATOR = "0010"; // Alphanumeric mode
	static final Integer CODE_WORD_COUNT = 7; // L
	static final int MATRIX_SIZE = 21; // 21x21 for Version 1

	private static final String ALPHANUMERIC_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:";
	private static final Map<Character, Integer> charToValueMap = new HashMap<>();

	static {
		// Populate the map with character values for alphanumeric encoding
		for (int i = 0; i < ALPHANUMERIC_CHARSET.length(); i++) {
			charToValueMap.put(ALPHANUMERIC_CHARSET.charAt(i), i);
		}
	}

	public static final int[][] generate(String text) {
		log.info("Generating QR for the text: {}", text);
		if (isValidInput(text)) {
			var dataBitsWithEC = calculateFinalData(text);
			var qrMatrix = placeDataInMatrix(dataBitsWithEC);
			return applyMask(qrMatrix);
		} else {
			throw new UnsupportedOperationException("Not valid input");
		}

	}

	static byte[] calculateFinalData(String text) {
		var encodedData = encodeAlphanumeric(text);
		var dataBits = dataBits(encodedData);
		var paddedDataBits = padDataBits(dataBits, encodedData.length(), DATABITS_SIZE);
		var dataBitsWithEC = addErrorCorrection(paddedDataBits);
		return dataBitsWithEC;
	}

	static final boolean isValidInput(String text) {
		return text != null && text.length() < 25 && text.length() > 0;
	}

	static final String encodeAlphanumeric(String text) {
		StringBuilder encoded = new StringBuilder();

		// Start with mode indicator for alphanumeric (0010)
		encoded.append(MODE_INDICATOR);

		// Append character count indicator (10 bits for character count)
		int length = text.length();
		String input = text.toUpperCase();
		String charCountIndicator = String.format("%09d", Integer.parseInt(Integer.toBinaryString(length)));
		encoded.append(charCountIndicator);

		// Encode the data
		for (int i = 0; i < length; i += 2) {
			if (i + 1 < length) {
				// Two characters to encode
				int value = (charToValueMap.get(input.charAt(i)) * 45) + charToValueMap.get(input.charAt(i + 1));
				String encodedValue = String.format("%011d", Long.parseLong(Integer.toBinaryString(value)));
				encoded.append(encodedValue);
			} else {
				// Last character (odd case)
				int value = charToValueMap.get(input.charAt(i));
				String encodedValue = String.format("%06d", Integer.parseInt(Integer.toBinaryString(value)));
				encoded.append(encodedValue);
			}
		}

		return encoded.toString();
	}

	static final byte[] dataBits(String encodedData) {
		int length = encodedData.length();
		byte[] bits = new byte[(length + 7) / 8]; // 8 bits per byte

		for (int i = 0; i < length; i++) {
			// Set each bit in the byte array
			if (encodedData.charAt(i) == '1') {
				bits[i / 8] |= (1 << (7 - (i % 8))); // Set the appropriate bit
			}
		}

		return bits;
	}

	static final byte[] generateErrorCorrectionCodewords(byte[] data, int numCodewords) {
		return ReedSolomon.generateErrorCorrectionCodewords(data, numCodewords);
	}

	static final byte[] padDataBits(byte[] dataBits, int currentDataBitsLength, int maxDataBytes) {
		int totalDataBitsRequired = maxDataBytes * 8;

		// Step 1: Add a 4-bit terminator (0000)
		int remainingBits = totalDataBitsRequired - currentDataBitsLength;
		if (remainingBits > 0) {
			// Add a 4-bit terminator if space allows
			if (remainingBits >= 4) {
				// Add the 4-bit terminator (0000)
				dataBits = appendBits(dataBits, currentDataBitsLength, 0, 4);
				currentDataBitsLength += 4;
				remainingBits -= 4;
			} else {
				// Add fewer bits if space is less than 4
				dataBits = appendBits(dataBits, 0, currentDataBitsLength, remainingBits);
				currentDataBitsLength += remainingBits;
				remainingBits = 0;
			}
		}

		// Step 2: Pad to the next byte boundary with zeros if necessary
		int bitRemainder = currentDataBitsLength % 8;
		if (bitRemainder > 0 && remainingBits > 0) {
			int bitsToNextByte = 8 - bitRemainder;
			dataBits = appendBits(dataBits, currentDataBitsLength, 0, Math.min(bitsToNextByte, remainingBits));
		}

		// Step 3: Alternate between padding bytes 11101100 (0xEC) and 00010001 (0x11)
		boolean toggle = true;
		while (dataBits.length < maxDataBytes) {
			if (toggle) {
				dataBits = appendByte(dataBits, 0xEC); // 11101100
			} else {
				dataBits = appendByte(dataBits, 0x11); // 00010001
			}
			toggle = !toggle;
		}

		return dataBits;
	}

	// Helper to append bits
	private static byte[] appendBits(byte[] data, int currentLengthInBits, int value, int numBits) {
		int newLengthInBits = currentLengthInBits + numBits; // New total length after appending

		// Calculate the number of bytes required to hold the new data
		int newLengthInBytes = (newLengthInBits + 7) / 8; // Round up to the nearest byte

		// Create a new byte array with the new size
		byte[] newData = new byte[newLengthInBytes];

		// Copy the old data into the new array
		System.arraycopy(data, 0, newData, 0, data.length);

		// Start appending bits from the `value`
		for (int i = 0; i < numBits; i++) {
			int bitPosition = currentLengthInBits + i;
			int byteIndex = bitPosition / 8;
			int bitIndex = 7 - (bitPosition % 8); // Most significant bit first (big-endian)

			// Extract the bit from `value` (starting from the most significant bit)
			int bit = (value >> (numBits - 1 - i)) & 1;

			// Set the bit in the byte array
			newData[byteIndex] |= (bit << bitIndex);
		}

		return newData;
	}

	// Helper to append a byte to the data
	private static byte[] appendByte(byte[] data, int value) {
		// The `value` must fit into a byte (8 bits)
		if (value < 0 || value > 255) {
			throw new IllegalArgumentException("Value must be between 0 and 255");
		}

		// Append the byte using `appendBits`, since a byte is just 8 bits
		return appendBits(data, data.length * 8, value, 8);
	}

	static final byte[] addErrorCorrection(byte[] dataBits) {
		// Generate error correction codewords
		byte[] ecCodewords = generateErrorCorrectionCodewords(dataBits, CODE_WORD_COUNT);

		// Combine data bits and error correction codewords
		byte[] combined = new byte[dataBits.length + ecCodewords.length];
		System.arraycopy(dataBits, 0, combined, 0, dataBits.length);
		System.arraycopy(ecCodewords, 0, combined, dataBits.length, ecCodewords.length);

		return combined;
	}

	static final boolean isReservedArea(int row, int col) {
		return (
				(row >= 0 && row <= 7 && col >= 0 && col <= 7) || // Top-left
				(row >= 0 && row <= 7 && col >= MATRIX_SIZE - 8) || // Top-right
				(row >= MATRIX_SIZE - 8 && row < MATRIX_SIZE && col >= 0 && col <= 7) || // Bottom-left
				(row == 6 && col >= 8 && col < MATRIX_SIZE - 8) || // Horizontal timing
				(col == 6 && row >= 8 && row < MATRIX_SIZE - 8) || // Vertical timing
				(row == 8 && (col <= 7 || col >= 12)) || // Horizontal format info
				(col == 8 && (row <= 7 || row >= 12)) // Vertical format info
		);
	}

	static final int[][] applyMask(int maskPattern, int[][] qrMatrix, BiPredicate<Integer, Integer> isReserved) {
		int matrixSize = qrMatrix.length;
		var appliedMask = new int[matrixSize][matrixSize];
		for (int row = 0; row < matrixSize; row++) {
			for (int col = 0; col < matrixSize; col++) {
				
				if (isReserved.test(row, col)) {
					appliedMask[row][col] = qrMatrix[row][col];
					continue;
				}

				if (shouldFlipBit(maskPattern, row, col)) {
					appliedMask[row][col] = qrMatrix[row][col] == 1 ? 0 : 1;
				}
			}
		}
		
		return appliedMask;
	}

	// Apply the format information (error correction and mask info) to the reserved
	// areas
	static final void addErrorCorrectionAndMaskInfo(int errorCorrectionLevel, int maskPattern, int[][] qrMatrix) {
		int formatBits = calculateFormatBits(errorCorrectionLevel, maskPattern);

		// Place format bits near the top-left finder pattern
		for (int i = 0; i < 6; i++) {
			qrMatrix[i][8] = (formatBits >> i) & 1; // Vertical
			qrMatrix[8][i] = (formatBits >> i) & 1; // Horizontal
		}

		qrMatrix[7][8] = (formatBits >> 6) & 1; // In the "dark module" position
		qrMatrix[8][7] = (formatBits >> 7) & 1;

		// Top-right format information
		for (int i = 0; i < 7; i++) {
			qrMatrix[8][MATRIX_SIZE - 1 - i] = (formatBits >> (8 + i)) & 1; // Horizontal part
		}

		// Bottom-left format information
		for (int i = 0; i < 7; i++) {
			qrMatrix[MATRIX_SIZE - 1 - i][8] = (formatBits >> (8 + i)) & 1; // Vertical part
		}
	}

	// This method calculates the format bits for error correction and mask pattern
	static final int calculateFormatBits(int errorCorrectionLevel, int maskPattern) {
		// Error correction levels: L = 01, M = 00, Q = 11, H = 10
		int ecBits;
		switch (errorCorrectionLevel) {
		case 0:
			ecBits = 0b01;
			break; // L
		case 1:
			ecBits = 0b00;
			break; // M
		case 2:
			ecBits = 0b11;
			break; // Q
		case 3:
			ecBits = 0b10;
			break; // H
		default:
			throw new IllegalArgumentException("Invalid error correction level");
		}

		// Combine the error correction bits and mask pattern
		int formatBits = (ecBits << 3) | maskPattern;

		// Apply error correction to the format bits (BCH(15, 5))
		return applyBCHCorrection(formatBits);
	}

	static final int applyBCHCorrection(int formatBits) {
		// The generator polynomial for the format info error correction (BCH code)
		int generator = 0b10100110111;

		// Left-shift formatBits to make space for the 10-bit BCH code
		int formatBitsShifted = formatBits << 10;

		// Perform the division to calculate the BCH code
		for (int i = 14; i >= 10; i--) {
			if (((formatBitsShifted >> i) & 1) == 1) {
				formatBitsShifted ^= (generator << (i - 10));
			}
		}

		// Combine the format bits with the calculated BCH code
		return (formatBits << 10) | formatBitsShifted;
	}

	static final boolean shouldFlipBit(int maskPattern, int row, int col) {
		boolean shouldFlip = false;

		switch (maskPattern) {
		case 0:
			shouldFlip = (row + col) % 2 == 0;
			break;
		case 1:
			shouldFlip = row % 2 == 0;
			break;
		case 2:
			shouldFlip = col % 3 == 0;
			break;
		case 3:
			shouldFlip = (row + col) % 3 == 0;
			break;
		case 4:
			shouldFlip = (row / 2 + col / 3) % 2 == 0;
			break;
		case 5:
			shouldFlip = ((row * col) % 2 + (row * col) % 3) == 0;
			break;
		case 6:
			shouldFlip = (((row * col) % 2) + ((row * col) % 3)) % 2 == 0;
			break;
		case 7:
			shouldFlip = (((row + col) % 2) + ((row * col) % 3)) % 2 == 0;
			break;
		default:
			break;
		}
		return shouldFlip;
	}

	static final int[][] applyMask(int[][] qrMatrix) {
		// TODO perform mask evaluation
		int maskPattern = 7;
		
		int[][] appliedMask = applyMask(maskPattern, qrMatrix, QRGenerator::isReservedArea);
		addErrorCorrectionAndMaskInfo(0, maskPattern, appliedMask);
		
		return appliedMask;
	}

	static final int[][] placeDataInMatrix(byte[] finalData) {
		int dataIndex = 0; // Index for the current data bit
		int bitIndex = 7; // Start with the highest bit (byte) for finalData
		int finalDataLength = finalData.length;
		int[][] qrMatrix = new int[MATRIX_SIZE][MATRIX_SIZE];
		// Start from the bottom-right corner of the matrix
		for (int col = MATRIX_SIZE - 1; col >= 0; col -= 2) { // Iterate through columns (two at a time)
			for (int row = 0; row < MATRIX_SIZE; row++) {
				// For each column, we fill two rows at a time
				// Check if we are in a reserved area
				if (isReservedArea(row, col))
					continue; // Skip reserved areas

				// Place bit if there is still data
				if (dataIndex < finalDataLength) {
					// Get the bit from the current byte
					byte currentByte = finalData[dataIndex];
					int bit = (currentByte >> bitIndex) & 0x01;
					qrMatrix[row][col] = bit; // Place the bit in the matrix
					if (bitIndex == 0) {
						bitIndex = 7; // Move to the next byte
						dataIndex++; // Move to the next data byte
					} else {
						bitIndex--; // Move to the next bit in the byte
					}
				}
			}

			// Change the direction for the next column
			for (int row = MATRIX_SIZE - 1; row >= 0; row--) {
				// Check if we are in a reserved area
				if (isReservedArea(row, col - 1) || col == 0)
					continue; // Skip reserved areas

				// Place bit if there is still data
				if (dataIndex < finalDataLength) {
					// Get the bit from the current byte
					byte currentByte2 = finalData[dataIndex];
					int bit = (currentByte2 >> bitIndex) & 0x01;
					qrMatrix[row][col - 1] = bit; // Place the bit in the matrix
					if (bitIndex == 0) {
						bitIndex = 7; // Move to the next byte
						dataIndex++; // Move to the next data byte
					} else {
						bitIndex--; // Move to the next bit in the byte
					}
				}
			}
		}
		fillReservedAreas(qrMatrix);
		return qrMatrix;
	}

	static final void fillReservedAreas(int[][] qrMatrix) {
		fillFinderPattern(0, 0, qrMatrix); // Top-left
		fillFinderPattern(0, MATRIX_SIZE - 7, qrMatrix); // Top-right
		fillFinderPattern(MATRIX_SIZE - 7, 0, qrMatrix); // Bottom-left

		fillTimingPatterns(qrMatrix);
	}

	static final void fillFinderPattern(int startRow, int startCol, int[][] qrMatrix) {
		// Add a white border around the 7x7 finder pattern (4-module-wide quiet zone)
		for (int row = -4; row < 11; row++) {
			for (int col = -4; col < 11; col++) {
				int r = startRow + row;
				int c = startCol + col;
				if (isInBounds(r, c)) {
					if (isPartOfFinderPattern(row, col)) {
						qrMatrix[r][c] = isInsideFinderPattern(row, col) ? 1 : 0;
					} else {
						// Outside the 7x7 area (quiet zone) -> White border
						qrMatrix[r][c] = 0;
					}
				}
			}
		}

	}

	static boolean isPartOfFinderPattern(int row, int col) {
		return row >= 0 && row < 7 && col >= 0 && col < 7;
	}

	static boolean isInBounds(int r, int c) {
		return r >= 0 && r < MATRIX_SIZE && c >= 0 && c < MATRIX_SIZE;
	}

	static boolean isInsideFinderPattern(int row, int col) {
		return (row == 0 || row == 6 || col == 0 || col == 6) || (row >= 2 && row <= 4 && col >= 2 && col <= 4);
	}

	static final void fillTimingPatterns(int[][] qrMatrix) {
		for (int i = 8; i < MATRIX_SIZE - 8; i++) {
			qrMatrix[6][i] = (i % 2 == 0) ? 1 : 0; // Horizontal timing pattern
			qrMatrix[i][6] = (i % 2 == 0) ? 1 : 0; // Vertical timing pattern
		}
	}
}
