package net.talaatharb.qr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class QRRenderer {

	private static final int MATRIX_SIZE = 21;
	private static final int DEFAULT_SCALE = 16;
	private final int[][] qr;

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

		try {
			saveQRCodeAsPNG(path, DEFAULT_SCALE, qr);
		} catch (IOException e) {
			log.error("Unable to save at location {}", path);
		}
		log.info(builder.toString());
	}

	static void saveQRCodeAsPNG(String filePath, int scale, int[][] qrMatrix) throws IOException {
		int paddedSize = MATRIX_SIZE + 2;
		int imageSize = paddedSize * scale;
		BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();

		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, imageSize, imageSize);

		for (int row = 0; row < MATRIX_SIZE; row++) {
			for (int col = 0; col < MATRIX_SIZE; col++) {
				if (qrMatrix[row][col] == 1) {
					g2d.setColor(Color.BLACK); // Black module
				} else {
					g2d.setColor(Color.WHITE); // White module
				}
				// Draw a filled rectangle for each module
				g2d.fillRect((col + 1) * scale, (row + 1) * scale, scale, scale);
			}
		}

		g2d.dispose(); // Clean up resources

		// Save the image as a PNG file
		File file = new File(filePath);
		ImageIO.write(image, "png", file);
	}
}
