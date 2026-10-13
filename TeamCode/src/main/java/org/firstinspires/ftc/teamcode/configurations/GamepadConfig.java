package org.firstinspires.ftc.teamcode.configurations;

import com.qualcomm.robotcore.hardware.Gamepad;

/**
 * GamepadConfig.java
 *
 * STUDENT GUIDE: Gamepad Input Abstraction & Edge Detection
 *
 * PURPOSE:
 * This class wraps the FTC SDK's Gamepad class to provide clean, reusable
 * access to a single controller's sticks and buttons. It only exposes raw
 * input - it doesn't know or care what those inputs will be used for.
 * Wire buttons to specific actions in your OpMode, not here.
 *
 * BUTTON BASICS:
 * - Every button method here is EDGE-DETECTED: it returns true only on the
 *   ONE loop cycle where the button transitions from released to pressed
 * - Holding the button down does NOT keep returning true every cycle
 * - This guards against a single physical press accidentally triggering an
 *   action dozens of times (the OpMode loop runs far faster than a human
 *   can release a button)
 *
 * STICK BASICS:
 * - Sticks return a continuous decimal value, not true/false
 * - Range is -1.0 to 1.0
 * - Raw FTC sticks report "up" as negative - we flip the sign so pushing
 *   the stick up returns a positive value (more intuitive)
 *
 * FTC SDK CONCEPTS:
 * - Gamepad: FTC SDK's raw controller object (gamepad1/gamepad2 in OpMode)
 * - Button fields (gamepad.a, gamepad.dpad_up, etc.): true/false, read
 *   fresh every time you access them - the SDK itself does NOT debounce
 * - Stick fields (gamepad.left_stick_y, etc.): -1.0 to 1.0 analog values
 */

public class GamepadConfig {

    // The actual FTC SDK gamepad object (gamepad1 or gamepad2 from OpMode)
    private Gamepad gamepad;

    // Track each button's previous-cycle state, so we can detect the exact
    // moment it transitions from released to pressed (edge detection)
    private boolean prevA = false;
    private boolean prevB = false;
    private boolean prevX = false;
    private boolean prevY = false;
    private boolean prevDpadUp = false;
    private boolean prevDpadDown = false;
    private boolean prevDpadLeft = false;
    private boolean prevDpadRight = false;
    private boolean prevLeftBumper = false;
    private boolean prevRightBumper = false;
    private boolean prevLeftTrigger = false;
    private boolean prevRightTrigger = false;
    private boolean prevStart = false;
    private boolean prevOptions = false;
    private boolean prevLeftStickButton = false;
    private boolean prevRightStickButton = false;

    // How far a trigger must be pulled (0.0 - 1.0) to count as "pressed"
    private static final double TRIGGER_THRESHOLD = 0.5;

    /**
     * Constructor - Initialize the gamepad wrapper
     *
     * STUDENT: Pass in gamepad1 or gamepad2 from your OpMode, depending on
     * which physical controller you want this instance to read.
     *
     * @param gamepad The FTC Gamepad object (gamepad1 or gamepad2)
     *
     * EXAMPLE USAGE:
     * GamepadConfig driver = new GamepadConfig(gamepad1);
     * GamepadConfig operator = new GamepadConfig(gamepad2);
     * // Now you can use: driver.isAPressed(), driver.getLeftStickY(), etc.
     *
     * NOTE: One GamepadConfig instance tracks state for ONE controller.
     * If you use both gamepad1 and gamepad2, create two separate instances -
     * sharing one instance between controllers would mix up whose button
     * was pressed.
     */
    public GamepadConfig(Gamepad gamepad) {
        this.gamepad = gamepad;
    }

    // ========== STICKS ==========

    /**
     * Get left stick Y-axis position
     *
     * STUDENT: Reads how far up/down the left stick is pushed.
     *
     * @return -1.0 (full down) to 1.0 (full up)
     *
     * FTC SDK DETAIL:
     * gamepad.left_stick_y is the raw SDK value, where pushing UP reports
     * NEGATIVE and pushing DOWN reports POSITIVE - backwards from what
     * most people expect. We negate it here so up = positive.
     *
     * EXAMPLE:
     * double power = driver.getLeftStickY();
     * motor.setPower(power);
     */
    public double getLeftStickY() {
        // FTC SDK: Query left stick Y-axis, flip sign so up = positive
        return -gamepad.left_stick_y;
    }

    /**
     * Get right stick Y-axis position
     *
     * STUDENT: Same idea as getLeftStickY(), for the right stick.
     *
     * @return -1.0 (full down) to 1.0 (full up)
     *
     * FTC SDK DETAIL:
     * gamepad.right_stick_y is negated for the same reason as the left
     * stick - the raw SDK value reports "up" as negative.
     */
    public double getRightStickY() {
        // FTC SDK: Query right stick Y-axis, flip sign so up = positive
        return -gamepad.right_stick_y;
    }

