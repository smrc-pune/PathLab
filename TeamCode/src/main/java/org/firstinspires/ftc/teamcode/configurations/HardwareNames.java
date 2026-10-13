package org.firstinspires.ftc.teamcode.configurations;

/**
 * HardwareNames.java  -  THE WIRING LIST
 *
 * WHAT IS THIS FILE?
 * Every motor, servo and camera has a name that we typed into the Robot
 * Controller app (Configure Robot). The code finds each device BY THAT NAME.
 * If the name in this file does not match the name in the app EXACTLY
 * (capital letters, spaces and all), the robot crashes with "device not found".
 *
 * THE RULE:
 * This is the ONLY file that contains device names. Parts (Gate, Flywheels...)
 * never contain names - Robot.java hands them the names from here.
 *
 * WHEN A MOTOR SPINS THE WRONG WAY:
 * Flip the matching "REVERSED" value in this file (true <-> false). Nothing else.
 *
 * WHEN YOU SEE "device not found":
 *   1. Open the Robot Controller app -> Configure Robot -> look at the name.
 *   2. Make the text below match it exactly.
 */
public class HardwareNames {

    // ==================== DRIVETRAIN (used by Pedro in Constants.java) ====================
    public static final String MOTOR_DRIVETRAIN_LEFT_FRONT = "leftFront";
    public static final String MOTOR_DRIVETRAIN_RIGHT_FRONT = "rightFront";
    public static final String MOTOR_DRIVETRAIN_LEFT_BACK = "leftBack";
    public static final String MOTOR_DRIVETRAIN_RIGHT_BACK = "rightBack";

    // Odometry computer (tells Pedro where the robot is)
    public static final String PINPOINT = "pinpoint";
}
