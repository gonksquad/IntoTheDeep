package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp
public class ContinuousCRServo extends LinearOpMode {

    private CRServo g1;
    private CRServo g2;

    @Override
    public void runOpMode() {
        // Initialize the hardware variables, linking them to the configuration names
        g1 = hardwareMap.get(CRServo.class, "G1");
        g2 = hardwareMap.get(CRServo.class, "G2");

        // Set the initial power for continuous rotation
        g1.setPower(1.0); // Rotate g1 forward at full speed
        g2.setPower(-1.0); // Rotate g2 forward at full speed

        // Wait for the start of the match
        waitForStart();

        // Run the CRServos continuously until the op mode is stopped
        while (opModeIsActive()) {
            // Optionally, you can add logic here to control power or direction using
            // gamepad inputs
            telemetry.addData("Status", "Running");
            telemetry.update();
        }

        // Optionally stop the servos at the end of the op mode
        g1.setPower(0);
        g2.setPower(0);
    }
}
