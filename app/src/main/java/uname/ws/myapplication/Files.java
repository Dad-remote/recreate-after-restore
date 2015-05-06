package uname.ws.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Pair;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class Files {

    private static final String IMAGE_FOLDER = "images";
    private static final float SMALL_IMAGE_RATIO = 0.3f;

    public static File newImageFile(Context context) {
        String imageName = UUID.randomUUID().toString() + ".jpg";
        return newImageFile(context, imageName);
    }

    public static File newImageFile(Context context, String imageName) {
        String dirPath = context.getExternalFilesDir(null).getAbsolutePath() + File.separator + IMAGE_FOLDER;
        File file = new File(dirPath + File.separator + imageName);
        try {
            com.google.common.io.Files.createParentDirs(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public static File saveSmallImage(Context context, File file) throws FileNotFoundException {
        int height = context.getResources().getDisplayMetrics().heightPixels;
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int smallSide = width > height ? height : width;
        Bitmap bitmap = decodeSampledBitmap(file, (int) (smallSide * SMALL_IMAGE_RATIO), (int) (smallSide * SMALL_IMAGE_RATIO));
        File smallFile = newImageFile(context);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(smallFile));
        bitmap.recycle();
        return smallFile;
    }

    public static Pair<String, File> saveCachedImage(Activity activity, Uri imageUri) throws IOException {
        String cachedImageName = Hashing.md5().hashString(imageUri.toString(), Charset.forName("UTF-8")).toString() + ".jpg";
        File cachedImage = newImageFile(activity, cachedImageName);
        if (!cachedImage.exists()) {
            InputStream inputStream = activity.getContentResolver().openInputStream(imageUri);
            ByteStreams.copy(inputStream, new FileOutputStream(cachedImage));

            ExifHelper helper = new ExifHelper();
            helper.createInFile(cachedImage.getAbsolutePath());
            helper.readExifData();

            scaleImage(cachedImage, helper.getOrientation());
            helper.resetOrientation();

            helper.createOutFile(cachedImage.getAbsolutePath());
            helper.writeExifData();
        }
        return Pair.create(cachedImageName, cachedImage);
    }

    private static void scaleImage(File file, int orientation) throws FileNotFoundException {
        Bitmap bitmap = decodeSampledBitmap(file, 800, 800);
        if (orientation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            bitmap = rotatedBitmap;
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
        bitmap.recycle();
    }

    public static Bitmap decodeSampledBitmap(File file, int reqWidth, int reqHeight) throws FileNotFoundException {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(new FileInputStream(file), null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(new FileInputStream(file), null, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
