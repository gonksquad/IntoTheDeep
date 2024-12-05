package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;


public class Hardware {
    public DcMotor LF;
    public DcMotor LR;
    public DcMotor RR;
    public DcMotor RF;
    public LynxModule lynx;
    public DcMotor upDownSlides;
    public DcMotor inOutSlides;
    public Servo grabber;
    public Servo placer;
    public Servo placerSpin;
    public Servo grabberCorrection;
    public Servo grabberSpin;
    public Limelight3A limelight;
    public Servo headlight;

    public Hardware(HardwareMap hardwareMap) {

        LF = hardwareMap.get(DcMotor.class, "2");
        LR = hardwareMap.get(DcMotor.class, "0");
        RR = hardwareMap.get(DcMotor.class, "1");
        RF = hardwareMap.get(DcMotor.class, "3");

        RR.setDirection(DcMotor.Direction.REVERSE);
        LR.setDirection(DcMotor.Direction.REVERSE);

        upDownSlides = hardwareMap.get(DcMotor.class, "E0");
        inOutSlides = hardwareMap.get(DcMotor.class, "E1");

        grabber = hardwareMap.get(Servo.class, "ES0");
        placer = hardwareMap.get(Servo.class, "ES1");
        placerSpin = hardwareMap.get(Servo.class, "ES2");
        grabberCorrection = hardwareMap.get(Servo.class, "ES3");
        grabberSpin = hardwareMap.get(Servo.class, "ES4");

        // Reset and initialize motors with encoders
        inOutSlides.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        inOutSlides.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        headlight = hardwareMap.get(Servo.class, "headlight");
    }

    public void grabberOpen() {
        grabber.setPosition(1);
    }
    public void grabberClose() {
        grabber.setPosition(0.56);
    }

    public void grabberFlipDown() {
        grabberSpin.setPosition(0);
    }
    public void grabberFlipMid() {grabberSpin.setPosition(0.7);}
    public void grabberFlipUp() {
        grabberSpin.setPosition(1);
    }

    public void placerOpen() {
        placer.setPosition(0.5);
    }
    public void placerClose() {
        placer.setPosition(0.7);
    }

    /*
     * grabber spin positions
     * 
     * 1: left horizontal
     * 2: left 45 (45 deg. above the left horizontal)
     * 3: vertical (90, short edge of block close to robot)
     * 4: right 45 (45 deg. above the right horizontal)
     * 5: right horizontal
     */

    public void grabberSpin(int position) {
        switch (position) {
            case 1:
                grabberCorrection.setPosition(0);
                break;
            case 2:
                grabberCorrection.setPosition(0.45);
                break;
            case 3:
                grabberCorrection.setPosition(0.6);
                break;
            case 4:
                grabberCorrection.setPosition(0.75);
                break;
            case 5:
                grabberCorrection.setPosition(0.55);
                break;
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    public void setUppyDownyPosition(int position, double power) {
        upDownSlides.setTargetPosition(position);
        upDownSlides.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        upDownSlides.setPower(power);
    }

    public void setReachyReachyPosition(int position, double power) {
        inOutSlides.setTargetPosition(position);
        inOutSlides.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        inOutSlides.setPower(power);
    }

    public void placerFlipGrabWall() {
        placerSpin.setPosition(1);
    }
    public void placerFlipPlace() {
        placerSpin.setPosition(0.8);
    }
    public void placerFlipIdle() {
        placerSpin.setPosition(0.4);
    }
    public void placerFlipTransfer() {
        placerSpin.setPosition(0.26);
    }

    public void setRaw(double Lr, double Lf, double Rr, double Rf) {
        LR.setPower(Lr);
        LF.setPower(Lf);
        RF.setPower(Rf);
        RR.setPower(Rr);
    }

    public final void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void strafeLeft(double power, double seconds) {
        RF.setPower(power);
        RR.setPower(power * -1);
        LF.setPower(power * -1);
        LR.setPower(power);
        sleep((long) (seconds * 100));
    }

    public void strafeRight(double power, double seconds) {
        RF.setPower(power * -1);
        RR.setPower(power);
        LF.setPower(power);
        LR.setPower(power * -1);
        sleep((long) (seconds * 100));
    }

    public void backward(double power, double seconds) {
        RF.setPower(power * -1);
        RR.setPower(power * -1);
        LF.setPower(power * -1);
        LR.setPower(power * -1);
        sleep((long) (seconds * 100));
    }

    public void forward(double power, double seconds) {
        RF.setPower(power);
        RR.setPower(power);
        LF.setPower(power);
        LR.setPower(power);
        sleep((long) (seconds * 100));
    }

    public void spinLeft(double power, double seconds) {
        RF.setPower(power * -1);
        RR.setPower(power * -1);
        LF.setPower(power);
        LR.setPower(power);
        sleep((long) (seconds * 100));
    }

    public void spinRight(double power, double seconds) {
        RF.setPower(power);
        RR.setPower(power);
        LF.setPower(power * -1);
        LR.setPower(power * -1);
        sleep((long) (seconds * 100));
    }

    public void forward() {
        forward(1, 0);
    }

    public void strafeLeft() {
        strafeLeft(1, 0);
    }

    public void strafeRight() {
        strafeRight(1, 0);
    }

    public void backward() {
        backward(1, 0);
    }

    public void spinLeft() {
        spinLeft(1, 0);
    }

    public void spinRight() {
        spinRight(1, 0);
    }
}