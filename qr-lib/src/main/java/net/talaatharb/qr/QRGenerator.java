package net.talaatharb.qr;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QRGenerator {

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
			var encodedData = encodeAlphanumeric(text);
			var dataBits = dataBits(encodedData);
			var dataBitsWithEC = addErrorCorrection(dataBits);
			var qrMatrix = placeDataInMatrix(dataBitsWithEC);
			return applyMask(qrMatrix);
		} else {
			throw new UnsupportedOperationException("Not valid input");
		}

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
				(row >= MATRIX_SIZE - 8 && row < MATRIX_SIZE && col >= 0 && col <= 7) ||
				(row == 6 && col >= 8 && col < MATRIX_SIZE - 8 ) ||
				(col == 6 && row >= 8 && row < MATRIX_SIZE - 8)
				);
	}

	static final int[][] applyMask(int maskPattern, int[][] qrMatrix) {
		for (int row = 0; row < MATRIX_SIZE; row++) {
			for (int col = 0; col < MATRIX_SIZE; col++) {
				if (isReservedArea(row, col)) {
                    continue;
                }

				// Apply the chosen mask pattern
				boolean shouldFlip = shouldFlipBit(maskPattern, row, col);

				// If the mask condition is met, flip the bit
				if (shouldFlip) {
					qrMatrix[row][col] = qrMatrix[row][col] == 1 ? 0 : 1; // Flip black to white and vice versa
				}
			}
		}
		return qrMatrix;
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
			shouldFlip = (((row + col) % 2) + ((row * col) % 3)) == 0;
			break;
		case 7:
			shouldFlip = (((row + col) % 3) + ((row * col) % 2)) == 0;
			break;
		default:
			break;
		}
		return shouldFlip;
	}

	static final int[][] applyMask(int[][] qrMatrix) {
		// TODO perform mask evaluation
		return applyMask(4, qrMatrix); 
	}

	static final int[][] placeDataInMatrix(byte[] finalData) {
		int dataIndex = 0; // Index for the current data bit
		int bitIndex = 7; // Start with the highest bit (byte) for finalData
		int[][] qrMatrix = new int[MATRIX_SIZE][MATRIX_SIZE];
		// Start from the bottom-right corner of the matrix
		for (int col = MATRIX_SIZE - 1; col >= 0; col -= 2) { // Iterate through columns (two at a time)
			for (int row = 0; row < MATRIX_SIZE; row++) {
				// For each column, we fill two rows at a time
				// Check if we are in a reserved area
				if (isReservedArea(row, col))
					continue; // Skip reserved areas

				// Place bit if there is still data
				if (dataIndex < finalData.length) {
					// Get the bit from the current byte
					int bit = (finalData[dataIndex] >> bitIndex) & 0x01;
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
				if (isReservedArea(row, col - 1))
					continue; // Skip reserved areas

				// Place bit if there is still data
				if (dataIndex < finalData.length) {
					// Get the bit from the current byte
					int bit = (finalData[dataIndex] >> bitIndex) & 0x01;
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
        // Fill finder patterns
        fillFinderPattern(0, 0, qrMatrix); // Top-left
        fillFinderPattern(0, MATRIX_SIZE - 7, qrMatrix); // Top-right
        fillFinderPattern(MATRIX_SIZE - 7, 0, qrMatrix); // Bottom-left

        // Fill timing patterns
        fillTimingPatterns(qrMatrix);
    }

    static final void fillFinderPattern(int startRow, int startCol, int[][] qrMatrix) {
    	// Add a white border around the 7x7 finder pattern (4-module-wide quiet zone)
        for (int row = -4; row < 11; row++) {
            for (int col = -4; col < 11; col++) {
                int r = startRow + row;
                int c = startCol + col;
                if (r >= 0 && r < MATRIX_SIZE && c >= 0 && c < MATRIX_SIZE) {
                    if (row >= 0 && row < 7 && col >= 0 && col < 7) {
                        // Inside the finder pattern
                        if ((row == 0 || row == 6 || col == 0 || col == 6) || (row >= 2 && row <= 4 && col >= 2 && col <= 4)) {
                            qrMatrix[r][c] = 1; // Black module
                        } else {
                            qrMatrix[r][c] = 0; // White module
                        }
                    } else {
                        // Outside the 7x7 area (quiet zone) -> White border
                        qrMatrix[r][c] = 0;
                    }
                }
            }
        }
        
    }

    static final void fillTimingPatterns(int[][] qrMatrix) {
    	for (int i = 8; i < MATRIX_SIZE - 8; i++) {
            qrMatrix[6][i] = (i % 2 == 0) ? 1 : 0; // Horizontal timing pattern
            qrMatrix[i][6] = (i % 2 == 0) ? 1 : 0; // Vertical timing pattern
        }
    }
}
