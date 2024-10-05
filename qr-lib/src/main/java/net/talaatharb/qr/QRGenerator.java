package net.talaatharb.qr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class QRGenerator {
	
	private final String text;
	
	public boolean[][] generate(){
		log.info("Generating QR for the text: {}", text);
		return new boolean[][] {};
	}

}
