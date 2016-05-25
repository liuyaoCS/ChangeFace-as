package com.ly.detect;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

class FaceppUtil {
    private static final String TAG = "FACE++";
    public static final String APP_KEY = "e960ec2e802650ffbff7878acdad1611";
    public static final String APP_SECRET = "sxyr6IyOBqYFvD1K3iwf5TVpSyMgB3WP";

    public static MainActivity.DetectCallback callback = null;
    public static  void setDetectCallback(MainActivity.MyDetectCallback detectCallback) {
        callback = detectCallback;
    }
    public static void detect(final Bitmap img) {

        new Thread(new Runnable() {

            public void run() {
                HttpRequests httpRequests = new HttpRequests(APP_KEY, APP_SECRET, true, true);
                //Log.v(TAG, "image size : " + img.getWidth() + " " + img.getHeight());

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                float scale = Math.min(1, Math.min(600f / img.getWidth(), 600f / img.getHeight()));
                Log.d(TAG, "SCALE:" + scale);
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);

                Bitmap imgSmall = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, false);
                //Log.v(TAG, "imgSmall size : " + imgSmall.getWidth() + " " + imgSmall.getHeight());

                imgSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] array = stream.toByteArray();
                Log.i(TAG,"pic size->"+array.length/1024+"k");

                try {
                    //detect
                    JSONObject faceResult = httpRequests.detectionDetect(new PostParameters().setImg(array).setAttribute("pose"));
                    JSONArray faceArray = faceResult.getJSONArray("face");
                    if (faceArray.length() == 0) {
                        Log.e(TAG,"result 0");
                        return;
                    }
//                    JSONObject position = faceArray.getJSONObject(0).getJSONObject("position");
//                    JSONObject leftEye=position.getJSONObject("eye_left");
//                    JSONObject rightEye=position.getJSONObject("eye_right");
//
//                    double lx=leftEye.getDouble("x");
//                    double ly=leftEye.getDouble("y");
//                    double rx=rightEye.getDouble("x");
//                    double ry=rightEye.getDouble("y");
//                    double t1=(ry-ly)/(rx-lx);
//                    double angle=Math.atan(t1)*180/Math.PI;

                    JSONObject attribute = faceArray.getJSONObject(0).getJSONObject("attribute");
                    JSONObject pose=attribute.getJSONObject("pose");
                    JSONObject roll_angle=pose.getJSONObject("roll_angle");
                    double angle=roll_angle.getDouble("value");
                    if (callback != null) {
                        callback.angleResult(angle);
                    }


                    //get keyPointresult
//                    String key_face_id=faceArray.getJSONObject(0).getString("face_id");
//                    JSONObject keyPointsResult=httpRequests.detectionLandmark(new PostParameters().setFaceId(key_face_id).setType("25p"));
//                    JSONArray result=keyPointsResult.getJSONArray("result");
//                    JSONObject landmark=result.getJSONObject(0).getJSONObject("landmark");
//
//                    JSONObject left_eye_bottom=landmark.getJSONObject("left_eye_bottom");
//                    JSONObject left_eye_center=landmark.getJSONObject("left_eye_center");
//                    JSONObject left_eye_left_corner=landmark.getJSONObject("left_eye_left_corner");
//                    JSONObject left_eye_pupil=landmark.getJSONObject("left_eye_pupil");
//                    JSONObject left_eye_right_corner=landmark.getJSONObject("left_eye_right_corner");
//                    JSONObject left_eye_top=landmark.getJSONObject("left_eye_top");
//                    JSONObject left_eyebrow_left_corner=landmark.getJSONObject("left_eyebrow_left_corner");
//                    JSONObject left_eyebrow_right_corner=landmark.getJSONObject("left_eyebrow_right_corner");
//                    JSONObject mouth_left_corner=landmark.getJSONObject("mouth_left_corner");
//                    JSONObject mouth_lower_lip_bottom=landmark.getJSONObject("mouth_lower_lip_bottom");
//                    JSONObject mouth_lower_lip_top=landmark.getJSONObject("mouth_lower_lip_top");
//                    JSONObject mouth_right_corner=landmark.getJSONObject("mouth_right_corner");
//                    JSONObject mouth_upper_lip_bottom=landmark.getJSONObject("mouth_upper_lip_bottom");
//                    JSONObject mouth_upper_lip_top=landmark.getJSONObject("mouth_upper_lip_top");
//                    JSONObject nose_left=landmark.getJSONObject("nose_left");
//                    JSONObject nose_right=landmark.getJSONObject("nose_right");
//                    JSONObject nose_tip=landmark.getJSONObject("nose_tip");
//                    JSONObject right_eye_bottom=landmark.getJSONObject("right_eye_bottom");
//                    JSONObject right_eye_center=landmark.getJSONObject("right_eye_center");
//                    JSONObject right_eye_left_corner=landmark.getJSONObject("right_eye_left_corner");
//                    JSONObject right_eye_pupil=landmark.getJSONObject("right_eye_pupil");
//                    JSONObject right_eye_right_corner=landmark.getJSONObject("right_eye_right_corner");
//                    JSONObject right_eye_top=landmark.getJSONObject("right_eye_top");
//                    JSONObject right_eyebrow_left_corner=landmark.getJSONObject("right_eyebrow_left_corner");
//                    JSONObject right_eyebrow_right_corner=landmark.getJSONObject("right_eyebrow_right_corner");
//
//                    List<JSONObject> keyPoints=new ArrayList<JSONObject>();
//                    keyPoints.add(left_eye_bottom);
//                    keyPoints.add(left_eye_center);
//                    keyPoints.add(left_eye_left_corner);
//                    keyPoints.add(left_eye_pupil);
//                    keyPoints.add(left_eye_right_corner);
//                    keyPoints.add(left_eye_top);
//                    keyPoints.add(left_eyebrow_left_corner);
//                    keyPoints.add(left_eyebrow_right_corner);
//                    keyPoints.add(mouth_left_corner);
//                    keyPoints.add(mouth_lower_lip_bottom);
//                    keyPoints.add(mouth_lower_lip_top);
//                    keyPoints.add(mouth_right_corner);
//                    keyPoints.add(mouth_upper_lip_bottom);
//                    keyPoints.add(mouth_upper_lip_top);
//                    keyPoints.add(nose_left);
//                    keyPoints.add(nose_right);
//                    keyPoints.add(nose_tip);
//                    keyPoints.add(right_eye_bottom);
//                    keyPoints.add(right_eye_center);
//                    keyPoints.add(right_eye_left_corner);
//                    keyPoints.add(right_eye_pupil);
//                    keyPoints.add(right_eye_right_corner);
//                    keyPoints.add(right_eye_top);
//                    keyPoints.add(right_eyebrow_left_corner);
//                    keyPoints.add(right_eyebrow_right_corner);
//
//                    if (callback != null) {
//                        callback.keyPointsResult(keyPoints);
//                    }
                    //finished , then call the callback function



                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}