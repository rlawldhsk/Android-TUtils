package com.tedkim.android.tutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

/**
 * Class collection of image
 * Created by Ted
 */
public class ImageUtils {

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
     * image setting
     *
     * @param view SimpleDraweeView
     * @param url  image url
     */
    public static void setImage(final SimpleDraweeView view, final String url) {
        view.post(new Runnable() {
            @Override
            public void run() {
                if (url == null) {
                    view.setImageURI(Uri.EMPTY);
                } else {
                    if (url.length() == 0)
                        view.setImageURI(Uri.EMPTY);
                    else
                        view.setImageURI(Uri.parse(url));
                }
            }
        });
    }

    /**
     * Adel, 2016-08-29
     * image setting with Glide
     *
     * @param view ImageView
     * @param url  image url
     * @param res  default image resource id
     */
    public static void setImage(final ImageView view, final String url, final int res) {
        view.post(new Runnable() {
            @Override
            public void run() {
                if (url == null) {
                    Glide.with(view.getContext()).load(res).into(view);
                } else {
                    if (url.length() == 0)
                        Glide.with(view.getContext()).load(res).into(view);
                    else
                        Glide.with(view.getContext()).load(url).into(view);

                }
            }
        });
    }
}
