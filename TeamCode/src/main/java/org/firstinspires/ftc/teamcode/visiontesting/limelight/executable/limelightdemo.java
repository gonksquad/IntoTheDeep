package org.firstinspires.ftc.teamcode.visiontesting.limelight.executable;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.List;

@TeleOp(name = "FTCDASH Limelight")
public class limelightdemo extends LinearOpMode {

    private Limelight3A limelight;
    public Servo headlight;
    private FtcDashboard dashboard;
    public String color;
    public String output1;

    @Override
    public void runOpMode() throws InterruptedException {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        headlight = hardwareMap.get(Servo.class, "headlight");
        dashboard = FtcDashboard.getInstance();

        limelight.pipelineSwitch(0);
        limelight.start();

        TelemetryPacket packet = new TelemetryPacket();
        packet.put("Status", "Robot Ready. Press Play.");
        dashboard.sendTelemetryPacket(packet);
        waitForStart();
        headlight.setPosition(1);

        while (opModeIsActive()) {
            packet = new TelemetryPacket(); // Create a new packet for each loop

            LLStatus status = limelight.getStatus();
            packet.put("Name", status.getName());
            packet.put("Temp (C)", status.getTemp());
            packet.put("CPU (%)", status.getCpu());
            packet.put("FPS", (int) status.getFps());
            packet.put("Pipeline Index", status.getPipelineIndex());
            packet.put("Pipeline Type", status.getPipelineType());

            LLResult result = limelight.getLatestResult();
            if (result != null) {
                packet.put("LL Latency", result.getCaptureLatency() + result.getTargetingLatency());
                packet.put("Parse Latency", result.getParseLatency());

                output1 = java.util.Arrays.toString(result.getPythonOutput());

                // Correct method name to getPythonOutputs()
                packet.put("Python Output", output1);

                if (output1 == "0.0") {
                    color = "not here";
                } else if (output1 == "1.0") {
                    color = "red";
                } else if (output1 == "2.0") {
                    color = "yellow";
                } else if (output1 == "3.0") {
                    color = "blue";
                }

                packet.put("LIMELIGHT SAYS THE BLOCK IS", color);



                if (result.isValid()) {
                    packet.put("tx", result.getTx());
                    packet.put("txnc", result.getTxNC());
                    packet.put("ty", result.getTy());
                    packet.put("tync", result.getTyNC());
                    packet.put("Botpose", result.getBotpose().toString());

                    List<LLResultTypes.BarcodeResult> barcodeResults = result.getBarcodeResults();
                    for (LLResultTypes.BarcodeResult br : barcodeResults) {
                        packet.put("Barcode Data", br.getData());
                    }

                    List<LLResultTypes.ClassifierResult> classifierResults = result.getClassifierResults();
                    for (LLResultTypes.ClassifierResult cr : classifierResults) {
                        packet.put("Classifier Class", cr.getClassName());
                        packet.put("Confidence", cr.getConfidence());
                    }

                    List<LLResultTypes.DetectorResult> detectorResults = result.getDetectorResults();
                    for (LLResultTypes.DetectorResult dr : detectorResults) {
                        packet.put("Detector Class", dr.getClassName());
                        packet.put("Area", dr.getTargetArea());
                    }

                    List<LLResultTypes.FiducialResult> fiducialResults = result.getFiducialResults();
                    for (LLResultTypes.FiducialResult fr : fiducialResults) {
                        packet.put("Fiducial ID", fr.getFiducialId());
                        packet.put("Family", fr.getFamily());
                        packet.put("Target X (Degrees)", fr.getTargetXDegrees());
                        packet.put("Target Y (Degrees)", fr.getTargetYDegrees());
                    }

                    List<LLResultTypes.ColorResult> colorResults = result.getColorResults();
                    for (LLResultTypes.ColorResult cr : colorResults) {
                        packet.put("Color X (Degrees)", cr.getTargetXDegrees());
                        packet.put("Color Y (Degrees)", cr.getTargetYDegrees());
                    }
                    packet.put("Py:",result.getPythonOutput().toString());
                }
            } else {
                packet.put("Limelight", "No data available");
            }

            dashboard.sendTelemetryPacket(packet); // Send data to FTC Dashboard
        }
        limelight.stop();
    }
}
