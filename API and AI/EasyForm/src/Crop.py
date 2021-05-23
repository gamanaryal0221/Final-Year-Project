import cv2
from FieldCoordinates import *
import numpy as np
from LetterSegmentation import segment_free_letters
import math


def crop_numbers_from_small_boxes(wrapped_image):
    kernel1 = np.ones((3, 3), np.uint8)

    croped_images = []
    date = []
    acc_number = []
    amount_in_digit = []
    phn_number = []
    

    for x, r in enumerate(date_coordinates):
        top_left_y = r[0][1]
        bottom_right_y = r[1][1]
        top_left_x = r[0][0]
        bottom_right_x = r[1][0]
        photo = wrapped_image[top_left_y:bottom_right_y,
                            top_left_x:bottom_right_x]

        grey_img = cv2.cvtColor(photo, cv2.COLOR_BGR2GRAY)

        # Applying Adaptive Threshold with kernel :- 21 X 21
        bin_img = cv2.adaptiveThreshold(
            grey_img, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY_INV, 21, 21)
        bin_img = cv2.GaussianBlur(bin_img, (3, 3), 0)
        bin_img = cv2.morphologyEx(bin_img, cv2.MORPH_OPEN, kernel1)
        bin_img = cv2.dilate(bin_img, kernel1, iterations=1)
        resized_image = cv2.resize(bin_img, (28, 28))
        reshaped_final_image = np.reshape(resized_image, (1, 28, 28, 1))
        

        date.append(reshaped_final_image)

    croped_images.append(["Date", date])


    for x, r in enumerate(account_number_coordinates):
        top_left_y = r[0][1]
        bottom_right_y = r[1][1]
        top_left_x = r[0][0]
        bottom_right_x = r[1][0]
        photo = wrapped_image[top_left_y:bottom_right_y,
                            top_left_x:bottom_right_x]

        grey_img = cv2.cvtColor(photo, cv2.COLOR_BGR2GRAY)

        # Applying Adaptive Threshold with kernel :- 21 X 21
        bin_img = cv2.adaptiveThreshold(
            grey_img, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY_INV, 21, 21)
        bin_img = cv2.GaussianBlur(bin_img, (3, 3), 0)
        bin_img = cv2.morphologyEx(bin_img, cv2.MORPH_OPEN, kernel1)
        bin_img = cv2.dilate(bin_img, kernel1, iterations=1)
        resized_image = cv2.resize(bin_img, (28, 28))
        reshaped_final_image = np.reshape(resized_image, (1, 28, 28, 1))


        acc_number.append(reshaped_final_image)

    croped_images.append(["Account Number", acc_number])
    
    
    for x, r in enumerate(amount_in_digit_coordinates):
        top_left_y = r[0][1]
        bottom_right_y = r[1][1]
        top_left_x = r[0][0]
        bottom_right_x = r[1][0]
        photo = wrapped_image[top_left_y:bottom_right_y,
                            top_left_x:bottom_right_x]

        grey_img = cv2.cvtColor(photo, cv2.COLOR_BGR2GRAY)

        # Applying Adaptive Threshold with kernel :- 21 X 21
        bin_img = cv2.adaptiveThreshold(
            grey_img, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY_INV, 21, 21)
        bin_img = cv2.GaussianBlur(bin_img, (3, 3), 0)
        bin_img = cv2.morphologyEx(bin_img, cv2.MORPH_OPEN, kernel1)
        bin_img = cv2.dilate(bin_img, kernel1, iterations=1)
        resized_image = cv2.resize(bin_img, (28, 28))
        reshaped_final_image = np.reshape(resized_image, (1, 28, 28, 1))

        amount_in_digit.append(reshaped_final_image)

    croped_images.append(["Amount(in digit)", amount_in_digit])
    
    
    for x, r in enumerate(depositer_phone_number_coordinates):
        top_left_y = r[0][1]
        bottom_right_y = r[1][1]
        top_left_x = r[0][0]
        bottom_right_x = r[1][0]
        photo = wrapped_image[top_left_y:bottom_right_y,
                            top_left_x:bottom_right_x]

        grey_img = cv2.cvtColor(photo, cv2.COLOR_BGR2GRAY)

        # Applying Adaptive Threshold with kernel :- 21 X 21
        bin_img = cv2.adaptiveThreshold(
            grey_img, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY_INV, 21, 21)
        bin_img = cv2.GaussianBlur(bin_img, (3, 3), 0)
        bin_img = cv2.morphologyEx(bin_img, cv2.MORPH_OPEN, kernel1)
        bin_img = cv2.dilate(bin_img, kernel1, iterations=1)
        resized_image = cv2.resize(bin_img, (28, 28))
        reshaped_final_image = np.reshape(resized_image, (1, 28, 28, 1))

        # cv2.imshow(str(x),resized_image)
        # cv2.waitKey(1500)

        phn_number.append(reshaped_final_image)

    croped_images.append(["Depositor's Phone Number", phn_number])

    return croped_images



