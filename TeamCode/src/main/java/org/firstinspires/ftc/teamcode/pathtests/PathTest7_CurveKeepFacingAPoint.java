package org.firstinspires.ftc.teamcode.pathtests;

// ---- Imports: tell Java where to find the classes this file uses ----
import com.acmerobotics.dashboard.FtcDashboard;                                // FTC Dashboard (the web page on the laptop)
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;                // sends telemetry to Driver Station AND Dashboard
import com.pedropathing.api.Paths;                                            // Pedro: makes paths (line, curve, through)
import com.pedropathing.follower.Follower;                                    // Pedro: drives the robot along a path
import com.pedropathing.paths.Path;                                           // Pedro: one path
import com.qualcomm.robotcore.eventloop.opmode.OpMode;                        // FTC SDK: the OpMode we build on
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;                        // FTC SDK: puts the OpMode on the Driver Station list
import com.qualcomm.robotcore.util.ElapsedTime;                               // FTC SDK: a stopwatch

import org.firstinspires.ftc.teamcode.configurations.GamepadConfig;           // our gamepad helper (one press = one action)
import org.firstinspires.ftc.teamcode.configurations.PathPoses;               // our shared points + pause time
import org.firstinspires.ftc.teamcode.pedro.Constants;                        // our tuned Follower settings
import org.firstinspires.ftc.teamcode.utils.PathTestHelper;                   // poses, measuring, drawing, telemetry

/**
 * PathTest7_CurveKeepFacingAPoint.java  -  PATH TEST 7: A CURVE, ALWAYS LOOKING AT ONE SPOT
 *
 * <p><b>WHAT DOES THIS TEST DO?</b><br>
 * The robot drives the same curve as Test 3, but it keeps LOOKING at the Facing Point
 * the whole time, turning as it moves, like a camera tracking a target. Robots do this
 * to aim at a goal while driving. The Endpoint's heading is not used.
 *
 * <p><b>BEFORE YOU START</b><br>
 * The robot turns to face the Facing Point right at the start. Set the Starting Point
 * heading so it already points at the Facing Point (for the defaults, 45) to avoid a
 * sudden turn.
 *
 * <p><b>WHAT TO COMPARE</b><br>
 * Run Test 3 and this test with the same points. Does turning to aim make the robot
 * miss the path more? Move the Facing Point close to the path: what happens?
 *
 * <p><b>THE BUTTONS (gamepad 1)</b>
 * <ul>
 *   <li><b>A = Start</b>: drive the path, using the newest numbers from Dashboard.</li>
 *   <li><b>B = Stop</b>: stop and hold still, at any time.</li>
 *   <li><b>Y = Restart</b>: drive back to the Starting Point, then get ready again.</li>
 * </ul>
 * After each run the robot waits at the Endpoint for
 * {@code PathPoses.PAUSE_AT_ENDPOINT_SECONDS}, then drives back by itself.
 *
 * <p><b>HOW AN OPMODE RUNS</b><br>
 * The FTC SDK calls these methods for us, in this order:
 * <pre>
 *   INIT pressed  → init()        once
 *                 → init_loop()   again and again, until PLAY
 *   PLAY pressed  → start()       once
 *                 → loop()        again and again (about 50 times a second), until STOP
 *   STOP pressed  → stop()        once
 * </pre>
 *
 * <p><b>THE STEPS (a "state machine")</b><br>
 * The robot is always in exactly one Step. Each loop we check if it is time to move on:
 * <pre>
 *   READY ──A──► DRIVING_PATH ──arrived──► PAUSING_AT_ENDPOINT ──time up──► RETURNING ──arrived──► READY
 *   B from any Step ──► STOPPED          Y from any Step ──► RETURNING (or READY if already there)
 * </pre>
 *
 * <p><b>WHAT IS DIFFERENT IN EACH PATH TEST?</b><br>
 * All Path Tests are the same file except the lines marked ★: the {@code @TeleOp} name,
 * the class name, {@code buildPath()}, and the names given to the helper.
 *
 * <p>Full explanation: {@code doc/PathTests-LLD.md}.
 */
@TeleOp(name = "Path 7: Curve - Keep Facing a Point", group = "Path Tests")  // ★ name on the Driver Station
public class PathTest7_CurveKeepFacingAPoint extends OpMode {  // ★ "extends OpMode": the SDK runs our init/loop/...

    /** The Steps the robot goes through. An enum is a fixed list of names. */
    enum Step { READY, DRIVING_PATH, PAUSING_AT_ENDPOINT, RETURNING, STOPPED }

    // ---- Things this OpMode keeps between loops ----
    Follower follower;                          // Pedro Pathing: drives the robot along a path
    GamepadConfig driver;                       // gamepad 1, with "just pressed" buttons
    PathTestHelper pathTestHelper;              // poses, measuring, drawing, telemetry
    Path path;                                  // the path being driven (empty until the first A press)
    Step step = Step.READY;                     // the Step we are in right now
    ElapsedTime pauseTimer = new ElapsedTime(); // stopwatch for the pause at the Endpoint

    /**
     * ★ THE PATH FOR THIS TEST. This is the one idea this test is about.
     *
     * <p>A curve from the Starting Point to the Endpoint, pulled toward Control Point 1.
     * {@code .facingPoint(...)} keeps the robot pointed at the Facing Point the whole way.
     *
     * <p>It reads the NEWEST numbers from Dashboard every time it is called. It is written the
     * same way as the Pedro Pathing Visualizer's exported code.
     *
     * <p>STUDENT: put the Facing Point right in the middle of the curve. How fast does the
     * robot have to turn as it drives past?
     *
     * @return a new path built from the current Path Poses
     */
    Path buildPath() {
        return Paths.curve(pathTestHelper.startingPoint(), pathTestHelper.controlPoint1(), pathTestHelper.endpoint())  // ★ same curve as Test 3
                .facingPoint(pathTestHelper.facingPoint());  // ★ keep looking at the Facing Point
    }

