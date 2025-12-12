package com.example.weighttogo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weighttogo.R;
import com.example.weighttogo.adapters.WeightEntryAdapter;
import com.example.weighttogo.database.GoalWeightDAO;
import com.example.weighttogo.database.UserDAO;
import com.example.weighttogo.database.WeighToGoDBHelper;
import com.example.weighttogo.database.WeightEntryDAO;
import com.example.weighttogo.models.GoalWeight;
import com.example.weighttogo.models.User;
import com.example.weighttogo.models.WeightEntry;
import com.example.weighttogo.utils.DateUtils;
import com.example.weighttogo.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Main dashboard activity for WeightToGo app.
 * Displays user greeting, progress card, quick stats, and recent weight entries.
 */
public class MainActivity extends AppCompatActivity implements WeightEntryAdapter.OnItemClickListener {

    // Request Codes
    private static final int REQUEST_CODE_WEIGHT_ENTRY = 1001;

    // UI Elements
    private TextView greetingText;
    private TextView userName;
    private ImageButton notificationButton;
    private ImageButton settingsButton;

    // Progress Card
    private MaterialCardView progressCard;
    private TextView currentWeightValue;
    private TextView startWeightValue;
    private TextView goalWeightValue;
    private View progressBarFill;
    private TextView progressPercentage;

    // Quick Stats
    private TextView totalLostValue;
    private TextView lbsToGoalValue;
    private TextView dayStreakValue;

    // Weight List
    private RecyclerView weightRecyclerView;
    private LinearLayout emptyStateContainer;
    private WeightEntryAdapter adapter;

    // Navigation
    private FloatingActionButton addEntryFab;
    private BottomNavigationView bottomNavigation;

    // Data Layer
    private WeighToGoDBHelper dbHelper;
    private UserDAO userDAO;
    private WeightEntryDAO weightEntryDAO;
    private GoalWeightDAO goalWeightDAO;
    private SessionManager sessionManager;

    // State
    private long currentUserId;
    private List<WeightEntry> weightEntries;
    private GoalWeight activeGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check authentication first
        if (!checkAuthentication()) {
            return; // Activity will finish in checkAuthentication()
        }

        setContentView(R.layout.activity_main);

        // Initialize data layer
        initDataLayer();

        // Initialize UI
        initViews();
        setupRecyclerView();
        setupFAB();
        setupBottomNavigation();

