import cv2
import numpy as np


# runPipeline() is called every frame by Limelight's backend.
def runPipeline(image, llrobot):
    # Initialize an array of 32 zeros
    llpython = [0] * 32

    # Convert the input image to HSV color space
    hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)

    # Define HSV color thresholds for red, yellow, and blue
    lower_red1 = np.array([0, 100, 100])
    upper_red1 = np.array([10, 255, 255])
    lower_red2 = np.array([160, 100, 100])
    upper_red2 = np.array([179, 255, 255])
    lower_yellow = np.array([20, 100, 100])
    upper_yellow = np.array([30, 255, 255])
    lower_blue = np.array([100, 150, 0])
    upper_blue = np.array([140, 255, 255])

    # Create masks for each color
    mask_red1 = cv2.inRange(hsv_image, lower_red1, upper_red1)
    mask_red2 = cv2.inRange(hsv_image, lower_red2, upper_red2)
    mask_red = cv2.bitwise_or(mask_red1, mask_red2)
    mask_yellow = cv2.inRange(hsv_image, lower_yellow, upper_yellow)
    mask_blue = cv2.inRange(hsv_image, lower_blue, upper_blue)

    # Combine all masks into one
    mask = cv2.bitwise_or(mask_red, mask_yellow)
    mask = cv2.bitwise_or(mask, mask_blue)

    # Find contours in the combined mask
    contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    # Create a copy of the original image to draw contours on
    contour_image = image.copy()
    cv2.drawContours(contour_image, contours, -1, (0, 255, 0), 2)

    # Initialize largestContour
    largestContour = np.array([[]])

    # Sort contours by area in descending order
    contours = sorted(contours, key=cv2.contourArea, reverse=True)

    # Process up to 4 largest contours
    num_targets = min(len(contours), 4)
    for i in range(num_targets):
        contour = contours[i]
        area = cv2.contourArea(contour)
        if area > 100:  # Adjusted minimum area to filter out noise
            x, y, w, h = cv2.boundingRect(contour)
            M = cv2.moments(contour)
            if M["m00"] != 0:
                cX = int(M["m10"] / M["m00"])
                cY = int(M["m01"] / M["m00"])
            else:
                cX = x + w // 2
                cY = y + h // 2

            # Determine the color of the contour
            hsv_value = hsv_image[cY, cX]
            hue = hsv_value[0]

            if (hue >= 0 and hue <= 10) or (hue >= 160 and hue <= 179):
                color_id = 1  # Red
            elif hue >= 20 and hue <= 30:
                color_id = 2  # Yellow
            elif hue >= 100 and hue <= 140:
                color_id = 3  # Blue
            else:
                color_id = 0  # Unknown

            # Draw the bounding rectangle and label on the image
            cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)

            # Fill llpython array
            idx = i * 8
            llpython[idx : idx + 8] = [color_id, cX, cY, int(area), x, y, w, h]

    # Update largestContour if contours are found
    if len(contours) > 0:
        largestContour = contours[0]

    print("llpython:", llpython)  # For debugging purposes
    return largestContour, contour_image, llpython
