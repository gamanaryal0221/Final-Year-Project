import cv2
import numpy as np
from imutils.contours import sort_contours

kernel1 = np.ones((3, 3), np.uint8)


def segment_free_letters(cropped_image):

    src_img = cropped_image
    copy = cropped_image.copy()
    height = src_img.shape[0]
    width = src_img.shape[1]
    
    # resizing image for calculations. interpolation inter_area is used to downscale a image. other options are inter_linear, inter_cubic etc.
    src_img = cv2.resize(copy, dsize=(
        1320, int(700*height/width)), interpolation=cv2.INTER_AREA)
    # src_img = cv2.copyMakeBorder(src_img, top=30, bottom=30,left=15, right=30, borderType=cv2.BORDER_REPLICATE)
    # calculate the width and height of source image
    height = src_img.shape[0]
    width = src_img.shape[1]

    grey_img = cv2.cvtColor(src_img, cv2.COLOR_BGR2GRAY)

    # Applying Adaptive Threshold with kernel :- 21 X 21
    bin_img = cv2.GaussianBlur(grey_img, (3, 3), 0)
    bin_img = cv2.adaptiveThreshold(
        grey_img, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY_INV, 21, 21)

    # cv2.imshow("threshold image",bin_img)
    # cv2.waitKey(1000)

    # finding contours from image
    contours, hierarchy = cv2.findContours(bin_img, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    contours = sort_contours(contours, method="left-to-right")[0]

    # apply bounding rectangle for every contour extract the region of interest

    segmented_letter_images = []
    for cnt in contours:

        # filtering out more of smaller dots and noises in image
        if (cv2.contourArea(cnt) > 100):

            # print(np.sum(cnt))
            # find the coordinates of bounding rectangle of the contour
            x, y, w, h = cv2.boundingRect(cnt)

            # find the region of interest based on contour
            region_of_interest = bin_img[y:y + h, x:x + w]

            # draw rectangles based on the region of interest
            #cv2.rectangle(src_img, (x, y), (x+w, y+h), (0, 255, 0), 2)
            padded = cv2.copyMakeBorder(
                region_of_interest, 
                top=25, 
                bottom=25, 
                left=25, 
                right=25, 
                borderType=cv2.BORDER_CONSTANT, 
                value=(0, 0, 0))
                
            padded = cv2.GaussianBlur(padded, (3, 3), 0)
            padded = cv2.morphologyEx(padded, cv2.MORPH_OPEN, kernel1)
            padded = cv2.dilate(padded, kernel1, iterations=1)
            resized_image = cv2.resize(padded, (28, 28))
            reshaped_final_image = np.reshape(resized_image, (1, 28, 28, 1))
            
            # cv2.imshow("i",resized_image)
            # cv2.waitKey(200)

            segmented_letter_images.append(reshaped_final_image)

    return segmented_letter_images

