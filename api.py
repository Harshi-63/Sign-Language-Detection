from flask import Flask, Response
from flask_cors import CORS
from flask_sock import Sock  # WebSocket Support
import pickle
import cv2
import mediapipe as mp
import numpy as np
import time  # For delay

app = Flask(__name__)
sock = Sock(app)  # WebSocket Setup
CORS(app)

# Load the trained model
with open(r"C:\Users\KIIT\Documents\GitHub\Sign-Language-Detection\SignLanguageDetection_Model\model.p", 'rb') as f:
    model_dict = pickle.load(f)
    model = model_dict['model']

# Initialize MediaPipe Hands
mp_hands = mp.solutions.hands
hands = mp_hands.Hands(static_image_mode=False, min_detection_confidence=0.8, min_tracking_confidence=0.8)

# Labels dictionary for predictions
labels_dict = {
    0: 'A', 1: 'B', 2: 'C', 3: 'D', 4: 'E', 5: 'F',
    6: 'G', 7: 'H', 8: 'I', 9: 'J', 10: 'K', 11: 'L', 12: 'M',
    13: 'N', 14: 'O', 15: 'P', 16: 'Q', 17: 'R', 18: 'S',
    19: 'T', 20: 'U', 21: 'V', 22: 'W', 23: 'X', 24: 'Y',
    25: 'Z', 26: 'Hello', 27: 'Thank You'
}

cap = cv2.VideoCapture(0)
last_detection_time = 0  # Track last detection time

def generate_frames():
    global last_detection_time

    while True:
        ret, frame = cap.read()
        if not ret:
            break

        # Ensure a 3-second delay between detections
        current_time = time.time()
        if current_time - last_detection_time < 1:
            continue  # Skip this frame if within 3 sec delay

        frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        results = hands.process(frame_rgb)

        predicted_character = "No Prediction"

        if results.multi_hand_landmarks:
            for hand_landmarks in results.multi_hand_landmarks:
                data_aux = []
                x_ = [landmark.x for landmark in hand_landmarks.landmark]
                y_ = [landmark.y for landmark in hand_landmarks.landmark]

                min_x, min_y = min(x_), min(y_)

                for landmark in hand_landmarks.landmark:
                    data_aux.append(landmark.x - min_x)
                    data_aux.append(landmark.y - min_y)

                if len(data_aux) == 42:
                    prediction = model.predict([np.asarray(data_aux)])
                    predicted_character = labels_dict.get(int(prediction[0]), "Unknown")

                # Draw bounding box and prediction
                h, w, _ = frame.shape
                x1, y1 = int(min_x * w), int(min_y * h)
                x2, y2 = int(max(x_) * w), int(max(y_) * h)

                cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)
                cv2.putText(frame, predicted_character, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 0, 0), 2)

                last_detection_time = current_time  # Update last detection time

        ret, buffer = cv2.imencode('.jpg', frame)
        frame = buffer.tobytes()

        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')

# Route for Video Streaming
@app.route('/video_feed_with_prediction')
def video_feed():
    return Response(generate_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')

# WebSocket for Instant Predictions
@sock.route('/ws')
def realtime_prediction(ws):
    global last_detection_time

    while True:
        _, frame = cap.read()
        if frame is not None:
            current_time = time.time()
            if current_time - last_detection_time < 3:
                continue  # Maintain 3-second delay

            frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            results = hands.process(frame_rgb)

            predicted_character = "No Prediction"

            if results.multi_hand_landmarks:
                for hand_landmarks in results.multi_hand_landmarks:
                    data_aux = []
                    x_ = [landmark.x for landmark in hand_landmarks.landmark]
                    y_ = [landmark.y for landmark in hand_landmarks.landmark]

                    min_x, min_y = min(x_), min(y_)

                    for landmark in hand_landmarks.landmark:
                        data_aux.append(landmark.x - min_x)
                        data_aux.append(landmark.y - min_y)

                    if len(data_aux) == 42:
                        prediction = model.predict([np.asarray(data_aux)])
                        predicted_character = labels_dict.get(int(prediction[0]), "Unknown")

                    last_detection_time = current_time  # Update last detection time

            ws.send(predicted_character)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True, threaded=True)
