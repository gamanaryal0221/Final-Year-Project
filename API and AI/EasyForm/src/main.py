from flask.json import load
from DetectData import detect_text
from flask import Flask, request, jsonify
import os
import uuid

import cv2
from tensorflow.keras.models import load_model

app = Flask(__name__)

APP_ROOT = os.path.dirname(os.path.abspath(__file__))

sample_form_dir = os.path.join(APP_ROOT, '/EasyForm/src/Sample Form Images')
received_img_dir = os.path.join(APP_ROOT, '/EasyForm/src/Received Images')

alphabet_model = load_model(os.path.join(APP_ROOT, '/EasyForm/src/models/alphabet_model.h5'))
digit_model = load_model(os.path.join(APP_ROOT, '/EasyForm/src/models/digit_model.h5'))

@app.route('/')
def starter():
    ip_address = request.remote_addr
    return "Welcome to Text Recognition System<br>Unique path for this server is [" + ip_address+"]"
    

@app.route('/POST',methods=["POST"])
def handel_post_request():

    for file in request.files.getlist("form_image"):

        form_id = file.filename
        print("--- Text detection request received [FORM ID = "+form_id+"] ---")
        
        if os.path.exists(sample_form_dir+"/"+form_id+".jpg"):
            print("\n### This format of form sample [FORM ID = "+form_id+"] is available ###")

            return save_image_and_detect_data(file,form_id)

        else:            
            print("\n!!! This format of form sample [FORM ID = "+form_id+"] is not available !!!\n")
            return "Sorry, This form format is not registered in your library"

        
def save_image_and_detect_data(file, form_id):
    
        current_image_path = received_img_dir+"/"+form_id
    
        if not os.path.exists(current_image_path):
            os.makedirs(current_image_path)

        current_image_name = str(uuid.uuid4())+".jpg"
        print("\nReceived image name is :- "+current_image_name)

        file.save("/".join([current_image_path, current_image_name])) 

        # check if the received image is saved or not
        if os.path.exists(current_image_path+"/"+current_image_name):
            print("\n### Received image [NAME ="+current_image_name+"] is saved in [PATH = "+current_image_path+"] ###")
            
            received_image = cv2.imread(current_image_path+"/"+current_image_name)
            sample_form_image = cv2.imread(sample_form_dir+"/"+form_id+".jpg")

            extracted_data = detect_text(received_image,sample_form_image,alphabet_model,digit_model)
            print(extracted_data)

            return extracted_data

        else:
            print("\n!!! Received image failed to save !!!")
            return "Sorry, This form format is not registered in your library"


if __name__ == "__main__":
    app.run(host='0.0.0.0', debug=True)
    # app.run(debug=True)