def crop_letter_from_big_boxes(wrapped_image):

    croped_images = []

    for x, r in enumerate(branch_coordinates):
        top_left_y = r[0][1]
        bottom_right_y = r[1][1]
        top_left_x = r[0][0]
        bottom_right_x = r[1][0]
        photo = wrapped_image[top_left_y: bottom_right_y,
                            top_left_x: bottom_right_x]

        branch = segment_free_letters(photo)

    croped_images.append(["Branch", branch])

    
    for x, r in enumerate(currency_coordinates):
        top_left_y = r[0][1]
        bottom_right_y = r[1][1]
        top_left_x = r[0][0]
        bottom_right_x = r[1][0]
        photo = wrapped_image[top_left_y: bottom_right_y,
                            top_left_x: bottom_right_x]
    
        currency = segment_free_letters(photo)

    croped_images.append(["Currency", currency])

    
    for x, r in enumerate(acc_holder_first_name_coordinates):
        top_left_y = r[0][1]
        bottom_right_y = r[1][1]
        top_left_x = r[0][0]
        bottom_right_x = r[1][0]
        photo = wrapped_image[top_left_y: bottom_right_y,
                            top_left_x: bottom_right_x]

        account_holder_first_name = segment_free_letters(photo)

    croped_images.append(["Account Holder's Name", account_holder_first_name])

    for x, r in enumerate(acc_holder_last_name_coordinates):
        top_left_y = r[0][1]
        bottom_right_y = r[1][1]
        top_left_x = r[0][0]
        bottom_right_x = r[1][0]
        photo = wrapped_image[top_left_y: bottom_right_y,
                            top_left_x: bottom_right_x]

        account_holder_last_name = segment_free_letters(photo)

    croped_images.append(["Account Holder's Name", account_holder_last_name])


    for x, r in enumerate(depositer_first_name_coordinates):
        top_left_y = r[0][1]
        bottom_right_y = r[1][1]
        top_left_x = r[0][0]
        bottom_right_x = r[1][0]
        photo = wrapped_image[top_left_y: bottom_right_y,
                            top_left_x: bottom_right_x]

        depositer_first_name = segment_free_letters(photo)

    croped_images.append(["Depositor's Name", depositer_first_name])

    for x, r in enumerate(depositer_last_name_coordinates):
        top_left_y = r[0][1]
        bottom_right_y = r[1][1]
        top_left_x = r[0][0]
        bottom_right_x = r[1][0]
        photo = wrapped_image[top_left_y: bottom_right_y,
                            top_left_x: bottom_right_x]

        depositer_last_name = segment_free_letters(photo)

    croped_images.append(["Depositor's Name", depositer_last_name])


    for x, r in enumerate(amount_in_word1_coordinates):
        top_left_y = r[0][1]
        bottom_right_y = r[1][1]
        top_left_x = r[0][0]
        bottom_right_x = r[1][0]
        photo = wrapped_image[top_left_y: bottom_right_y,
                            top_left_x: bottom_right_x]
        
        amount_in_words_first = segment_free_letters(photo)

    croped_images.append(["Total Amount (in words)", amount_in_words_first])

    for x, r in enumerate(amount_in_word2_coordinates):
        top_left_y = r[0][1]
        bottom_right_y = r[1][1]
        top_left_x = r[0][0]
        bottom_right_x = r[1][0]
        photo = wrapped_image[top_left_y: bottom_right_y,
                            top_left_x: bottom_right_x]
    
        amount_in_words_second = segment_free_letters(photo)

    croped_images.append(["Total Amount (in words)", amount_in_words_second])

    return croped_images
    