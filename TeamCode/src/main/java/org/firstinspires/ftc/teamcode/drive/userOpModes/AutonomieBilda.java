package org.firstinspires.ftc.teamcode.drive.userOpModes;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.drive.userOpModes.Vision.EasyOpenVision;

@Autonomous(name = "AutonomieBilda")
public class AutonomieBilda extends LinearOpMode {

    int position = 0;

    SampleMecanumDrive drive;

    DcMotor shooter;
    //DcMotor intake;
    DcMotor arm;

    Servo shooterServo;
    Servo armServoUp;
    Servo armServoDown;

    Pose2d startPos = new Pose2d(-60, -17, Math.toRadians(0));

    final int TICKS_PER_REV = 2700;

    public void initialize() {

        drive = new SampleMecanumDrive(hardwareMap);

        drive.setPoseEstimate(startPos);
    }

    public void shoot() {
        shooter.setPower(1);
        sleep(2800);
        shooterServo.setPosition(1);
        sleep(500);
        shooter.setPower(0);
        shooterServo.setPosition(0);
    }

    public void move(double position, boolean clockwise) {
        arm.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        double distance = position / 360;
        int target = (int)(distance * TICKS_PER_REV);
        double power = 0.16;

        if(clockwise) {
            arm.setTargetPosition(-target);
        } else {
            arm.setTargetPosition(target);
        }

        arm.setPower(power);

        arm.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while(arm.isBusy()) {
            telemetry.addData("Brat", "going to position");
            telemetry.update();
        }
    }

    public void ASUPosition(boolean opened) {
        if(opened) {
            armServoUp.setPosition(1);
        } else {
            armServoUp.setPosition(0);
        }
    }

    public void ASDPosition(boolean opened) {
        if(opened) {
            armServoDown.setPosition(0);
        } else {
            armServoDown.setPosition(1);
        }
    }

    public Trajectory toCameraScan(Pose2d startPos) {
        return drive.trajectoryBuilder(startPos)
                //.lineTo(new Vector2d(-22, -17))
                .forward(48)
                .build();
    }

    public Trajectory toShootingSpot(Pose2d cameraScanPos) {
        return drive.trajectoryBuilder(cameraScanPos)
                .splineToLinearHeading(new Pose2d(0, -45), Math.toRadians(5))
                .build();
    }

    public Trajectory toWobbleDrop(int position) {
        switch(position) {
            case 0:
                return drive.trajectoryBuilder(toShootingSpot(toCameraScan(startPos).end()).end())
                        .forward(12)
                        //.splineToLinearHeading(new Pose2d(12, -36), Math.toRadians(-90))
                        .build();

            case 1:
                return drive.trajectoryBuilder(toShootingSpot(toCameraScan(startPos).end()).end())
                        .forward(24)
                        //.splineToLinearHeading(new Pose2d(30, -36), Math.toRadians(0))
                        .build();
            case 4:
            default:
                return drive.trajectoryBuilder(toShootingSpot(toCameraScan(startPos).end()).end())
                        .forward(35)
                        //.splineToLinearHeading(new Pose2d(36, -60), Math.toRadians(0))
                        .build();
        }
    }

    public Trajectory leaveWobbleDrop() {
        return drive.trajectoryBuilder(toWobbleDrop(position).end())
                .back(12)
                .build();
    }

    public Trajectory toParkingSpot(Trajectory leaveWobbleDrop) {
        switch(position) {
            case 1:
                return drive.trajectoryBuilder(leaveWobbleDrop.end())
                        .lineToLinearHeading(new Pose2d(12, -24, Math.toRadians(0)))
                        .build();
            case 4:
            default:

                return drive.trajectoryBuilder(leaveWobbleDrop.end())
                        .lineToLinearHeading(new Pose2d(12, -48, Math.toRadians(0)))
                        .build();
        }
    }

    @Override
    public void runOpMode() throws InterruptedException {

        initialize();
        telemetry.addData("Status", "initialized");
        waitForStart();

        Trajectory toPark = drive.trajectoryBuilder(new Pose2d())
                .back(23)
                .build();


        drive.followTrajectory(toCameraScan(startPos));

        telemetry.addData("Pozitie:", position);
        telemetry.update();

        drive.followTrajectory(toShootingSpot(toCameraScan(startPos).end()));

        switch(position) {
            case 0:
                drive.followTrajectory(toWobbleDrop(position));
                drive.turn(Math.toRadians(-90));
                drive.followTrajectory(toWobbleDrop(position));
                break;
            case 1:
                drive.followTrajectory(toWobbleDrop(position));
                break;
            case 4:
                drive.followTrajectory(toWobbleDrop(0));
                drive.turn(Math.toRadians(-30));
                drive.followTrajectory(toWobbleDrop(position));
                break;
        }

        switch(position) {
            case 0:
                drive.followTrajectory(leaveWobbleDrop());
                drive.turn(Math.toRadians(90));
                break;
            case 1:
                drive.followTrajectory(leaveWobbleDrop());
                drive.followTrajectory(toParkingSpot(leaveWobbleDrop()));
                break;
            case 4:
                drive.followTrajectory(leaveWobbleDrop());
                drive.followTrajectory(toPark);
                //drive.followTrajectory(toParkingSpot(leaveWobbleDrop()));
                break;
        }
    }
}
