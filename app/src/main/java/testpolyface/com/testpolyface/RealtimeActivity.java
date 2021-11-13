package testpolyface.com.testpolyface;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Frame;
import com.otaliastudios.cameraview.FrameProcessor;
import com.otaliastudios.cameraview.Size;

import java.util.List;

import testpolyface.com.testpolyface.graphics.DotDotOverlayYellow;
import testpolyface.com.testpolyface.graphics.DotDotOverlayRed;
import testpolyface.com.testpolyface.graphics.GraphicOverlay;

public class RealtimeActivity extends AppCompatActivity {

    private CameraView cameraView;
    private GraphicOverlay graphicOverlay;


    private FirebaseVisionFaceDetectorOptions options;
    private FirebaseVisionFaceDetector detector;

    private Computation computation;
    private ImageButton referenceButton;

    private int frameCounter;

    private FirebaseVisionFaceContour contourRef;
    private DotDotOverlayRed dotRef;
    private TextView emotionTextView;
    private TextView counterView;



    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime);


        //EmotionTextView
        emotionTextView = (TextView) findViewById(R.id.activity_realtime_emotionTextView);
        counterView = (TextView) findViewById(R.id.activity_realtime_counter);

        // Get the camera and the graphicOverlay
        cameraView = findViewById(R.id.activity_realtime_camera_view);
        graphicOverlay = findViewById(R.id.activity_realtime_graphic_overlay);


        // Setting facing to front camera
        cameraView.setFacing(Facing.FRONT);

        // Firebase : detection options
        options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                .build();

        // Firebase : creating face detector
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);


        //Computation object to analyze captured face landmark points
        computation = new Computation(RealtimeActivity.this);
        //Initialization of frame counter
        frameCounter = 0;
        //Get reference button
        referenceButton = (ImageButton) findViewById(R.id.activity_realtime_reference);

        counterView.setText(String.valueOf(frameCounter));



        // Frame processing
        cameraView.addFrameProcessor(new FrameProcessor() {
            @Override
            @WorkerThread
            public void process(final Frame frame) {
                //Clear the previous graphic overlay (previous drawn dots)
                graphicOverlay.clear();
                //Increment frame counter and update view
                frameCounter++;
                counterView.setText(String.valueOf(frameCounter));

                //Get image frame from camera API
                byte[] data = frame.getData();
                //Get image frame rotation
                int rotation = frame.getRotation();
                //Get image frame size
                Size size = frame.getSize();
                //Image frame rotation for Firebase
                int frameRotation = rotation / 90;


                //Firebase : setting up
                FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setWidth(size.getWidth())
                        .setRotation(frameRotation)
                        .setHeight(size.getHeight())
                        .build();

                //Firebase : capturing faces from Firebase API
                FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromByteArray(data, metadata);

                //LOG for frame image size
                int height = firebaseVisionImage.getBitmapForDebugging().getHeight() ;
                int width = firebaseVisionImage.getBitmapForDebugging().getWidth();
                Log.i("IMAGE_SIZE", "Height " + height + " Width " + width);



                //Firebase extracting face landmarks points
                detector.detectInImage(firebaseVisionImage)
                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>()
                        {
                            @Override
                            public void onSuccess(final List<FirebaseVisionFace> firebaseVisionFaces)
                            {
                                //If atleast one face is detected
                                if(firebaseVisionFaces.size()!=0)
                                {
                                    //Reference button listener on click
                                    referenceButton.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            //If atleast one face is detected
                                            if(firebaseVisionFaces.size()!=0)
                                            {
                                                //Setting current landmarks points as reference
                                                computation.setReference(new FacialPoints(firebaseVisionFaces.get(0)));
                                                //Pop up referenced
                                                Toast.makeText(RealtimeActivity.this, "FaceContour referenced", Toast.LENGTH_SHORT).show();
                                                //Displaying reference contour
                                                contourRef = firebaseVisionFaces.get(0).getContour(FirebaseVisionFaceContour.ALL_POINTS);
                                                dotRef = new DotDotOverlayRed(graphicOverlay, contourRef.getPoints());
                                            }
                                        }
                                    });

                                    //Every 15 frame => emotion computation
                                    if(frameCounter>15 && computation.getReference()!=null && firebaseVisionFaces.size()!=0)
                                    {
                                        //Clearing emotion text view
                                        emotionTextView.clearComposingText();
                                        //Reset frame counter
                                        frameCounter = 0;
                                        //Setting up actual landmarks points
                                        computation.setActual(new FacialPoints(firebaseVisionFaces.get(0)));
                                        //Compute
                                        String results = computation.compute();
                                        //Displaying results
                                        emotionTextView.setText(results);
                                    }

                                    //Display detected face landmarks points
                                    FirebaseVisionFaceContour contour = firebaseVisionFaces.get(0).getContour(FirebaseVisionFaceContour.ALL_POINTS);
                                    DotDotOverlayYellow dot = new DotDotOverlayYellow(graphicOverlay, contour.getPoints());
                                    if(dotRef!=null)
                                    {
                                        graphicOverlay.add(dotRef);
                                    }
                                    graphicOverlay.add(dot);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(RealtimeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
