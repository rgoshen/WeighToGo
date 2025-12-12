package com.example.weighttogo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weighttogo.R;
import com.example.weighttogo.adapters.GoalHistoryAdapter;
import com.example.weighttogo.database.GoalWeightDAO;
import com.example.weighttogo.fragments.GoalDialogFragment;
import com.example.weighttogo.database.UserDAO;
import com.example.weighttogo.database.WeighToGoDBHelper;
import com.example.weighttogo.database.WeightEntryDAO;
import com.example.weighttogo.models.GoalWeight;
import com.example.weighttogo.models.WeightEntry;
import com.example.weighttogo.utils.DateUtils;
import com.example.weighttogo.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for managing weight goals.
 * Displays current goal with expanded stats and goal history.
 */
public class GoalsActivity extends AppCompatActivity
        implements GoalDialogFragment.GoalDialogListener {

    // UI Elements - Header
    private ImageButton btnBack;

    // UI Elements - Current Goal Card
    private MaterialCardView cardCurrentGoal;
    private ImageButton btnEditGoal;
    private ImageButton btnDeleteGoal;
    private TextView textStartWeight;
    private TextView textStartUnit;
    private TextView textCurrentWeightValue;
    private TextView textCurrentUnit;
    private TextView textGoalWeight;
    private TextView textGoalUnit;
    private TextView textDaysSinceStart;
    private TextView textPace;
    private TextView textProjection;
    private TextView textAvgWeeklyLoss;

    // UI Elements - Empty State
    private LinearLayout emptyStateContainer;

    // UI Elements - Goal History
    private LinearLayout goalHistorySection;
    private RecyclerView recyclerGoalHistory;
    private GoalHistoryAdapter adapter;

    // UI Elements - FAB
    private FloatingActionButton fabAddGoal;

    // Data Layer
    private WeighToGoDBHelper dbHelper;
    private GoalWeightDAO goalWeightDAO;
    private WeightEntryDAO weightEntryDAO;
    private UserDAO userDAO;
    private SessionManager sessionManager;

    // State
    private long currentUserId;
    private GoalWeight activeGoal;
    private List<GoalWeight> goalHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check authentication first
        if (!checkAuthentication()) {
            return; // Activity will finish in checkAuthentication()
        }

        setContentView(R.layout.activity_goals);

        // Initialize data layer
        initDataLayer();

        // Initialize UI
        initViews();
        setupRecyclerView();
        setupFAB();

        // Load data
        loadGoalData();
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
        goalWeightDAO = new GoalWeightDAO(dbHelper);
        weightEntryDAO = new WeightEntryDAO(dbHelper);
        userDAO = new UserDAO(dbHelper);
        goalHistory = new ArrayList<>();
    }

    /**
     * Initialize all view references.
     */
    private void initViews() {
        // Header
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Current Goal Card
        cardCurrentGoal = findViewById(R.id.card_current_goal);
        btnEditGoal = findViewById(R.id.btn_edit_goal);
        btnDeleteGoal = findViewById(R.id.btn_delete_goal);
        textStartWeight = findViewById(R.id.text_start_weight);
        textStartUnit = findViewById(R.id.text_start_unit);
        textCurrentWeightValue = findViewById(R.id.text_current_weight_value);
        textCurrentUnit = findViewById(R.id.text_current_unit);
        textGoalWeight = findViewById(R.id.text_goal_weight);
        textGoalUnit = findViewById(R.id.text_goal_unit);
        textDaysSinceStart = findViewById(R.id.text_days_since_start);
        textPace = findViewById(R.id.text_pace);
        textProjection = findViewById(R.id.text_projection);
        textAvgWeeklyLoss = findViewById(R.id.text_avg_weekly_loss);

        // Empty State
        emptyStateContainer = findViewById(R.id.empty_state_container);

        // Goal History
        goalHistorySection = findViewById(R.id.goal_history_section);
        recyclerGoalHistory = findViewById(R.id.recycler_goal_history);

        // FAB
        fabAddGoal = findViewById(R.id.fab_add_goal);

        // Button listeners
        btnEditGoal.setOnClickListener(v -> handleEditGoal());
        btnDeleteGoal.setOnClickListener(v -> handleDeleteGoal());
    }

    /**
     * Setup RecyclerView for goal history.
     */
    private void setupRecyclerView() {
        adapter = new GoalHistoryAdapter(goalHistory);
        recyclerGoalHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerGoalHistory.setAdapter(adapter);
    }

    /**
     * Setup FAB click listener.
     */
    private void setupFAB() {
        fabAddGoal.setOnClickListener(v -> showSetGoalDialog());
    }

    /**
     * Shows the goal dialog fragment.
     * Fragment handles all UI, validation, and database operations.
     */
    private void showSetGoalDialog() {
        // Get current weight (latest entry)
        WeightEntry latestEntry = weightEntryDAO.getLatestWeightEntry(currentUserId);

        if (latestEntry == null) {
            Toast.makeText(this, "Please add a weight entry first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create and show dialog fragment
        GoalDialogFragment dialog = GoalDialogFragment.newInstance(
                currentUserId,
                latestEntry.getWeightValue(),
                latestEntry.getWeightUnit()
        );
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "GoalDialogFragment");
    }

    /**
     * Load goal data from database (active goal + history).
     */
    private void loadGoalData() {
        // Load active goal
        activeGoal = goalWeightDAO.getActiveGoal(currentUserId);

        // Load goal history (inactive goals)
        List<GoalWeight> allGoals = goalWeightDAO.getGoalHistory(currentUserId);
        goalHistory.clear();
        for (GoalWeight goal : allGoals) {
            if (!goal.isActive()) {
                goalHistory.add(goal);
            }
        }

        // Update UI based on data
        if (activeGoal != null) {
            // Show current goal card
            cardCurrentGoal.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
            fabAddGoal.setVisibility(View.GONE);

            updateCurrentGoalCard();
            updateExpandedStats();
        } else {
            // Show empty state
            cardCurrentGoal.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
            fabAddGoal.setVisibility(View.VISIBLE);
        }

        // Show/hide goal history section
        if (!goalHistory.isEmpty()) {
            goalHistorySection.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        } else {
            goalHistorySection.setVisibility(View.GONE);
        }
    }

    /**
     * Update current goal card with start/current/goal weights.
     */
    private void updateCurrentGoalCard() {
        if (activeGoal == null) {
            return;
        }

        // Start weight
        textStartWeight.setText(String.format("%.1f", activeGoal.getStartWeight()));
        textStartUnit.setText(activeGoal.getGoalUnit());

        // Current weight
        double currentWeight = getCurrentWeight();
        textCurrentWeightValue.setText(String.format("%.1f", currentWeight));
        textCurrentUnit.setText(activeGoal.getGoalUnit());

        // Goal weight
        textGoalWeight.setText(String.format("%.1f", activeGoal.getGoalWeight()));
        textGoalUnit.setText(activeGoal.getGoalUnit());
    }

    /**
     * Update expanded stats (days since start, pace, projection, avg weekly loss).
     */
    private void updateExpandedStats() {
        if (activeGoal == null) {
            return;
        }

        LocalDate startDate = activeGoal.getCreatedAt().toLocalDate();
        LocalDate today = LocalDate.now();
        long daysSinceStart = ChronoUnit.DAYS.between(startDate, today);

        // Days since start
        String daysText = String.format(getString(R.string.days_format), daysSinceStart);
        textDaysSinceStart.setText(daysText);

        // Calculate pace and projection
        double currentWeight = getCurrentWeight();
        double startWeight = activeGoal.getStartWeight();
        double goalWeight = activeGoal.getGoalWeight();
        double weightLost = Math.abs(startWeight - currentWeight);
        double weightRemaining = Math.abs(currentWeight - goalWeight);

        if (daysSinceStart > 0) {
            // Pace (lbs/week)
            double pace = (weightLost / daysSinceStart) * 7;
            String paceText = String.format(getString(R.string.pace_format), pace);
            textPace.setText(paceText);

            // Projection (estimated completion date)
            if (pace > 0.01) { // Avoid division by zero
                long daysToGoal = (long) ((weightRemaining / pace) * 7);
                LocalDate projectedDate = today.plusDays(daysToGoal);
                String projectionText = DateUtils.formatDateFull(projectedDate);
                textProjection.setText(projectionText);
            } else {
                textProjection.setText("N/A");
            }

            // Avg weekly loss
            double avgWeeklyLoss = -((weightLost / daysSinceStart) * 7);
            String avgText = String.format(getString(R.string.avg_weekly_format), avgWeeklyLoss);
            textAvgWeeklyLoss.setText(avgText);
        } else {
            textPace.setText("N/A");
            textProjection.setText("N/A");
            textAvgWeeklyLoss.setText("N/A");
        }
    }

    // ============================================================
    // GoalDialogFragment.GoalDialogListener Implementation
    // ============================================================

    @Override
    public void onGoalSaved(GoalWeight goal) {
        // Reload all goal data (active goal + history)
        loadGoalData();
    }

    @Override
    public void onGoalSaveError(String errorMessage) {
        // Error already shown via Toast in fragment
        android.util.Log.e("GoalsActivity", "Goal save failed: " + errorMessage);
    }

    // ============================================================
    // Goal Actions
    // ============================================================

    /**
     * Handle edit goal button click.
     * TODO: Implement edit mode with GoalDialogFragment
     */
    private void handleEditGoal() {
        // TODO Phase 2: Implement edit mode
        // Will use GoalDialogFragment.newInstance() with existingGoalId parameter
        Toast.makeText(this, "Edit goal coming soon", Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle delete goal button click.
     * Shows confirmation dialog and deactivates goal if confirmed.
     */
    private void handleDeleteGoal() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_goal_title)
                .setMessage(R.string.confirm_delete_goal_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    if (activeGoal != null) {
                        int rowsUpdated = goalWeightDAO.deactivateGoal(activeGoal.getGoalId());
                        if (rowsUpdated > 0) {
                            Toast.makeText(this, R.string.success_goal_deleted, Toast.LENGTH_SHORT).show();
                            loadGoalData(); // Refresh UI
                        } else {
                            Toast.makeText(this, "Failed to delete goal", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Get current weight from latest weight entry.
     *
     * @return current weight, or 0.0 if no entries exist
     */
    private double getCurrentWeight() {
        WeightEntry latestEntry = weightEntryDAO.getLatestWeightEntry(currentUserId);
        if (latestEntry != null) {
            return latestEntry.getWeightValue();
        }
        return 0.0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from MainActivity
        loadGoalData();
    }
}
