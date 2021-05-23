import numpy as np
from Align import wrap_form_image
from Crop import crop_numbers_from_small_boxes,crop_letter_from_big_boxes

word_dictionary = {0: 'A', 1: 'B', 2: 'C', 3: 'D', 4: 'E', 5: 'F', 6: 'G', 7: 'H', 8: 'I', 9: 'J', 10: 'K', 11: 'L', 12: 'M',
                   13: 'N', 14: 'O', 15: 'P', 16: 'Q', 17: 'R', 18: 'S', 19: 'T', 20: 'U', 21: 'V', 22: 'W', 23: 'X', 24: 'Y', 25: 'Z'}
number_dictionary = {0: '0', 1: '1', 2: '2', 3: '3',
                     4: '4', 5: '5', 6: '6', 7: '7', 8: '8', 9: '9'}


def detect_text(received_image, sample_form_image, character_model, digits_model):
    
    jsondata = {"extracted_data":[]}

    wrapped_image = wrap_form_image(received_image,sample_form_image)

    cropped_images_of_numbers = crop_numbers_from_small_boxes(wrapped_image)
    cropped_images_of_letters = crop_letter_from_big_boxes(wrapped_image)
    

    for x, r in enumerate(cropped_images_of_numbers):
        predictions = predict_numbers(digits_model, r[1])
        jsondata["extracted_data"].append({"FIELD":r[0], "DATA":predictions})


    for x, r in enumerate(cropped_images_of_letters):
        predictions = predict_letters(character_model, r[1])
        jsondata["extracted_data"].append({"FIELD":r[0], "DATA":predictions})

    return jsondata


def predict_letters(model, image):
    result_list = []
    for char in image:
        if np.sum(char) > 2000:
            img_pred = word_dictionary[np.argmax(model.predict(char))]
            result_list.append(img_pred)
    return listToString(result_list)


def predict_numbers(model, image):
    result_list = []
    for char in image:
        if np.sum(char) > 2000:
            img_pred = number_dictionary[np.argmax(model.predict(char))]
            result_list.append(img_pred)

    return listToString(result_list)


def listToString(list):

    # initialize an empty string
    string = ""

    # traverse in the string
    for element in list:
        string += element

    return string
