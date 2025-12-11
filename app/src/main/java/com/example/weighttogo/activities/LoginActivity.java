package com.example.weighttogo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weighttogo.R;
import com.example.weighttogo.database.UserDAO;
import com.example.weighttogo.database.WeighToGoDBHelper;
import com.example.weighttogo.models.User;
import com.example.weighttogo.utils.PasswordUtils;
import com.example.weighttogo.utils.SessionManager;
import com.example.weighttogo.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDateTime;

/**
 * LoginActivity handles user authentication (sign in and registration).
 *
 * Features:
 * - Tab toggle between Sign In and Register modes
 * - Input validation using ValidationUtils
 * - Username and password fields
 * - Forgot password link (Phase 3 feature)
 *
 * Dependencies:
 * - ValidationUtils for input validation
 * - SessionManager for session management (used in Commits 5-6)
 * - UserDAO for authentication (used in Commits 5-6)
 * - PasswordUtils for password hashing (used in Commits 5-6)
 *
 * Layout:
 * - activity_login.xml with Material Design 3 components
 * - Supports both Sign In and Registration modes
 *
 * Navigation:
 * - Success → MainActivity
 * - Initial entry point for unauthenticated users
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // =============================================================================================
    // UI COMPONENTS
    // =============================================================================================

    // Input fields
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;

    // Input layouts (for error messages)
    private TextInputLayout usernameInputLayout;
    private TextInputLayout passwordInputLayout;

    // Buttons and tabs
    private MaterialButton signInButton;
    private TextView tabSignIn;
    private TextView tabRegister;
    private TextView forgotPasswordText;

    // =============================================================================================
    // DATA ACCESS AND SESSION
    // =============================================================================================

    private UserDAO userDAO;
    private SessionManager sessionManager;

    // =============================================================================================
    // LIFECYCLE METHODS
    // =============================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: LoginActivity started");

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);

        // Set content view
        setContentView(R.layout.activity_login);

        // Initialize data access and session management
        WeighToGoDBHelper dbHelper = WeighToGoDBHelper.getInstance(this);
        userDAO = new UserDAO(dbHelper);
        sessionManager = SessionManager.getInstance(this);

        // Initialize UI components
        initViews();

        // Setup click listeners
        setupClickListeners();
    }

    // =============================================================================================
    // UI INITIALIZATION
    // =============================================================================================

    /**
     * Initialize all UI components via findViewById.
     * Binds views to instance variables for later access.
     */
    private void initViews() {
        // Input fields
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // Input layouts
        usernameInputLayout = findViewById(R.id.usernameInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

        // Buttons and tabs
        signInButton = findViewById(R.id.signInButton);
        tabSignIn = findViewById(R.id.tabSignIn);
        tabRegister = findViewById(R.id.tabRegister);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        Log.d(TAG, "initViews: All views initialized");
    }

    /**
     * Setup click listeners for buttons and tabs.
     * Wires up UI interactions.
     */
    private void setupClickListeners() {
        // Sign In button
        signInButton.setOnClickListener(v -> handleSignInClick());

        // Tab switching (Commit 7)
        // tabSignIn.setOnClickListener(v -> switchToSignInMode());
        // tabRegister.setOnClickListener(v -> switchToRegisterMode());

        // Forgot password (Phase 3 feature)
        // forgotPasswordText.setOnClickListener(v -> handleForgotPassword());

        Log.d(TAG, "setupClickListeners: Click listeners configured");
    }

    // =============================================================================================
    // VALIDATION (Commit 4)
    // =============================================================================================

    /**
     * Handle Sign In button click.
     * Validates input and proceeds with authentication if valid.
     */
    private void handleSignInClick() {
        Log.d(TAG, "handleSignInClick: Sign In button clicked");

        // Clear previous errors
        usernameInputLayout.setError(null);
        passwordInputLayout.setError(null);

        // Validate input
        if (!validateInput()) {
            Log.w(TAG, "handleSignInClick: Input validation failed");
            return;
        }

        Log.d(TAG, "handleSignInClick: Input validation passed, proceeding with authentication");
        handleSignIn();
    }

    /**
     * Validate username and password inputs.
     * Uses ValidationUtils for format validation.
     *
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInput() {
        String username = usernameEditText.getText() != null ? usernameEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";

        boolean isValid = true;

        // Validate username
        if (username.isEmpty()) {
            usernameInputLayout.setError("Username is required");
            isValid = false;
            Log.w(TAG, "validateInput: Username is empty");
        } else if (!ValidationUtils.isValidUsername(username)) {
            usernameInputLayout.setError("Username is invalid (3-20 chars, alphanumeric + underscore)");
            isValid = false;
            Log.w(TAG, "validateInput: Username is invalid");
        }

        // Validate password
        if (password.isEmpty()) {
            passwordInputLayout.setError("Password is required");
            isValid = false;
            Log.w(TAG, "validateInput: Password is empty");
        } else if (!ValidationUtils.isValidPassword(password)) {
            passwordInputLayout.setError("Password is invalid (6+ chars, at least 1 digit)");
            isValid = false;
            Log.w(TAG, "validateInput: Password is invalid");
        }

        return isValid;
    }

    // =============================================================================================
    // AUTHENTICATION (Commit 5)
    // =============================================================================================

    /**
     * Handle sign-in authentication flow.
     * Queries database, verifies password, creates session, and navigates to MainActivity.
     */
    private void handleSignIn() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        Log.d(TAG, "handleSignIn: Attempting authentication for username: " + username);

        // Query user from database
        User user = userDAO.getUserByUsername(username);

        if (user == null) {
            // User doesn't exist
            Log.w(TAG, "handleSignIn: User not found with username: " + username);
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify password
        boolean passwordMatches = PasswordUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash());

        if (!passwordMatches) {
            // Wrong password
            Log.w(TAG, "handleSignIn: Password verification failed for username: " + username);
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Authentication successful
        Log.i(TAG, "handleSignIn: Authentication successful for user_id: " + user.getUserId());

        // Update last_login timestamp
        int rowsUpdated = userDAO.updateLastLogin(user.getUserId(), LocalDateTime.now());
        if (rowsUpdated > 0) {
            Log.d(TAG, "handleSignIn: Updated last_login for user_id: " + user.getUserId());
        }

        // Create session
        sessionManager.createSession(user);
        Log.d(TAG, "handleSignIn: Session created for user_id: " + user.getUserId());

        // Navigate to MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();  // Prevent back button from returning to login

        Toast.makeText(this, "Welcome, " + (user.getDisplayName() != null ? user.getDisplayName() : user.getUsername()) + "!", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "handleSignIn: Navigated to MainActivity");
    }
}
