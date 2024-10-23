import cv2
import numpy as np

# runPipeline() is called every frame by Limelight's backend.
def runPipeline(image, llrobot):
    # Initialize an empty array to store data to send back to the robot
    llpython = [0, 0, 0, 0, 0, 0, 0, 0]

    # Convert the input image to HSV color space
    hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)

    # Define HSV color thresholds for red, yellow, and blue
    # Adjust these values based on your specific lighting conditions
    # Red thresholds (note that red wraps around the hue spectrum)
    lower_red1 = np.array([0, 100, 100])
    upper_red1 = np.array([10, 255, 255])

    lower_red2 = np.array([160, 100, 100])
    upper_red2 = np.array([179, 255, 255])

    # Yellow thresholds
    lower_yellow = np.array([20, 100, 100])
    upper_yellow = np.array([30, 255, 255])

    # Blue thresholds
    lower_blue = np.array([100, 150, 0])
    upper_blue = np.array([140, 255, 255])

    # Create masks for each color
    # For red, combine two ranges
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

    largestContour = np.array([[]])

    # Initialize lists to store data
    target_ids = []
    target_x = []
    target_y = []
    target_areas = []

    # If contours are detected, process them
    if len(contours) > 0:
        # Record the largest contour
        largestContour = max(contours, key=cv2.contourArea)

        for contour in contours:
            area = cv2.contourArea(contour)
            if area > 500:  # Minimum area to filter out noise; adjust as needed
                # Get the bounding rectangle
                x, y, w, h = cv2.boundingRect(contour)
                # Calculate the centroid of the contour
                M = cv2.moments(contour)
                if M['m00'] != 0:
                    cX = int(M['m10'] / M['m00'])
                    cY = int(M['m01'] / M['m00'])

                    # Determine the color of the contour
                    # Sample the HSV value at the centroid
                    hsv_value = hsv_image[cY, cX]
                    hue = hsv_value[0]

                    if (hue >= 0 and hue <= 10) or (hue >= 160 and hue <= 179):
                        color_name = 'red'
                        color_id = 1
                    elif hue >= 20 and hue <= 30:
                        color_name = 'yellow'
                        color_id = 2
                    elif hue >= 100 and hue <= 140:
                        color_name = 'blue'
                        color_id = 3
                    else:
                        color_name = 'unknown'
                        color_id = 0

                    # Draw the bounding rectangle and label on the image
                    cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)
                    cv2.putText(image, color_name, (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX,
                                0.9, (0, 255, 0), 2)

                    # Add data to lists
                    target_ids.append(color_id)
                    target_x.append(cX)
                    target_y.append(cY)
                    target_areas.append(area)

    # Prepare data to send back to the robot
    # For example, send data of the largest detected block
    if len(target_areas) > 0:
        # Find the index of the largest area
        max_area_index = target_areas.index(max(target_areas))
        llpython = [
            target_ids[max_area_index],
            target_x[max_area_index],
            target_y[max_area_index],
            target_areas[max_area_index],
            0, 0, 0, 0
        ]
    else:
        # No targets detected
        llpython = [0, 0, 0, 0, 0, 0, 0, 0]

    # Return the largest contour, the modified image, and custom robot data
    return largestContour, image, llpython
