package com.demo.claim;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import com.parascript.mobile.onboarding.ParascriptException;
import com.parascript.mobile.onboarding.ParascriptFileOperations;
import com.parascript.mobile.onboarding.ParascriptImageProcessing;
import com.parascript.mobile.onboarding.ParascriptVertex;
import com.wtw.testnewsample.wsclient.WSClientNew;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.Marshal;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.MarshalDate;
import org.ksoap2.serialization.SoapPrimitive;
import org.opencv.core.Mat;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    @BindView(R.id.btnSubmit)
    Button btnSubmitForm;
    @BindView(R.id.btnAddArecepit)
    Button btnAddArecepit;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    @BindView(R.id.ivClaimImage)
    ImageView ivImage;
    private String userChoosenTask;
    @BindView(R.id.extCatValue)
    EditText edtcategory;
    //    @BindView(R.id.edtClaimtypeValue)
//    EditText edtCalimType;
    @BindView(R.id.edtAmountValue)
    EditText edtAmountValue;
    @BindView(R.id.edtDateValue)
    EditText edtDateValue;
    @BindView(R.id.edtProviderValue)
    EditText edtProviderValue;
    @BindView(R.id.tvClaimresponse)
    TextView claimResponse;

    private byte[] bytes;
    private static final String NAMESPACE = "http://wsclient.testnewsample.wtw.com/";
    private static final String METHODNAME = "imageRecogProcess";
    private static final String WSDL = "http://192.168.102.77:84/ProcessClaims/ProcessImageInterface";
    //private static final String SOAP_ACTION = NAMESPACE + "ProcessImageInterface" + METHODNAME ;
    private int TimeOut=120000;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    Mat imageMat=new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initDebug();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        verifyStoragePermissions();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        btnSubmitForm.setOnClickListener(this);
        btnAddArecepit.setOnClickListener(this);
        StrictMode.ThreadPolicy threadPolicy =  new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);

    }

    @Override
    public void onClick(View view)
    {

        switch (view.getId())
        {
            case R.id.btnSubmit:

                String inputDate=edtDateValue.getText().toString();
                Date date=null;
                try
                {
                    date=getDate(inputDate);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                // wsClientNew.imageRecogProcess(bytes,edtAmountValue.getText().toString(),edtProviderValue.getText().toString(),date,"Medical");
                Toast.makeText(this,"Data Sent!",Toast.LENGTH_SHORT).show();


                String endPoint = "http://10.196.31.203:8080/ProcessClaims/ProcessImageInterface";

                String soapAction = "\"\"";

                SoapObject rpc= new SoapObject(NAMESPACE, METHODNAME);

                rpc.addProperty("Image",bytes);
                rpc.addProperty("Amount", edtAmountValue.getText().toString());
                rpc.addProperty("Provider", edtProviderValue.getText().toString());
                PropertyInfo dataparam = new PropertyInfo();
                dataparam.setValue(date);
                dataparam.type = MarshalDate.DATE_CLASS;
                dataparam.setName("Date");
                rpc.addProperty(dataparam);
                //rpc.addProperty("Type", "Medical");
                //SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                        SoapEnvelope.VER11);

                new MarshalBase64().register(envelope); // serialization
                MarshalDate marshalDate =  new MarshalDate();
                marshalDate.register(envelope);
                envelope.bodyOut = rpc;
                //envelope.dotNet = true;

                envelope.setOutputSoapObject(rpc);

                HttpTransportSE transport = new HttpTransportSE(endPoint,TimeOut);

                // HttpTransportSE transport = new HttpTransportSE(WSDL);
                transport.debug = true;
                //HttpTransportSE httpTransport = new HttpTransportSE(URL);
                //SoapObject response= null;
                SoapPrimitive response =null;
                String responseDump = "";
                String requestDump = "";
                String str="";

                try
                {
                    System.setProperty("http.keepAlive", "false");
                    transport.call(soapAction, envelope);
                    // httpTransport.call(soapAction, envelope);
                    requestDump = transport.requestDump;
                    Log.v("Log Request::",requestDump);
                    responseDump = transport.responseDump;
                    Log.v("Log Request::",responseDump);
                    //response = (SoapObject) envelope.getResponse();
                    response = (SoapPrimitive) envelope.getResponse();
                    //int result =  Integer.parseInt(response.getProperty(0).toString());
                    str=response.toString();
                    System.out.println("Response from service"+str);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                Toast.makeText(this,"Result::"+response,Toast.LENGTH_LONG).show();
                if("Image Distorted Please rescan".equals(str))
                {
                    claimResponse.setText(str);
                    claimResponse.setTextColor(Color.parseColor("#ff0000"));
                }
                else
                {
                    claimResponse.setText(str);
                    claimResponse.setTextColor(Color.parseColor("#bdbdbd"));
                }
                //claimResponse.setText(str);

                break;

            case R.id.btnAddArecepit:

                selectImage();

                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                }
                else
                {
                    //code for deny
                }
                break;
        }
    }

    private void selectImage()
    {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int item)
            {
                boolean result=Utility.checkPermission(MainActivity.this);

                if (items[item].equals("Take Photo"))
                {
                    userChoosenTask ="Take Photo";
                    if(result)
                        cameraIntent();

                }
                else if (items[item].equals("Choose from Library"))
                {
                    userChoosenTask ="Choose from Library";
                    if(result)
                        galleryIntent();

                }
                else if (items[item].equals("Cancel"))
                {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OpenCVLoader.initDebug();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        verifyStoragePermissions();
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        thumbnail.setHeight(90);
        thumbnail.setWidth(90);
        File destination = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ivImage.setImageBitmap(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                Bitmap processedBitmap = preprocessImage(bm);
                bytes=convertBitmapToByteArrayUncompressed(processedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ivImage.setImageBitmap(bm);
    }

    public Bitmap preprocessImage(Bitmap rawBitmap)
    {
        Bitmap bitmap = null;
        try {
            //detect the vertices, remove distortion, increasse brightness , convert into binary image after removing noises
            ArrayList<ParascriptVertex> vertices = ParascriptImageProcessing.detectVertices(rawBitmap, 1, 1);
            Bitmap undistortedBitmap = ParascriptImageProcessing.removeDistortionAndCrop(rawBitmap, vertices, 1, 1);
            Bitmap brightednedBitmap = ParascriptImageProcessing.increaseBrightness(undistortedBitmap);
            Bitmap binarizedBitmap = ParascriptImageProcessing.binarizeAdaptive(brightednedBitmap);

            bitmap = binarizedBitmap;
        }
        catch (Exception e)
        {   //If there is some exception from Parascript, pre pre-processing, return the un processed image.
            bitmap = rawBitmap;
        }
        return bitmap;
    }

    public String getURLForResource (int resourceId)
    {
        return Uri.parse("android.resource://"+R.class.getPackage().getName()+"/" +resourceId).toString();
    }

    public Date getDate(String date) throws Exception
    {
        SimpleDateFormat curFormater = new SimpleDateFormat("MM/dd/yyyy");
        Date dateObj = curFormater.parse(date);

        return dateObj;
    }

    public  byte[] convertBitmapToByteArrayUncompressed(Bitmap bitmap)
    {
        java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        byteBuffer.rewind();
        return byteBuffer.array();
    }
    public String getSoapAction(String method) {
        return "\"" + NAMESPACE + method + "\"";
    }
}