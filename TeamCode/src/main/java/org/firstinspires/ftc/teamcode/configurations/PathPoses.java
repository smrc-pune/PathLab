package org.firstinspires.ftc.teamcode.configurations;

import com.acmerobotics.dashboard.config.Config;

/**
 * PathPoses.java  -  THE POINTS EVERY PATH TEST USES
 *
 * <p><b>WHAT IS THIS FILE?</b><br>
 * The shared points for all the Path Test OpModes (in the {@code pathtests} folder).
 * Every test builds its path from these same points, so you can compare different
 * kinds of paths fairly: same points, different approach.
 *
 * <p><b>HOW DO I CHANGE THE POINTS?</b><br>
 * Live, without changing code:
 * <ol>
 *   <li>Connect the laptop to the robot's Wi-Fi.</li>
 *   <li>Open {@code http://192.168.43.1:8080/dash} in a browser.</li>
 *   <li>Open <b>Configuration → Path Poses</b>, expand a point, type new numbers,
 *       and press Enter.</li>
 *   <li>Press A on the gamepad. The new points are used for that run.</li>
 * </ol>
 *
 * <p><b>IMPORTANT:</b> Dashboard changes are forgotten when the robot turns off.
 * When you find points you like, copy the numbers into this file and redeploy.
 *
 * <p><b>NAMES MATCH THE PEDRO PATHING VISUALIZER</b>
 * (https://visualizer.pedropathing.com/). You can plan a path there and copy
 * the numbers here. Units are the same too: inches, and heading in degrees.
 *
 * <p><b>FTC DASHBOARD DETAIL:</b><br>
 * {@code @Config("Path Poses")} tells Dashboard to show this class as a panel called
 * "Path Poses". Dashboard can only edit fields that are {@code public static} and
 * NOT {@code final}, so every setting below is written that way.
 */
@Config("Path Poses")
public class PathPoses {

    // ==================== THE POINTS ====================

    /**
     * Where every path begins, and where the robot drives back to after each run.
     *
     * <p>STUDENT: put the robot here (center of the robot on the point, facing the
     * heading) before pressing PLAY.
     */
    public static FieldPoint STARTING_POINT = new FieldPoint(24, 24, 0);

    /**
     * Pulls a curve toward it. The robot does NOT drive through it.
     * Used by tests 3, 4, 5 and 7. Test 6 (Through Points) DOES drive through it.
     * Its heading is not used.
     */
    public static FieldPoint CONTROL_POINT_1 = new FieldPoint(60, 24, 0);

    /**
     * A second pull point, for S-shaped curves. Used by test 5 only.
     * It is on the OTHER side of the path from Control Point 1 (left of the Endpoint,
     * while Control Point 1 is to the right), which is what makes the curve an S.
     * Its heading is not used.
     */
    public static FieldPoint CONTROL_POINT_2 = new FieldPoint(36, 60, 0);

    /** Where every path ends. Its heading is used by tests that end facing a set direction. */
    public static FieldPoint ENDPOINT = new FieldPoint(72, 60, 90);

    /**
     * The spot the robot keeps looking at while it drives. Used by test 7 only.
     * Its heading is not used (the robot faces the point, not a set direction).
     */
    public static FieldPoint FACING_POINT = new FieldPoint(96, 96, 0);

    // ==================== TIMING ====================

    /**
     * How long the robot waits at the Endpoint before driving back to the
     * Starting Point, in seconds. Use the pause to look at where the robot stopped.
     */
    public static double PAUSE_AT_ENDPOINT_SECONDS = 2.0;
}
