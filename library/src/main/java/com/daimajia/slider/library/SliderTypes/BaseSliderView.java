package com.daimajia.slider.library.SliderTypes;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.daimajia.slider.library.R;

import java.io.File;

/**
 * When you want to make your own slider view, you must extends from this class.
 * BaseSliderView provides some useful methods.
 * I provide two example: {@link com.daimajia.slider.library.SliderTypes.DefaultSliderView} and
 * {@link com.daimajia.slider.library.SliderTypes.TextSliderView}
 * if you want to show progressbar, you just need to set a progressbar id as @+id/loading_bar.
 */
public abstract class BaseSliderView {

    protected Context mContext;

    private Bundle mBundle;

    /**
     * Error place holder image.
     */
    private int mErrorPlaceHolderRes;

    /**
     * Empty imageView placeholder.
     */
    private int mEmptyPlaceHolderRes;

    private String mUrl;
    private File mFile;
    private int mRes;

    protected OnSliderClickListener mOnSliderClickListener;

    private boolean mErrorDisappear;

    private ImageLoadListener mLoadListener;

    private String mDescription;

    private LazyHeaders lazyHeaders = null;

    /**
     * Scale type of the image.
     */
    private ScaleType mScaleType = ScaleType.Fit;

    public enum ScaleType {
        CenterCrop, CenterInside, Fit, FitCenterCrop
    }

    protected BaseSliderView(Context context) {
        mContext = context;
    }

    public BaseSliderView requestHeaders(String key, String value) {
        lazyHeaders = new LazyHeaders.Builder().addHeader(key, value).build();
        return this;
    }

    /**
     * the placeholder image when loading image from url or file.
     *
     * @param resId Image resource id
     * @return
     */
    public BaseSliderView empty(int resId) {
        mEmptyPlaceHolderRes = resId;
        return this;
    }

    /**
     * determine whether remove the image which failed to download or load from file
     *
     * @param disappear
     * @return
     */
    public BaseSliderView errorDisappear(boolean disappear) {
        mErrorDisappear = disappear;
        return this;
    }

    /**
     * if you set errorDisappear false, this will set a error placeholder image.
     *
     * @param resId image resource id
     * @return
     */
    public BaseSliderView error(int resId) {
        mErrorPlaceHolderRes = resId;
        return this;
    }

    /**
     * the description of a slider image.
     *
     * @param description
     * @return
     */
    public BaseSliderView description(String description) {
        mDescription = description;
        return this;
    }

