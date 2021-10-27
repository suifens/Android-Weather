package com.goodtech.tq.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.blankj.utilcode.util.ImageUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageTools {

	/**
	 * bitmap的图片格式转为base64的图片格式
	 * 
	 * @param bitmap
	 */
	public static String bitmapToBase64(Bitmap bitmap) {
		String result = null;
		ByteArrayOutputStream baos = null;
		try {
			if (bitmap != null) {
				baos = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.JPEG, 90, baos);
				baos.flush();
				baos.close();
				byte[] bitmapBytes = baos.toByteArray();
				result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * base64格式的图片转为Bitmap的格式
	 *
	 * @param base64Data
	 */
	public static Bitmap base64ToBitmap(String base64Data) {// base64转为Bitmap
		byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return bitmap;
	}

	public static byte[] base64ToByte(String base64Data) {
		byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
		return bytes;
	}

	/**
	 * Url格式的图片转成base64格式的图片
	 *
	 * @param ImageUrl
	 */
	public static String getBase64Image(String ImageUrl) {
		String result = null;
		ByteArrayOutputStream baos = null;
		try {
			URL url = new URL(ImageUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(3000);
			conn.setRequestMethod("GET");
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStream is = conn.getInputStream();
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				if (bitmap != null) {
					baos = new ByteArrayOutputStream();
					bitmap.compress(CompressFormat.JPEG, 30, baos);
					baos.flush();
					baos.close();
					byte[] bitmapBytes = baos.toByteArray();
					result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
				}
			} else {
				return null;
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (baos != null) {
				try {
					baos.flush();
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * url图片转化为Bitmap图片
	 */
	public static Bitmap getNetBitmap(String url) {

		BufferedInputStream inputStream = null;
		Bitmap bitmap = null;
		try {
			inputStream = new BufferedInputStream(new URL(url).openStream(),
					1024);
			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			BufferedOutputStream outputStream = new BufferedOutputStream(
					dataStream, 1024);
			copy(inputStream, outputStream);
			outputStream.flush();
			byte[] data = dataStream.toByteArray();
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			data = null;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			return null;
		}
		return bitmap;
	}

	/**
	 * copy 函数
	 *
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] b = new byte[1024];
		int read;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}

	/**
	 * 对直角图片进行圆角处理
	 *
	 * @param bitmap
	 * @param pixels
	 */
	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
		if (bitmap == null) {
			return null;
		}
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);

		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;

		final Paint paint = new Paint();

		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		final RectF rectF = new RectF(rect);

		final float roundPx = pixels;

		paint.setAntiAlias(true);

		canvas.drawARGB(0, 0, 0, 0);

		paint.setColor(color);

		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;

	}

	/**
	 * 从传入的bitmap中剪出一个圆形，这个圆形以原图的中心为圆心，较短边的边长1/2为半径。
	 *
	 * @param bitmap
	 * @return 剪出的圆形bitmap
	 */
	public static Bitmap cutRoundBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		int radius = width < height ? width / 2 : height / 2;
		canvas.drawCircle(width / 2, height / 2, radius, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		Rect rect = new Rect(0, 0, width, height);
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	/**
	 * 缩放图片到指定大小，并且以窄边1/2为半径，图片中心为中心
	 *
	 * @param context
	 * @param bitmap
	 *            原图的bitmap
	 * @param scale
	 *            屏幕宽的几分之几，如=3，则返回的 bitmap 的宽是屏幕的1/3
	 * @return
	 */
	public static Bitmap resizeBitmap(Context context, Bitmap bitmap, int scale) {
		if (bitmap == null) {
			return null;
		}
		float preferedDim = getScaledScreenWidth(context,
				ImageTools.SCREEN_WIDTH, scale);

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		float s = width < height ? preferedDim / width : preferedDim / height;
		Matrix matrix = new Matrix();
		matrix.postScale(s, s);

		Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		Bitmap circleBitmap = cutRoundBitmap(scaledBitmap);

		bitmap.recycle();
		scaledBitmap.recycle();

		return circleBitmap;
	}

	/**
	 * Try to return the absolute file path from the given Uri
	 *
	 * @param context
	 * @param uri
	 * @return the file path or null
	 */
	public static String getRealFilePath(final Context context, final Uri uri) {
		if (null == uri)
			return null;
		final String scheme = uri.getScheme();
		String data = null;
		if (scheme == null)
			data = uri.getPath();
		else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			Cursor cursor = context.getContentResolver().query(uri,
					new String[] { ImageColumns.DATA }, null, null, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(ImageColumns.DATA);
					if (index > -1) {
						data = cursor.getString(index);
					}
				}
				cursor.close();
			}
		}
		return data;
	}

	/**
	 * 获取指定Activity的截屏，保存到png文件
	 *
	 * @param activity
	 * @return
	 */
	public static Bitmap takeScreenShot(Activity activity) {
		// View是你需要截图的View
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();

		// 获取状态栏高度
		Rect frame = new Rect();
		view.getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;

		// 获取屏幕宽
		int width = activity.getWindowManager().getDefaultDisplay().getWidth();
		// 去掉标题栏
		Bitmap bm = Bitmap.createBitmap(b1, 0, statusBarHeight, width,
				b1.getHeight() - statusBarHeight);

		return bm;
	}

	public static Bitmap fastblur(Context context, Bitmap bitmap) {
		return fastblur(context, bitmap, 5);
	}

	/**
	 * 对图片做模糊处理
	 *
	 * @param context
	 * @param bitmap
	 * @param radius
	 * @return
	 */
	public static Bitmap fastblur(Context context, Bitmap bitmap, int radius) {

		Bitmap bm = bitmap.copy(bitmap.getConfig(), true);

		if (radius < 1) {
			return (null);
		}

		int w = bm.getWidth();
		int h = bm.getHeight();

		int[] pix = new int[w * h];
		Log.e("pix", w + " " + h + " " + pix.length);
		bm.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
						| (dv[gsum] << 8) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		Log.e("pix", w + " " + h + " " + pix.length);
		bm.setPixels(pix, 0, w, 0, 0, w, h);
		return bm;
	}

	public static final int SCREEN_WIDTH = 1;
	public static final int SCREEN_HEIGHT = 2;

	/**
	 * @param context
	 *            假设 2，那返回的尺寸为屏幕宽的1/2
	 * @return
	 */
	public static int getScaledScreenWidth(Context context, int widthOrHeight, int scale) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(metrics);
		int size = 0;
		switch (widthOrHeight) {
		case SCREEN_WIDTH:
			size = metrics.widthPixels / scale;
		case SCREEN_HEIGHT:
			size = metrics.heightPixels / scale;
		}
		return size;
	}


	/**
	 * 获取屏幕的密度
	 *
	 * @param context
	 * @return density
	 */
	public static float getScreenDensity(Context context) {
		DisplayMetrics outMetrics = getScreenMetrics(context);
		return outMetrics.density;
	}

	/**
	 * @return 屏幕的像素宽度
	 */
	public static int getScreenWidth(Context context) {
		DisplayMetrics outMetrics = getScreenMetrics(context);
		return outMetrics.widthPixels;
	}

	private static DisplayMetrics getScreenMetrics(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		return outMetrics;
	}

	/**
	 * 从相册选择图片截图
	 *
	 * @param activity
	 * @param uri
	 * @param outputX
	 * @param outputY
	 * @param requestCode
	 */
	public static void cropImageFromAlbum(Activity activity, Uri uri,
										  int outputX, int outputY, int requestCode) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		setCropIntent(uri, outputX, outputY, intent);
		activity.startActivityForResult(intent, requestCode);
	}

	public static void cropImageFromCamera(Activity activity, Uri uri,
										   int outputX, int outputY, int requestCode) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		setCropIntent(uri, outputX, outputY, intent);
		activity.startActivityForResult(intent, requestCode);
	}

	private static void setCropIntent(Uri uri, int outputX, int outputY,
									  Intent intent) {
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", false);
		intent.putExtra("outputFormat", CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
	}

	public static Bitmap decodeUriAsBitmap(Context context, Uri uri)
			throws FileNotFoundException {
		InputStream is = context.getContentResolver().openInputStream(uri);
		Bitmap bitmap = BitmapFactory.decodeStream(is);
		return bitmap;
	}

	/**
	 * 得到多个图片URL集合中的第一张图片地址
	 *
	 * @return null或者内容配图的完整URL数组 Sring[]
	 */
	public static String getFristImageUrl(String imageUrl) {
		if (TextUtils.isEmpty(imageUrl)) {
			return null;
		}
		String[] urlsArray = imageUrl.split(",");

		return urlsArray[0];
	}

	/**
	 * 保存图片到/image/文件夹中
	 * @param bmp
	 * @return
	 */
	public static String saveBitmapToSD(Bitmap bmp) {
		String imageName = java.util.UUID.randomUUID().toString() + ".png";
		File file = new File(FileUtils.getDirByType(FileUtils.DIR_TYPE_IMAGE), imageName);
		try {
			FileOutputStream out = new FileOutputStream(file);
			bmp.compress(CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file.getPath();
	}


	/**
	 * 保存图片到手机相册中
	 */
	public static String saveImageToGallery(Context context, Bitmap bmp) {

		File file = ImageUtils.save2Album(bmp,
				DeviceUtils.getAppName(context),
				CompressFormat.JPEG, true);
		return file != null ? file.getAbsolutePath() : "";

//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//			// 首先保存图片
//			File appDir = new File(Environment.getExternalStorageDirectory(),
//					"茵战");
//			if (!appDir.exists()) {
//				boolean isDir = appDir.mkdirs();
//				Log.e("", "isDir = " + isDir);
//			}
//			String fileName = System.currentTimeMillis() + ".jpg";
//			File file = new File(appDir, fileName);
//			try {
//				FileOutputStream fos = new FileOutputStream(file);
//				bmp.compress(CompressFormat.JPEG, 100, fos);
//				fos.flush();
//				fos.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			// 最后通知图库更新
//			context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
//					Uri.fromFile(file)));
//			return file.getAbsolutePath();
//
//		} else {
//			//	Android 10以后分区存储
//			//在自身目录下创建apk文件夹
//			File tempFile = context.getExternalFilesDir("tempImg");
//			String fileName = System.currentTimeMillis() + ".jpg";
//			File file = new File(tempFile, fileName);
//			try {
//				FileOutputStream fos = new FileOutputStream(file);
//				bmp.compress(CompressFormat.JPEG, 100, fos);
//				fos.flush();
//				fos.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//			ContentValues values = new ContentValues();
//			values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
//			values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//			values.put(MediaStore.Images.Media.TITLE, fileName);
//			//注意MediaStore.Downloads.RELATIVE_PATH需要targetVersion=29,
//			//故该方法只可在Android10的手机上执行
//			values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + DeviceUtil.getAppName(context));
//			//插入
//			ContentResolver resolver = context.getContentResolver();
//			Uri insertUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//
//			FileUtils.writeFile(context, file.getAbsolutePath(), values, resolver, insertUri, true);
//			resolver.update(insertUri, values, null, null);
//			return null;
//		}

	}

	/**
	 *  图片压缩
	 * @param image
	 * @return
	 */
	private static final String TAG = "ImageTools";
	public static Bitmap compressImage(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while (baos.toByteArray().length / 1024 > 400) { // 循环判断如果压缩后图片是否大于400kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			image.compress(CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;// 每次都减少10
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmap;
	}
	
	/**
	 * 将图片按照某个角度进行旋转
	 *
	 * @param bm
	 *            需要旋转的图片
	 * @param degree
	 *            旋转角度
	 * @return 旋转后的图片
	 */
	public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
		Bitmap returnBm = null;
		
		// 根据旋转角度，生成旋转矩阵
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		try {
			// 将原始图片按照旋转矩阵进行旋转，并得到新的图片
			returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
		} catch (OutOfMemoryError e) {
		}
		if (returnBm == null) {
			returnBm = bm;
		}
		if (bm != returnBm) {
			bm.recycle();
		}
		return returnBm;
	}
	
	/**
	 * 将图片按照某个角度进行旋转
	 *
	 * @param bm
	 *            需要翻转的图片
	 * @return 旋转后的图片
	 */
	public static Bitmap rotateBitmapByDegree(Bitmap bm) {
		Bitmap returnBm = null;
		
		// 根据旋转角度，生成旋转矩阵
		Matrix matrix = new Matrix();
		matrix.setScale(-1,1);//翻转
		try {
			// 将原始图片按照旋转矩阵进行旋转，并得到新的图片
			returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
		} catch (OutOfMemoryError e) {
		}
		if (returnBm == null) {
			returnBm = bm;
		}
		if (bm != returnBm) {
			bm.recycle();
		}
		return returnBm;
	}
}
