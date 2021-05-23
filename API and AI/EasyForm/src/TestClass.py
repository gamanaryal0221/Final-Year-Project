
from DetectData import detect_text
import cv2
from tensorflow.keras.models import load_model
import os


def main():
    
    APP_ROOT = os.path.dirname(os.path.abspath(__file__))

    image = cv2.imread(os.path.join(APP_ROOT, '/EasyForm/src/test.jpg'))
    sample_form_image = cv2.imread(os.path.join(APP_ROOT, '/EasyForm/src/sample_form.jpg'))

    alphabet_model = load_model(os.path.join(APP_ROOT, '/EasyForm/src/models/alphabet_model.h5'))
    digit_model = load_model(os.path.join(APP_ROOT, '/EasyForm/src/models/digit_model.h5'))

    testImage(image,sample_form_image,alphabet_model,digit_model)


def testImage(image,sample_form_image,alphabet_model,digit_model):
    
    json_data = detect_text(image,sample_form_image,alphabet_model,digit_model)
    print(json_data)
    
    
if __name__ == "__main__":
    main()