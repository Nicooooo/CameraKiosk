package fimobile.technology.inc.CameraKiosk.imageeditor;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

import fimobile.technology.inc.CameraKiosk.R;

/**
 * Created by FM-JMK on 22/05/2018.
 */

public class FrameBSFragment extends BottomSheetDialogFragment implements ImageAdapter.OnImageClickListener{

//    public FrameBSFragment(@NonNull Context context) {
//        super(context);
//    }

    private static final String TAG = FrameBSFragment.class.getSimpleName();
    private ArrayList<Bitmap> frameBitmaps;
    private EditImageActivity editImageActivity;

    private FrameBSFragment mFrameBSFragment;

    public FrameBSFragment() {
        // Required empty public constructor
    }

    private FrameBSFragment.FrameListener mFrameListener;

    public void setFrameListener(FrameBSFragment.FrameListener frameListener) {
        mFrameListener = frameListener;
    }

    @Override
    public void onImageClickListener(Bitmap image) {
        editImageActivity.onFrameClick(image);
    }

    public interface FrameListener {
        void onFrameClick(Bitmap bitmap);
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };


    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_bottom_sticker_emoji_dialog, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
        ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));

        TypedArray images = getResources().obtainTypedArray(R.array.camerakiosk_frame);

        editImageActivity = (EditImageActivity)getActivity();
        frameBitmaps = new ArrayList<>();
        for (int i = 0; i < images.length(); i++) {
            frameBitmaps.add(decodeSampledBitmapFromResource(editImageActivity.getResources(), images.getResourceId(i, -1), 120, 120));
        }

        RecyclerView rvEmoji = (RecyclerView) contentView.findViewById(R.id.rvEmoji);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        rvEmoji.setLayoutManager(gridLayoutManager);
        ImageAdapter adapter = new ImageAdapter(editImageActivity, frameBitmaps);
        adapter.setOnImageClickListener(this);
        rvEmoji.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }
}
