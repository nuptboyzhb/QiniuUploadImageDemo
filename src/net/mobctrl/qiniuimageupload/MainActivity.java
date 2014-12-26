package net.mobctrl.qiniuimageupload;

import net.mobctrl.qiniuimageupload.QiniuUploadUitls.QiniuUploadUitlsListener;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * 
 * @author Zheng Haibo
 * @web http://www.mobctrl.net
 * @blog http://blog.csdn.net/nuptboyzhb
 */
public class MainActivity extends Activity {

	private static final int CHOOSE_IMAGE_CODE = 1;

	ProgressBar pbProgress = null;
	Button btnChoose = null;
	ImageView ivShowimage = null;
	ProgressBar pbLoading = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		init();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void init() {
		pbProgress = (ProgressBar) findViewById(R.id.pb_progress);
		btnChoose = (Button) findViewById(R.id.btn_choose_upload);
		ivShowimage = (ImageView) findViewById(R.id.iv_showimage);
		pbLoading = (ProgressBar)findViewById(R.id.pb_loading);
		pbLoading.setVisibility(View.GONE);

		btnChoose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(intent, CHOOSE_IMAGE_CODE);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == CHOOSE_IMAGE_CODE) {
			Uri uri = data.getData();
			ContentResolver cr = this.getContentResolver();
			try {
				Bitmap bitmap = BitmapFactory.decodeStream(cr
						.openInputStream(uri));
				uploadBitmap(bitmap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 上传从相册选择的图片
	 * @param result
	 */
	private void uploadBitmap(Bitmap result) {
		QiniuUploadUitls.getInstance().uploadImage(result,new QiniuUploadUitlsListener() {
			
			@Override
			public void onSucess(String fileUrl) {
				// TODO Auto-generated method stub
				showUrlImage(fileUrl);
			}
			
			@Override
			public void onProgress(int progress) {
				// TODO Auto-generated method stub
				pbProgress.setProgress(progress);
			}
			
			@Override
			public void onError(int errorCode, String msg) {
				// TODO Auto-generated method stub
				showToast("errorCode="+errorCode+",msg="+msg);
			}
		});
	}
	
	protected void showUrlImage(String fileUrl) {
		pbLoading.setVisibility(View.VISIBLE);
		Picasso.with(this)  
        .load(fileUrl)
        .error(R.drawable.ic_launcher)  
        .into(ivShowimage,new Callback() {
			
			@Override
			public void onSuccess() {
				pbLoading.setVisibility(View.GONE);
			}
			
			@Override
			public void onError() {
				pbLoading.setVisibility(View.GONE);
			}
		});  
	}

	private void showToast(String content){
		Toast.makeText(getApplicationContext(), content, Toast.LENGTH_LONG).show();
	}

}
