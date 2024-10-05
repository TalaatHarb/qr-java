package net.talaatharb.qr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class QRRenderer {

	private final boolean [][] qr;
	
	public void render(String path) {
		log.info("Rendering QR code to image: {}", path);
	}
}
