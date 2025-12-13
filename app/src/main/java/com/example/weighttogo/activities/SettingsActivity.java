package com.example.weighttogo.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.weighttogo.R;
import com.example.weighttogo.database.UserPreferenceDAO;
import com.example.weighttogo.database.WeighToGoDBHelper;
import com.example.weighttogo.utils.SessionManager;

/**
 * SettingsActivity - Centralized settings management screen
 *
 * Features:
 * - Weight unit preference (lbs/kg) toggle
 * - SMS notification settings
 * - Phone number management
 *
 * Part of Phase 6.0.4: Global Weight Unit Preference System
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    // UI Elements
    private ImageButton backButton;
    private TextView unitLbs;
    private TextView unitKg;

    // Data Layer
    private WeighToGoDBHelper dbHelper;
    private UserPreferenceDAO userPreferenceDAO;

    // State
    private String currentUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initDataLayer();
        initViews();
        setupClickListeners();
        loadCurrentPreference();
    }

    /**
     * Initialize database helper and DAOs
     */
    private void initDataLayer() {
        dbHelper = WeighToGoDBHelper.getInstance(this);
        userPreferenceDAO = new UserPreferenceDAO(dbHelper);
    }

    /**
     * Initialize view references
     */
    private void initViews() {
        backButton = findViewById(R.id.backButton);
        unitLbs = findViewById(R.id.unitLbs);
        unitKg = findViewById(R.id.unitKg);
    }

    /**
     * Load user's current weight unit preference from database
     */
    private void loadCurrentPreference() {
        long userId = SessionManager.getInstance(this).getCurrentUserId();
        currentUnit = userPreferenceDAO.getWeightUnit(userId);
        updateUnitButtonUI();
    }

    /**
     * Setup click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Back button navigation
        backButton.setOnClickListener(v -> finish());

        // Weight unit toggle
        unitLbs.setOnClickListener(v -> saveWeightUnit("lbs"));
        unitKg.setOnClickListener(v -> saveWeightUnit("kg"));
    }

    /**
     * Save selected weight unit to database and update UI
     *
     * @param unit Weight unit to save ("lbs" or "kg")
     */
    private void saveWeightUnit(String unit) {
        long userId = SessionManager.getInstance(this).getCurrentUserId();

        boolean success = userPreferenceDAO.setWeightUnit(userId, unit);
        if (success) {
            currentUnit = unit;
            updateUnitButtonUI();
            Toast.makeText(this,
                    getString(R.string.weight_unit_updated, unit),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    R.string.weight_unit_update_failed,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update toggle button UI to reflect current unit selection
     */
    private void updateUnitButtonUI() {
        if ("lbs".equals(currentUnit)) {
            // Lbs active, kg inactive
            unitLbs.setBackgroundResource(R.drawable.bg_unit_toggle_active);
            unitLbs.setTextColor(ContextCompat.getColor(this, R.color.text_on_primary));
            unitKg.setBackgroundResource(R.drawable.bg_unit_toggle_inactive);
            unitKg.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        } else {
            // Kg active, lbs inactive
            unitKg.setBackgroundResource(R.drawable.bg_unit_toggle_active);
            unitKg.setTextColor(ContextCompat.getColor(this, R.color.text_on_primary));
            unitLbs.setBackgroundResource(R.drawable.bg_unit_toggle_inactive);
            unitLbs.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }
}