        // Load data
        loadWeightEntries();
        updateProgressCard();
        calculateQuickStats();
        updateGreeting();
        updateUserName();
    }

    /**
     * Check if user is authenticated. Redirect to login if not.
     *
     * @return true if authenticated, false otherwise
     */
    private boolean checkAuthentication() {
        sessionManager = SessionManager.getInstance(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return false;
        }

        currentUserId = sessionManager.getCurrentUserId();
        return true;
    }

    /**
     * Initialize database and DAOs.
     */
    private void initDataLayer() {
        dbHelper = WeighToGoDBHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);
        weightEntryDAO = new WeightEntryDAO(dbHelper);
        goalWeightDAO = new GoalWeightDAO(dbHelper);
        weightEntries = new ArrayList<>();
    }

    /**
     * Initialize all view references.
     */
    private void initViews() {
        // Header
        greetingText = findViewById(R.id.greetingText);
        userName = findViewById(R.id.userName);
        notificationButton = findViewById(R.id.notificationButton);
        settingsButton = findViewById(R.id.settingsButton);

        // Progress Card
        progressCard = findViewById(R.id.progressCard);
        currentWeightValue = findViewById(R.id.currentWeightValue);
        startWeightValue = findViewById(R.id.startWeightValue);
        goalWeightValue = findViewById(R.id.goalWeightValue);
        progressBarFill = findViewById(R.id.progressBarFill);
        progressPercentage = findViewById(R.id.progressPercentage);

        // Quick Stats
        totalLostValue = findViewById(R.id.totalLostValue);
        lbsToGoalValue = findViewById(R.id.lbsToGoalValue);
        dayStreakValue = findViewById(R.id.dayStreakValue);

        // Weight List
        weightRecyclerView = findViewById(R.id.weightRecyclerView);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);

        // Navigation
        addEntryFab = findViewById(R.id.addEntryFab);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    /**
     * Setup RecyclerView with adapter and layout manager.
     */
    private void setupRecyclerView() {
        adapter = new WeightEntryAdapter(weightEntries, this);
        weightRecyclerView.setAdapter(adapter);
        weightRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Setup FAB click listener.
     */
    private void setupFAB() {
        addEntryFab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WeightEntryActivity.class);
            intent.putExtra(WeightEntryActivity.EXTRA_USER_ID, currentUserId);
            intent.putExtra(WeightEntryActivity.EXTRA_IS_EDIT_MODE, false);
            startActivityForResult(intent, REQUEST_CODE_WEIGHT_ENTRY);
        });
    }

    /**
     * Setup bottom navigation item selection listener.
     */
    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on home, do nothing
                return true;
            } else if (itemId == R.id.nav_trends) {
                Toast.makeText(this, "Trends - Coming in Phase 5", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_goals) {
                Toast.makeText(this, "Goals - Coming in Phase 6", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Toast.makeText(this, "Profile - Coming in Phase 7", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });
    }

    /**
     * Load weight entries from database and update UI.
     */
    private void loadWeightEntries() {
        weightEntries.clear();
        List<WeightEntry> entries = weightEntryDAO.getWeightEntriesForUser(currentUserId);
        weightEntries.addAll(entries);
        adapter.notifyDataSetChanged();

        showEmptyState(weightEntries.isEmpty());
    }

    /**
     * Update progress card with goal data.
     * Uses cached weightEntries to avoid redundant database query.
     */
    private void updateProgressCard() {
        activeGoal = goalWeightDAO.getActiveGoal(currentUserId);

        if (activeGoal == null) {
            progressCard.setVisibility(View.GONE);
            return;
        }

        progressCard.setVisibility(View.VISIBLE);

        // Get current weight from most recent entry (use cached list)
        double current = activeGoal.getStartWeight();
        if (!weightEntries.isEmpty()) {
            current = weightEntries.get(0).getWeightValue();
        }

        // Display weight values
        startWeightValue.setText(String.format("%.1f", activeGoal.getStartWeight()));
        currentWeightValue.setText(String.format("%.1f", current));
        goalWeightValue.setText(String.format("%.1f", activeGoal.getGoalWeight()));

        // Update progress bar
        updateProgressBar(current, activeGoal.getStartWeight(), activeGoal.getGoalWeight());
    }

    /**
     * Update progress bar width and percentage.
     *
     * @param current current weight
     * @param start starting weight
     * @param goal goal weight
     */
    private void updateProgressBar(double current, double start, double goal) {
        double totalRange = Math.abs(start - goal);
        double progress = Math.abs(start - current);
        int percentageValue = (int) ((progress / totalRange) * 100);

        // Clamp percentage between 0 and 100
        final int percentage = Math.max(0, Math.min(100, percentageValue));

        progressPercentage.setText(percentage + "%");

        // Set progress bar width
        ViewGroup.LayoutParams params = progressBarFill.getLayoutParams();
        params.width = 0; // Will be updated after layout
        progressBarFill.setLayoutParams(params);

        // Post to update after layout
        progressBarFill.post(() -> {
            int containerWidth = progressBarFill.getParent() instanceof View ?
                    ((View) progressBarFill.getParent()).getWidth() : 0;
            ViewGroup.LayoutParams layoutParams = progressBarFill.getLayoutParams();
            layoutParams.width = (int) (containerWidth * (percentage / 100.0));
            progressBarFill.setLayoutParams(layoutParams);
        });
    }

    /**
     * Calculate and display quick stats.
     * Uses cached weightEntries to avoid redundant database query.
     */
    private void calculateQuickStats() {
        activeGoal = goalWeightDAO.getActiveGoal(currentUserId);

        if (activeGoal != null && !weightEntries.isEmpty()) {
            double current = weightEntries.get(0).getWeightValue();
            double start = activeGoal.getStartWeight();
            double goal = activeGoal.getGoalWeight();

            // Total lost
            double totalLost = start - current;
            totalLostValue.setText(String.format("%.0f", totalLost));

            // Lbs to goal
            double lbsToGoal = Math.abs(current - goal);
            lbsToGoalValue.setText(String.format("%.0f", lbsToGoal));
        }

        // Day streak (use cached list)
        int streak = DateUtils.calculateDayStreak(weightEntries);
        dayStreakValue.setText(String.valueOf(streak));
    }

    /**
     * Update greeting based on time of day.
     */
    private void updateGreeting() {
        int hour = LocalTime.now().getHour();
        String greeting;

        if (hour < 12) {
            greeting = "Good morning";
        } else if (hour < 18) {
            greeting = "Good afternoon";
        } else {
            greeting = "Good evening";
        }

        greetingText.setText(greeting);
    }

    /**
     * Update user name display.
     * Falls back to username if display_name is null or empty (defensive programming).
     */
    private void updateUserName() {
        User user = userDAO.getUserById(currentUserId);
        if (user != null) {
            String displayName = user.getDisplayName();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = user.getUsername();  // Fallback to username
            }
            userName.setText(displayName);
        }
    }

    /**
     * Show or hide empty state.
     *
     * @param show true to show empty state, false to hide
     */
    private void showEmptyState(boolean show) {
        emptyStateContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        weightRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Handle delete entry with confirmation dialog.
     *
     * @param entry the weight entry to delete
     */
    private void handleDeleteEntry(WeightEntry entry) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this weight entry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    weightEntryDAO.deleteWeightEntry(entry.getWeightId());
                    Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show();
                    loadWeightEntries();
                    updateProgressCard();
                    calculateQuickStats();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ============================================================
    // WeightEntryAdapter.OnItemClickListener Implementation
    // ============================================================

    @Override
    public void onEditClick(WeightEntry entry) {
        Intent intent = new Intent(MainActivity.this, WeightEntryActivity.class);
        intent.putExtra(WeightEntryActivity.EXTRA_USER_ID, currentUserId);
        intent.putExtra(WeightEntryActivity.EXTRA_IS_EDIT_MODE, true);
        intent.putExtra(WeightEntryActivity.EXTRA_WEIGHT_ID, entry.getWeightId());
        intent.putExtra(WeightEntryActivity.EXTRA_WEIGHT_VALUE, entry.getWeightValue());
        intent.putExtra(WeightEntryActivity.EXTRA_WEIGHT_DATE, entry.getWeightDate().toString());
        intent.putExtra(WeightEntryActivity.EXTRA_WEIGHT_UNIT, entry.getWeightUnit());
        startActivityForResult(intent, REQUEST_CODE_WEIGHT_ENTRY);
    }

    @Override
    public void onDeleteClick(WeightEntry entry) {
        handleDeleteEntry(entry);
    }

    /**
     * Handle result from WeightEntryActivity.
     * Refreshes data if entry was saved or updated.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_WEIGHT_ENTRY && resultCode == RESULT_OK) {
            // Refresh all data from database
            loadWeightEntries();
            updateProgressCard();
            calculateQuickStats();
        }
    }
}