    /**
     * Get left stick X-axis position
     *
     * STUDENT: Reads how far left/right the left stick is pushed.
     *
     * @return -1.0 (full left) to 1.0 (full right)
     *
     * FTC SDK DETAIL:
     * Unlike the Y-axis, gamepad.left_stick_x does NOT need flipping -
     * pushing right already reports positive, matching what you'd expect.
     * Only Y needed the sign flip.
     */
    public double getLeftStickX() {
        // FTC SDK: Query left stick X-axis (no sign flip needed)
        return gamepad.left_stick_x;
    }

    /**
     * Get right stick X-axis position
     *
     * STUDENT: Same idea as getLeftStickX(), for the right stick.
     *
     * @return -1.0 (full left) to 1.0 (full right)
     */
    public double getRightStickX() {
        // FTC SDK: Query right stick X-axis (no sign flip needed)
        return gamepad.right_stick_x;
    }

    // ========== FACE BUTTONS ==========

    /**
     * Check if A was just pressed
     *
     * STUDENT: Returns true for exactly one loop cycle per physical press.
     *
     * @return true only on the cycle A transitions from released to pressed
     *
     * FTC SDK DETAIL:
     * gamepad.a is the SDK's raw, undebounced button state (true while
     * held). We compare it against last cycle's state (prevA) to find the
     * exact moment it was newly pressed, then remember today's state for
     * next cycle's comparison.
     *
     * STUDENT LEARNING:
     * This is EDGE DETECTION - detecting a state TRANSITION, not just a
     * state. Without it, holding A down for half a second (which is many
     * loop cycles) would trigger your action dozens of times instead of
     * once.
     *
     * EXAMPLE:
     * if (driver.isAPressed()) {
     *     // Runs ONCE per press, no matter how long A is held
     * }
     */
    public boolean isAPressed() {
        boolean currentPress = gamepad.a;
        boolean wasPressed = currentPress && !prevA;
        prevA = currentPress;
        return wasPressed;
    }

    /**
     * Check if B was just pressed
     *
     * STUDENT: Same edge-detection pattern as isAPressed(), for B.
     *
     * @return true only on the cycle B transitions from released to pressed
     */
    public boolean isBPressed() {
        boolean currentPress = gamepad.b;
        boolean wasPressed = currentPress && !prevB;
        prevB = currentPress;
        return wasPressed;
    }

    /**
     * Check if X was just pressed
     *
     * STUDENT: Same edge-detection pattern as isAPressed(), for X.
     *
     * @return true only on the cycle X transitions from released to pressed
     */
    public boolean isXPressed() {
        boolean currentPress = gamepad.x;
        boolean wasPressed = currentPress && !prevX;
        prevX = currentPress;
        return wasPressed;
    }

    /**
     * Check if Y was just pressed
     *
     * STUDENT: Same edge-detection pattern as isAPressed(), for Y.
     *
     * @return true only on the cycle Y transitions from released to pressed
     */
    public boolean isYPressed() {
        boolean currentPress = gamepad.y;
        boolean wasPressed = currentPress && !prevY;
        prevY = currentPress;
        return wasPressed;
    }

    // ========== DPAD ==========

    /**
     * Check if Dpad Up was just pressed
     *
     * STUDENT: Same edge-detection pattern as isAPressed(), for Dpad Up.
     * Handy for stepping a value up by a fixed amount per press.
     *
     * @return true only on the cycle Dpad Up transitions from released to pressed
     *
     * EXAMPLE:
     * if (operator.isDpadUpPressed()) {
     *     targetPosition += 10;  // Step up by a fixed amount, once per press
     * }
     */
    public boolean isDpadUpPressed() {
        boolean currentPress = gamepad.dpad_up;
        boolean wasPressed = currentPress && !prevDpadUp;
        prevDpadUp = currentPress;
        return wasPressed;
    }

    /**
     * Check if Dpad Down was just pressed
     *
     * STUDENT: Same edge-detection pattern as isAPressed(), for Dpad Down.
     *
     * @return true only on the cycle Dpad Down transitions from released to pressed
     */
    public boolean isDpadDownPressed() {
        boolean currentPress = gamepad.dpad_down;
        boolean wasPressed = currentPress && !prevDpadDown;
        prevDpadDown = currentPress;
        return wasPressed;
    }

    /**
     * Check if Dpad Left was just pressed
     *
     * STUDENT: Same edge-detection pattern as isAPressed(), for Dpad Left.
     *
     * @return true only on the cycle Dpad Left transitions from released to pressed
     */
    public boolean isDpadLeftPressed() {
        boolean currentPress = gamepad.dpad_left;
        boolean wasPressed = currentPress && !prevDpadLeft;
        prevDpadLeft = currentPress;
        return wasPressed;
    }

    /**
     * Check if Dpad Right was just pressed
     *
     * STUDENT: Same edge-detection pattern as isAPressed(), for Dpad Right.
     *
     * @return true only on the cycle Dpad Right transitions from released to pressed
     */
    public boolean isDpadRightPressed() {
        boolean currentPress = gamepad.dpad_right;
        boolean wasPressed = currentPress && !prevDpadRight;
        prevDpadRight = currentPress;
        return wasPressed;
    }

