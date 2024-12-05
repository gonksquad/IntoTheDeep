package org.firstinspires.ftc.teamcode.visiontesting.opencv;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;

import java.util.ArrayList;
import java.util.List;

@TeleOp
public class aaa extends LinearOpMode {
    OpenCvWebcam webcam;
    SamplePipeline pipeline;

    @Override
    public void runOpMode() {
        // Initialize the camera
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id",
                hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"),
                cameraMonitorViewId);

        // Initialize the pipeline
        pipeline = new SamplePipeline();
        webcam.setPipeline(pipeline);

        // Open the camera asynchronously
        webcam.setMillisecondsPermissionTimeout(2500);
        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {
                telemetry.addData("Camera Error", "Error Code: " + errorCode);
                telemetry.update();
            }
        });

        telemetry.addLine("Waiting for start");
        telemetry.update();

        // Initialization Loop
        while (!isStarted() && !isStopRequested()) {
            displayTelemetry();
            sleep(100); // Throttle to 10Hz
        }

        // Wait for the user to press start
        waitForStart();

        // Main Loop After Start
        while (opModeIsActive()) {
            displayTelemetry();
            sleep(100); // Throttle to 10Hz
        }
    }

    private void displayTelemetry() {
        List<Point> redPoints = pipeline.getRedCenters();
        List<Point> bluePoints = pipeline.getBlueCenters();
        List<Point> yellowPoints = pipeline.getYellowCenters();

        for (int i = 0; i < redPoints.size(); i++) {
            Point point = redPoints.get(i);
            if (point != null) {
                telemetry.addData("Red Sample " + (i + 1), "X: " + point.x + ", Y: " + point.y);
            }
        }

        for (int i = 0; i < bluePoints.size(); i++) {
            Point point = bluePoints.get(i);
            if (point != null) {
                telemetry.addData("Blue Sample " + (i + 1), "X: " + point.x + ", Y: " + point.y);
            }
        }

        for (int i = 0; i < yellowPoints.size(); i++) {
            Point point = yellowPoints.get(i);
            if (point != null) {
                telemetry.addData("Yellow Sample " + (i + 1), "X: " + point.x + ", Y: " + point.y);
            }
        }

        telemetry.update();
    }

    class SamplePipeline extends OpenCvPipeline {
        boolean viewportPaused;
        Mat hsvMat = new Mat();
        Mat maskRed = new Mat();
        Mat maskRed1 = new Mat();
        Mat maskRed2 = new Mat();
        Mat maskBlue = new Mat();
        Mat maskYellow = new Mat();
        Mat hierarchy = new Mat();
        List<Point> redCenters = new ArrayList<>();
        List<Point> blueCenters = new ArrayList<>();
        List<Point> yellowCenters = new ArrayList<>();

        @Override
        public Mat processFrame(Mat input) {
            redCenters.clear();
            blueCenters.clear();
            yellowCenters.clear();

            Imgproc.cvtColor(input, hsvMat, Imgproc.COLOR_RGB2HSV);

            // Adjusted HSV ranges for paler colors
            Scalar lowRed1 = new Scalar(0, 50, 70);
            Scalar highRed1 = new Scalar(10, 150, 255);
            Scalar lowRed2 = new Scalar(170, 50, 70);
            Scalar highRed2 = new Scalar(180, 150, 255);

            Core.inRange(hsvMat, lowRed1, highRed1, maskRed1);
            Core.inRange(hsvMat, lowRed2, highRed2, maskRed2);
            Core.addWeighted(maskRed1, 1.0, maskRed2, 1.0, 0.0, maskRed);

            Scalar lowBlue = new Scalar(90, 50, 70);
            Scalar highBlue = new Scalar(130, 150, 255);
            Core.inRange(hsvMat, lowBlue, highBlue, maskBlue);

            Scalar lowYellow = new Scalar(15, 50, 70);
            Scalar highYellow = new Scalar(35, 150, 255);
            Core.inRange(hsvMat, lowYellow, highYellow, maskYellow);

            List<MatOfPoint> contoursRed = new ArrayList<>();
            Imgproc.findContours(maskRed, contoursRed, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            List<MatOfPoint> contoursBlue = new ArrayList<>();
            Imgproc.findContours(maskBlue, contoursBlue, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            List<MatOfPoint> contoursYellow = new ArrayList<>();
            Imgproc.findContours(maskYellow, contoursYellow, hierarchy, Imgproc.RETR_EXTERNAL,
                    Imgproc.CHAIN_APPROX_SIMPLE);

            processContours(contoursRed, input, new Scalar(255, 0, 0), redCenters);
            processContours(contoursBlue, input, new Scalar(0, 0, 255), blueCenters);
            processContours(contoursYellow, input, new Scalar(0, 255, 255), yellowCenters);

            return input;
        }

        private void processContours(List<MatOfPoint> contours, Mat input, Scalar color, List<Point> centers) {
            if (contours.isEmpty()) {
                return;
            }

            // Sort contours by area in descending order
            contours.sort((c1, c2) -> Double.compare(Imgproc.contourArea(c2), Imgproc.contourArea(c1)));

            // Keep only the largest three contours
            List<MatOfPoint> largestContours = contours.size() > 3 ? contours.subList(0, 3) : contours;

            for (MatOfPoint contour : largestContours) {
                Rect rect = Imgproc.boundingRect(contour);

                // Calculate the center of the bounding rectangle
                Point center = new Point(rect.x + rect.width / 2.0, rect.y + rect.height / 2.0);
                if (center != null) {
                    centers.add(center);

                    // Draw a circle at the center
                    Imgproc.circle(input, center, 5, color, -1);

                    // Draw bounding rectangle
                    Imgproc.rectangle(input, rect, color, 2);
                }
            }
        }

        @Override
        public void onViewportTapped() {
            viewportPaused = !viewportPaused;
            if (viewportPaused) {
                webcam.pauseViewport();
            } else {
                webcam.resumeViewport();
            }
        }

        public List<Point> getRedCenters() {
            return redCenters;
        }

        public List<Point> getBlueCenters() {
            return blueCenters;
        }

        public List<Point> getYellowCenters() {
            return yellowCenters;
        }
    }
}
