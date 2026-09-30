package com.example.photoeditpro;

import android.app.Activity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.view.View;
import android.widget.*;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private ImageView imageView; private Bitmap original, edited; private int mode=0;
    private final int OPEN=10;
    @Override public void onCreate(Bundle b){ super.onCreate(b); setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.imageView); SeekBar seek=findViewById(R.id.adjustSeek); TextView value=findViewById(R.id.valueText);
        findViewById(R.id.openBtn).setOnClickListener(v->open());
        findViewById(R.id.brightnessBtn).setOnClickListener(v->{mode=1;seek.setProgress(100);value.setText("Brightness 0");});
        findViewById(R.id.contrastBtn).setOnClickListener(v->{mode=2;seek.setProgress(100);value.setText("Contrast 100");});
        findViewById(R.id.saturationBtn).setOnClickListener(v->{mode=3;seek.setProgress(100);value.setText("Saturation 100");});
        findViewById(R.id.aiStyleBtn).setOnClickListener(v->showAIStyles());
        findViewById(R.id.grayBtn).setOnClickListener(v->{if(original!=null){ edited=filter(original,0,100,0); imageView.setImageBitmap(edited); }});
        findViewById(R.id.rotateBtn).setOnClickListener(v->{if(edited!=null){ Matrix m=new Matrix();m.postRotate(90);edited=Bitmap.createBitmap(edited,0,0,edited.getWidth(),edited.getHeight(),m,true);imageView.setImageBitmap(edited);}});
        findViewById(R.id.upscaleBtn).setOnClickListener(v->{if(edited!=null){int w=Math.min(edited.getWidth()*2,8192),h=Math.min(edited.getHeight()*2,8192);edited=Bitmap.createScaledBitmap(edited,w,h,true);imageView.setImageBitmap(edited);Toast.makeText(this,"Upscaled to "+w+" × "+h,Toast.LENGTH_SHORT).show();}});
        findViewById(R.id.saveBtn).setOnClickListener(v->save());
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean f){if(original==null)return; if(mode==1)value.setText("Brightness "+(p-100)); else if(mode==2)value.setText("Contrast "+p); else if(mode==3)value.setText("Saturation "+p); else return; edited=filter(original,mode,p,0);imageView.setImageBitmap(edited);}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});
    }
    void showAIStyles(){
        if(original==null){Toast.makeText(this,"Open a photo first",Toast.LENGTH_SHORT).show();return;}
        final String[] styles={"Cinematic","Warm Sunset","Cool Portrait","Vintage","HDR Pop","Soft Film"};
        new AlertDialog.Builder(this).setTitle("AI Style").setItems(styles,(d,which)->applyAIStyle(which)).setNegativeButton("Cancel",null).show();
    }
    void applyAIStyle(int style){
        Bitmap src=original.copy(Bitmap.Config.ARGB_8888,true); Bitmap out=src.copy(Bitmap.Config.ARGB_8888,true);
        ColorMatrix cm=new ColorMatrix();
        float sat=1.0f, contrast=1.0f, bright=0f;
        switch(style){
            case 0: sat=1.18f; contrast=1.12f; bright=4f; break;
            case 1: sat=1.25f; contrast=1.05f; bright=7f; break;
            case 2: sat=1.08f; contrast=1.10f; bright=2f; break;
            case 3: sat=0.86f; contrast=1.03f; bright=3f; break;
            case 4: sat=1.35f; contrast=1.25f; bright=4f; break;
            case 5: sat=0.95f; contrast=1.06f; bright=5f; break;
        }
        cm.setSaturation(sat);
        float scale=contrast, t=128*(1-scale)+bright;
        cm.postConcat(new ColorMatrix(new float[]{scale,0,0,0,t,0,scale,0,0,t,0,0,scale,0,t,0,0,0,1,0}));
        Canvas c=new Canvas(out); Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); p.setColorFilter(new ColorMatrixColorFilter(cm)); c.drawBitmap(src,0,0,p);
        edited=out; imageView.setImageBitmap(edited); Toast.makeText(this,styles[style]+" style applied",Toast.LENGTH_SHORT).show();
    }
    void open(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,OPEN);}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==OPEN&&c==RESULT_OK&&d!=null){try{Uri u=d.getData();original=MediaStore.Images.Media.getBitmap(getContentResolver(),u);edited=original.copy(Bitmap.Config.ARGB_8888,true);imageView.setImageBitmap(edited);}catch(Exception e){Toast.makeText(this,"Could not open image",Toast.LENGTH_SHORT).show();}}}
    Bitmap filter(Bitmap src,int type,int val,int dummy){Bitmap out=src.copy(Bitmap.Config.ARGB_8888,true);float bright=type==1?val-100:0, contrast=type==2?val/100f:1, sat=type==3?val/100f:1;ColorMatrix cm=new ColorMatrix();cm.setSaturation(sat);float scale=contrast, t=128*(1-scale)+bright;cm.postConcat(new ColorMatrix(new float[]{scale,0,0,0,t,0,scale,0,0,t,0,0,scale,0,t,0,0,0,1,0}));Canvas c=new Canvas(out);Paint p=new Paint();p.setColorFilter(new ColorMatrixColorFilter(cm));c.drawBitmap(src,0,0,p);return out;}
    void save(){if(edited==null){Toast.makeText(this,"Open a photo first",Toast.LENGTH_SHORT).show();return;}try{String name="PhotoEdit_"+System.currentTimeMillis()+".jpg";android.content.ContentValues cv=new android.content.ContentValues();cv.put(MediaStore.Images.Media.DISPLAY_NAME,name);cv.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");cv.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/PhotoEditPro");Uri u=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);OutputStream os=getContentResolver().openOutputStream(u);edited.compress(Bitmap.CompressFormat.JPEG,95,os);os.close();Toast.makeText(this,"Saved to Pictures/PhotoEditPro",Toast.LENGTH_LONG).show();}catch(Exception e){Toast.makeText(this,"Save failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
}
