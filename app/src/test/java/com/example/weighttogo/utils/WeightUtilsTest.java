package com.example.weighttogo.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for WeightUtils.
 * Tests weight conversion (lbs ↔ kg), validation, and formatting.
 */
public class WeightUtilsTest {

    private static final double DELTA = 0.01; // Tolerance for double comparison

    /**
     * Test 1: Convert pounds to kilograms with valid input
     */
    @Test
    public void test_convertLbsToKg_withValidInput_returnsCorrectValue() {
        // ARRANGE
        double weightLbs = 150.0;
        double expectedKg = 68.0; // 150 * 0.453592 ≈ 68.0

        // ACT
        double actualKg = WeightUtils.convertLbsToKg(weightLbs);

        // ASSERT
        assertEquals("150 lbs should convert to approximately 68 kg",
                expectedKg, actualKg, DELTA);
    }

    /**
     * Test 2: Convert kilograms to pounds with valid input
     */
    @Test
    public void test_convertKgToLbs_withValidInput_returnsCorrectValue() {
        // ARRANGE
        double weightKg = 68.0;
        double expectedLbs = 149.9; // 68 / 0.453592 = 149.914... ≈ 149.9 (rounded to 1 decimal)

        // ACT
        double actualLbs = WeightUtils.convertKgToLbs(weightKg);

        // ASSERT
        assertEquals("68 kg should convert to approximately 149.9 lbs",
                expectedLbs, actualLbs, DELTA);
    }

    /**
     * Test 3: Convert zero pounds to kilograms
     */
    @Test
    public void test_convertLbsToKg_withZero_returnsZero() {
        // ARRANGE
        double weightLbs = 0.0;
        double expectedKg = 0.0;

        // ACT
        double actualKg = WeightUtils.convertLbsToKg(weightLbs);

        // ASSERT
        assertEquals("0 lbs should convert to 0 kg",
                expectedKg, actualKg, DELTA);
    }

    /**
     * Test 4: Round weight to one decimal place
     */
    @Test
    public void test_roundToOneDecimal_withPreciseValue_roundsCorrectly() {
        // ARRANGE
        double preciseValue = 68.0388; // 150 lbs * 0.453592 exact
        double expected = 68.0;

        // ACT
        double actual = WeightUtils.roundToOneDecimal(preciseValue);

        // ASSERT
        assertEquals("68.0388 should round to 68.0",
                expected, actual, 0.01);
    }

    /**
     * Test 5: Validate weight within valid range (lbs)
     */
    @Test
    public void test_isValidWeight_withValidRange_returnsTrue() {
        // ARRANGE
        double validWeightLbs = 150.0;
        String unitLbs = "lbs";
        double validWeightKg = 68.0;
        String unitKg = "kg";

        // ACT
        boolean resultLbs = WeightUtils.isValidWeight(validWeightLbs, unitLbs);
        boolean resultKg = WeightUtils.isValidWeight(validWeightKg, unitKg);

        // ASSERT
        assertTrue("150 lbs is within valid range (0-700)", resultLbs);
        assertTrue("68 kg is within valid range (0-317.5)", resultKg);
    }

    /**
     * Test 6: Validate weight outside valid range
     */
    @Test
    public void test_isValidWeight_withInvalidRange_returnsFalse() {
        // ARRANGE
        double tooHighLbs = 701.0;
        String unitLbs = "lbs";
        double tooHighKg = 318.0;
        String unitKg = "kg";
        double negativeLbs = -1.0;

        // ACT
        boolean resultTooHighLbs = WeightUtils.isValidWeight(tooHighLbs, unitLbs);
        boolean resultTooHighKg = WeightUtils.isValidWeight(tooHighKg, unitKg);
        boolean resultNegative = WeightUtils.isValidWeight(negativeLbs, unitLbs);

        // ASSERT
        assertFalse("701 lbs exceeds max (700)", resultTooHighLbs);
        assertFalse("318 kg exceeds max (317.5)", resultTooHighKg);
        assertFalse("Negative weight is invalid", resultNegative);
    }
}
