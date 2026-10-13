package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.configurations.HardwareNames;

public class Constants {
    public static Follower create(HardwareMap h) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set(HardwareNames.MOTOR_DRIVETRAIN_LEFT_FRONT);
        c.backLeftName.set(HardwareNames.MOTOR_DRIVETRAIN_LEFT_BACK);
        c.frontRightName.set(HardwareNames.MOTOR_DRIVETRAIN_RIGHT_FRONT);
        c.backRightName.set(HardwareNames.MOTOR_DRIVETRAIN_RIGHT_BACK);
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set(HardwareNames.PINPOINT);
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.offsetUnits.set(DistanceUnit.MM);
        c.xPodOffset.set(55.00);
        c.yPodOffset.set(120.00);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
    });

    public static ForesightConfig foresightConfig = new ForesightConfig(c -> {
        Controller primaryTranslationalForward = Controller.proportional(0.36848038788159176);
        Controller secondaryTranslationalForward = Controller.proportional(0.13614365734494796);
        Controller primaryTranslationalLateral = Controller.proportional(0.4853127595238933);
        Controller secondaryTranslationalLateral = Controller.proportional(0.17931009684831278);

        c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
        c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

        c.coast.set(Controller.proportionalFeedforward(0.022138965056973447));
        c.brake.set(Controller.proportionalFeedforward(0.01881812029842743));

        c.headingFeedback.set(Controller.proportional(4.538592616454048));
        c.headingBrakeCoefficients.set(Vector2D.cartesian(0.0527884000998084, 0.006803034377253284));

        c.linearBrakeCoefficients.set(Matrix.diag(0.08496148418131028, 0.054096949333369525));
        c.quadraticBrakeCoefficients.set(Matrix.diag(0.0016566628383949708, 0.0022706018176637853));

        c.maxAchievableForwardVelocity.set(47.32077836285254);
        c.maxAchievableStrafeVelocity.set(36.426485616324285);
        c.naturalForwardDeceleration.set(31.4495241616827);
        c.naturalStrafeDeceleration.set(71.69539023542342);
    });
}