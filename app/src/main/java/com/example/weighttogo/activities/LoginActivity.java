package com.example.weighttogo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weighttogo.R;
import com.google.android.material.snackbar.Snackbar;
import com.example.weighttogo.database.DatabaseException;
import com.example.weighttogo.database.DuplicateUsernameException;
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
    // STATE
    // =============================================================================================

    private boolean isSignInMode = true;  // Start in Sign In mode

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
        // Sign In / Register button (changes based on mode)
        signInButton.setOnClickListener(v -> handleButtonClick());

        // Tab switching
        tabSignIn.setOnClickListener(v -> switchToSignInMode());
        tabRegister.setOnClickListener(v -> switchToRegisterMode());

        // Forgot password (Phase 3 feature)
        // forgotPasswordText.setOnClickListener(v -> handleForgotPassword());

        Log.d(TAG, "setupClickListeners: Click listeners configured");
    }

    // =============================================================================================
    // VALIDATION (Commit 4)
    // =============================================================================================

    /**
     * Handle button click (Sign In or Register based on current mode).
     * Validates input and proceeds with authentication or registration.
     */
    private void handleButtonClick() {
        String action = isSignInMode ? "Sign In" : "Register";
        Log.d(TAG, "handleButtonClick: " + action + " button clicked");

        // Clear previous errors
        usernameInputLayout.setError(null);
        passwordInputLayout.setError(null);

        // Validate input (pass mode to determine error specificity)
        if (!validateInput(isSignInMode)) {
            Log.w(TAG, "handleButtonClick: Input validation failed");
            return;
        }

        Log.d(TAG, "handleButtonClick: Input validation passed, proceeding with " + action);

        // Call appropriate handler based on mode
        if (isSignInMode) {
            handleSignIn();
        } else {
            handleRegister();
        }
    }

    /**
     * Validate username and password inputs.
     * Uses ValidationUtils for format validation.
     *
     * **Security:** Sign In mode shows generic error to prevent username enumeration.
     * Register mode shows specific errors to help users create valid accounts.
     *
     * @param isSignInMode true for Sign In mode (generic errors), false for Register mode (specific errors)
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInput(boolean isSignInMode) {
        // Username is trimmed to prevent accidental leading/trailing spaces
        String username = usernameEditText.getText() != null ? usernameEditText.getText().toString().trim() : "";

        // Password is NOT trimmed - user may intentionally include leading/trailing spaces
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";

        boolean isValid = true;

        if (isSignInMode) {
            // Sign In mode: Only check if fields are non-empty (prevents username enumeration)
            if (username.isEmpty() || password.isEmpty()) {
                // Show generic error via Snackbar (no field highlighting to prevent info leakage)
                showAuthenticationError("Please enter username and password");
                isValid = false;
                Log.w(TAG, "validateInput: Sign In validation failed - empty field(s)");
            }
        } else {
            // Register mode: Detailed validation to help users create valid accounts
            if (username.isEmpty()) {
                usernameInputLayout.setError(getString(R.string.error_username_required));
                isValid = false;
                Log.w(TAG, "validateInput: Username is empty");
            } else if (!ValidationUtils.isValidUsername(username)) {
                usernameInputLayout.setError(getString(R.string.error_invalid_username));
                isValid = false;
                Log.w(TAG, "validateInput: Username is invalid");
            }

            if (password.isEmpty()) {
                passwordInputLayout.setError(getString(R.string.error_password_required));
                isValid = false;
                Log.w(TAG, "validateInput: Password is empty");
            } else if (!ValidationUtils.isValidPassword(password)) {
                passwordInputLayout.setError(getString(R.string.error_invalid_password));
                isValid = false;
                Log.w(TAG, "validateInput: Password is invalid");
            }
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
            showAuthenticationError("Invalid username or password");
            return;
        }

        // Verify password
        boolean passwordMatches = PasswordUtils.verifyPassword(password, user.getSalt(), user.getPasswordHash());

        if (!passwordMatches) {
            // Wrong password
            Log.w(TAG, "handleSignIn: Password verification failed for username: " + username);
            showAuthenticationError("Invalid username or password");
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

    // =============================================================================================
    // REGISTRATION (Commit 6)
    // =============================================================================================

    /**
     * Handle user registration flow.
     * Creates new user account with hashed password and auto-login.
     * Called internally from handleButtonClick() when in registration mode.
     */
    private void handleRegister() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        Log.d(TAG, "handleRegister: Attempting registration for username: " + username);

        // Check if username already exists
        if (userDAO.usernameExists(username)) {
            Log.w(TAG, "handleRegister: Username already exists: " + username);
            usernameInputLayout.setError("Username already taken");
            Toast.makeText(this, "Username already taken. Please choose another.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate salt and hash password
        String salt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hashPassword(password, salt);

        if (passwordHash == null) {
            Log.e(TAG, "handleRegister: Failed to hash password");
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create User object
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(passwordHash);
        newUser.setSalt(salt);
        newUser.setDisplayName(username);  // Default display name to username
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        newUser.setActive(true);

        try {
            // Insert user into database
            long userId = userDAO.insertUser(newUser);

            if (userId > 0) {
                Log.i(TAG, "handleRegister: Successfully registered user_id: " + userId);
                newUser.setUserId(userId);

                // Auto-login: Update last_login timestamp
                userDAO.updateLastLogin(userId, LocalDateTime.now());

                // Create session
                sessionManager.createSession(newUser);
                Log.d(TAG, "handleRegister: Session created for new user_id: " + userId);

                // Navigate to MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();  // Prevent back button from returning to login

                Toast.makeText(this, "Welcome, " + username + "! Your account has been created.", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "handleRegister: Registration complete, navigated to MainActivity");

            } else {
                Log.e(TAG, "handleRegister: Insert returned invalid user_id");
                Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
            }

        } catch (DuplicateUsernameException e) {
            Log.w(TAG, "handleRegister: Duplicate username caught: " + username, e);
            usernameInputLayout.setError("Username already taken");
            Toast.makeText(this, "Username already taken. Please choose another.", Toast.LENGTH_SHORT).show();

        } catch (DatabaseException e) {
            Log.e(TAG, "handleRegister: Database error during registration", e);
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // =============================================================================================
    // ERROR HANDLING
    // =============================================================================================

    /**
     * Show authentication error using Snackbar.
     *
     * **Security:** Uses prominent Snackbar instead of Toast for better visibility.
     * All authentication errors (empty fields, invalid credentials) use same styling
     * to prevent information leakage.
     *
     * @param message Generic error message to display
     */
    private void showAuthenticationError(String message) {
        Snackbar.make(findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.error, null))
                .setTextColor(getResources().getColor(android.R.color.white, null))
                .show();
    }

    // =============================================================================================
    // TAB SWITCHING (Commit 7)
    // =============================================================================================

    /**
     * Switch to Sign In mode.
     * Updates UI to show sign-in form and visual tab indication.
     */
    private void switchToSignInMode() {
        if (isSignInMode) {
            return;  // Already in sign-in mode
        }

        Log.d(TAG, "switchToSignInMode: Switching to Sign In mode");

        isSignInMode = true;

        // Update button text
        signInButton.setText(R.string.btn_sign_in);

        // Update tab visual indication
        tabSignIn.setBackgroundResource(R.drawable.bg_tab_active);
        tabSignIn.setTextColor(getResources().getColor(R.color.primary_teal, null));

        tabRegister.setBackgroundResource(android.R.color.transparent);
        tabRegister.setTextColor(getResources().getColor(R.color.text_secondary, null));

        // Clear any error messages
        usernameInputLayout.setError(null);
        passwordInputLayout.setError(null);

        Log.d(TAG, "switchToSignInMode: Switched to Sign In mode");
    }

    /**
     * Switch to Register mode.
     * Updates UI to show registration form and visual tab indication.
     */
    private void switchToRegisterMode() {
        if (!isSignInMode) {
            return;  // Already in register mode
        }

        Log.d(TAG, "switchToRegisterMode: Switching to Register mode");

        isSignInMode = false;

        // Update button text
        signInButton.setText(R.string.btn_create_account);

        // Update tab visual indication
        tabRegister.setBackgroundResource(R.drawable.bg_tab_active);
        tabRegister.setTextColor(getResources().getColor(R.color.primary_teal, null));

        tabSignIn.setBackgroundResource(android.R.color.transparent);
        tabSignIn.setTextColor(getResources().getColor(R.color.text_secondary, null));

        // Clear any error messages
        usernameInputLayout.setError(null);
        passwordInputLayout.setError(null);

        Log.d(TAG, "switchToRegisterMode: Switched to Register mode");
    }
}