    // ========== BUMPERS ==========

    /**
     * Check if the left bumper was just pressed
     *
     * STUDENT: Same edge-detection pattern as isAPressed(), for the left bumper.
     * Bumpers are often used for one-shot actions like toggles, since a
     * toggle that fired every cycle while held would just flicker.
     *
     * @return true only on the cycle the left bumper transitions from released to pressed
     */
    public boolean isLeftBumperPressed() {
        boolean currentPress = gamepad.left_bumper;
        boolean wasPressed = currentPress && !prevLeftBumper;
        prevLeftBumper = currentPress;
        return wasPressed;
    }

    /**
     * Check if the right bumper was just pressed
     *
     * STUDENT: Same edge-detection pattern as isAPressed(), for the right bumper.
     *
     * @return true only on the cycle the right bumper transitions from released to pressed
     */
    public boolean isRightBumperPressed() {
        boolean currentPress = gamepad.right_bumper;
        boolean wasPressed = currentPress && !prevRightBumper;
        prevRightBumper = currentPress;
        return wasPressed;
    }

    // ========== TRIGGERS ==========

    /**
     * Get left trigger pull amount
     *
     * STUDENT: Triggers are ANALOG - they report how far they are pulled,
     * not just pressed/released. Good for variable speed control.
     *
     * @return 0.0 (released) to 1.0 (fully pulled)
     *
     * FTC SDK DETAIL:
     * gamepad.left_trigger is already 0.0 to 1.0, no sign flip needed.
     */
    public double getLeftTrigger() {
        return gamepad.left_trigger;
    }

    /**
     * Get right trigger pull amount
     *
     * STUDENT: Same idea as getLeftTrigger(), for the right trigger.
     *
     * @return 0.0 (released) to 1.0 (fully pulled)
     */
    public double getRightTrigger() {
        return gamepad.right_trigger;
    }

    /**
     * Check if the left trigger was just pulled past the threshold
     *
     * STUDENT: Treats the trigger like a button. Same edge-detection pattern
     * as isAPressed(), but "pressed" means pulled past TRIGGER_THRESHOLD.
     *
     * @return true only on the cycle the trigger crosses the threshold
     */
    public boolean isLeftTriggerPressed() {
        boolean currentPress = gamepad.left_trigger > TRIGGER_THRESHOLD;
        boolean wasPressed = currentPress && !prevLeftTrigger;
        prevLeftTrigger = currentPress;
        return wasPressed;
    }

    /**
     * Check if the right trigger was just pulled past the threshold
     *
     * STUDENT: Same edge-detection pattern as isLeftTriggerPressed().
     *
     * @return true only on the cycle the trigger crosses the threshold
     */
    public boolean isRightTriggerPressed() {
        boolean currentPress = gamepad.right_trigger > TRIGGER_THRESHOLD;
        boolean wasPressed = currentPress && !prevRightTrigger;
        prevRightTrigger = currentPress;
        return wasPressed;
    }

    // ========== CENTER BUTTONS ==========

    /**
     * Check if Start was just pressed
     *
     * STUDENT: Same edge-detection pattern as isAPressed(), for Start.
     *
     * @return true only on the cycle Start transitions from released to pressed
     */
    public boolean isStartPressed() {
        boolean currentPress = gamepad.start;
        boolean wasPressed = currentPress && !prevStart;
        prevStart = currentPress;
        return wasPressed;
    }

    /**
     * Check if Options was just pressed
     *
     * STUDENT: Same edge-detection pattern as isAPressed(), for Options
     * (the "Back"/"Select" button on Xbox-style controllers).
     *
     * @return true only on the cycle Options transitions from released to pressed
     */
    public boolean isOptionsPressed() {
        boolean currentPress = gamepad.options;
        boolean wasPressed = currentPress && !prevOptions;
        prevOptions = currentPress;
        return wasPressed;
    }

    // ========== STICK BUTTONS ==========

    /**
     * Check if the left stick button was just pressed (clicking the stick in)
     *
     * STUDENT: Same edge-detection pattern as isAPressed().
     *
     * @return true only on the cycle the click transitions from released to pressed
     */
    public boolean isLeftStickButtonPressed() {
        boolean currentPress = gamepad.left_stick_button;
        boolean wasPressed = currentPress && !prevLeftStickButton;
        prevLeftStickButton = currentPress;
        return wasPressed;
    }

    /**
     * Check if the right stick button was just pressed (clicking the stick in)
     *
     * STUDENT: Same edge-detection pattern as isAPressed().
     *
     * @return true only on the cycle the click transitions from released to pressed
     */
    public boolean isRightStickButtonPressed() {
        boolean currentPress = gamepad.right_stick_button;
        boolean wasPressed = currentPress && !prevRightStickButton;
        prevRightStickButton = currentPress;
        return wasPressed;
    }
}
