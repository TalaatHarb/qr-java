package net.talaatharb.qr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class QRRenderer {

	private static final int MATRIX_SIZE = 21;
	private final int [][] qr;
	
	public void render(String path) {
		log.info("Rendering QR code to image: {}", path);
		StringBuilder builder = new StringBuilder(System.lineSeparator());
		builder.append(System.lineSeparator());
		for (int row = 0; row < MATRIX_SIZE; row++) {
			for (int col = 0; col < MATRIX_SIZE; col++) {
				builder.append(qr[row][col] == 1 ? "█" : " "); // Print black and white squares
			}
			builder.append(System.lineSeparator());
		}
		log.info(builder.toString());
	}
}
