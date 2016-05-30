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

    private static final String[] points_25=new String[]{
            "left_eye_bottom","left_eye_center","left_eye_left_corner","left_eye_pupil","left_eye_right_corner","left_eye_top","left_eyebrow_left_corner","left_eyebrow_right_corner",
            "mouth_left_corner","mouth_lower_lip_bottom","mouth_lower_lip_top","mouth_right_corner","mouth_upper_lip_bottom","mouth_upper_lip_top",
            "nose_left","nose_right","nose_tip",
            "right_eye_bottom","right_eye_center","right_eye_left_corner","right_eye_pupil","right_eye_right_corner","right_eye_top","right_eyebrow_left_corner","right_eyebrow_right_corner"
    };
    private static final String[] points_83=new String[]{
            "contour_chin","contour_left1","contour_left2","contour_left3","contour_left4","contour_left5","contour_left6",
            "contour_left7","contour_left8","contour_left9",
            "contour_right1","contour_right2","contour_right3","contour_right4","contour_right5","contour_right6",
            "contour_right7","contour_right8","contour_right9",
            "left_eye_bottom","left_eye_center","left_eye_left_corner","left_eye_lower_left_quarter","left_eye_lower_right_quarter",
            "left_eye_pupil","left_eye_right_corner","left_eye_top","left_eye_upper_left_quarter","left_eye_upper_right_quarter",
            "left_eyebrow_left_corner","left_eyebrow_lower_left_quarter","left_eyebrow_lower_middle","left_eyebrow_lower_right_quarter",
            "left_eyebrow_right_corner","left_eyebrow_upper_left_quarter","left_eyebrow_upper_middle","left_eyebrow_upper_right_quarter",
            "mouth_left_corner","mouth_lower_lip_bottom","mouth_lower_lip_bottom","mouth_lower_lip_bottom","mouth_lower_lip_left_contour3",
            "mouth_lower_lip_right_contour1","mouth_lower_lip_right_contour2","mouth_lower_lip_top",
            "mouth_right_corner","mouth_upper_lip_bottom","mouth_upper_lip_left_contour1","mouth_upper_lip_left_contour2",
            "mouth_upper_lip_left_contour3","mouth_upper_lip_right_contour1","mouth_upper_lip_right_contour2",
            "mouth_upper_lip_right_contour3","mouth_upper_lip_top","nose_contour_left1","nose_contour_left2",
            "nose_contour_left3","nose_contour_lower_middle","nose_contour_right1","nose_contour_right2",
            "nose_contour_right3","nose_left","nose_right","nose_tip","right_eye_bottom","right_eye_center",
            "right_eye_left_corner","right_eye_lower_left_quarter","right_eye_lower_right_quarter","right_eye_pupil",
            "right_eye_right_corner","right_eye_top","right_eye_upper_left_quarter","right_eye_upper_right_quarter",
            "right_eyebrow_left_corner","right_eyebrow_lower_left_quarter","right_eyebrow_lower_middle","right_eyebrow_lower_right_quarter",
            "right_eyebrow_right_corner","right_eyebrow_upper_left_quarter","right_eyebrow_upper_middle","right_eyebrow_upper_right_quarter"
    };
    private static final String[] points_contour=new String[]{
            "contour_chin","contour_left1","contour_left2","contour_left3","contour_left4",
            "contour_left5","contour_left6", "contour_left7","contour_left8","contour_left9",
            "contour_right1","contour_right2","contour_right3","contour_right4",
            "contour_right5","contour_right6", "contour_right7","contour_right8","contour_right9",
            "left_eyebrow_left_corner", "left_eyebrow_right_corner",
            "left_eyebrow_upper_left_quarter","left_eyebrow_upper_middle","left_eyebrow_upper_right_quarter",
            "right_eyebrow_left_corner", "right_eyebrow_right_corner",
            "right_eyebrow_upper_left_quarter","right_eyebrow_upper_middle","right_eyebrow_upper_right_quarter"
    };
    private static final String[] points_contour2=new String[]{
            "left_eyebrow_upper_left_quarter", "right_eyebrow_upper_right_quarter",
            "nose_left","nose_right",
            "mouth_left_corner","mouth_right_corner",
    };


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
                    String key_face_id=faceArray.getJSONObject(0).getString("face_id");
                    JSONObject keyPointsResult=httpRequests.detectionLandmark(new PostParameters().setFaceId(key_face_id).setType("83p"));
                    JSONArray result=keyPointsResult.getJSONArray("result");
                    JSONObject landmark=result.getJSONObject(0).getJSONObject("landmark");

                    List<JSONObject> keyPoints=new ArrayList<JSONObject>();
                    for(String str:points_contour2){
                        JSONObject jo=landmark.getJSONObject(str);
                        keyPoints.add(jo);
                    }

                    if (callback != null) {
                        callback.keyPointsResult(keyPoints);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}