    /** Runs ONCE when INIT is pressed. Sets everything up. The robot does not move yet. */
    @Override
    public void init() {
        // Send telemetry to BOTH the Driver Station and the Dashboard web page.
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        // Build the Pedro Pathing Follower with our tuned settings (motors, Pinpoint, controller).
        follower = Constants.create(hardwareMap);

        // Wrap gamepad 1 so each button press counts once (see GamepadConfig).
        driver = new GamepadConfig(gamepad1);

        // Make the helper and give it the names to show on screen.
        pathTestHelper = new PathTestHelper("Path 7: Curve - Keep Facing a Point", "Facing Point");  // ★ test name, heading type

        // Tell the Follower the robot is at the Starting Point, so the screen shows it there.
        follower.setPose(pathTestHelper.startingPoint());
    }

    /** Runs again and again after INIT, until PLAY is pressed. Use it to check the path. */
    @Override
    public void init_loop() {
        // Draw the planned path on the Dashboard field. Change numbers in Dashboard
        // and the picture changes too, before the robot moves at all.
        pathTestHelper.preview(buildPath());

        pathTestHelper.showTelemetry(telemetry, follower, "WAITING FOR PLAY",
                "Put the robot on the Starting Point, then press PLAY");
    }

    /** Runs ONCE when PLAY is pressed. */
    @Override
    public void start() {
        // Set it again: the Starting Point may have been changed in Dashboard during INIT.
        follower.setPose(pathTestHelper.startingPoint());
        step = Step.READY;
    }

    /** Runs again and again (about 50 times a second) until STOP is pressed. */
    @Override
    public void loop() {
        // ALWAYS FIRST: the Follower reads the Pinpoint (where are we?) and sets the motor
        // powers (how do we stay on the path?). Without this line the robot never moves.
        follower.update();

        // Read each button ONCE per loop and keep the answer. GamepadConfig remembers the
        // button inside each isXPressed() call, so asking twice in one loop would miss presses.
        boolean startPressed = driver.isAPressed();
        boolean stopPressed = driver.isBPressed();
        boolean restartPressed = driver.isYPressed();

        // A short message for the screen. Usually empty; set only when the driver needs to know something.
        String telemetryMessage = "";

        // B = Stop works in every Step: hold the robot still where it is.
        if (stopPressed) {
            follower.hold(follower.pose());
            step = Step.STOPPED;
        }

        // Y = Restart works in every Step: go back to the Starting Point.
        if (restartPressed) {
            driveBackToStartingPoint();
        }

        // Do the work for the Step we are in. "switch" runs only the matching "case".
        switch (step) {
            case READY:
                if (!pathTestHelper.isAtStartingPoint(follower.pose())) {
                    // The Starting Point was moved in Dashboard: don't start from the wrong place.
                    telemetryMessage = "Starting Point moved. Press Y to drive there first";
                } else if (startPressed) {
                    path = buildPath();                     // newest numbers from Dashboard
                    pathTestHelper.startRun(path);          // start the stopwatch and measuring
                    follower.follow(path);                  // Pedro: start driving the path
                    step = Step.DRIVING_PATH;
                } else {
                    // Ready and waiting: tell the driver what to do.
                    telemetryMessage = "Press A to start";
                }
                break;

            case DRIVING_PATH:
                // isBusy() stays true until the robot reaches the end AND settles there.
                if (!follower.isBusy()) {
                    pathTestHelper.finishRun(follower, path);   // work out "Last run" results
                    pauseTimer.reset();                         // start the pause stopwatch
                    step = Step.PAUSING_AT_ENDPOINT;
                }
                break;

            case PAUSING_AT_ENDPOINT:
                // Pedro keeps holding the robot at the Endpoint while we wait.
                if (pauseTimer.seconds() >= PathPoses.PAUSE_AT_ENDPOINT_SECONDS) {
                    driveBackToStartingPoint();
                }
                break;

            case RETURNING:
                if (!follower.isBusy()) {        // back at the Starting Point
                    step = Step.READY;
                }
                break;

            case STOPPED:
                break;                                      // wait for Y (Restart)
        }

        pathTestHelper.track(follower);                     // measure how far off the path we are
        pathTestHelper.draw(follower, path);                // field picture on Dashboard
        pathTestHelper.showTelemetry(telemetry, follower, step.toString(), telemetryMessage);
    }

    /** Runs ONCE when STOP is pressed. */
    @Override
    public void stop() {
        follower.stop();                         // stop the motors
    }

    /**
     * Sends the robot back to the Starting Point (used by Y and after the pause).
     *
     * <p>If the robot is already there, it just holds still and is READY. Otherwise it follows
     * a straight line back and the Step becomes RETURNING.
     */
    void driveBackToStartingPoint() {
        if (pathTestHelper.isAtStartingPoint(follower.pose())) {
            // Already there. (A line from a point to itself has no direction, so don't make one.)
            follower.hold(pathTestHelper.startingPoint());
            step = Step.READY;
        } else {
            follower.follow(pathTestHelper.pathBackToStartingPoint(follower.pose()));
            step = Step.RETURNING;
        }
    }
}
