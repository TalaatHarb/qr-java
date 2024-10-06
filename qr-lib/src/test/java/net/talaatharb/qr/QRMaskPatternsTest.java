package net.talaatharb.qr;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class QRMaskPatternsTest {

    private static final int[][] QR_MATRIX = new int[6][6];

    @Test
    void testMaskPattern0() {
        // Mask pattern 0 (col + row) % 2 == 0
        int maskPattern = 0;
        int[][] expectedMatrix = {
            {1, 0, 1, 0, 1, 0},
            {0, 1, 0, 1, 0, 1}, 
            {1, 0, 1, 0, 1, 0}, 
            {0, 1, 0, 1, 0, 1}, 
            {1, 0, 1, 0, 1, 0},
            {0, 1, 0, 1, 0, 1}
        };

        int[][] maskedMatrix = QRGenerator.applyMask(maskPattern, QR_MATRIX, (r, c) -> false);
        assertArrayEquals(expectedMatrix, maskedMatrix, "Mask Pattern 0 failed");
    }

    @Test
    void testMaskPattern1() {
        // Mask pattern 1 (row % 2) == 0
        int maskPattern = 1;
        int[][] expectedMatrix = {
        		{1, 1, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0}, 
                {1, 1, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0},
                {1, 1, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0}
        };

        int[][] maskedMatrix = QRGenerator.applyMask(maskPattern, QR_MATRIX, (r, c) -> false);
        assertArrayEquals(expectedMatrix, maskedMatrix, "Mask Pattern 1 failed");
    }

    @Test
    void testMaskPattern2() {
        // Mask pattern 2 (col % 3) == 0
        int maskPattern = 2;
        int[][] expectedMatrix = {
        	{1, 0, 0, 1, 0, 0},
        	{1, 0, 0, 1, 0, 0},
        	{1, 0, 0, 1, 0, 0},
        	{1, 0, 0, 1, 0, 0},
        	{1, 0, 0, 1, 0, 0},
        	{1, 0, 0, 1, 0, 0},
        };

        int[][] maskedMatrix = QRGenerator.applyMask(maskPattern, QR_MATRIX, (r, c) -> false);
        assertArrayEquals(expectedMatrix, maskedMatrix, "Mask Pattern 2 failed");
    }

    @Test
    void testMaskPattern3() {
        // Mask pattern 3 (row + col) % 3 == 0
        int maskPattern = 3;
        int[][] expectedMatrix = {
        	{1, 0, 0, 1, 0, 0},
            {0, 0, 1, 0, 0, 1},
            {0, 1, 0, 0, 1, 0},
            {1, 0, 0, 1, 0, 0},
            {0, 0, 1, 0, 0, 1},
            {0, 1, 0, 0, 1, 0},
        };

        int[][] maskedMatrix = QRGenerator.applyMask(maskPattern, QR_MATRIX, (r, c) -> false);
        assertArrayEquals(expectedMatrix, maskedMatrix, "Mask Pattern 3 failed");
    }

    @Test
    void testMaskPattern4() {
        // Mask pattern 4 ((row / 2) + (col / 3)) % 2 == 0
        int maskPattern = 4;
        int[][] expectedMatrix = {
        	{1, 1, 1, 0, 0, 0},
        	{1, 1, 1, 0, 0, 0},
            {0, 0, 0, 1, 1, 1},
            {0, 0, 0, 1, 1, 1},
            {1, 1, 1, 0, 0, 0},
        	{1, 1, 1, 0, 0, 0},
        };

        int[][] maskedMatrix = QRGenerator.applyMask(maskPattern, QR_MATRIX, (r, c) -> false);
        assertArrayEquals(expectedMatrix, maskedMatrix, "Mask Pattern 4 failed");
    }

    @Test
    void testMaskPattern5() {
        // Mask pattern 5 ((row * col) % 2) + ((row * col) % 3) == 0
        int maskPattern = 5;
        int[][] expectedMatrix = {
        	{1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0}, 
            {1, 0, 0, 1, 0, 0},
            {1, 0, 1, 0, 1, 0},
            {1, 0, 0, 1, 0, 0},
            {1, 0, 0, 0, 0, 0}
        };

        int[][] maskedMatrix = QRGenerator.applyMask(maskPattern, QR_MATRIX, (r, c) -> false);
        assertArrayEquals(expectedMatrix, maskedMatrix, "Mask Pattern 5 failed");
    }

    @Test
    void testMaskPattern6() {
        // Mask pattern 6 (((row * col) % 2) + ((row * col) % 3)) % 2 == 0
        int maskPattern = 6;
        int[][] expectedMatrix = {
            {1, 1, 1, 1, 1, 1},
            {1, 1, 1, 0, 0, 0}, 
            {1, 1, 0, 1, 1, 0},
            {1, 0, 1, 0, 1, 0},
            {1, 0, 1, 1, 0, 1},
            {1, 0, 0, 0, 1, 1}
        };

        int[][] maskedMatrix = QRGenerator.applyMask(maskPattern, QR_MATRIX, (r, c) -> false);
        assertArrayEquals(expectedMatrix, maskedMatrix, "Mask Pattern 6 failed");
    }

    @Test
    void testMaskPattern7() {
        // Mask pattern 7 (((row + col) % 2) + ((row * col) % 3)) % 2 == 0
        int maskPattern = 7;
        int[][] expectedMatrix = {
        	{1, 0, 1, 0, 1, 0},
        	{0, 0, 0, 1, 1, 1},
        	{1, 0, 0, 0, 1, 1},
        	{0, 1, 0, 1, 0, 1},
        	{1, 1, 1, 0, 0, 0},
        	{0, 1, 1, 1, 0, 0}
        };

        int[][] maskedMatrix = QRGenerator.applyMask(maskPattern, QR_MATRIX, (r, c) -> false);
        assertArrayEquals(expectedMatrix, maskedMatrix, "Mask Pattern 7 failed");
    }
}
