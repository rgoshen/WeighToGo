package com.example.weighttogo.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weighttogo.R;
import com.example.weighttogo.database.WeighToGoDBHelper;
import com.example.weighttogo.database.WeightEntryDAO;
import com.example.weighttogo.models.WeightEntry;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;

/**
 * WeightEntryActivity handles adding and editing weight entries.
 *
 * Features:
 * - Number pad input (0-9, decimal, backspace)
 * - Quick adjust buttons (-1, -0.5, +0.5, +1)
 * - Unit toggle (lbs ↔ kg)
 * - Date navigation (previous/next with today detection)
 * - Validation (50-700 lbs or 22.7-317.5 kg)
 * - Duplicate entry detection (one entry per user per date)
 *
 * Navigation:
 * - From MainActivity FAB (add mode)
 * - From MainActivity edit button (edit mode)
 * - Returns RESULT_OK on successful save/update
 */
public class WeightEntryActivity extends AppCompatActivity {

    private static final String TAG = "WeightEntryActivity";
    private static final int MAX_DIGITS = 5;
    private static final String DECIMAL_POINT = ".";

    // =============================================================================================
    // INTENT EXTRAS CONSTANTS
    // =============================================================================================

    public static final String EXTRA_USER_ID = "userId";
    public static final String EXTRA_IS_EDIT_MODE = "isEditMode";
    public static final String EXTRA_WEIGHT_ID = "weightId";
    public static final String EXTRA_WEIGHT_VALUE = "weightValue";
    public static final String EXTRA_WEIGHT_DATE = "weightDate";
    public static final String EXTRA_WEIGHT_UNIT = "weightUnit";
    public static final String EXTRA_NOTES = "notes";

    // =============================================================================================
    // UI COMPONENTS
    // =============================================================================================

    // Navigation
    private ImageButton backButton;
    private ImageButton prevDateButton;
    private ImageButton nextDateButton;

    // Date Display
    private TextView dayNumber;
    private TextView fullDate;
    private TextView todayBadge;

    // Weight Display
    private TextView weightValue;
    private TextView weightUnit;

    // Number Pad (12 buttons: 0-9, decimal, backspace)
    private TextView numpad0;
    private TextView numpad1;
    private TextView numpad2;
    private TextView numpad3;
    private TextView numpad4;
    private TextView numpad5;
    private TextView numpad6;
    private TextView numpad7;
    private TextView numpad8;
    private TextView numpad9;
    private TextView numpadDecimal;
    private TextView numpadBackspace;

    // Quick Adjust Buttons
    private TextView adjustMinusOne;
    private TextView adjustMinusHalf;
    private TextView adjustPlusHalf;
    private TextView adjustPlusOne;

    // Unit Toggle
    private TextView unitLbs;
    private TextView unitKg;

    // Save Button
    private MaterialButton saveButton;

    // Previous Entry Hint
    private TextView lastEntryValue;
    private TextView lastEntryDate;

    // =============================================================================================
    // DATA LAYER
    // =============================================================================================

    private WeighToGoDBHelper dbHelper;
    private WeightEntryDAO weightEntryDAO;

    // =============================================================================================
    // STATE
    // =============================================================================================

    private long userId;
    private boolean isEditMode;
    private long editWeightId;
    private LocalDate currentDate;
    private String currentUnit = "lbs";
    private StringBuilder weightInput;

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: WeightEntryActivity started");

        setContentView(R.layout.activity_weight_entry);

        // Initialize weight input
        weightInput = new StringBuilder();

        // Get intent extras
        getIntentExtras();

        // Initialize data layer
        initDataLayer();

        // Initialize UI
        initViews();
        setupClickListeners();

        // Load data if edit mode
        if (isEditMode) {
            loadExistingEntry();
        } else {
            loadPreviousEntry();
        }

