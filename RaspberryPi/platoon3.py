import numpy as np
import cv2 as cv


camera = PiCamera()
camera.resolution = (640, 480)
camera.framerate = 32
camera.rotation = 180
rawCapture = PiRGBArray(camera, size=(640, 480))


# allow the camera to warmup
time.sleep(0.1)

frameInit = camera.capture(rawCapture, format="bgr")
frameImg = frameInit.array
r,h,c,w = 250,90,400,125  # simply hardcoded the values
track_window = (c,r,w,h)
# set up the ROI for tracking
roi = frameImg[r:r+h, c:c+w]
hsv_roi =  cv.cvtColor(roi, cv.COLOR_BGR2HSV)
mask = cv.inRange(hsv_roi, np.array((0., 60.,32.)), np.array((180.,255.,255.)))
roi_hist = cv.calcHist([hsv_roi],[0],mask,[180],[0,180])
cv.normalize(roi_hist,roi_hist,0,255,cv.NORM_MINMAX)

# capture frames from the camera
for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
    img = frame.array
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
    dst = cv2.calcBackProject([hsv],[0],roi_hist,[0,180],1)
    # apply meanshift to get the new location
    ret, track_window = cv2.CamShift(dst, track_window, term_crit)
    # Draw it on image
    pts = cv2.boxPoints(ret)
    pts = np.int0(pts)
    img2 = cv2.polylines(img,[pts],True, 255,2)
    cv2.imshow('img2',img2)
    key = cv.waitKey(1) & 0xFF
    if key == ord("q"):
        break
