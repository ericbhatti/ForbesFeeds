package com.bezy_apps.forbesfeeds;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bezy_apps.forbesfeeds.data.HeadlinesContract;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Eric on 2/27/2015.
 */


public class NewsAdapter extends CursorAdapter {
    final String CACHE_FOLDER_NAME = "/bezyapps_forbes/";

    public NewsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_list_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String title = cursor.getString(cursor.getColumnIndex(HeadlinesContract.HeadlinesEntry.COLUMN_TITLE));
        String desc = cursor.getString(cursor.getColumnIndex(HeadlinesContract.HeadlinesEntry.COLUMN_DESC));
        String path = cursor.getString(cursor.getColumnIndex(HeadlinesContract.HeadlinesEntry.COLUMN_IMAGE_PATH));
        viewHolder.textView_news_desc.setText(desc);
        viewHolder.textView_news_title.setText(title);

        File myDir = Environment.getExternalStorageDirectory();
        File mediaImage = new File(myDir.getPath() + CACHE_FOLDER_NAME + path);
        if (path != null) {
            loadBitmap(mediaImage.getAbsolutePath(), viewHolder.imageView_news_image);
        } else {
            viewHolder.imageView_news_image.setImageResource(R.drawable.forbes_default);
        }

    }


    public void loadBitmap(String pathName, ImageView imageView) {
        if (cancelPotentialWork(pathName, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(pathName);
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(BitmapWorkerTask bitmapWorkerTask) {
            //   super(res,bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);


        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(String pathName, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.pathName;
            if (bitmapData == null || !bitmapData.equals(pathName)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }


    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String pathName = null;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            pathName = params[0];
            return decodeSampledBitmapFromFile(pathName, 85, 85);


        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setImageResource(R.drawable.forbes_default);
                    }
                }
            }
        }

        public Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            File file = new File(path);
            if (file.exists()) {
                BitmapFactory.decodeFile(path, options);
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeFile(path, options);
            } else {
                BitmapFactory.decodeResource(mContext.getResources(), R.drawable.forbes_default, options);
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.forbes_default);
            }
        }

        public int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
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


    public static class ViewHolder {
        public final ImageView imageView_news_image;
        public final TextView textView_news_title;
        public final TextView textView_news_desc;

        public ViewHolder(View view) {
            imageView_news_image = (ImageView) view.findViewById(R.id.imageView_news_image);
            textView_news_title = (TextView) view.findViewById(R.id.textView_news_title);
            textView_news_desc = (TextView) view.findViewById(R.id.textView_news_desc);
        }
    }
}