    /**
     * set a url as a image that preparing to load
     *
     * @param url
     * @return
     */
    public BaseSliderView image(String url) {
        if (mFile != null || mRes != 0) {
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mUrl = url;
        return this;
    }

    /**
     * set a file as a image that will to load
     *
     * @param file
     * @return
     */
    public BaseSliderView image(File file) {
        if (mUrl != null || mRes != 0) {
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mFile = file;
        return this;
    }

    public BaseSliderView image(int res) {
        if (mUrl != null || mFile != null) {
            throw new IllegalStateException("Call multi image function," +
                    "you only have permission to call it once");
        }
        mRes = res;
        return this;
    }

    /**
     * lets users add a bundle of additional information
     *
     * @param bundle
     * @return
     */
    public BaseSliderView bundle(Bundle bundle) {
        mBundle = bundle;
        return this;
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean isErrorDisappear() {
        return mErrorDisappear;
    }

    public int getEmpty() {
        return mEmptyPlaceHolderRes;
    }

    public int getError() {
        return mErrorPlaceHolderRes;
    }

    public String getDescription() {
        return mDescription;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * set a slider image click listener
     *
     * @param l
     * @return
     */
    public BaseSliderView setOnSliderClickListener(OnSliderClickListener l) {
        mOnSliderClickListener = l;
        return this;
    }

    /**
     * When you want to implement your own slider view, please call this method in the end in `getView()` method
     *
     * @param v               the whole view
     * @param targetImageView where to place image
     */
    protected void bindEventAndShow(final View v, ImageView targetImageView) {
        final BaseSliderView me = this;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSliderClickListener != null) {
                    mOnSliderClickListener.onSliderClick(me);
                }
            }
        });
        if (targetImageView == null)
            return;
        if (mLoadListener != null) {
            mLoadListener.onStart(me);
        }
        RequestManager requestManager = Glide.with(getContext());
        DrawableTypeRequest<GlideUrl> drawTypeWithHeaders = null;
        DrawableTypeRequest<String> drawTypeString = null;
        DrawableTypeRequest<File> drawTypeFile = null;
        DrawableTypeRequest<Integer> drawTypeInteger = null;
        if (mUrl != null) {
            if (lazyHeaders != null) {
                drawTypeWithHeaders = requestManager.load(new GlideUrl(mUrl, lazyHeaders));
            } else {
                drawTypeString = requestManager.load(mUrl);
            }
        } else if (mFile != null) {
            drawTypeFile = requestManager.load(mFile);
        } else if (mRes != 0) {
            drawTypeInteger = requestManager.load(mRes);
        } else {
            return;
        }
        if (drawTypeString != null) {
            if (getEmpty() != 0) {
                drawTypeString.placeholder(getEmpty());
            }
            if (getError() != 0) {
                drawTypeString.error(getError());
            }
            switch (mScaleType) {
                case Fit:
                    drawTypeString.fitCenter();
                    break;
                case CenterCrop:
                    drawTypeString.centerCrop();
                    break;
                case CenterInside:
                    drawTypeString.fitCenter();
                    break;
            }
            drawTypeString.listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    if (mLoadListener != null) {
                        mLoadListener.onEnd(false, me);
                    }
                    if (v.findViewById(R.id.loading_bar) != null) {
                        v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    if (v.findViewById(R.id.loading_bar) != null) {
                        v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                    }
                    return false;
                }
            });
            drawTypeString.into(targetImageView);
        } else if (drawTypeFile != null) {
            if (getEmpty() != 0) {
                drawTypeFile.placeholder(getEmpty());
            }
            if (getError() != 0) {
                drawTypeFile.error(getError());
            }
            switch (mScaleType) {
                case Fit:
                    drawTypeFile.fitCenter();
                    break;
                case CenterCrop:
                    drawTypeFile.centerCrop();
                    break;
                case CenterInside:
                    drawTypeFile.fitCenter();
                    break;
            }
            drawTypeFile.listener(new RequestListener<File, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                    if (mLoadListener != null) {
                        mLoadListener.onEnd(false, me);
                    }
                    if (v.findViewById(R.id.loading_bar) != null) {
                        v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    if (v.findViewById(R.id.loading_bar) != null) {
                        v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                    }
                    return false;
                }
            });
            drawTypeFile.into(targetImageView);
        } else if (drawTypeInteger != null) {
            if (getEmpty() != 0) {
                drawTypeInteger.placeholder(getEmpty());
            }
            if (getError() != 0) {
                drawTypeInteger.error(getError());
            }
            switch (mScaleType) {
                case Fit:
                    drawTypeInteger.fitCenter();
                    break;
                case CenterCrop:
                    drawTypeInteger.centerCrop();
                    break;
                case CenterInside:
                    drawTypeInteger.fitCenter();
                    break;
            }
            drawTypeInteger.listener(new RequestListener<Integer, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, Integer model, Target<GlideDrawable> target, boolean isFirstResource) {
                    if (mLoadListener != null) {
                        mLoadListener.onEnd(false, me);
                    }
                    if (v.findViewById(R.id.loading_bar) != null) {
                        v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, Integer model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    if (v.findViewById(R.id.loading_bar) != null) {
                        v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                    }
                    return false;
                }
            });
            drawTypeInteger.into(targetImageView);
        } else if (drawTypeWithHeaders != null) {
            if (getEmpty() != 0) {
                drawTypeWithHeaders.placeholder(getEmpty());
            }
            if (getError() != 0) {
                drawTypeWithHeaders.error(getError());
            }
            switch (mScaleType) {
                case Fit:
                    drawTypeWithHeaders.fitCenter();
                    break;
                case CenterCrop:
                    drawTypeWithHeaders.centerCrop();
                    break;
                case CenterInside:
                    drawTypeWithHeaders.fitCenter();
                    break;
            }
            drawTypeWithHeaders.listener(new RequestListener<GlideUrl, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, GlideUrl model, Target<GlideDrawable> target, boolean isFirstResource) {
                    if (mLoadListener != null) {
                        mLoadListener.onEnd(false, me);
                    }
                    if (v.findViewById(R.id.loading_bar) != null) {
                        v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, GlideUrl model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    if (v.findViewById(R.id.loading_bar) != null) {
                        v.findViewById(R.id.loading_bar).setVisibility(View.INVISIBLE);
                    }
                    return false;
                }
            });
            drawTypeWithHeaders.into(targetImageView);
        }
    }


    public BaseSliderView setScaleType(ScaleType type) {
        mScaleType = type;
        return this;
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    /**
     * the extended class have to implement getView(), which is called by the adapter,
     * every extended class response to render their own view.
     *
     * @return
     */
    public abstract View getView();

    /**
     * set a listener to get a message , if load error.
     *
     * @param l
     */
    public void setOnImageLoadListener(ImageLoadListener l) {
        mLoadListener = l;
    }

    public interface OnSliderClickListener {
        public void onSliderClick(BaseSliderView slider);
    }

    /**
     * when you have some extra information, please put it in this bundle.
     *
     * @return
     */
    public Bundle getBundle() {
        return mBundle;
    }

    public interface ImageLoadListener {
        public void onStart(BaseSliderView target);

        public void onEnd(boolean result, BaseSliderView target);
    }
}
