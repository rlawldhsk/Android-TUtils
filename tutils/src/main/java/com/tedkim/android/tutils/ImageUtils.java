package com.tedkim.android.tutils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.tedkim.android.tutils.interfaces.ImageUtilsListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Class collection of image
 * Created by Ted
 */
public class ImageUtils {

    private static final String TAG = ImageUtils.class.getSimpleName();

    /**
     * Round bitmap processing
     */
    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * Get the image Drawable Uri
     */
    public static Uri getImageResourceUri(Context context, int imageName) {
        return Uri.parse("res://" + context.getResources().getResourcePackageName(imageName) + "/" + imageName);
    }

    /**
     * Blur Image processing by Fresco
     *
     * @param context context
     * @param uri     image uri
     * @param imgView SimpleDraweeView
     * @param radius  radius (0.0f ~ 25.0f)
     */
    public static void setBlurImage(final Context context, Uri uri, SimpleDraweeView imgView, final float radius) {
        try {
            Postprocessor redMeshPostprocessor = new BasePostprocessor() {
                @Override
                public String getName() {
                    return "redMeshPostprocessor";
                }

                @Override
                public void process(Bitmap bitmap) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {

                        final RenderScript rs = RenderScript.create(context);
                        final Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE,
                                Allocation.USAGE_SCRIPT);
                        final Allocation output = Allocation.createTyped(rs, input.getType());
                        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
                        script.setRadius(radius); // 0.0f ~ 25.0f
                        script.setInput(input);
                        script.forEach(output);
                        output.copyTo(bitmap);
                    }
                }
            };

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setPostprocessor(redMeshPostprocessor)
                    .build();

            PipelineDraweeController controller = (PipelineDraweeController)
                    Fresco.newDraweeControllerBuilder()
                            .setImageRequest(request)
                            .setOldController(imgView.getController())
                            // other setters as you need
                            .build();
            imgView.setController(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * clear fresco image cache
     *
     * @param url image url
     */
    public static void clearCacheFresco(String url) {
        Fresco.getImagePipeline().evictFromMemoryCache(Uri.parse(url));
        Fresco.getImagePipelineFactory().getMainDiskStorageCache().remove(new SimpleCacheKey(url));
        Fresco.getImagePipelineFactory().getSmallImageDiskStorageCache().remove(new SimpleCacheKey(url));
    }

    /**
     * Get bitmap to url by Fresco
     *
     * @param context  context
     * @param url      image url
     * @param listener ImageUtilsListener
     */
    public static void getBitmapToUrl(Context context, String url, final ImageUtilsListener listener) {
        // To get image using Fresco
        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(url))
                .setProgressiveRenderingEnabled(true)
                .build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, context);
        dataSource.subscribe(new BaseBitmapDataSubscriber() {

            @Override
            public void onNewResultImpl(@Nullable Bitmap bitmap) {
                listener.onBitmapToUrl(bitmap);
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                listener.onBitmapToUrl(null);
            }

        }, CallerThreadExecutor.getInstance());
    }

    /**
     * Image SDCard Save (input Bitmap
     *
     * @param bitmap : input bitmap file
     * @param folder : input folder name
     * @param name   : output file name
     */
    public static void saveBitmapToJpeg(Context context, Bitmap bitmap, String folder, String name, ImageUtilsListener listener) {
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        String folderName = "/" + folder + "/";
        String fileName = name + ".jpg";
        String imagePath = ex_storage + folderName;

        File file;
        try {
            file = new File(imagePath);
            if (!file.isDirectory()) {
                file.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(imagePath + fileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

            // 갤러리에 업데이트
            context.sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)) );
            addPicToGallery(context, imagePath + fileName);

            listener.onSaveImageToGallery(true, imagePath + fileName);

        } catch (FileNotFoundException exception) {
            Log.e(TAG, "[FileNotFoundException] " + exception.getMessage());
            listener.onSaveImageToGallery(false, null);
        } catch (Exception exception) {
            Log.e(TAG, "[IOException] " + exception.getMessage());
            listener.onSaveImageToGallery(false, null);
        }
    }

    public static void addPicToGallery(Context context, String photoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * image setting
     *
     * @param view SimpleDraweeView
     * @param url  image url
     */
    public static void setImage(final SimpleDraweeView view, final String url) {
        if (url == null) {
            view.setImageURI(Uri.EMPTY);
        } else {
            if (url.length() == 0)
                view.setImageURI(Uri.EMPTY);
            else
                view.setImageURI(Uri.parse(url));
        }
    }


}
