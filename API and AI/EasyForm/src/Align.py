import cv2
import numpy as np

import os


def main():
    
    APP_ROOT = os.path.dirname(os.path.abspath(__file__))

    image = cv2.imread(os.path.join(APP_ROOT, '/EasyForm/src/test.jpg'))
    sample_form_image = cv2.imread(os.path.join(APP_ROOT, '/EasyForm/src/sample_form.jpg'))

    wrap_form_image(image,sample_form_image)



def wrap_form_image(received_image, sample_form_image):

    match_filter = 25

    h, w, c = sample_form_image.shape

    # Initiate SIFT detector
    orb = cv2.ORB_create(5000)
    key_points_of_sample_img, descriptors_of_sample_img = orb.detectAndCompute(sample_form_image, None)

    key_points_of_received_img, descriptors_of_received_img = orb.detectAndCompute(received_image, None)


    # print(sample_form_image.shape)
    # print(received_image.shape)
    
    # cv2.imshow("received image", img)
    # cv2.imshow("sample_form_image", sample_form_image)
    # cv2.waitKey(2000)



    brute_force_result = cv2.BFMatcher(cv2.NORM_HAMMING)
    matches = brute_force_result.match(descriptors_of_received_img, descriptors_of_sample_img)
    matches.sort(key=lambda x: x.distance) 
    good_matches = matches[:int(len(matches)*(match_filter/100))]
    imgMatch = cv2.drawMatches(received_image, key_points_of_received_img, sample_form_image, key_points_of_sample_img, good_matches[:20], None, flags=2)

    h, w, c = imgMatch.shape
    imgMatch = cv2.resize(imgMatch,(w//5,h//5))

    # cv2.imshow("image match",imgMatch)
    # cv2.waitKey(2000)

    srcPoints = np.float32(
        [key_points_of_received_img[m.queryIdx].pt for m in good_matches]).reshape(-1, 1, 2)
    dstPoints = np.float32(
        [key_points_of_sample_img[m.trainIdx].pt for m in good_matches]).reshape(-1, 1, 2)

    M, _ = cv2.findHomography(srcPoints, dstPoints, cv2.RANSAC, 5.0)

    return cv2.warpPerspective(received_image, M, (w, h))



if __name__ == "__main__":
    main()