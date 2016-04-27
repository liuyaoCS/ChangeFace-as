package com.ly.detect;


import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
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
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

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
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);  
        
        initView();
        CVHelper.init(this);
      
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
            	//adjustAngle();
            	change();   
  
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
    }
    private void prepare(){
    	imgMat = new Mat();  
        Utils.bitmapToMat(img, imgMat);  
        
        imgMatDest = new Mat();
        Utils.bitmapToMat(imgDest, imgMatDest);
    }
    private void adjustAngle(){	
        
        Rect[] ret=CVHelper.eyeTestDetect(imgMat, true);
    	Rect faceRect=ret[0];       
        Mat faceMat =imgMat.submat(faceRect);   //showMatInfo(faceMat, "faceMat0");   
        CVHelper.drawMat(faceMat, imageViewTest4);
    	            
        double angledest=CVHelper.getAngle(imgMatDest);
        double anglesrc=CVHelper.getAngle(imgMat);
        
        Log.i("ly", "angledest->"+angledest+" anglesrc->"+anglesrc);
        imgMat=CVHelper.rotateMat(imgMat, angledest-anglesrc);
        CVHelper.drawMat(imgMat, imageViewTest5);       
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
        CVHelper.drawMat(antiCombinedMask, imageViewTest1);
        
        Mat tmp1=new Mat(combinedMask.rows(), combinedMask.cols(), combinedMask.type()); 
        Core.multiply(faceMatDest, antiCombinedMask, tmp1);
        CVHelper.drawMat(tmp1, imageViewTest2);
 
        Mat tmp2=new Mat(combinedMask.rows(), combinedMask.cols(), combinedMask.type());  
        Core.multiply(faceMatScaled, combinedMask, tmp2);
        CVHelper.drawMat(tmp2, imageViewTest1);
     
        Core.add(tmp1, tmp2, faceMatDest);             
        CVHelper.drawMat(faceMatDest, imageViewTest3);
       
        faceMatDest.assignTo(faceMatDest, CvType.CV_8UC4);
        Imgproc.GaussianBlur( faceMatDest,  faceMatDest, new Size(CVHelper.WHOLE_BLUR_SIZE,CVHelper.WHOLE_BLUR_SIZE), 0); 
//        Imgproc.medianBlur(faceMatDest, faceMatDest,  CVHelper.WHOLE_BLUR_SIZE); //CVHelper.showMatInfo(faceMatDest, "faceMatDest");
       
        faceMatDest.copyTo(imgMatDest.submat(faceRectDest));      
        CVHelper.drawMat(imgMatDest, imageViewDest);
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
    static{
		
		System.loadLibrary("detect");
		System.loadLibrary("dlib_shared");
	}
    private native int getLandmarks(long matAddr);
}  