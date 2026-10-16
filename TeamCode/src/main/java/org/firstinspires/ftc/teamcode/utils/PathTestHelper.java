package org.firstinspires.ftc.teamcode.utils;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.utils.Angle;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.configurations.FieldPoint;
import org.firstinspires.ftc.teamcode.configurations.PathPoses;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * PathTestHelper.java  -  PATH TOOLING SHARED BY ALL PATH TESTS
 *
 * <p><b>WHAT IS THIS FILE?</b><br>
 * The Path Test OpModes (in the {@code pathtests} folder) all need the same "extra" jobs
 * done around driving a path. This class does those jobs so each OpMode stays short
 * and easy to read:
 * <ol>
 *   <li><b>Poses:</b> turns the numbers in {@link PathPoses} into Pedro Pathing
 *       {@link Pose}s ({@link #startingPoint()}, {@link #endpoint()}, ...).</li>
 *   <li><b>Return path:</b> builds the path that drives the robot back to the
 *       Starting Point ({@link #pathBackToStartingPoint(Pose)}).</li>
 *   <li><b>Measuring:</b> times each run and measures how far off the path the
 *       robot went ({@link #startRun}, {@link #track}, {@link #finishRun}).</li>
 *   <li><b>Drawing:</b> draws the path and the robot on the FTC Dashboard field
 *       ({@link #preview}, {@link #draw}).</li>
 *   <li><b>Telemetry:</b> writes the information screen ({@link #showTelemetry}).</li>
 * </ol>
 *
 * <p><b>WHAT THIS CLASS NEVER DOES</b><br>
 * It never creates the Follower and never moves the robot. Creating the Follower,
 * calling {@code follower.update()}, {@code follower.follow(...)} and checking
 * {@code follower.isBusy()} all happen in the OpMode, where you can see them.
 *
 * <p><b>COORDINATES</b><br>
 * Same as the Pedro Pathing Visualizer: inches, (0, 0) at the bottom-left corner of the
 * field, the field is 141.5 inches wide, headings typed in degrees.
 * See {@code doc/PathTests-LLD.md} for diagrams.
 *
 * <p>STUDENT: you do not need to change this file. Read it if you are curious how the
 * measuring and drawing work.
 */
public class PathTestHelper {

    // ==================== FIXED VALUES ====================
    // These are implementation details, not settings. Settings live in
    // the configurations folder (PathPoses).

    /**
     * Pedro Pathing's "pose maker". {@code PoseFactory.degrees()} means we give it
     * headings in DEGREES and it hands Pedro a Pose in RADIANS (what Pedro uses inside).
     * The Pedro Pathing Visualizer's exported code uses exactly this.
     */
    private static final PoseFactory POSES = PoseFactory.degrees();

    /** How close (in inches) the robot must be to the Starting Point to start a new run. */
    private static final double AT_START_TOLERANCE_INCHES = 2.0;

    /** Width of the field in inches (same as the Pedro Pathing Visualizer). */
    private static final double FIELD_SIZE = 141.5;

    /** Radius of the robot circle drawn on the Dashboard field, in inches. */
    private static final double ROBOT_RADIUS = 9.0;

    /** Radius of the dots drawn for each point, in inches. */
    private static final double POINT_RADIUS = 2.0;

    /** How many steps to split the path into when drawing it (more = smoother line). */
    private static final int PATH_DRAW_STEPS = 50;

    /** The most robot positions we remember for the trail (stops the list growing forever). */
    private static final int MAX_TRAIL_POINTS = 1000;

    // Dashboard colors (any web color name works)
    private static final String COLOR_PLANNED_PATH = "gray";
    private static final String COLOR_STARTING_POINT = "green";
    private static final String COLOR_CONTROL_POINT = "blue";
    private static final String COLOR_ENDPOINT = "red";
    private static final String COLOR_FACING_POINT = "orange";
    private static final String COLOR_TRAIL = "purple";
    private static final String COLOR_ROBOT = "black";

    // ==================== WHAT THIS TEST IS ====================

    /** Shown at the top of telemetry, e.g. "Path 1: Straight Line - Keep Heading". */
    private final String testName;

    /** Shown in telemetry; the same word as the visualizer's heading dropdown, e.g. "Constant". */
    private final String headingName;

    /** FTC Dashboard, used to send the field picture. */
    private final FtcDashboard dashboard;

    // Which optional points this test uses. They start false and become true the first
    // time buildPath() asks for that point, so the field picture and telemetry only
    // show the points this test actually uses.
    private boolean usesControlPoint1 = false;
    private boolean usesControlPoint2 = false;
    private boolean usesFacingPoint = false;

    // ==================== MEASURING ====================

    /** Stopwatch for the current run. */
    private final ElapsedTime runTimer = new ElapsedTime();

    /** True between startRun() and finishRun(), i.e. while we are measuring. */
    private boolean running = false;

    /** How far the robot is from the path right now, in inches. */
    private double missNow = 0;

    /** The biggest "missNow" during this run, in inches. */
    private double biggestMiss = 0;

    /** The "Last run: ..." line shown in telemetry. */
    private String lastRunText = "none yet - press A";

    /** Where the robot has been during this run (Dashboard coordinates), for the trail. */
    private final List<Double> trailX = new ArrayList<>();
    private final List<Double> trailY = new ArrayList<>();

    /**
     * Makes the helper for one Path Test.
     *
     * <p>EXAMPLE: {@code new PathTestHelper("Path 1: Straight Line - Keep Heading", "Constant")}
     *
     * @param testName    the test's name, shown at the top of telemetry
     * @param headingName the heading type this test uses, in visualizer words:
     *                    "Constant", "Linear", "Tangent", "Reverse Tangent" or "Facing Point"
     */
    public PathTestHelper(String testName, String headingName) {
        this.testName = testName;
        this.headingName = headingName;
        // FTC Dashboard is a single shared object; getInstance() gives us that one object.
        this.dashboard = FtcDashboard.getInstance();
    }

    // ==================== 1. POSES ====================
    // Each method reads the NEWEST numbers from PathPoses (which Dashboard may have
    // just changed) and turns them into a Pedro Pathing Pose.

    /**
     * The Starting Point, as a Pedro Pathing pose.
     *
     * @return where every path begins (from {@link PathPoses#STARTING_POINT})
     */
    public Pose startingPoint() {
        return toPose(PathPoses.STARTING_POINT);
    }

    /**
     * Control Point 1, as a Pedro Pathing pose.
     *
     * @return the first point that pulls the curve (from {@link PathPoses#CONTROL_POINT_1})
     */
    public Pose controlPoint1() {
        usesControlPoint1 = true;   // remember to draw it and show it in telemetry
        return toPose(PathPoses.CONTROL_POINT_1);
    }

    /**
     * Control Point 2, as a Pedro Pathing pose.
     *
     * @return the second point that pulls the curve (from {@link PathPoses#CONTROL_POINT_2})
     */
    public Pose controlPoint2() {
        usesControlPoint2 = true;
        return toPose(PathPoses.CONTROL_POINT_2);
    }

    /**
     * The Endpoint, as a Pedro Pathing pose.
     *
     * @return where every path ends (from {@link PathPoses#ENDPOINT})
     */
    public Pose endpoint() {
        return toPose(PathPoses.ENDPOINT);
    }

    /**
     * The Facing Point, as a Pedro Pathing pose.
     *
     * @return the spot the robot keeps looking at (from {@link PathPoses#FACING_POINT})
     */
    public Pose facingPoint() {
        usesFacingPoint = true;
        return toPose(PathPoses.FACING_POINT);
    }

    /**
     * Turns one configured point into a Pedro Pathing Pose.
     *
     * <p>PEDRO PATHING DETAIL: {@code POSES.of(x, y, heading)} makes a Pose and converts the
     * heading from degrees to radians, because {@code POSES} is {@code PoseFactory.degrees()}.
     */
    private static Pose toPose(FieldPoint point) {
        return POSES.of(point.x, point.y, point.heading);
    }

    // ==================== 2. RETURN PATH ====================

    /**
     * Makes the path that drives the robot back to the Starting Point.
     *
     * <p>It is a straight line from where the robot is now, and the robot turns smoothly
     * from its current heading to the Starting Point heading while it drives
     * ("Linear" heading, like the visualizer).
     *
     * <p>It starts from the robot's CURRENT pose (not the Endpoint) so it also works after
     * you press B (Stop) in the middle of a path.
     *
     * <p>Only use this when the robot is NOT already at the Starting Point
     * (check with {@link #isAtStartingPoint(Pose)}): a line from a point to the same point
     * has no direction, so Pedro cannot follow it.
     *
     * @param from where the robot is right now, usually {@code follower.pose()}
     * @return a path the OpMode can give to {@code follower.follow(...)}
     */
    public Path pathBackToStartingPoint(Pose from) {
        Pose start = startingPoint();
        // Paths.line(a, b)  = a straight line from a to b
        // .linear(a, b)     = turn smoothly from a's heading to b's heading along the way
        return Paths.line(from, start).linear(from, start);
    }

    /**
     * Checks whether the robot is close enough to the Starting Point to begin a run.
     *
     * <p>"Close enough" means within 2 inches (AT_START_TOLERANCE_INCHES). This becomes
     * false if you move the Starting Point in Dashboard, because the robot is no longer on it.
     *
     * @param robot where the robot is right now, usually {@code follower.pose()}
     * @return true if the robot is at the Starting Point
     */
    public boolean isAtStartingPoint(Pose robot) {
        // PEDRO PATHING DETAIL: Pose.distance() is the straight-line distance between the
        // two x/y positions, in inches. It ignores heading.
        return robot.distance(startingPoint()) <= AT_START_TOLERANCE_INCHES;
    }

    // ==================== 3. MEASURING ====================

    /**
     * Starts measuring a new run. Call right before {@code follower.follow(path)}.
     *
     * <p>Starts the stopwatch and clears the biggest miss and the trail.
     *
     * @param path the path about to be driven (kept for symmetry with {@link #finishRun})
     */
    public void startRun(Path path) {
        running = true;
        runTimer.reset();
        missNow = 0;
        biggestMiss = 0;
        trailX.clear();
        trailY.clear();
    }

    /**
     * Measures how far off the path the robot is. Call once every loop.
     * Does nothing when no run is being measured.
     *
     * <p>"Off the path" is the distance from the robot to the closest point on the path.
     *
     * @param follower the Follower, to read where the robot is and the closest path point
     */
    public void track(Follower follower) {
        if (!running) {
            return;
        }

        Pose robot = follower.pose();

        // PEDRO PATHING DETAIL: closestPose() is the point on the path nearest to the
        // robot. It is empty (null) until the Follower has driven its first path.
        Pose closest = follower.closestPose();
        if (closest != null) {
            missNow = robot.distance(closest);
            biggestMiss = Math.max(biggestMiss, missNow);
        }

        // Remember where the robot is, for the purple trail on the Dashboard field.
        if (trailX.size() < MAX_TRAIL_POINTS) {
            trailX.add(toDashX(robot.x()));
            trailY.add(toDashY(robot.y()));
        }
    }

    /**
     * Stops measuring and works out the results. Call when the robot has arrived
     * ({@code follower.isBusy()} became false).
     *
     * <p>Builds the "Last run" line: time, biggest miss, and how far the robot stopped
     * from the end of the path (in inches and degrees).
     *
     * @param follower the Follower, to read where the robot stopped
     * @param path     the path that was driven, to know where it should have stopped
     */
    public void finishRun(Follower follower, Path path) {
        running = false;
        double seconds = runTimer.seconds();

        Pose robot = follower.pose();
        // PEDRO PATHING DETAIL: endPose() is where the path ends AND the heading the
        // path wants the robot to have there.
        Pose target = path.endPose();

        double endInches = robot.distance(target);
        // Angle.smallestDifference gives the gap between two headings (radians, 0 to PI),
        // going the short way around the circle.
        double endDegrees = Math.toDegrees(Angle.smallestDifference(robot.heading(), target.heading()));

        lastRunText = String.format(Locale.US,
                "time %.1f s · biggest miss %.1f in · ended %.1f in / %.0f° off",
                seconds, biggestMiss, endInches, endDegrees);
    }

    // ==================== 4. DRAWING ON THE DASHBOARD FIELD ====================
    // How it works (see doc/PathTests-LLD.md §6.3.1):
    //   1. Make a TelemetryPacket. Its fieldOverlay() is a canvas on top of the field image.
    //   2. Draw shapes on it, converting Pedro coordinates with toDashX / toDashY.
    //   3. Send it. Each packet replaces the last drawing, so the robot appears to move.

    /**
     * Draws the planned path and its points on the Dashboard field, without the robot.
     * Call in {@code init_loop()} so you can check the path before pressing PLAY.
     * The picture updates live when you change numbers in Dashboard.
     *
     * @param path the path the OpMode would drive (from its {@code buildPath()})
     */
    public void preview(Path path) {
        TelemetryPacket packet = new TelemetryPacket();
        Canvas field = packet.fieldOverlay();
        drawPlannedPath(field, path);
        drawPoints(field);
        dashboard.sendTelemetryPacket(packet);
    }

    /**
     * Draws the full field picture: planned path, points, the robot's trail, and the robot.
     * Also sends numbers for Dashboard's Graph view. Call once every loop.
     *
     * @param follower the Follower, to read where the robot is
     * @param path     the path being driven, or {@code null} before the first run
     */
    public void draw(Follower follower, Path path) {
        TelemetryPacket packet = new TelemetryPacket();
        Canvas field = packet.fieldOverlay();

        if (path != null) {
            drawPlannedPath(field, path);
        }
        drawPoints(field);
        drawTrail(field);
        drawRobot(field, follower.pose());

        // Numbers for Dashboard's Graph view (pick them in the Graph panel to plot them).
        packet.put("Off the path (in)", missNow);
        if (follower.following()) {
            // PEDRO PATHING DETAIL: tangentialVelocity() is the robot's speed along the path.
            // It only makes sense (and only works) while following a path.
            //packet.put("Speed (in per s)", follower.tangentialVelocity());
            packet.put("Playback completion (%)", follower.completion() * 100);
        }

        dashboard.sendTelemetryPacket(packet);
    }

    /** Draws the planned path as a gray line through points along the path. */
    private void drawPlannedPath(Canvas field, Path path) {
        double[] xs = new double[PATH_DRAW_STEPS + 1];
        double[] ys = new double[PATH_DRAW_STEPS + 1];
        for (int i = 0; i <= PATH_DRAW_STEPS; i++) {
            // PEDRO PATHING DETAIL: path.get(t) is the pose at "t" along the path, where
            // t = 0 is the start and t = 1 is the end.
            Pose p = path.get((double) i / PATH_DRAW_STEPS);
            xs[i] = toDashX(p.x());
            ys[i] = toDashY(p.y());
        }
        field.setStroke(COLOR_PLANNED_PATH);
        field.strokePolyline(xs, ys);
    }

    /** Draws a dot for each point this test uses. Control points are hollow (the robot doesn't go there). */
    private void drawPoints(Canvas field) {
        drawDot(field, PathPoses.STARTING_POINT, COLOR_STARTING_POINT, true);
        if (usesControlPoint1) {
            drawDot(field, PathPoses.CONTROL_POINT_1, COLOR_CONTROL_POINT, false);
        }
        if (usesControlPoint2) {
            drawDot(field, PathPoses.CONTROL_POINT_2, COLOR_CONTROL_POINT, false);
        }
        drawDot(field, PathPoses.ENDPOINT, COLOR_ENDPOINT, true);
        if (usesFacingPoint) {
            drawDot(field, PathPoses.FACING_POINT, COLOR_FACING_POINT, true);
        }
    }

    private void drawDot(Canvas field, FieldPoint point, String color, boolean filled) {
        double x = toDashX(point.x);
        double y = toDashY(point.y);
        if (filled) {
            field.setFill(color);
            field.fillCircle(x, y, POINT_RADIUS);
        } else {
            field.setStroke(color);
            field.strokeCircle(x, y, POINT_RADIUS);
        }
    }

    /** Draws where the robot has been during this run, as a purple line. */
    private void drawTrail(Canvas field) {
        if (trailX.size() < 2) {
            return;   // need at least two positions to draw a line
        }
        double[] xs = new double[trailX.size()];
        double[] ys = new double[trailY.size()];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = trailX.get(i);
            ys[i] = trailY.get(i);
        }
        field.setStroke(COLOR_TRAIL);
        field.strokePolyline(xs, ys);
    }

    /** Draws the robot as a circle, with a line from the center showing which way it faces. */
    private void drawRobot(Canvas field, Pose robot) {
        double x = toDashX(robot.x());
        double y = toDashY(robot.y());
        field.setStroke(COLOR_ROBOT);
        field.strokeCircle(x, y, ROBOT_RADIUS);
        // cos/sin turn the heading (radians) into a direction: the line ends on the edge
        // of the circle, on the side the robot is facing.
        field.strokeLine(x, y,
                x + ROBOT_RADIUS * Math.cos(robot.heading()),
                y + ROBOT_RADIUS * Math.sin(robot.heading()));
    }

    /**
     * Pedro x (0 to 141.5, measured from the field corner) → Dashboard x (measured from
     * the field center). If the Dashboard picture is rotated compared to the visualizer,
     * this method and {@link #toDashY} are the only places to fix.
     */
    private static double toDashX(double pedroX) {
        return pedroX - FIELD_SIZE / 2;
    }

    /** Pedro y (0 to 141.5, measured from the field corner) → Dashboard y (measured from the center). */
    private static double toDashY(double pedroY) {
        return pedroY - FIELD_SIZE / 2;
    }

    // ==================== 5. TELEMETRY ====================

    /**
     * Writes the information screen and sends it. Call once every loop (and in init_loop).
     *
     * <p>The words match the Pedro Pathing Visualizer (Starting Point, Control Point,
     * Endpoint, Path number, Playback completion, Current path distance remaining).
     *
     * @param telemetry the OpMode's telemetry (a MultipleTelemetry, so it shows on both
     *                  the Driver Station and Dashboard)
     * @param follower  the Follower, to read where the robot is and how far along the path
     * @param step      the OpMode's current step, e.g. "DRIVING_PATH"
     * @param telemetryMessage a short message for the driver, e.g. "Starting Point moved..."
     *                         (empty string = no message line)
     */
    public void showTelemetry(Telemetry telemetry, Follower follower, String step, String telemetryMessage) {
        telemetry.addLine(testName);
        telemetry.addLine("Buttons: A = Start   B = Stop   Y = Restart");
        telemetry.addData("Step", step);
        if (!telemetryMessage.isEmpty()) {
            telemetry.addData("Message", telemetryMessage);
        }

        // --- The points (only the ones this test uses) ---
        telemetry.addLine();
        telemetry.addData("Starting Point", pointWithHeading(PathPoses.STARTING_POINT));
        if (usesControlPoint1) {
            telemetry.addData("Control Point 1", pointWithoutHeading(PathPoses.CONTROL_POINT_1));
        }
        if (usesControlPoint2) {
            telemetry.addData("Control Point 2", pointWithoutHeading(PathPoses.CONTROL_POINT_2));
        }
        telemetry.addData("Endpoint", pointWithHeading(PathPoses.ENDPOINT));
        if (usesFacingPoint) {
            telemetry.addData("Facing Point", pointWithoutHeading(PathPoses.FACING_POINT));
        }
        telemetry.addData("Heading", headingName);

        // --- The robot right now ---
        telemetry.addLine();
        Pose robot = follower.pose();
        telemetry.addData("Robot", String.format(Locale.US, "x %.1f   y %.1f   heading %.0f°",
                robot.x(), robot.y(), Math.toDegrees(Angle.normalize(robot.heading()))));

        if (follower.following() && follower.currentPath() != null) {
            // PEDRO PATHING DETAIL: a path can be made of several pieces (segments).
            // pathIndex() counts from 0, so we add 1 for people.
            int pieces = follower.currentPath().getSegments().size();
            telemetry.addData("Path number", (follower.pathIndex() + 1) + " / " + pieces);
            telemetry.addData("Playback completion", String.format(Locale.US, "%.0f%%",
                    follower.completion() * 100));
            telemetry.addData("Current path distance remaining", String.format(Locale.US, "%.1f in",
                    follower.remainingDistance()));
        }
        if (running) {
            telemetry.addData("Off the path right now", String.format(Locale.US, "%.1f in", missNow));
        }

        // --- Results ---
        telemetry.addLine();
        telemetry.addData("Last run", lastRunText);

        telemetry.update();   // send everything above to the screen(s)
    }

    private static String pointWithHeading(FieldPoint p) {
        return String.format(Locale.US, "x %.1f   y %.1f   heading %.0f°", p.x, p.y, p.heading);
    }

    private static String pointWithoutHeading(FieldPoint p) {
        return String.format(Locale.US, "x %.1f   y %.1f", p.x, p.y);
    }
}
