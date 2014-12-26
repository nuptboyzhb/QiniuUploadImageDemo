package net.mobctrl.qiniuimageupload;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.rs.PutPolicy;

/**
 * @date 2014年12月26日 上午11:00:43
 * @author Zheng Haibo
 * @web http://www.mobctrl.net
 * @Description: 七牛云上传图片
 */
public class QiniuUploadUitls {

	/** 在网站上查看 */
	private static final String ACCESS_KEY = "<Your ACCESS_KEY in the web>";
	/** 在网站上查看 */
	private static final String SECRET_KEY = "<Your SECRET_KEY in the web>";
    /** 你所创建的空间的名称*/
	private static final String bucketName = "<Your bucket Name in the web>";

	private static final String fileName = "temp.jpg";

	private static final String tempJpeg = Environment
			.getExternalStorageDirectory().getPath() + "/" + fileName;
	
	private int maxWidth = 720;
	private int maxHeight = 1080;
	
	public interface QiniuUploadUitlsListener{
		public void onSucess(String fileUrl);
		public void onError(int errorCode,String msg);
		public void onProgress(int progress);
	}
	
	private QiniuUploadUitls() {

	}

	private static QiniuUploadUitls qiniuUploadUitls = null;

	private UploadManager uploadManager = new UploadManager();

	public static QiniuUploadUitls getInstance() {
		if (qiniuUploadUitls == null) {
			qiniuUploadUitls = new QiniuUploadUitls();
		}
		return qiniuUploadUitls;
	}

	public boolean saveBitmapToJpegFile(Bitmap bitmap, String filePath) {
		return saveBitmapToJpegFile(bitmap, filePath, 75);
	}

	public boolean saveBitmapToJpegFile(Bitmap bitmap, String filePath,
			int quality) {
		try {
			FileOutputStream fileOutStr = new FileOutputStream(filePath);
			BufferedOutputStream bufOutStr = new BufferedOutputStream(
					fileOutStr);
			resizeBitmap(bitmap).compress(CompressFormat.JPEG, quality, bufOutStr);
			bufOutStr.flush();
			bufOutStr.close();
		} catch (Exception exception) {
			return false;
		}
		return true;
	}

	/**
	 * 缩小图片
	 * @param bitmap
	 * @return
	 */
	public Bitmap resizeBitmap(Bitmap bitmap) {
		if(bitmap != null){
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			if(width>maxWidth){
				int pWidth = maxWidth;
				int pHeight = maxWidth*height/width;
				Bitmap result = Bitmap.createScaledBitmap(bitmap, pWidth, pHeight, false);
				bitmap.recycle();
				return result;
			}
			if(height>maxHeight){
				int pHeight = maxHeight;
				int pWidth = maxHeight*width/height;
				Bitmap result = Bitmap.createScaledBitmap(bitmap, pWidth, pHeight, false);
				bitmap.recycle();
				return result;
			}
		}
		return bitmap;
	}

	public void uploadImage(Bitmap bitmap,QiniuUploadUitlsListener listener) {
		saveBitmapToJpegFile(bitmap, tempJpeg);
		uploadImage(tempJpeg,listener);
	}

	public void uploadImage(String filePath,final QiniuUploadUitlsListener listener) {
		final String fileUrlUUID = getFileUrlUUID();
		String token = getToken();
		if (token == null) {
			if(listener != null){
				listener.onError(-1, "token is null");
			}
			return;
		}
		uploadManager.put(filePath, fileUrlUUID, token,
				new UpCompletionHandler() {
					@Override
					public void complete(String key, ResponseInfo info,
							JSONObject response) {
						System.out.println("debug:info = " + info
								+ ",response = " + response);
						if (info != null && info.statusCode == 200) {// 上传成功
							String fileRealUrl = getRealUrl(fileUrlUUID);
							System.out.println("debug:fileRealUrl = "+fileRealUrl);
							if(listener != null){
								listener.onSucess(fileRealUrl);
							}
						} else {
							if(listener != null){
								listener.onError(info.statusCode, info.error);
							}
						}
					}
				}, new UploadOptions(null, null, false,
						new UpProgressHandler() {
							public void progress(String key, double percent) {
								if(listener != null){
									listener.onProgress((int)(percent*100));
								}
							}
						}, null));

	}
	
	/**
	 * 生成远程文件路径（全局唯一）
	 * 
	 * @return
	 */
	private String getFileUrlUUID() {
		String filePath = android.os.Build.MODEL + "__"
				+ System.currentTimeMillis() + "__"
				+ (new Random().nextInt(500000)) + "_"
				+ (new Random().nextInt(10000));
		return filePath.replace(".", "0");
	}
	
	private String getRealUrl(String fileUrlUUID){
		String filePath = "http://"+bucketName+".qiniudn.com/"+fileUrlUUID;
		return filePath;
	}

	/**
	 * 获取token 本地生成
	 * 
	 * @return
	 */
	private String getToken() {
		Mac mac = new Mac(ACCESS_KEY, SECRET_KEY);
		PutPolicy putPolicy = new PutPolicy(bucketName);
		putPolicy.returnBody = "{\"name\": $(fname),\"size\": \"$(fsize)\",\"w\": \"$(imageInfo.width)\",\"h\": \"$(imageInfo.height)\",\"key\":$(etag)}";
		try {
			String uptoken = putPolicy.token(mac);
			System.out.println("debug:uptoken = " + uptoken);
			return uptoken;
		} catch (AuthException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

}
