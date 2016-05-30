package com.ly.detect;


import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {  
	  
    private static final String TAG = "ly";  
    private final int PICTURE_CHOOSE = 1;  
  
    private ImageView imageView,imageViewDest;
    private ImageView imageViewTest1,imageViewTest2,imageViewTest3;  
    private ImageView imageViewTest4,imageViewTest5;  
    private Bitmap img,imgDest;      
    private Button detect,get;
	
	private Mat test;
	private Mat imgMatDest;
	private Mat imgMat;

    private Handler handler;

    private MatOfPoint matOfPoints;
    private int srcHeight,srcWidth;

    private ImageView maker;
    private int lastX,lastY,initX,initY;
    private List<Point> points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);  
        
        initView();
        CVHelper.init(this);
        matOfPoints= new MatOfPoint();
        FaceppUtil.setDetectCallback(new MyDetectCallback());
        handler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if(message.what==1){
                    Bundle data=message.getData();
                    double angle=data.getDouble("angle");
                    imgMat=CVHelper.rotateMat(imgMat, angle);
                    CVHelper.drawMat(imgMat, imageViewTest5);
                    change();
                }else if(message.what==2){
                    keyPointsTest();
                }
                return false;
            }
        });
      
    }  
    private void initView(){
    	get = (Button) this.findViewById(R.id.get);  
        get.setOnClickListener(new OnClickListener() {  
  
            public void onClick(View arg0) {  
                // get a picture form your phone  
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);  
                photoPickerIntent.setType("image/*");  
                startActivityForResult(photoPickerIntent, PICTURE_CHOOSE);  
            }  
        });  
  
  
        detect = (Button) this.findViewById(R.id.detect);  
        detect.setVisibility(View.INVISIBLE);  
        detect.setOnClickListener(new OnClickListener() {  
            public void onClick(View arg0) {              
            	prepare();

                //test();

                faceppFetchAngleAndChange();

                //adjustAngle();
            	//change();
  
            }  
        });  
  
        imageView = (ImageView) this.findViewById(R.id.imageView);  
        imageView.setImageBitmap(img); 
        imageViewDest = (ImageView) this.findViewById(R.id.imageView_dest);  
        imageViewDest.setImageBitmap(img); 
        imageViewTest1= (ImageView) this.findViewById(R.id.imageViewTest1); 
        imageViewTest2= (ImageView) this.findViewById(R.id.imageViewTest2); 
        imageViewTest3= (ImageView) this.findViewById(R.id.imageViewTest3); 
        
        imageViewTest4= (ImageView) this.findViewById(R.id.imageViewTest4); 
        imageViewTest5= (ImageView) this.findViewById(R.id.imageViewTest5);

        maker= (ImageView) this.findViewById(R.id.maker);
        maker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        lastX=(int)motionEvent.getRawX();
                        lastY=(int)motionEvent.getRawY();
                        initX=lastX;
                        initY=lastY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) motionEvent.getRawX() - lastX;
                        int dy = (int) motionEvent.getRawY() - lastY;

                        int left = view.getLeft() + dx;
                        int top = view.getTop() + dy;
                        int right = view.getRight() + dx;
                        int bottom = view.getBottom() + dy;

                        view.layout(left, top, right, bottom);

                        lastX = (int) motionEvent.getRawX();
                        lastY = (int) motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
    }
    private void prepare(){
    	imgMat = new Mat();  
        Utils.bitmapToMat(img, imgMat);
        srcWidth=imgMat.cols();
        srcHeight=imgMat.rows();
        
        imgMatDest = new Mat();
        Utils.bitmapToMat(imgDest, imgMatDest);
    }
    private void test(){
        Rect[] ret=CVHelper.testDetect(imgMat, true);
        Rect faceRect=ret[0];
        Mat faceMat =imgMat.submat(faceRect);   //showMatInfo(faceMat, "faceMat0");
        CVHelper.drawMat(faceMat, imageViewTest4);
    }

    private void adjustAngle(){	

        double angleDest=CVHelper.getAngle(imgMat,false);
        double angleSrc=CVHelper.getAngle(imgMat,false);
        double angleTotal=angleDest-angleSrc;
        
        Log.i("ly", "angleDest->"+angleDest+" angleSrc->"+angleSrc+" total->"+angleTotal);
        imgMat=CVHelper.rotateMat(imgMat, angleTotal);
        CVHelper.drawMat(imgMat, imageViewTest5);       
    }
    private void faceppFetchAngleAndChange(){
        FaceppUtil.detect(img);
    }
    private void keyPointsTest(){
        Rect[] ret=CVHelper.faceDetectFine(imgMat,false);
        Rect faceRect=ret[0];
        int dx=faceRect.x;
        int dy=faceRect.y;

        Mat faceMat =imgMat.submat(faceRect);   //showMatInfo(faceMat, "faceMat0");
        List<Point> tmp=new ArrayList<Point>();
        for(Point p:points){
           // tmp.add(new Point(p.x-dx,p.y-dy));
            tmp.add(p);
        }
        matOfPoints.fromList(tmp);

        Mat faceMask=CVHelper.getKeyPointMask(imgMat,CvType.CV_64FC4,matOfPoints);
        CVHelper.drawMat(faceMask, imageViewTest4);
    }
    private void change(){
//    	imgMat = new Mat();  
//        Utils.bitmapToMat(img, imgMat);         // test=imgMat; Log.i("ly", "rows="+getLandmarks(test.getNativeObjAddr()));
        Rect[] ret=CVHelper.faceDetectFine(imgMat,false);
    	Rect faceRect=ret[0];
        Rect eyeRect = ret[1];
        Rect noseRect = ret[2];
        Rect mouseRect = ret[3];
        Mat faceMat =imgMat.submat(faceRect);   //showMatInfo(faceMat, "faceMat0");

//        imgMatDest = new Mat();
//        Utils.bitmapToMat(imgDest, imgMatDest);
        Rect[] retDest=CVHelper.faceDetectFine(imgMatDest,false);
        Rect faceRectDest=retDest[0];
        Rect eyeRectDest = retDest[1];
        Rect noseRectDest = retDest[2];
        Rect mouseRectDest = retDest[3];
        Mat faceMatDest=imgMatDest.submat(faceRectDest);
        
        //get mask
        Mat faceMask=CVHelper.getMask(faceMat,CvType.CV_64FC4,eyeRect,noseRect,mouseRect); //showMatInfo(faceMat, "faceMat1");showMatInfo(faceMask, "faceMask");
        Mat faceMaskScaled=CVHelper.scaledMat(faceMask, faceRectDest, CvType.CV_64FC4);
        Mat faceMaskDest=CVHelper.getMask(faceMatDest,CvType.CV_64FC4,eyeRectDest,noseRectDest,mouseRectDest);

        Core.max(faceMaskScaled, faceMaskDest, faceMaskScaled);
             
        
        //correct color
        int blur_amount=(int) (faceRect.width*CVHelper.COLOUR_CORRECT_BLUR_FRAC);       
        if(blur_amount%2==0){
        	blur_amount+=1;
        }
        Log.i(TAG, "blur_amount->"+blur_amount);
        
        Mat faceMatScaled=CVHelper.scaledMat(faceMat,  faceRectDest,CvType.CV_64FC4); //showMatInfo(faceMatScaled, "faceMatScaled");           
        faceMatDest.convertTo(faceMatDest, CvType.CV_64FC4);
                
        Mat im1_blur=new Mat();
        Mat im2_blur=new Mat();    
        Imgproc.GaussianBlur(faceMatScaled, im2_blur, new Size(blur_amount,blur_amount), 0); //showMatInfo(faceMatScaled, "faceMatScaled"); 
        Imgproc.GaussianBlur(faceMatDest, im1_blur, new Size(blur_amount,blur_amount), 0); //showMatInfo(im1_blur, "im1_blur"); 
       
        
        Mat tmp=new Mat(im2_blur.rows(), im2_blur.cols(), im2_blur.type());       //showMatInfo(tmp, "tmp");
        Mat ones=Mat.ones(im2_blur.rows(), im2_blur.cols(), im2_blur.type());      // showMatInfo(ones, "ones");         
        Core.compare(im2_blur, ones, tmp, Core.CMP_LE);                 CVHelper.showMatInfo(tmp, "tmp");
        tmp.convertTo(tmp, tmp.type(), 128);                     //CVHelper.showData(tmp, "tmp");   // showMatInfo(im2_blur, "im2_blur");
        tmp.assignTo(tmp, im2_blur.type());                      //CVHelper.showData(tmp, "tmp2");    // showMatInfo(tmp, "tmp");
        Core.add(im2_blur, tmp, im2_blur);          
    
        Core.multiply(faceMatScaled,im1_blur ,im1_blur);
        Core.divide(im1_blur, im2_blur, faceMatScaled);  
              
        CVHelper.drawMat(imgMat, imageView);
        
        //change face  
        Mat combinedMask=faceMaskScaled;               //showData(combinedMask, "combinedMask");
       
        Mat onesMat=Mat.ones(combinedMask.rows(), combinedMask.cols(), combinedMask.type());  
        onesMat.setTo(new Scalar(1.0, 1.0, 1.0, 1.0));
        Mat antiCombinedMask=new Mat(combinedMask.rows(), combinedMask.cols(), combinedMask.type());
        Core.subtract(onesMat, combinedMask, antiCombinedMask);	  //showData(antiCombinedMask, "antiCombinedMask");      


        //CVHelper.drawMat(antiCombinedMask, imageViewTest1);

        Mat tmp1=new Mat(combinedMask.rows(), combinedMask.cols(), combinedMask.type());
        Core.multiply(faceMatScaled, combinedMask, tmp1);
        CVHelper.drawMat(tmp1, imageViewTest1);

        Mat tmp2=new Mat(combinedMask.rows(), combinedMask.cols(), combinedMask.type());
        Core.multiply(faceMatDest, antiCombinedMask, tmp2);
        CVHelper.drawMat(tmp2, imageViewTest2);
 

     
        Core.add(tmp1, tmp2, faceMatDest);             
        CVHelper.drawMat(faceMatDest, imageViewTest3);
       
        faceMatDest.assignTo(faceMatDest, CvType.CV_8UC4);
        Imgproc.GaussianBlur( faceMatDest,  faceMatDest, new Size(CVHelper.WHOLE_BLUR_SIZE,CVHelper.WHOLE_BLUR_SIZE), 0); 
//        Imgproc.medianBlur(faceMatDest, faceMatDest,  CVHelper.WHOLE_BLUR_SIZE); //CVHelper.showMatInfo(faceMatDest, "faceMatDest");
       
        faceMatDest.copyTo(imgMatDest.submat(faceRectDest));      
        CVHelper.drawMat(imgMatDest, imageViewDest);

        maker.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams lp= (RelativeLayout.LayoutParams) maker.getLayoutParams();
        lp.setMargins(0,imageViewDest.getHeight()/2,0,0);
        maker.setLayoutParams(lp);

    }
    
    @Override  
    protected void onActivityResult(int requestCode, int resultCode,  
            Intent intent) {  
        super.onActivityResult(requestCode, resultCode, intent);  
  
        // the image picker callback  
        if (requestCode == PICTURE_CHOOSE) {  
            if (intent != null) {  
  
                Cursor cursor = getContentResolver().query(intent.getData(),  
                        null, null, null, null);  
                cursor.moveToFirst();  
                int idx = cursor.getColumnIndex(ImageColumns.DATA);  
                String fileSrc = cursor.getString(idx);  
  
                Options options = new Options();  
                options.inJustDecodeBounds = true;  
                img = BitmapFactory.decodeFile(fileSrc, options);  
  
                options.inSampleSize = Math.max(1, (int) Math.ceil(Math.max(  
                        (double) options.outWidth / 1024f,  
                        (double) options.outHeight / 1024f)));  
                options.inJustDecodeBounds = false;  
                
                img = BitmapFactory.decodeFile(fileSrc, options);  
                imageView.setImageBitmap(img);   
  
                imgDest=BitmapFactory.decodeResource(getResources(), R.drawable.star);
                imageViewDest.setImageBitmap(imgDest);  
                
                detect.setVisibility(View.VISIBLE);  
            } else {  
                Log.d(TAG, "idButSelPic Photopicker canceled");  
            }  
        }  
    }
    interface DetectCallback {
        void angleResult(double angle);
        void keyPointsResult(List<JSONObject> keyPoints);
    }

    class MyDetectCallback implements DetectCallback {
        public void angleResult(double angle) {
            Log.i(TAG,"angle->"+angle);
            Message message=new Message();
            message.what=1;
            Bundle data=new Bundle();
            data.putDouble("angle",angle);
            message.setData(data);
            handler.sendMessage(message);
        }

        @Override
        public void keyPointsResult(List<JSONObject> keyPoints) {

            points = new ArrayList<Point>();
            Log.i("ly","src->"+srcWidth+","+srcHeight);
            for (JSONObject item:keyPoints){
                try {

                    Point p=new Point(item.getDouble("x")*srcWidth/100,item.getDouble("y")*srcHeight/100);
                    points.add(p);
                   // Log.i("ly","point->"+p.x+","+p.y);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //matOfPoints.fromList(points);

            Message message=new Message();
            message.what=2;
            handler.sendMessage(message);
        }
    }
//    static{
//
//		System.loadLibrary("detect");
//		System.loadLibrary("dlib_shared");
//	}
    //private native int getLandmarks(long matAddr);
}  