        // Initialize date to today
        if (currentDate == null) {
            currentDate = LocalDate.now();
        }
        updateDateDisplay(currentDate);
    }

    // =============================================================================================
    // INITIALIZATION
    // =============================================================================================

    /**
     * Extract intent extras for userId, edit mode, and entry data.
     */
    private void getIntentExtras() {
        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
        isEditMode = getIntent().getBooleanExtra(EXTRA_IS_EDIT_MODE, false);

        if (isEditMode) {
            editWeightId = getIntent().getLongExtra(EXTRA_WEIGHT_ID, -1);
            double weightValue = getIntent().getDoubleExtra(EXTRA_WEIGHT_VALUE, 0.0);
            String dateStr = getIntent().getStringExtra(EXTRA_WEIGHT_DATE);
            currentUnit = getIntent().getStringExtra(EXTRA_WEIGHT_UNIT);

            if (dateStr != null) {
                currentDate = LocalDate.parse(dateStr);
            }

            if (weightValue > 0) {
                weightInput = new StringBuilder(String.format("%.1f", weightValue));
            }
        }

        Log.d(TAG, "getIntentExtras: userId=" + userId + ", isEditMode=" + isEditMode);
    }

    /**
     * Initialize database helper and DAOs.
     */
    private void initDataLayer() {
        dbHelper = WeighToGoDBHelper.getInstance(this);
        weightEntryDAO = new WeightEntryDAO(dbHelper);
        Log.d(TAG, "initDataLayer: Data layer initialized");
    }

    /**
     * Initialize all UI components via findViewById.
     */
    private void initViews() {
        // Navigation
        backButton = findViewById(R.id.backButton);
        prevDateButton = findViewById(R.id.prevDateButton);
        nextDateButton = findViewById(R.id.nextDateButton);

        // Date Display
        dayNumber = findViewById(R.id.dayNumber);
        fullDate = findViewById(R.id.fullDate);
        todayBadge = findViewById(R.id.todayBadge);

        // Weight Display
        weightValue = findViewById(R.id.weightValue);
        weightUnit = findViewById(R.id.weightUnit);

        // Number Pad
        numpad0 = findViewById(R.id.numpad0);
        numpad1 = findViewById(R.id.numpad1);
        numpad2 = findViewById(R.id.numpad2);
        numpad3 = findViewById(R.id.numpad3);
        numpad4 = findViewById(R.id.numpad4);
        numpad5 = findViewById(R.id.numpad5);
        numpad6 = findViewById(R.id.numpad6);
        numpad7 = findViewById(R.id.numpad7);
        numpad8 = findViewById(R.id.numpad8);
        numpad9 = findViewById(R.id.numpad9);
        numpadDecimal = findViewById(R.id.numpadDecimal);
        numpadBackspace = findViewById(R.id.numpadBackspace);

        // Quick Adjust
        adjustMinusOne = findViewById(R.id.adjustMinusOne);
        adjustMinusHalf = findViewById(R.id.adjustMinusHalf);
        adjustPlusHalf = findViewById(R.id.adjustPlusHalf);
        adjustPlusOne = findViewById(R.id.adjustPlusOne);

        // Unit Toggle
        unitLbs = findViewById(R.id.unitLbs);
        unitKg = findViewById(R.id.unitKg);

        // Save Button
        saveButton = findViewById(R.id.saveButton);

        // Previous Entry Hint
        lastEntryValue = findViewById(R.id.lastEntryValue);
        lastEntryDate = findViewById(R.id.lastEntryDate);

        Log.d(TAG, "initViews: All views initialized");
    }

    /**
     * Setup click listeners for all buttons.
     */
    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Date navigation (stubbed - will implement in Commit 5)
        prevDateButton.setOnClickListener(v -> Log.d(TAG, "Previous date - coming in Commit 5"));
        nextDateButton.setOnClickListener(v -> Log.d(TAG, "Next date - coming in Commit 5"));

        // Number pad
        setupNumberPadListeners();

        // Quick adjust (stubbed - will implement in Commit 3)
        // Unit toggle (stubbed - will implement in Commit 4)
        // Save button (stubbed - will implement in Commit 6)

        Log.d(TAG, "setupClickListeners: Click listeners configured");
    }

    // =============================================================================================
    // NUMBER PAD INPUT (Commit 2)
    // =============================================================================================

    /**
     * Setup number pad button click listeners (0-9, decimal, backspace).
     */
    private void setupNumberPadListeners() {
        // Array of number pad button IDs
        int[] numpadIds = {
                R.id.numpad0, R.id.numpad1, R.id.numpad2, R.id.numpad3, R.id.numpad4,
                R.id.numpad5, R.id.numpad6, R.id.numpad7, R.id.numpad8, R.id.numpad9
        };

        // Wire up digit buttons (0-9)
        for (int i = 0; i < numpadIds.length; i++) {
            final int digit = i;
            findViewById(numpadIds[i]).setOnClickListener(v -> handleNumberInput(String.valueOf(digit)));
        }

        // Wire up decimal and backspace
        numpadDecimal.setOnClickListener(v -> handleDecimalPoint());
        numpadBackspace.setOnClickListener(v -> handleBackspace());

        Log.d(TAG, "setupNumberPadListeners: Number pad configured");
    }

    /**
     * Handle digit button press (0-9).
     * Prevents leading zeros (except "0.") and enforces max digit limit.
     *
     * @param digit the digit to append (0-9)
     */
    private void handleNumberInput(String digit) {
        String current = weightInput.toString();

        // Prevent leading zeros (except "0.")
        if (current.equals("0") && !digit.equals("0")) {
            weightInput = new StringBuilder(digit);
        } else if (current.length() < MAX_DIGITS + 1) {  // +1 for decimal point
            weightInput.append(digit);
        }

        updateWeightDisplay();
        Log.d(TAG, "handleNumberInput: Input = " + weightInput.toString());
    }

    /**
     * Handle decimal point button press.
     * Only adds decimal if not already present and input is not empty.
     */
    private void handleDecimalPoint() {
        String current = weightInput.toString();

        // Only add decimal if not already present and not empty
        if (!current.contains(DECIMAL_POINT) && current.length() > 0) {
            weightInput.append(DECIMAL_POINT);
            updateWeightDisplay();
            Log.d(TAG, "handleDecimalPoint: Input = " + weightInput.toString());
        }
    }

    /**
     * Handle backspace button press.
     * Removes the last character from input.
     */
    private void handleBackspace() {
        if (weightInput.length() > 0) {
            weightInput.deleteCharAt(weightInput.length() - 1);
            updateWeightDisplay();
            Log.d(TAG, "handleBackspace: Input = " + weightInput.toString());
        }
    }

    /**
     * Load existing entry data for edit mode.
     */
    private void loadExistingEntry() {
        WeightEntry entry = weightEntryDAO.getWeightEntryById(editWeightId);

        if (entry != null) {
            weightInput = new StringBuilder(String.format("%.1f", entry.getWeightValue()));
            currentUnit = entry.getWeightUnit();
            currentDate = entry.getWeightDate();

            updateWeightDisplay();
            Log.d(TAG, "loadExistingEntry: Loaded entry " + editWeightId);
        } else {
            Log.w(TAG, "loadExistingEntry: Entry not found for weightId=" + editWeightId);
        }
    }

    /**
     * Load previous entry to show as hint in add mode.
     */
    private void loadPreviousEntry() {
        WeightEntry lastEntry = weightEntryDAO.getLatestWeightEntry(userId);

        if (lastEntry != null) {
            String value = String.format("%.1f %s",
                    lastEntry.getWeightValue(), lastEntry.getWeightUnit());
            lastEntryValue.setText(value);

            // Will format date in future commit when DateUtils is used
            lastEntryDate.setText("(previous entry)");

            Log.d(TAG, "loadPreviousEntry: Loaded previous entry");
        } else {
            lastEntryValue.setVisibility(View.GONE);
            lastEntryDate.setVisibility(View.GONE);
            Log.d(TAG, "loadPreviousEntry: No previous entry found");
        }
    }

    /**
     * Update date display components.
     *
     * @param date the date to display
     */
    private void updateDateDisplay(LocalDate date) {
        currentDate = date;

        // Update day number
        dayNumber.setText(String.valueOf(date.getDayOfMonth()));

        // Update full date (will use DateUtils in future commit)
        fullDate.setText(date.toString());

        // Update "Today" badge visibility
        boolean isToday = date.equals(LocalDate.now());
        todayBadge.setVisibility(isToday ? View.VISIBLE : View.GONE);

        // Disable next button if today
        nextDateButton.setEnabled(!isToday);
        nextDateButton.setAlpha(isToday ? 0.3f : 1.0f);

        Log.d(TAG, "updateDateDisplay: Date set to " + date);
    }

    /**
     * Update weight value display.
     */
    private void updateWeightDisplay() {
        String value = weightInput.toString();
        if (value.isEmpty()) {
            value = "0.0";
        }
        weightValue.setText(value);
        weightUnit.setText(currentUnit);
    }
}
