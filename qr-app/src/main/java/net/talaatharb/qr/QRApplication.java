package net.talaatharb.qr;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QRApplication {
	
	private static final double NANO_TO_S = 1000000000.0;

	public static void main(String[] args) {
		String text = "Hello, World!";
		
		long startTime = System.nanoTime();
		
		QRGenerator generator = new QRGenerator(text);
		var generatedQR = generator.generate();
		
		double period = (System.nanoTime() - startTime) / NANO_TO_S;
		
		log.info("Generation complete");
		log.info("It took {} seconds", period);
		
		startTime = System.nanoTime();
		
		QRRenderer renderer = new QRRenderer(generatedQR);
		renderer.render("./tmp/QR.png");
		
		period = (System.nanoTime() - startTime) / NANO_TO_S;
		
		log.info("Rendering complete");
		log.info("It took {} seconds", period);
	}
}
