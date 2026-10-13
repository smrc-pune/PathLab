package org.firstinspires.ftc.teamcode.configurations;

/**
 * FieldPoint.java  -  ONE POINT ON THE FIELD
 *
 * <p><b>WHAT IS THIS FILE?</b><br>
 * A tiny "data holder": three numbers that describe one point on the field.
 * It has no logic. It never moves the robot and never talks to Pedro Pathing.
 *
 * <p><b>WHY DOES IT EXIST?</b><br>
 * FTC Dashboard shows every {@code public} field of an object as an editable box.
 * Because each point in {@link PathPoses} is a {@code FieldPoint}, Dashboard shows it
 * as one small group you can open, with {@code x}, {@code y} and {@code heading} inside:
 * <pre>
 *   Path Poses
 *     ▸ STARTING_POINT     x: 24   y: 24   heading: 0
 *     ▸ ENDPOINT           x: 72   y: 60   heading: 90
 * </pre>
 *
 * <p><b>UNITS (same as the Pedro Pathing Visualizer)</b>
 * <ul>
 *   <li>{@code x}: inches from the LEFT edge of the field (0 to 141.5)</li>
 *   <li>{@code y}: inches from the BOTTOM edge of the field (0 to 141.5)</li>
 *   <li>{@code heading}: DEGREES. 0 = facing right, 90 = facing up,
 *       180 = facing left, 270 = facing down. Counter-clockwise is positive.</li>
 * </ul>
 *
 * <p><b>IS THIS A HELPER OR A CONFIGURATION?</b><br>
 * Configuration. It only describes what one configured point looks like.
 * Turning it into a Pedro {@code Pose} (the actual work) happens in
 * {@code utils/PathTestHelper.java}.
 *
 * <p>STUDENT: you never need to edit this file. Change the numbers in Dashboard
 * (Configuration → Path Poses), or the starting values in {@link PathPoses}.
 */
public class FieldPoint {

    // These fields are "public" (Dashboard can see them) and NOT "final"
    // (Dashboard can change them). Both are required for live editing.

    /** Inches from the left edge of the field (0 to 141.5). */
    public double x;

    /** Inches from the bottom edge of the field (0 to 141.5). */
    public double y;

    /** Which way the robot faces, in degrees. 0 = right, 90 = up. */
    public double heading;

    /**
     * Makes a new point.
     *
     * <p>EXAMPLE: {@code new FieldPoint(24, 24, 0)} is 24 inches from the left,
     * 24 inches from the bottom, facing right.
     *
     * @param x       inches from the left edge of the field
     * @param y       inches from the bottom edge of the field
     * @param heading which way the robot faces, in degrees (0 = right)
     */
    public FieldPoint(double x, double y, double heading) {
        this.x = x;
        this.y = y;
        this.heading = heading;
    }
}
