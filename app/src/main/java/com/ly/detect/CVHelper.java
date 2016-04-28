package com.ly.detect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.widget.ImageView;

public class CVHelper {
	
	private static final String TAG = "ly"; 	
	
	private static final int CV_FILLED = -1;
	private static final int MASK_BLUR_SIZE = 31;
	
	public static final float COLOUR_CORRECT_BLUR_FRAC = 0.5f;
	public static final int WHOLE_BLUR_SIZE = 5;
	

    private static final Scalar WHITE_COLOR = new Scalar(255, 255, 255, 255);
	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 0, 0, 255);
	private static final Scalar EYE_RECT_COLOR = new Scalar(255, 0, 0, 255);
	private static final Scalar NOSE_RECT_COLOR = new Scalar(0, 255, 0, 255);
	private static final Scalar MOUTH_RECT_COLOR = new Scalar(0, 0,255, 255);
	
	private static CascadeClassifier faceDetector;
	private static CascadeClassifier lefteyeDetector;
	private static CascadeClassifier righteyeDetector;
	private static CascadeClassifier eyepairDetector;
	private static CascadeClassifier noseDetector;
	private static CascadeClassifier mouthDetector;
	
	private static Context mContext;
	
	public static void init(Context context){
		mContext=context;
		
		Log.i(TAG, "OpenCV library load!");
		if (!OpenCVLoader.initDebug()) {
			Log.e(TAG, "OpenCV load not successfully");
		} else {
			Log.i(TAG, "OpenCV load  successfully");
			generateDetectors();
		}
	}

    public static Mat getMask(Mat im,int type,Rect eyeRect,Rect noseRect,Rect mouseRect){
    	Mat ret=Mat.zeros(im.rows(), im.cols(), type);  
    	//Core.fillConvexPoly(ret, points, EYE_RECT_COLOR);
    	Core.rectangle(ret, eyeRect.tl(), eyeRect.br(), WHITE_COLOR,CV_FILLED);//CV_FILLED
    	Core.rectangle(ret, noseRect.tl(), noseRect.br(), WHITE_COLOR,CV_FILLED);//CV_FILLED   
    	Core.rectangle(ret, mouseRect.tl(), mouseRect.br(), WHITE_COLOR,CV_FILLED);//CV_FILLED   
 
    	Imgproc.GaussianBlur(ret, ret, new Size(MASK_BLUR_SIZE,MASK_BLUR_SIZE),0);
    	ret.convertTo(ret, type, 1/255.0);
    		    
    	return ret;	
    }
	public static Mat getMask2(Mat im, int type, MatOfPoint matOfPoint){
		Mat ret=Mat.zeros(im.rows(), im.cols(), type);
		Core.fillConvexPoly(ret, matOfPoint, WHITE_COLOR);
//		Core.rectangle(ret, eyeRect.tl(), eyeRect.br(), WHITE_COLOR,CV_FILLED);//CV_FILLED
//		Core.rectangle(ret, noseRect.tl(), noseRect.br(), WHITE_COLOR,CV_FILLED);//CV_FILLED
//		Core.rectangle(ret, mouseRect.tl(), mouseRect.br(), WHITE_COLOR,CV_FILLED);//CV_FILLED

		Imgproc.GaussianBlur(ret, ret, new Size(MASK_BLUR_SIZE,MASK_BLUR_SIZE),0);
		ret.convertTo(ret, type, 1/255.0);

		return ret;
	}
    public static void drawMat(Mat mat,ImageView view){
        
    	Mat show=new Mat();
    	mat.assignTo(show, CvType.CV_8UC4);
        Bitmap bm=Bitmap.createBitmap(show.width(), show.height(), Config.ARGB_8888);
        Utils.matToBitmap(show, bm);                  //showMatInfo(faceMatScaled, "faceMatScaled");
        view.setImageBitmap(bm);  
    }
    public static Mat scaledMat(Mat src,Rect dst,int type){
   	 
        Mat scaledDestMat = new Mat();
        Size dsize = new Size(dst.width, dst.height); 
        Imgproc.resize(src, scaledDestMat,dsize);  
        
        scaledDestMat.assignTo(scaledDestMat, type);
        
        return scaledDestMat;
   }
    public static Mat rotateMat(Mat imgMat,double angle){
 	   int x=imgMat.rows()/2;
 	   int y=imgMat.cols()/2;
 	   Point center=new Point(y, x);
 	   Mat mat=Imgproc.getRotationMatrix2D(center, angle, 1.0);
 	   Imgproc.warpAffine(imgMat, imgMat, mat, imgMat.size());
 	   return imgMat;
    }
    public static void showMatInfo(Mat mat,String description){
    	Log.i(TAG, description+": size->("+mat.rows()+","+mat.cols()+")"
    			+" channels->"+mat.channels()
    			+" type->"+mat.type());
    }
    public static void showMatData(Mat mat,String description){
    	 for(int i=0;i<mat.rows();i++){
         	for(int j=0;j<mat.cols();j++){
         		double[] data=mat.get(i, j);
         		if(data.length==1){
        			Log.i(TAG, description+"->("+i+","+j+")="+data[0]);
        		}else if(data.length==4){
        			Log.i(TAG, description+"->("+i+","+j+")="+data[0]+","+data[1]+","+data[2]+","+data[3]);
        		}       		
         	}
         }
    }
    public static void showMatDataAtPos(Mat mat,String description,int i,int j){
   	       
		double[] data=mat.get(i, j);
		if(data.length==1){
			Log.i(TAG, description+"->("+i+","+j+")="+data[0]);
		}else if(data.length==4){
			Log.i(TAG, description+"->("+i+","+j+")="+data[0]+","+data[1]+","+data[2]+","+data[3]);
		}
		
   }
    public static Rect[] testDetect(Mat imgMat, boolean isShowLine){
    	
    	Rect[] ret=new Rect[4];
    	
        MatOfRect faceDetections = new MatOfRect();  
        faceDetector.detectMultiScale(imgMat, faceDetections, 1.1, 5,
				Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_DO_CANNY_PRUNING|Objdetect.CASCADE_SCALE_IMAGE, // 2
				new Size(),new Size());  
        
        Rect[] results=faceDetections.toArray();
        //face
        ret[0]=results[0];
		Mat faceMat=imgMat.submat(ret[0]);

		//mouth
		MatOfRect mouthDetections = new MatOfRect();

		Rect roi=new Rect(0,
				(int)(results[0].height/2),
				results[0].width,
				(int) (results[0].height/2));
//		Rect roi=new Rect(results[0].width / 4,
//				(int)(results[0].height/2+results[0].height/5),
//				results[0].width/2,
//				(int) (results[0].height*2 /9));
		Mat roiMat=faceMat.submat(roi);
		mouthDetector.detectMultiScale(roiMat, mouthDetections, 1.1, 2,
				Objdetect.CASCADE_SCALE_IMAGE, // 2
				new Size(),new Size());
		Rect[] mouthresults=mouthDetections.toArray();
		ret[3]=mouthresults[0];
           
        if(isShowLine){
        	Core.rectangle(imgMat,new Point(ret[0].x, ret[0].y),
        			new Point(ret[0].x + ret[0].width, ret[0].y + ret[0].height),FACE_RECT_COLOR,0);
        	
//        	Core.rectangle(imgMat.submat(ret[0]), ret[1].tl(), ret[1].br(),
//        			EYE_RECT_COLOR, 0);
//        	Core.rectangle(imgMat.submat(ret[0]), ret[2].tl(), ret[2].br(),
//        			EYE_RECT_COLOR, 0);
			Core.rectangle(roiMat, ret[3].tl(), ret[3].br(),
					MOUTH_RECT_COLOR, 0);
        }
        		
        return ret;
    }
   public static double getAngle(Mat imgMat,boolean isShowLine){
	   Rect[] ret=new Rect[6];
   	
       MatOfRect faceDetections = new MatOfRect();  
       faceDetector.detectMultiScale(imgMat, faceDetections, 1.1, 2,
				Objdetect.CASCADE_DO_CANNY_PRUNING|Objdetect.CASCADE_SCALE_IMAGE, // 2
				new Size(),new Size());  
       
       Rect[] results=faceDetections.toArray();
       //face
       ret[0]=results[0];      
       Mat faceMat=imgMat.submat(ret[0]);
     //left eye
	   Rect lroi=new Rect(0,
			   (int)(0),
			   results[0].width/2,
			   (int) (results[0].height/2));
	   Mat lroiMat=faceMat.submat(lroi);
	   MatOfRect lefteyeDetections = new MatOfRect();
       lefteyeDetector.detectMultiScale(lroiMat, lefteyeDetections, 1.1, 2,
				Objdetect.CASCADE_SCALE_IMAGE, // 2
				new Size(),new Size());
       Rect[] lefteyeresults=lefteyeDetections.toArray();
       if(lefteyeresults.length==0){
    	   Log.e("ly", "no  left eye  detected,rotate 0");
    	   return 0.0;
       }
       ret[1]=lefteyeresults[0];
     //right eye
	   Rect rroi=new Rect(results[0].width/2,
			   (int)(0),
			   results[0].width/2,
			   (int) (results[0].height/2));
	   Mat rroiMat=faceMat.submat(rroi);
       MatOfRect righteyeDetections = new MatOfRect();
       righteyeDetector.detectMultiScale(rroiMat, righteyeDetections, 1.1, 2,
				Objdetect.CASCADE_SCALE_IMAGE, // 2
				new Size(),new Size());
       Rect[] righteyeresults=righteyeDetections.toArray();
       if(righteyeresults.length==0){
    	   Log.e("ly", "no  right eye  detected,rotate 0");
    	   return 0.0;
       }
       ret[2]=righteyeresults[0];

//	   //eye
//	   MatOfRect eyepairDetections = new MatOfRect();
//	   eyepairDetector.detectMultiScale(faceMat, eyepairDetections, 1.1, 2,
//			   Objdetect.CASCADE_DO_CANNY_PRUNING|Objdetect.CASCADE_SCALE_IMAGE, // 2
//			   new Size(),new Size());
//	   Rect[] eyepairresults=eyepairDetections.toArray();
//	   ret[3]=eyepairresults[0];
//
//	   //nose
//	   MatOfRect noseDetections = new MatOfRect();
//	   noseDetector.detectMultiScale(faceMat, noseDetections, 1.1, 2,
//			   Objdetect.CASCADE_DO_CANNY_PRUNING|Objdetect.CASCADE_SCALE_IMAGE, // 2
//			   new Size(),new Size());
//	   Rect[] noseresults=noseDetections.toArray();
//	   ret[4]=noseresults[0];
//	   //mouth
//	   MatOfRect mouthDetections = new MatOfRect();
//	   mouthDetector.detectMultiScale(faceMat, mouthDetections, 1.1, 2,
//			   Objdetect.CASCADE_SCALE_IMAGE, // 2
//			   new Size(),new Size());
//	   Rect[] mouthresults=mouthDetections.toArray();
//	   ret[5]=mouthresults[0];

	   if(isShowLine){

        	Core.rectangle(lroiMat, ret[1].tl(), ret[1].br(),
        			EYE_RECT_COLOR, 0);
        	Core.rectangle(rroiMat, ret[2].tl(), ret[2].br(),
        			EYE_RECT_COLOR, 0);
//		   Core.rectangle(imgMat.submat(ret[0]), ret[3].tl(), ret[3].br(),
//				   EYE_RECT_COLOR, 0);
//		   Core.rectangle(imgMat.submat(ret[0]), ret[4].tl(), ret[4].br(),
//				   NOSE_RECT_COLOR, 0);
//		   Core.rectangle(imgMat.submat(ret[0]), ret[5].tl(), ret[5].br(),
//				   MOUTH_RECT_COLOR, 0);
	   }

	   Point c1=new Point(ret[1].x+ret[1].width/2,ret[1].y+ret[1].height/2);
	   Point c2=new Point(ret[2].x+ret[2].width/2,ret[2].y+ret[2].height/2);
       double angle=Math.atan((double)(c2.y-c1.y)/(c2.x-c1.x))*(180/Math.PI);//180/Math.PI;
       Log.i("ly","angle->"+angle);
       return angle;
   }
  
    public static Rect[] faceDetect(Mat imgMat,boolean isShowLine){
    	
    	Rect[] ret=new Rect[4];
    	
        MatOfRect faceDetections = new MatOfRect();  
        faceDetector.detectMultiScale(imgMat, faceDetections, 1.1, 5,
				Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_DO_CANNY_PRUNING|Objdetect.CASCADE_SCALE_IMAGE, // 2
				new Size(),new Size());  
        
        Rect[] results=faceDetections.toArray();
        //face
        ret[0]=results[0];

        //eye
        ret[1] = new Rect(results[0].width / 7,
				(int) (results[0].height / 4.5),
				(results[0].width - 2 * results[0].width / 7) , 
				(int) (results[0].height / 3));
        //nose
        ret[2]=new Rect(results[0].width/4,
				(int) (results[0].height / 2),
				results[0].width / 2,
				(int) (results[0].height / 3));
		//mouse
        ret[3]=new Rect(results[0].width / 4,
				(int)(results[0].height/2+results[0].height/5),
				results[0].width/2, 
				(int) (results[0].height*2 /9));
        if(isShowLine){
        	Core.rectangle(imgMat,new Point(ret[0].x, ret[0].y),
        			new Point(ret[0].x + ret[0].width, ret[0].y + ret[0].height),FACE_RECT_COLOR,0);
        	
        	Core.rectangle(imgMat.submat(ret[0]), ret[1].tl(), ret[1].br(),
        			EYE_RECT_COLOR, 0);
        	Core.rectangle(imgMat.submat(ret[0]), ret[2].tl(), ret[2].br(),
        			NOSE_RECT_COLOR, 0);
        	Core.rectangle(imgMat.submat(ret[0]), ret[3].tl(), ret[3].br(),
					MOUTH_RECT_COLOR, 0);
        }
        		
        return ret;
    }
    public static Rect[] faceDetectFine(Mat imgMat,boolean isShowLine){
    	
    	Rect[] ret=new Rect[4];
    	
        MatOfRect faceDetections = new MatOfRect();  
        faceDetector.detectMultiScale(imgMat, faceDetections, 1.1, 5,
				Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_DO_CANNY_PRUNING|Objdetect.CASCADE_SCALE_IMAGE, // 2
				new Size(),new Size());  
        
        Rect[] results=faceDetections.toArray();
        //face
        ret[0]=results[0];
        Mat faceMat=imgMat.submat(ret[0]);
        //eye       
        MatOfRect eyepairDetections = new MatOfRect();  
        eyepairDetector.detectMultiScale(faceMat, eyepairDetections, 1.1, 5,
				Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_DO_CANNY_PRUNING|Objdetect.CASCADE_SCALE_IMAGE, // 2
				new Size(),new Size());  
        Rect[] eyepairresults=eyepairDetections.toArray();
 
        if(eyepairresults.length!=0){
        	ret[1]=eyepairresults[0];
        }else{
        	ret[1]=new Rect(results[0].width / 7,
    				(int) (results[0].height / 4.5),
    				(results[0].width - 2 * results[0].width / 7) , 
    				(int) (results[0].height / 3));
        	Log.e("ly", "no  eye pair detected,using default");
        }
       
        //nose
        MatOfRect noseDetections = new MatOfRect();  
        noseDetector.detectMultiScale(faceMat, noseDetections, 1.1, 5,
				Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_DO_CANNY_PRUNING|Objdetect.CASCADE_SCALE_IMAGE, // 2
				new Size(),new Size());  
        Rect[] noseresults=noseDetections.toArray();
        if(noseresults.length!=0){
        	ret[2]=noseresults[0];
        }else{
        	ret[2]=new Rect(results[0].width/4,
    				(int) (results[0].height / 2),
    				results[0].width / 2,
    				(int) (results[0].height / 3));
        	Log.e("ly", "no  nose detected,using default");
        }
       
		//mouse
        MatOfRect mouthDetections = new MatOfRect();  
        mouthDetector.detectMultiScale(faceMat, mouthDetections, 1.1, 5,
				Objdetect.CASCADE_FIND_BIGGEST_OBJECT|Objdetect.CASCADE_DO_CANNY_PRUNING|Objdetect.CASCADE_SCALE_IMAGE, // 2
				new Size(),new Size());  
        Rect[] mouthresults=mouthDetections.toArray();
        if(mouthresults.length!=0){
        	ret[3]=mouthresults[0];
        }else{
        	ret[3]=new Rect(results[0].width/4,
    				(int) (results[0].height / 2),
    				results[0].width / 2,
    				(int) (results[0].height / 3));
        	Log.e("ly", "no  mouth detected,using default");
        }
        
        if(isShowLine){
        	Core.rectangle(imgMat,new Point(ret[0].x, ret[0].y),
        			new Point(ret[0].x + ret[0].width, ret[0].y + ret[0].height),FACE_RECT_COLOR,0);
        	
        	Core.rectangle(imgMat.submat(ret[0]), ret[1].tl(), ret[1].br(),
        			EYE_RECT_COLOR, 0);
        	Core.rectangle(imgMat.submat(ret[0]), ret[2].tl(), ret[2].br(),
        			NOSE_RECT_COLOR, 0);
        	Core.rectangle(imgMat.submat(ret[0]), ret[3].tl(), ret[3].br(),
					MOUTH_RECT_COLOR, 0);
        }
        		
        return ret;
    }

    public static void generateDetectors(){

		faceDetector=generateClassifier(R.raw.haarcascade_frontalface_alt,"haarcascade_frontalface_alt.xml");
		lefteyeDetector=generateClassifier(R.raw.haarcascade_lefteye_2splits,"haarcascade_lefteye_2splits.xml");
		righteyeDetector=generateClassifier(R.raw.haarcascade_righteye_2splits,"haarcascade_righteye_2splits.xml");
		eyepairDetector=generateClassifier(R.raw.haarcascade_mcs_eyepair_small,"haarcascade_mcs_eyepair.xml");
		noseDetector=generateClassifier(R.raw.haarcascade_mcs_nose,"haarcascade_mcs_nose.xml");
		mouthDetector=generateClassifier(R.raw.haarcascade_mcs_mouth,"haarcascade_mcs_mouth.xml");
	}
    private static CascadeClassifier generateClassifier(int resId,String fileName){
		CascadeClassifier mDetector = null;
		try{
			InputStream is = mContext.getResources().openRawResource(resId);					
			File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
			File CascadeFile = new File(cascadeDir,fileName);					
			FileOutputStream os = new FileOutputStream(CascadeFile);
	
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			is.close();
			os.close();
			
			mDetector = new CascadeClassifier(CascadeFile.getAbsolutePath());					
			if (mDetector.empty()) {
				Log.e(TAG, "Failed to load cascade classifier");
				mDetector = null;
			} else{
				Log.i(TAG, "Loaded cascade classifier from "+ CascadeFile.getAbsolutePath());
						
			}
			
			CascadeFile.delete();
			cascadeDir.delete();
		}catch(Exception e){
			e.printStackTrace();
		}
		return mDetector;
	}
}
